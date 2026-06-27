package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LargeLaunchPadBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.missile.MissileEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.missile.MissileItem;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class LargeLaunchPadBlockEntity extends LaunchPadBlockEntity {
    private static final String TAG_FORM_FACTOR = "formFactor";
    private static final String TAG_ERECTED = "erected";
    private static final String TAG_READY_TO_LOAD = "readyToLoad";
    private static final String TAG_SCHEDULE_ERECT = "scheduleErect";
    private static final String TAG_LIFT = "lift";
    private static final String TAG_ERECTOR = "erector";
    private static final String TAG_PREV_LIFT = "prevLift";
    private static final String TAG_PREV_ERECTOR = "prevErector";

    private int formFactor = -1;
    private boolean erected;
    private boolean readyToLoad;
    private boolean scheduleErect;
    private float lift = 1.0F;
    private float erector = 90.0F;
    private float prevLift = 1.0F;
    private float prevErector = 90.0F;
    private boolean liftMoving;
    private boolean erectorMoving;

    public LargeLaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD_LARGE.get(), pos, state);
        delay = 20;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LargeLaunchPadBlockEntity launchPad) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, launchPad);
        }
        boolean changed = launchPad.tickMachine(level, pos, state);
        if (changed) {
            launchPad.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        launchPad.networkPackNT(250);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LargeLaunchPadBlockEntity launchPad) {
        if (!level.isClientSide) {
            return;
        }
        if (launchPad.erected && launchPad.isLargeVaporFormFactor() && launchPad.oxidizerTank().getFill() > 0) {
            ParticleUtil.spawnLaunchPadFuelVapor(level, pos);
        }
        if (!level.getEntitiesOfClass(MissileEntity.class,
                        new AABB(pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D)).isEmpty()) {
            Direction facing = state.hasProperty(LargeLaunchPadBlock.FACING)
                    ? state.getValue(LargeLaunchPadBlock.FACING)
                    : Direction.NORTH;
            ParticleUtil.spawnLaunchPadSmokeBurst(level, pos, facing, true);
        }
    }

    @Override
    protected boolean tickMachine(Level level, BlockPos pos, BlockState blockState) {
        long oldPower = energy.getPower();
        int oldFuel = fuelTank().getFill();
        int oldOxidizer = oxidizerTank().getFill();
        int oldState = state;
        int oldDelay = delay;
        boolean oldRedstone = redstonePowered;
        boolean oldErected = erected;
        boolean oldReady = readyToLoad;
        float oldLift = lift;
        float oldErector = erector;

        prevLift = lift;
        prevErector = erector;

        processFluidItemTransfers(getItems(), HbmFluidItemTransfer.loadTransfers(
                SLOT_FUEL_INPUT, SLOT_FUEL_OUTPUT, 2, fuelTank(), oxidizerTank()));
        HbmEnergyUtil.chargeStorageFromItem(getItems().getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        updateFuelTankTypes();
        updateFormFactor();
        tickAnimation(level, pos);

        redstonePowered = isLaunchPadPowered(level, pos, blockState);
        if (redstonePowered && !prevRedstonePowered) {
            launchFromDesignator();
        }
        prevRedstonePowered = redstonePowered;

        if (!hasFuel() || !isMissileValid() || !canInstantiateMissile()) {
            state = STATE_MISSING;
        } else if (erected && canLaunchBase()) {
            state = STATE_READY;
        } else {
            state = STATE_LOADING;
        }

        return oldPower != energy.getPower()
                || oldFuel != fuelTank().getFill()
                || oldOxidizer != oxidizerTank().getFill()
                || oldState != state
                || oldDelay != delay
                || oldRedstone != redstonePowered
                || oldErected != erected
                || oldReady != readyToLoad
                || oldLift != lift
                || oldErector != erector;
    }

    private void tickAnimation(Level level, BlockPos pos) {
        float erectorSpeed = 1.5F;
        float liftSpeed = 0.025F;
        if (isLargeVaporFormFactor()) {
            erectorSpeed /= 2.0F;
            liftSpeed /= 2.0F;
        }

        if (isMissileValid()) {
            if (erector == 90.0F && lift == 1.0F) {
                readyToLoad = true;
            }
        } else {
            readyToLoad = false;
            erected = false;
            scheduleErect = false;
            delay = 20;
        }

        if (energy.getPower() >= LAUNCH_POWER) {
            if (delay > 0) {
                delay--;
                if (delay < 10 && scheduleErect) {
                    erected = true;
                    scheduleErect = false;
                }
                if (getItems().getStackInSlot(SLOT_MISSILE).isEmpty() || !readyToLoad) {
                    retractErector(erectorSpeed, liftSpeed);
                }
            } else if (!erected && readyToLoad) {
                state = STATE_LOADING;
                if (erector != 0.0F) {
                    erector = Math.max(erector - erectorSpeed, 0.0F);
                    if (erector == 0.0F) {
                        delay = 20;
                    }
                } else if (lift > 0.0F) {
                    lift = Math.max(lift - liftSpeed, 0.0F);
                    if (lift == 0.0F) {
                        scheduleErect = true;
                        delay = 20;
                    }
                }
            } else {
                retractErector(erectorSpeed, liftSpeed);
            }
        }

        boolean wasLiftMoving = liftMoving;
        boolean wasErectorMoving = erectorMoving;
        liftMoving = prevLift != lift;
        erectorMoving = prevErector != erector;
        if (!level.isClientSide && wasLiftMoving && !liftMoving) {
            LegacySoundPlayer.playSoundEffect(level, pos, "hbm:door.wgh_stop", SoundSource.BLOCKS, 2.0F, 1.0F);
        }
        if (!level.isClientSide && wasErectorMoving && !erectorMoving) {
            LegacySoundPlayer.playSoundEffect(level, pos, "hbm:door.garage_stop", SoundSource.BLOCKS, 2.0F, 1.0F);
        }
    }

    private void retractErector(float erectorSpeed, float liftSpeed) {
        if (erector < 90.0F) {
            erector = Math.min(erector + erectorSpeed, 90.0F);
            if (erector == 90.0F) {
                delay = 20;
            }
        } else if (lift < 1.0F) {
            lift = Math.min(lift + liftSpeed, 1.0F);
            if (lift == 1.0F) {
                readyToLoad = true;
                delay = 20;
            }
        }
    }

    @Override
    protected boolean isLaunchPadPowered(Level level, BlockPos pos, BlockState blockState) {
        if (level.hasNeighborSignal(pos)) {
            return true;
        }
        if (blockState.getBlock() instanceof LargeLaunchPadBlock block) {
            LegacyMultiblockLayout layout = block.getMultiblockLayout(blockState, level, pos);
            for (BlockPos offset : layout.offsets()) {
                if (!offset.equals(BlockPos.ZERO) && level.hasNeighborSignal(pos.offset(offset))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean canLaunchBase() {
        return isMissileValid() && hasFuel() && erected && readyToLoad && canInstantiateMissile();
    }

    @Override
    protected double getLaunchOffset() {
        return 2.0D;
    }

    @Override
    protected void finalizeLaunch(ServerLevel level, Entity missile) {
        super.finalizeLaunch(level, missile);
        erected = false;
        scheduleErect = false;
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        if (networkFluidPorts == null) {
            networkFluidPorts = List.of(
                    new FluidPort(new BlockPos(5, 0, -2), Direction.EAST),
                    new FluidPort(new BlockPos(5, 0, 2), Direction.EAST),
                    new FluidPort(new BlockPos(-5, 0, -2), Direction.WEST),
                    new FluidPort(new BlockPos(-5, 0, 2), Direction.WEST),
                    new FluidPort(new BlockPos(-2, 0, 5), Direction.SOUTH),
                    new FluidPort(new BlockPos(2, 0, 5), Direction.SOUTH),
                    new FluidPort(new BlockPos(-2, 0, -5), Direction.NORTH),
                    new FluidPort(new BlockPos(2, 0, -5), Direction.NORTH));
        }
        return networkFluidPorts;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        if (energyPorts == null) {
            energyPorts = List.of(
                    new EnergyPort(new BlockPos(5, 0, -2), Direction.EAST),
                    new EnergyPort(new BlockPos(5, 0, 2), Direction.EAST),
                    new EnergyPort(new BlockPos(-5, 0, -2), Direction.WEST),
                    new EnergyPort(new BlockPos(-5, 0, 2), Direction.WEST),
                    new EnergyPort(new BlockPos(-2, 0, 5), Direction.SOUTH),
                    new EnergyPort(new BlockPos(2, 0, 5), Direction.SOUTH),
                    new EnergyPort(new BlockPos(-2, 0, -5), Direction.NORTH),
                    new EnergyPort(new BlockPos(2, 0, -5), Direction.NORTH));
        }
        return energyPorts;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatableWithFallback(
                "container.hbm_ntm_rebirth.launch_pad_large", "Launch Pad");
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-10, 0, -10), worldPosition.offset(11, 15, 11));
    }

    public int getFormFactor() {
        return formFactor;
    }

    public boolean isErected() {
        return erected;
    }

    public boolean isReadyToLoad() {
        return readyToLoad;
    }

    public float getLift(float partialTick) {
        return prevLift + (lift - prevLift) * partialTick;
    }

    public float getErector(float partialTick) {
        return prevErector + (erector - prevErector) * partialTick;
    }

    private void updateFormFactor() {
        ItemStack missile = getItems().getStackInSlot(SLOT_MISSILE);
        if (missile.getItem() instanceof MissileItem missileItem) {
            formFactor = missileItem.formFactor().ordinal();
        } else if (missile.isEmpty()) {
            formFactor = -1;
        } else {
            formFactor = MissileItem.FormFactor.OTHER.ordinal();
        }
    }

    private boolean isLargeVaporFormFactor() {
        return formFactor == MissileItem.FormFactor.HUGE.ordinal()
                || formFactor == MissileItem.FormFactor.ATLAS.ordinal();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_FORM_FACTOR, formFactor);
        tag.putBoolean(TAG_ERECTED, erected);
        tag.putBoolean(TAG_READY_TO_LOAD, readyToLoad);
        tag.putBoolean(TAG_SCHEDULE_ERECT, scheduleErect);
        tag.putFloat(TAG_LIFT, lift);
        tag.putFloat(TAG_ERECTOR, erector);
        tag.putFloat(TAG_PREV_LIFT, prevLift);
        tag.putFloat(TAG_PREV_ERECTOR, prevErector);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        formFactor = tag.contains(TAG_FORM_FACTOR) ? tag.getInt(TAG_FORM_FACTOR) : -1;
        erected = tag.getBoolean(TAG_ERECTED);
        readyToLoad = tag.getBoolean(TAG_READY_TO_LOAD);
        scheduleErect = tag.getBoolean(TAG_SCHEDULE_ERECT);
        lift = tag.contains(TAG_LIFT) ? tag.getFloat(TAG_LIFT) : 1.0F;
        erector = tag.contains(TAG_ERECTOR) ? tag.getFloat(TAG_ERECTOR) : 90.0F;
        prevLift = tag.contains(TAG_PREV_LIFT) ? tag.getFloat(TAG_PREV_LIFT) : lift;
        prevErector = tag.contains(TAG_PREV_ERECTOR) ? tag.getFloat(TAG_PREV_ERECTOR) : erector;
    }
}

package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.entity.RadarCommandReceiver;
import com.hbm.ntm.api.item.DesignatorItem;
import com.hbm.ntm.block.LaunchPadBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.missile.AntiBallisticMissileEntity;
import com.hbm.ntm.entity.missile.MissileEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.item.missile.MissileItem;
import com.hbm.ntm.menu.LaunchPadMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaunchPadBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, RadarCommandReceiver {
    public static final int SLOT_MISSILE = 0;
    public static final int SLOT_DESIGNATOR = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_FUEL_INPUT = 3;
    public static final int SLOT_FUEL_OUTPUT = 4;
    public static final int SLOT_OXIDIZER_INPUT = 5;
    public static final int SLOT_OXIDIZER_OUTPUT = 6;
    public static final int SLOT_COUNT = 7;
    public static final int STATE_MISSING = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_READY = 2;

    protected static final long MAX_POWER = 100_000L;
    protected static final long LAUNCH_POWER = 75_000L;
    protected static final int TANK_CAPACITY = 24_000;
    /** TileEntityLaunchPad's post-launch/loading timer. */
    protected static final int RELOAD_DELAY = 100;
    private static final String TAG_STATE = "state";
    private static final String TAG_POWER = "power";
    private static final String TAG_REDSTONE = "redstonePower";
    private static final String TAG_PREV_REDSTONE = "prevRedstonePower";
    private static final String TAG_DELAY = "delay";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_MISSILE -> isMissileValid(stack);
                case SLOT_DESIGNATOR -> stack.getItem() instanceof DesignatorItem;
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                case SLOT_FUEL_OUTPUT, SLOT_OXIDIZER_OUTPUT -> false;
                default -> true;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    protected boolean redstonePowered;
    protected boolean prevRedstonePowered;
    /**
     * TileEntityLaunchPad uses this as its 100-tick reload timer.  The large
     * pad reuses the field for its independent 20-tick erector-motion timer.
     */
    protected int delay;
    protected int state = STATE_MISSING;
    protected List<FluidPort> networkFluidPorts;
    protected List<EnergyPort> energyPorts;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.LAUNCH_PAD.get(), pos, state);
    }

    protected LaunchPadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
                        new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY)));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity launchPad) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, launchPad);
        }
        boolean changed = launchPad.tickMachine(level, pos, state);
        if (changed) {
            launchPad.setChanged();
        }
        launchPad.networkPackNT(250);
        if (changed) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity launchPad) {
        if (!level.isClientSide) {
            return;
        }
        if (LegacyClientAnimationLod.shouldSkipAnimationUpdate(level, pos)) {
            return;
        }
        if (level.getEntitiesOfClass(MissileEntity.class,
                        new net.minecraft.world.phys.AABB(pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D)).isEmpty()) {
            return;
        }
        Direction facing = state.hasProperty(LaunchPadBlock.FACING) ? state.getValue(LaunchPadBlock.FACING) : Direction.NORTH;
        ParticleUtil.spawnLaunchPadSmokeBurst(level, pos, facing, true);
    }

    protected boolean tickMachine(Level level, BlockPos pos, BlockState blockState) {
        long oldPower = energy.getPower();
        int oldFuel = fuelTank().getFill();
        int oldOxidizer = oxidizerTank().getFill();
        int oldState = state;
        int oldDelay = delay;
        boolean oldRedstone = redstonePowered;

        processFluidItemLoadTransfers(items, SLOT_FUEL_INPUT, SLOT_FUEL_OUTPUT, 2, fuelTank(), oxidizerTank());
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        updateFuelTankTypes();

        if (usesReloadDelay()) {
            if (delay > 0) {
                delay--;
            }
            // TileEntityLaunchPad resets its loading timer while unloaded or
            // unfuelled; inserting a valid missile then counts down from 100.
            if (!isMissileValid() || !hasFuel()) {
                delay = RELOAD_DELAY;
            }
        }

        redstonePowered = isLaunchPadPowered(level, pos, blockState);
        if (redstonePowered && !prevRedstonePowered) {
            launchFromDesignator();
        }
        prevRedstonePowered = redstonePowered;

        if (!hasFuel() || !isMissileValid() || !canInstantiateMissile()) {
            state = STATE_MISSING;
        } else if (!isReadyForLaunch()) {
            state = STATE_LOADING;
        } else {
            state = STATE_READY;
        }

        return oldPower != energy.getPower()
                || oldFuel != fuelTank().getFill()
                || oldOxidizer != oxidizerTank().getFill()
                || oldState != state
                || oldDelay != delay
                || oldRedstone != redstonePowered;
    }

    protected boolean isLaunchPadPowered(Level level, BlockPos pos, BlockState blockState) {
        if (level.hasNeighborSignal(pos)) {
            return true;
        }
        if (blockState.getBlock() instanceof LaunchPadBlock block) {
            LegacyMultiblockLayout layout = block.getMultiblockLayout(blockState, level, pos);
            for (BlockPos offset : layout.offsets()) {
                if (!offset.equals(BlockPos.ZERO) && level.hasNeighborSignal(pos.offset(offset))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean launchFromDesignator() {
        if (!canLaunchBase()) {
            return false;
        }
        if (items.getStackInSlot(SLOT_MISSILE).is(ModItems.MISSILE_ANTI_BALLISTIC.get())) {
            return launchToCoordinate(worldPosition.getX(), worldPosition.getZ());
        }
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        if (stack.isEmpty()) {
            return false;
        }
        BlockPos target = designatorTarget(stack);
        return target != null && launchToCoordinate(target.getX(), target.getZ());
    }

    public boolean launchToCoordinate(int targetX, int targetZ) {
        if (!(level instanceof ServerLevel serverLevel) || !canLaunchBase()) {
            return false;
        }
        Entity missile = instantiateMissile(serverLevel, targetX, targetZ, null);
        if (missile == null) {
            return false;
        }
        return finalizeLaunch(serverLevel, missile);
    }

    @Override
    public boolean sendCommandPosition(int x, int y, int z) {
        return launchToCoordinate(x, z);
    }

    @Override
    public boolean sendCommandEntity(Entity target) {
        return target != null && launchToEntity(target);
    }

    protected boolean canLaunchBase() {
        return isMissileValid() && hasFuel() && isReadyForLaunch() && canInstantiateMissile();
    }

    protected boolean isReadyForLaunch() {
        return delay <= 0;
    }

    protected boolean usesReloadDelay() {
        return true;
    }

    private boolean launchToEntity(Entity target) {
        if (!(level instanceof ServerLevel serverLevel) || !canLaunchBase()) {
            return false;
        }
        Entity missile = instantiateMissile(serverLevel, (int) Math.floor(target.getX()),
                (int) Math.floor(target.getZ()), target);
        if (missile == null) {
            return false;
        }
        return finalizeLaunch(serverLevel, missile);
    }

    protected Entity instantiateMissile(ServerLevel level, int targetX, int targetZ, @Nullable Entity targetEntity) {
        ItemStack stack = items.getStackInSlot(SLOT_MISSILE);
        Entity missile;
        MissileEntity.Variant variant;
        if (stack.is(ModItems.MISSILE_TEST.get())) {
            variant = MissileEntity.Variant.TEST;
            missile = new MissileEntity(ModEntityTypes.MISSILE_TEST.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_GENERIC.get())) {
            variant = MissileEntity.Variant.GENERIC;
            missile = new MissileEntity(ModEntityTypes.MISSILE_GENERIC.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_DECOY.get())) {
            variant = MissileEntity.Variant.DECOY;
            missile = new MissileEntity(ModEntityTypes.MISSILE_DECOY.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_INCENDIARY.get())) {
            variant = MissileEntity.Variant.INCENDIARY;
            missile = new MissileEntity(ModEntityTypes.MISSILE_INCENDIARY.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_CLUSTER.get())) {
            variant = MissileEntity.Variant.CLUSTER;
            missile = new MissileEntity(ModEntityTypes.MISSILE_CLUSTER.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_BUSTER.get())) {
            variant = MissileEntity.Variant.BUSTER;
            missile = new MissileEntity(ModEntityTypes.MISSILE_BUSTER.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_STRONG.get())) {
            variant = MissileEntity.Variant.STRONG;
            missile = new MissileEntity(ModEntityTypes.MISSILE_STRONG.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_INCENDIARY_STRONG.get())) {
            variant = MissileEntity.Variant.INCENDIARY_STRONG;
            missile = new MissileEntity(ModEntityTypes.MISSILE_INCENDIARY_STRONG.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_CLUSTER_STRONG.get())) {
            variant = MissileEntity.Variant.CLUSTER_STRONG;
            missile = new MissileEntity(ModEntityTypes.MISSILE_CLUSTER_STRONG.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_BUSTER_STRONG.get())) {
            variant = MissileEntity.Variant.BUSTER_STRONG;
            missile = new MissileEntity(ModEntityTypes.MISSILE_BUSTER_STRONG.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_EMP_STRONG.get())) {
            variant = MissileEntity.Variant.EMP_STRONG;
            missile = new MissileEntity(ModEntityTypes.MISSILE_EMP_STRONG.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_BURST.get())) {
            variant = MissileEntity.Variant.BURST;
            missile = new MissileEntity(ModEntityTypes.MISSILE_BURST.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_INFERNO.get())) {
            variant = MissileEntity.Variant.INFERNO;
            missile = new MissileEntity(ModEntityTypes.MISSILE_INFERNO.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_RAIN.get())) {
            variant = MissileEntity.Variant.RAIN;
            missile = new MissileEntity(ModEntityTypes.MISSILE_RAIN.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_DRILL.get())) {
            variant = MissileEntity.Variant.DRILL;
            missile = new MissileEntity(ModEntityTypes.MISSILE_DRILL.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_STEALTH.get())) {
            variant = MissileEntity.Variant.STEALTH;
            missile = new MissileEntity(ModEntityTypes.MISSILE_STEALTH.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_EMP.get())) {
            variant = MissileEntity.Variant.EMP;
            missile = new MissileEntity(ModEntityTypes.MISSILE_EMP.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_MICRO.get())) {
            variant = MissileEntity.Variant.MICRO;
            missile = new MissileEntity(ModEntityTypes.MISSILE_MICRO.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_SCHRABIDIUM.get())) {
            variant = MissileEntity.Variant.SCHRABIDIUM;
            missile = new MissileEntity(ModEntityTypes.MISSILE_SCHRABIDIUM.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_BHOLE.get())) {
            variant = MissileEntity.Variant.BHOLE;
            missile = new MissileEntity(ModEntityTypes.MISSILE_BHOLE.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_TAINT.get())) {
            variant = MissileEntity.Variant.TAINT;
            missile = new MissileEntity(ModEntityTypes.MISSILE_TAINT.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_NUCLEAR.get())) {
            variant = MissileEntity.Variant.NUCLEAR;
            missile = new MissileEntity(ModEntityTypes.MISSILE_NUCLEAR.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_NUCLEAR_CLUSTER.get())) {
            variant = MissileEntity.Variant.MIRV;
            missile = new MissileEntity(ModEntityTypes.MISSILE_NUCLEAR_CLUSTER.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_VOLCANO.get())) {
            variant = MissileEntity.Variant.VOLCANO;
            missile = new MissileEntity(ModEntityTypes.MISSILE_VOLCANO.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_SHUTTLE.get())) {
            variant = MissileEntity.Variant.SHUTTLE;
            missile = new MissileEntity(ModEntityTypes.MISSILE_SHUTTLE.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_DOOMSDAY.get())) {
            variant = MissileEntity.Variant.DOOMSDAY;
            missile = new MissileEntity(ModEntityTypes.MISSILE_DOOMSDAY.get(), level, variant);
        } else if (stack.is(ModItems.MISSILE_ANTI_BALLISTIC.get())) {
            AntiBallisticMissileEntity abm = new AntiBallisticMissileEntity(ModEntityTypes.MISSILE_ANTI_BALLISTIC.get(), level);
            abm.setTrackingTarget(targetEntity);
            missile = abm;
        } else {
            return null;
        }
        if (missile instanceof MissileEntity ballistic) {
            ballistic.configureLaunch(worldPosition.getX() + 0.5D, worldPosition.getY() + getLaunchOffset(),
                    worldPosition.getZ() + 0.5D, targetX, targetZ);
            // BlockDummyable stored ForgeDirection.ordinal() in the core metadata and
            // passed metadata - offset to missile watcher 3.  Modern Direction keeps
            // those horizontal ordinal values (N=2, S=3, W=4, E=5).
            BlockState state = getBlockState();
            Direction facing = state.hasProperty(LaunchPadBlock.FACING)
                    ? state.getValue(LaunchPadBlock.FACING) : Direction.EAST;
            ballistic.setLaunchFacing(facing.get3DDataValue());
        } else if (missile instanceof AntiBallisticMissileEntity abm) {
            abm.configureLaunch(worldPosition.getX() + 0.5D, worldPosition.getY() + getLaunchOffset(),
                    worldPosition.getZ() + 0.5D);
        }
        return missile;
    }

    protected double getLaunchOffset() {
        return 1.0D;
    }

    protected boolean finalizeLaunch(ServerLevel level, Entity missile) {
        // The legacy sequence commits launch resources only after handing the
        // entity to the world.  ServerLevel can reject an entity when its chunk
        // is not yet usable, so do not turn that modern failure into a consumed
        // missile/power/fuel launch.
        if (!level.addFreshEntity(missile)) {
            return false;
        }
        LegacySoundPlayer.playSoundEffect(level, worldPosition.getX() + 0.5D, worldPosition.getY(),
                worldPosition.getZ() + 0.5D, "hbm:weapon.missileTakeOff", SoundSource.PLAYERS, 2.0F, 1.0F);
        energy.setPower(Math.max(0L, energy.getPower() - LAUNCH_POWER));
        ItemStack stack = items.getStackInSlot(SLOT_MISSILE);
        if (stack.getItem() instanceof MissileItem missileItem && missileItem.fuel() != MissileItem.Fuel.SOLID) {
            fuelTank().drain(missileItem.fuelCap(), false);
            oxidizerTank().drain(missileItem.fuelCap(), false);
        }
        items.extractItem(SLOT_MISSILE, 1, false);
        if (usesReloadDelay()) {
            delay = RELOAD_DELAY;
        }
        setChanged();
        return true;
    }

    protected boolean canInstantiateMissile() {
        ItemStack stack = items.getStackInSlot(SLOT_MISSILE);
        return stack.is(ModItems.MISSILE_GENERIC.get())
                || stack.is(ModItems.MISSILE_TEST.get())
                || stack.is(ModItems.MISSILE_DECOY.get())
                || stack.is(ModItems.MISSILE_INCENDIARY.get())
                || stack.is(ModItems.MISSILE_CLUSTER.get())
                || stack.is(ModItems.MISSILE_BUSTER.get())
                || stack.is(ModItems.MISSILE_STRONG.get())
                || stack.is(ModItems.MISSILE_INCENDIARY_STRONG.get())
                || stack.is(ModItems.MISSILE_CLUSTER_STRONG.get())
                || stack.is(ModItems.MISSILE_BUSTER_STRONG.get())
                || stack.is(ModItems.MISSILE_EMP_STRONG.get())
                || stack.is(ModItems.MISSILE_BURST.get())
                || stack.is(ModItems.MISSILE_INFERNO.get())
                || stack.is(ModItems.MISSILE_RAIN.get())
                || stack.is(ModItems.MISSILE_DRILL.get())
                || stack.is(ModItems.MISSILE_STEALTH.get())
                || stack.is(ModItems.MISSILE_EMP.get())
                || stack.is(ModItems.MISSILE_MICRO.get())
                || stack.is(ModItems.MISSILE_SCHRABIDIUM.get())
                || stack.is(ModItems.MISSILE_BHOLE.get())
                || stack.is(ModItems.MISSILE_TAINT.get())
                || stack.is(ModItems.MISSILE_NUCLEAR.get())
                || stack.is(ModItems.MISSILE_NUCLEAR_CLUSTER.get())
                || stack.is(ModItems.MISSILE_VOLCANO.get())
                || stack.is(ModItems.MISSILE_SHUTTLE.get())
                || stack.is(ModItems.MISSILE_DOOMSDAY.get())
                || stack.is(ModItems.MISSILE_ANTI_BALLISTIC.get());
    }

    public boolean isMissileValid() {
        return isMissileValid(items.getStackInSlot(SLOT_MISSILE));
    }

    public boolean isMissileValid(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MissileItem missile && missile.launchable();
    }

    public boolean hasFuel() {
        if (energy.getPower() < LAUNCH_POWER || !isMissileValid()) {
            return false;
        }
        ItemStack stack = items.getStackInSlot(SLOT_MISSILE);
        MissileItem missile = (MissileItem) stack.getItem();
        return missile.fuel() == MissileItem.Fuel.SOLID
                || (fuelTank().getFill() >= missile.fuelCap() && oxidizerTank().getFill() >= missile.fuelCap());
    }

    public int getFuelState() {
        return getGaugeState(0);
    }

    public int getOxidizerState() {
        return getGaugeState(1);
    }

    private int getGaugeState(int tank) {
        ItemStack stack = items.getStackInSlot(SLOT_MISSILE);
        if (!(stack.getItem() instanceof MissileItem missile) || missile.fuel() == MissileItem.Fuel.SOLID) {
            return 0;
        }
        HbmFluidTank selected = tank == 0 ? fuelTank() : oxidizerTank();
        return selected.getFill() >= missile.fuelCap() ? 1 : -1;
    }

    protected void updateFuelTankTypes() {
        ItemStack stack = items.getStackInSlot(SLOT_MISSILE);
        if (!(stack.getItem() instanceof MissileItem missile)) {
            return;
        }
        FluidPair pair = fluidPair(missile.fuel());
        if (pair.fuel != null) {
            fuelTank().setTankType(pair.fuel);
        }
        if (pair.oxidizer != null) {
            oxidizerTank().setTankType(pair.oxidizer);
        }
    }

    private BlockPos designatorTarget(ItemStack stack) {
        if (level != null && stack.getItem() instanceof DesignatorItem designator
                && designator.isReady(level, stack, worldPosition)) {
            return designator.getHorizontalTarget(level, stack, worldPosition);
        }
        return null;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank fuelTank() {
        return getAllTanks().get(0);
    }

    public HbmFluidTank oxidizerTank() {
        return getAllTanks().get(1);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public long getStoredPower() {
        return energy.getPower();
    }

    public long getMaxStoredPower() {
        return energy.getMaxPower();
    }

    public int getState() {
        return state;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(fuelTank(), oxidizerTank());
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        // TileEntityLaunchPadBase#canConnect rejects the vertical faces.  A
        // null side is the modern internal/network query, not a physical
        // ForgeDirection.UNKNOWN face, and must remain usable for remote-port
        // subscription bookkeeping.
        return side == Direction.UP || side == Direction.DOWN
                ? HbmFluidSideMode.NONE : HbmFluidSideMode.INPUT;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(fuelTank(), oxidizerTank());
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == fuelTank().getTankType() || type == oxidizerTank().getTankType();
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        if (networkFluidPorts == null) {
            networkFluidPorts = List.of(
                    new FluidPort(new BlockPos(2, 0, -1), Direction.EAST),
                    new FluidPort(new BlockPos(2, 0, 1), Direction.EAST),
                    new FluidPort(new BlockPos(-2, 0, -1), Direction.WEST),
                    new FluidPort(new BlockPos(-2, 0, 1), Direction.WEST),
                    new FluidPort(new BlockPos(-1, 0, 2), Direction.SOUTH),
                    new FluidPort(new BlockPos(1, 0, 2), Direction.SOUTH),
                    new FluidPort(new BlockPos(-1, 0, -2), Direction.NORTH),
                    new FluidPort(new BlockPos(1, 0, -2), Direction.NORTH));
        }
        return networkFluidPorts;
    }

    /**
     * The legacy launch-pad base retries every remote fluid endpoint on its
     * fixed 20-tick pass. Large launch pads inherit the same contract.
     */
    @Override
    protected boolean shouldRefreshFluidNetworkSubscriptionsEveryTick() {
        return true;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        if (energyPorts == null) {
            energyPorts = List.of(
                    new EnergyPort(new BlockPos(2, 0, -1), Direction.EAST),
                    new EnergyPort(new BlockPos(2, 0, 1), Direction.EAST),
                    new EnergyPort(new BlockPos(-2, 0, -1), Direction.WEST),
                    new EnergyPort(new BlockPos(-2, 0, 1), Direction.WEST),
                    new EnergyPort(new BlockPos(-1, 0, 2), Direction.SOUTH),
                    new EnergyPort(new BlockPos(1, 0, 2), Direction.SOUTH),
                    new EnergyPort(new BlockPos(-1, 0, -2), Direction.NORTH),
                    new EnergyPort(new BlockPos(1, 0, -2), Direction.NORTH));
        }
        return energyPorts;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == Direction.UP || side == Direction.DOWN
                ? HbmEnergySideMode.NONE : HbmEnergySideMode.INPUT;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != Direction.UP && side != Direction.DOWN
                && type != null && type != HbmFluids.NONE
                && (type == fuelTank().getTankType() || type == oxidizerTank().getTankType());
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.hbm_ntm_rebirth.launch_pad", "Silo Launch Pad");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LaunchPadMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        fuelTank().writeToNbt(tag, "t0");
        oxidizerTank().writeToNbt(tag, "t1");
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_STATE, state);
        tag.putBoolean(TAG_REDSTONE, redstonePowered);
        tag.putBoolean(TAG_PREV_REDSTONE, prevRedstonePowered);
        if (usesReloadDelay()) {
            tag.putInt(TAG_DELAY, delay);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        fuelTank().readFromNbt(tag, "t0");
        oxidizerTank().readFromNbt(tag, "t1");
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        state = tag.getInt(TAG_STATE);
        redstonePowered = tag.getBoolean(TAG_REDSTONE);
        prevRedstonePowered = tag.getBoolean(TAG_PREV_REDSTONE);
        if (usesReloadDelay()) {
            delay = tag.getInt(TAG_DELAY);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 15, 3));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static FluidPair fluidPair(MissileItem.Fuel fuel) {
        return switch (fuel) {
            case ETHANOL_PEROXIDE -> new FluidPair(HbmFluids.ETHANOL, HbmFluids.PEROXIDE);
            case KEROSENE_PEROXIDE -> new FluidPair(HbmFluids.KEROSENE, HbmFluids.PEROXIDE);
            case KEROSENE_LOXY -> new FluidPair(HbmFluids.KEROSENE, HbmFluids.OXYGEN);
            case JETFUEL_LOXY -> new FluidPair(HbmFluids.KEROSENE_REFORM, HbmFluids.OXYGEN);
            case SOLID -> new FluidPair(null, null);
        };
    }

    private record FluidPair(@Nullable FluidType fuel, @Nullable FluidType oxidizer) {
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_MISSILE) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == 0 ? items.insertItem(SLOT_MISSILE, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(SLOT_MISSILE) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && items.isItemValid(SLOT_MISSILE, stack);
        }
    }
}

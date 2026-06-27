package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.Laserable;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.DfcEmitterMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class DfcEmitterBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, Laserable, HbmLegacyButtonReceiver {
    public static final long MAX_POWER = 1_000_000_000L;
    public static final int CRYOGEL_CAPACITY = 64_000;
    public static final int RANGE = 50;
    public static final int CONTROL_SET_WATTS = 0;
    public static final int CONTROL_TOGGLE = 1;

    private final HbmFluidTank cryogel;
    private int watts = 1;
    private int beam;
    private long joules;
    private long prev;
    private boolean isOn;

    public DfcEmitterBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.CRYOGEL, CRYOGEL_CAPACITY));
    }

    private DfcEmitterBlockEntity(BlockPos pos, BlockState state, HbmFluidTank cryogel) {
        super(ModBlockEntities.DFC_EMITTER.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(cryogel));
        this.cryogel = cryogel;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DfcEmitterBlockEntity emitter) {
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, emitter);
        emitter.tickServer(level, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DfcEmitterBlockEntity emitter) {
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        watts = Mth.clamp(watts, 1, 100);
        long demand = MAX_POWER * watts / 2000L;
        beam = 0;
        if (joules > 0L || prev > 0L) {
            if (cryogel.getFill() >= 20) {
                cryogel.setFill(cryogel.getFill() - 20);
            } else {
                level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
        if (isOn) {
            if (energy.getPower() >= demand) {
                energy.setPower(energy.getPower() - demand);
                joules += watts * 100L;
            }
            prev = joules;
            if (joules > 0L) {
                emitLaser(level, pos);
                joules = 0L;
            }
        } else {
            joules = 0L;
            prev = 0L;
        }
        networkPackNT(250);
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    private void emitLaser(Level level, BlockPos pos) {
        long out = joules * 95L / 100L;
        Direction direction = facing();
        for (int i = 1; i <= RANGE; i++) {
            beam = i;
            BlockPos target = pos.relative(direction, i);
            BlockEntity targetEntity = level.getBlockEntity(target);
            BlockState targetState = level.getBlockState(target);
            if (targetEntity instanceof DfcCoreBlockEntity core) {
                out = core.burn(out);
                continue;
            }
            if (targetEntity instanceof Laserable laserable) {
                laserable.addEnergy(level, target, out, direction);
                break;
            }
            if (targetState.getBlock() instanceof Laserable laserable) {
                laserable.addEnergy(level, target, out, direction);
                break;
            }
            if (!targetState.isAir()) {
                if (targetState.getBlock() instanceof LiquidBlock) {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else if (targetState.getDestroySpeed(level, target) >= 0.0F
                        && targetState.getExplosionResistance(level, target, null) < 6000.0F
                        && level.random.nextInt(20) == 0) {
                    level.destroyBlock(target, false);
                }
                break;
            }
        }
        AABB box = beamBox(pos, direction, beam);
        for (Entity entity : level.getEntities(null, box)) {
            entity.hurt(ModDamageSources.source(level, ModDamageSources.AMS_CORE), 50.0F);
            entity.setSecondsOnFire(10);
        }
    }

    private static AABB beamBox(BlockPos pos, Direction direction, int beam) {
        BlockPos end = pos.relative(direction, beam);
        return new AABB(
                Math.min(pos.getX(), end.getX()) + 0.2D,
                Math.min(pos.getY(), end.getY()) + 0.2D,
                Math.min(pos.getZ(), end.getZ()) + 0.2D,
                Math.max(pos.getX(), end.getX()) + 0.8D,
                Math.max(pos.getY(), end.getY()) + 0.8D,
                Math.max(pos.getZ(), end.getZ()) + 0.8D);
    }

    private Direction facing() {
        return getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public void addEnergy(Level level, BlockPos pos, long energy, @Nullable Direction side) {
        if (side == null || side.getOpposite() != facing()) {
            joules += energy;
            setChanged();
        }
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_SET_WATTS) {
            watts = Mth.clamp(value, 1, 100);
        } else if (id == CONTROL_TOGGLE) {
            isOn = !isOn;
        }
        setChanged();
    }

    public HbmFluidTank getCryogelTank() { return cryogel; }
    public int getWatts() { return watts; }
    public int getBeam() { return beam; }
    public long getPrev() { return prev; }
    public boolean isOn() { return isOn; }
    public int getWattsScaled(int width) { return watts * width / 100; }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.dfcEmitter", "DFC Emitter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DfcEmitterMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(cryogel);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == cryogel.getTankType();
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
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE + 1, RANGE + 1, RANGE + 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("power", energy.getPower());
        tag.putInt("watts", watts);
        tag.putLong("joules", joules);
        tag.putLong("prev", prev);
        tag.putBoolean("isOn", isOn);
        tag.putInt("beam", beam);
        cryogel.writeToNbt(tag, "tank");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("power")) energy.setPower(tag.getLong("power"));
        watts = tag.getInt("watts");
        joules = tag.getLong("joules");
        prev = tag.getLong("prev");
        isOn = tag.getBoolean("isOn");
        beam = tag.getInt("beam");
        cryogel.readFromNbt(tag, "tank");
    }
}

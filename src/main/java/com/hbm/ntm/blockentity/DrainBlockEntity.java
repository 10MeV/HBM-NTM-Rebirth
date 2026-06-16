package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DrainBlockEntity extends HbmFluidNetworkBlockEntity implements HbmStandardFluidReceiver {
    private static final int TANK_CAPACITY = 2_000;

    private final HbmFluidTank tank;

    public DrainBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private DrainBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.DRAIN.get(), pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DrainBlockEntity drain) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, drain);

        int previousFill = drain.tank.getFill();
        if (drain.tank.getFill() > 0) {
            if (drain.tank.getTankType().isAntimatter()) {
                level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        10.0F, true, Level.ExplosionInteraction.BLOCK);
                return;
            }
            int toSpill = Math.max(drain.tank.getFill() / 2, 1);
            drain.tank.release(level, pos, toSpill, FluidReleaseType.SPILL, false);
        }

        if (previousFill != drain.tank.getFill() || level.getGameTime() % 20L == 0L) {
            drain.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        drain.networkPackNT(50);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DrainBlockEntity drain) {
        if (!level.isClientSide || drain.tank.getFill() <= 0 || level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 100.0D, false) == null) {
            return;
        }
        Direction facing = drain.facing();
        CompoundTag data = new CompoundTag();
        if (drain.tank.getTankType().hasTrait(SimpleFluidTraits.Gaseous.class)) {
            data.putString("type", ParticleUtil.TYPE_COOLING_TOWER);
            data.putFloat("lift", 0.5F);
            data.putFloat("base", 0.375F);
            data.putFloat("max", 3.0F);
            data.putInt("life", 100 + level.random.nextInt(50));
        } else {
            data.putString("type", ParticleUtil.TYPE_SPLASH);
        }
        data.putInt("color", drain.tank.getTankType().getColor());
        ParticleUtil.spawnAux(level,
                pos.getX() + 0.5D - facing.getStepX() * 2.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D - facing.getStepZ() * 2.5D,
                data, 100.0D);
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public void setTankType(FluidType type) {
        tank.setTankType(type);
        onFluidContentsChanged();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(LegacyLookOverlayLines.compactTank(true, tank)));
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
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
    public HbmEnergyReceiver.ConnectionPriority getFluidPriority() {
        return HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != null && type != HbmFluids.NONE && type == tank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX(), 0, facing.getStepZ(), facing),
                FluidPort.of(rot.getStepX(), 0, rot.getStepZ(), rot),
                FluidPort.of(-rot.getStepX(), 0, -rot.getStepZ(), rot.getOpposite()));
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal() ? HbmFluidSideMode.INPUT : HbmFluidSideMode.NONE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.writeToNbt(tag, "t");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("t") || tag.contains("t_type") || tag.contains("t_type_id")) {
            tank.readFromNbt(tag, "t");
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
    }
}

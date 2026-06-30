package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidProvider;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BigAssTankBlockEntity extends LegacyBigTankBlockEntity {
    private static final int TANK_CAPACITY = 16_000_000;
    private static final long TRANSFER_SPEED_FLOOR = 50_000L;
    private static final int FLOOR_COUNT = 4 * 4;
    private int tiltBlocksChecked;
    private int tiltBlocksValid;

    public BigAssTankBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ModBlockEntities.BIG_ASS_TANK.get(), new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BigAssTankBlockEntity tank) {
        if (level.isClientSide) {
            return;
        }
        tank.checkTiltAgainstFoundation(level);
        FluidTankBlockEntity.serverTick(level, pos, state, tank);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return List.of(
                FluidPort.of(facing.getStepX() * 7, 0, facing.getStepZ() * 7, facing),
                FluidPort.of(-facing.getStepX() * 7, 0, -facing.getStepZ() * 7, facing.getOpposite()));
    }

    @Override
    protected long getTransferSpeedFloor() {
        return TRANSFER_SPEED_FLOOR;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        return isTilted() ? 0L : super.getDemand(type, pressure);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        return isTilted() ? amount : super.transferFluid(type, pressure, amount);
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        if (isTilted() && getMode() != MODE_BUFFER) {
            return false;
        }
        return super.shouldSubscribeAsFluidProvider(type);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        if (isTilted() && getMode() != MODE_BUFFER) {
            return false;
        }
        return super.shouldSubscribeAsFluidReceiver(type);
    }

    @Override
    protected HbmFluidUtil.PortTransferReport tryProvideFluidToPortsReport(FluidType type, int pressure,
            HbmFluidProvider provider) {
        return isTilted() ? HbmFluidUtil.PortTransferReport.empty()
                : super.tryProvideFluidToPortsReport(type, pressure, provider);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(Direction side) {
        return isTilted() ? HbmFluidSideMode.NONE : super.getFluidSideMode(side);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bigAssTank");
    }

    private boolean checkTiltAgainstFoundation(Level level) {
        if ((level.getGameTime() + blockIdentity(worldPosition)) % 20L != 0L) {
            return false;
        }
        boolean changed = false;
        if (tiltBlocksChecked >= FLOOR_COUNT) {
            changed = setTiltedState(level, tiltBlocksValid < tiltBlocksChecked * 0.95D);
            tiltBlocksChecked = 0;
            tiltBlocksValid = 0;
        }

        BlockPos floor = standardFloor7x7(tiltBlocksChecked);
        tiltBlocksChecked++;
        if (isValidHeavyFoundation(level, floor)) {
            tiltBlocksValid++;
        }
        return changed;
    }

    private boolean setTiltedState(Level level, boolean tilted) {
        if (isTilted() == tilted) {
            return false;
        }
        if (tilted) {
            level.playSound(null, worldPosition, ModSounds.BLOCK_METAL_IMPACT.get(), SoundSource.BLOCKS, 3.0F, 1.0F);
        }
        setTilted(tilted);
        markFluidSubscriptionDirty();
        invalidateFluidHandlers();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }

    private BlockPos standardFloor7x7(int index) {
        return new BlockPos(
                worldPosition.getX() - 3 + (index / 4) * 2,
                worldPosition.getY() - 1,
                worldPosition.getZ() - 3 + (index % 4) * 2);
    }

    private static int blockIdentity(BlockPos pos) {
        return (pos.getY() + pos.getZ() * 27_644_437) * 27_644_437 + pos.getX();
    }

    private static boolean isValidHeavyFoundation(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()
                || !state.isFaceSturdy(level, pos, Direction.UP)
                || !state.isSolidRender(level, pos)
                || isLooseHeavyFoundationMaterial(state)) {
            return false;
        }
        return state.getExplosionResistance(level, pos, null) >= Blocks.STONE.getExplosionResistance();
    }

    private static boolean isLooseHeavyFoundationMaterial(BlockState state) {
        return state.is(BlockTags.SAND)
                || state.is(BlockTags.WOOL)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD)
                || state.is(Blocks.FARMLAND);
    }
}

package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class BigAssTankBlockEntity extends FluidTankBlockEntity {
    private static final int TANK_CAPACITY = 16_000_000;
    private static final long TRANSFER_SPEED_FLOOR = 50_000L;

    public BigAssTankBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ModBlockEntities.BIG_ASS_TANK.get(), new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
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
    public Component getDisplayName() {
        return Component.translatable("container.bigAssTank");
    }
}

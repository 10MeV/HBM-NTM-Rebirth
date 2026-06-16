package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class OrbusBlockEntity extends LegacyBigTankBlockEntity {
    private static final int TANK_CAPACITY = 512_000;
    private static final long TRANSFER_SPEED_FLOOR = 1_000L;

    public OrbusBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ModBlockEntities.ORBUS.get(), new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                HbmFluidPortLayouts.legacy(facing, side, 0, 0, -1, Direction.DOWN),
                HbmFluidPortLayouts.legacy(facing, side, -1, 0, -1, Direction.DOWN),
                HbmFluidPortLayouts.legacy(facing, side, 0, 1, -1, Direction.DOWN),
                HbmFluidPortLayouts.legacy(facing, side, -1, 1, -1, Direction.DOWN),
                HbmFluidPortLayouts.legacy(facing, side, 0, 0, 5, Direction.UP),
                HbmFluidPortLayouts.legacy(facing, side, -1, 0, 5, Direction.UP),
                HbmFluidPortLayouts.legacy(facing, side, 0, 1, 5, Direction.UP),
                HbmFluidPortLayouts.legacy(facing, side, -1, 1, 5, Direction.UP));
    }

    @Override
    protected boolean checkHazards() {
        return false;
    }

    @Override
    protected long getTransferSpeedFloor() {
        return TRANSFER_SPEED_FLOOR;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.orbus");
    }
}

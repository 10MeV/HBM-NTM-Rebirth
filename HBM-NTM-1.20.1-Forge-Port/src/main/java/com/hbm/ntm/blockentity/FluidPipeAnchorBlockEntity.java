package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class FluidPipeAnchorBlockEntity extends FluidPipeBlockEntity {
    public FluidPipeAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE_ANCHOR.get(), pos, state);
    }

    @Override
    protected Set<Direction> getConnections() {
        if (level == null) {
            return Set.of();
        }
        Direction attachedSide = FluidPipeAnchorBlock.attachedSide(getBlockState());
        return HbmFluidConnectionUtil.canConnect(level, worldPosition, getFluidType(), this, attachedSide)
                ? Set.of(attachedSide)
                : Set.of();
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return super.canConnectFluid(type, side) && side == FluidPipeAnchorBlock.attachedSide(getBlockState());
    }
}

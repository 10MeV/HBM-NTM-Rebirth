package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class RadioTorchCounterBlock extends RadioTorchBlock {
    protected RadioTorchCounterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(supportPos);
        // Unlike RadioTorchBase, RadioTorchCounter#canBlockStay did not
        // accept every comparator/power source as a support.  Its exact
        // legacy branches are a sturdy support face, a normal-render block,
        // or an adjacent inventory; Forge's item handler is the modern
        // equivalent of that last branch.
        return support.isFaceSturdy(level, supportPos, facing)
                || support.isSolidRender(level, supportPos)
                || hasAttachedItemHandler(level, pos, facing);
    }
}

package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyNtmGlassPaneBlock extends IronBarsBlock {
    public LegacyNtmGlassPaneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), connectsTo(level, pos, direction));
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState adjacentState,
            LevelAccessor level, BlockPos pos, BlockPos adjacentPos) {
        BlockState updated = super.updateShape(state, direction, adjacentState, level, pos, adjacentPos);
        if (direction.getAxis().isHorizontal()) {
            return updated.setValue(PROPERTY_BY_DIRECTION.get(direction), connectsTo(level, pos, direction));
        }
        return updated;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    private boolean connectsTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos adjacentPos = pos.relative(direction);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        return adjacentState.getBlock() instanceof LegacyNtmGlassBlock
                || attachsTo(adjacentState, adjacentState.isFaceSturdy(level, adjacentPos, direction.getOpposite()));
    }
}

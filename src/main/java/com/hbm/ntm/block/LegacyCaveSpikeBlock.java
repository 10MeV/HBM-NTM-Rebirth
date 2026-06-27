package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LegacyCaveSpikeBlock extends Block {
    private static final VoxelShape STALAGMITE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);
    private static final VoxelShape STALACTITE_SHAPE = Block.box(3.0D, 2.0D, 3.0D, 13.0D, 16.0D, 13.0D);

    private final Kind kind;

    public LegacyCaveSpikeBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return kind == Kind.STALACTITE ? STALACTITE_SHAPE : STALAGMITE_SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = kind == Kind.STALACTITE ? pos.above() : pos.below();
        Direction face = kind == Kind.STALACTITE ? Direction.DOWN : Direction.UP;
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, face);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos)
                ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    public enum Kind {
        STALACTITE,
        STALAGMITE
    }
}

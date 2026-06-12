package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class LegacyChainBlock extends Block {
    public static final DirectionProperty SUPPORT = DirectionProperty.create("support", direction -> direction != Direction.DOWN);
    public static final BooleanProperty END = BooleanProperty.create("end");

    private static final VoxelShape VERTICAL = box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape VERTICAL_END = box(6.0D, 4.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape NORTH = box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 2.0D);
    private static final VoxelShape NORTH_END = box(6.0D, 4.0D, 0.0D, 10.0D, 16.0D, 2.0D);
    private static final VoxelShape SOUTH = box(6.0D, 0.0D, 14.0D, 10.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_END = box(6.0D, 4.0D, 14.0D, 10.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST = box(0.0D, 0.0D, 6.0D, 2.0D, 16.0D, 10.0D);
    private static final VoxelShape WEST_END = box(0.0D, 4.0D, 6.0D, 2.0D, 16.0D, 10.0D);
    private static final VoxelShape EAST = box(14.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape EAST_END = box(14.0D, 4.0D, 6.0D, 16.0D, 16.0D, 10.0D);

    public LegacyChainBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(SUPPORT, Direction.UP)
                .setValue(END, true));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction support = context.getClickedFace().getOpposite();
        BlockState state = stateForSupport(level, pos, support);
        if (state != null) {
            return state;
        }

        BlockState above = level.getBlockState(pos.above());
        if (above.is(this)) {
            return stateForSupport(level, pos, above.getValue(SUPPORT));
        }

        state = stateForSupport(level, pos, Direction.UP);
        if (state != null) {
            return state;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            state = stateForSupport(level, pos, direction);
            if (state != null) {
                return state;
            }
        }
        return null;
    }

    private BlockState stateForSupport(LevelReader level, BlockPos pos, Direction support) {
        if (support == Direction.DOWN || !hasSupport(level, pos, support)) {
            return null;
        }
        return defaultBlockState()
                .setValue(SUPPORT, support)
                .setValue(END, isEnd(level, pos, support));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSupport(level, pos, state.getValue(SUPPORT));
    }

    private boolean hasSupport(LevelReader level, BlockPos pos, Direction support) {
        if (support == Direction.DOWN) {
            return false;
        }
        BlockPos supportPos = pos.relative(support);
        BlockState supportState = level.getBlockState(supportPos);
        if (support == Direction.UP && supportState.is(this)) {
            return true;
        }
        return supportState.isFaceSturdy(level, supportPos, support.getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state.setValue(END, isEnd(level, pos, state.getValue(SUPPORT)));
    }

    private boolean isEnd(BlockGetter level, BlockPos pos, Direction support) {
        BlockState below = level.getBlockState(pos.below());
        return !below.is(this) || below.getValue(SUPPORT) != support;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean end = state.getValue(END);
        return switch (state.getValue(SUPPORT)) {
            case NORTH -> end ? NORTH_END : NORTH;
            case SOUTH -> end ? SOUTH_END : SOUTH;
            case WEST -> end ? WEST_END : WEST;
            case EAST -> end ? EAST_END : EAST;
            default -> end ? VERTICAL_END : VERTICAL;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(SUPPORT, rotation.rotate(state.getValue(SUPPORT)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(SUPPORT)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SUPPORT, END);
    }
}

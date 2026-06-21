package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class LegacyBarrierBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape WEST_PANEL = box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_PANEL = box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_PANEL = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
    private static final VoxelShape SOUTH_PANEL = box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);

    public LegacyBarrierBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return defaultBlockState().setValue(FACING, clickedFace);
        }

        return defaultBlockState().setValue(FACING, switch (LegacyDirectionalShapeBlock.legacyYawQuadrant(context.getRotation())) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        });
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panelForSide(state.getValue(FACING).getOpposite());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (connectsTo(level, pos, state, side)) {
                shape = Shapes.or(shape, panelForSide(side));
            }
        }
        return shape;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    private static boolean connectsTo(BlockGetter level, BlockPos pos, BlockState state, Direction side) {
        if (state.getValue(FACING).getOpposite() == side) {
            return true;
        }
        BlockPos neighborPos = pos.relative(side);
        BlockState neighbor = level.getBlockState(neighborPos);
        return neighbor.isFaceSturdy(level, neighborPos, side.getOpposite())
                || neighbor.isSolidRender(level, neighborPos);
    }

    private static VoxelShape panelForSide(Direction side) {
        return switch (side) {
            case EAST -> EAST_PANEL;
            case SOUTH -> SOUTH_PANEL;
            case WEST -> WEST_PANEL;
            default -> NORTH_PANEL;
        };
    }
}

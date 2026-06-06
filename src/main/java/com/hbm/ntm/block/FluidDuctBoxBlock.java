package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FluidDuctBoxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidDuctBoxBlock extends FluidPipeBlock {
    private static final VoxelShape CORE = box(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    private static final VoxelShape NORTH_ARM = box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 2.0D);
    private static final VoxelShape EAST_ARM = box(14.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
    private static final VoxelShape SOUTH_ARM = box(2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D);
    private static final VoxelShape WEST_ARM = box(0.0D, 2.0D, 2.0D, 2.0D, 14.0D, 14.0D);
    private static final VoxelShape UP_ARM = box(2.0D, 14.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape DOWN_ARM = box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    private static final VoxelShape X_STRAIGHT_SHAPE = Shapes.or(WEST_ARM, CORE, EAST_ARM);
    private static final VoxelShape Y_STRAIGHT_SHAPE = Shapes.or(DOWN_ARM, CORE, UP_ARM);
    private static final VoxelShape Z_STRAIGHT_SHAPE = Shapes.or(NORTH_ARM, CORE, SOUTH_ARM);

    public FluidDuctBoxBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDuctBoxBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    protected static VoxelShape shapeForState(BlockState state) {
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);

        if ((east || west) && !north && !south && !up && !down) {
            return X_STRAIGHT_SHAPE;
        }
        if ((up || down) && !north && !south && !east && !west) {
            return Y_STRAIGHT_SHAPE;
        }
        if ((north || south) && !east && !west && !up && !down) {
            return Z_STRAIGHT_SHAPE;
        }

        VoxelShape shape = CORE;
        if (north) shape = Shapes.or(shape, NORTH_ARM);
        if (east) shape = Shapes.or(shape, EAST_ARM);
        if (south) shape = Shapes.or(shape, SOUTH_ARM);
        if (west) shape = Shapes.or(shape, WEST_ARM);
        if (up) shape = Shapes.or(shape, UP_ARM);
        if (down) shape = Shapes.or(shape, DOWN_ARM);
        return shape;
    }
}

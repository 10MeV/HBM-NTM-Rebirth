package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlock extends HbmFluidNodeBlock {
    private static final VoxelShape CORE = box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    private static final VoxelShape NORTH_ARM = box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D);
    private static final VoxelShape EAST_ARM = box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    private static final VoxelShape SOUTH_ARM = box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D);
    private static final VoxelShape WEST_ARM = box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D);
    private static final VoxelShape UP_ARM = box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape DOWN_ARM = box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D);

    public FluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    private static VoxelShape shapeForState(BlockState state) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_ARM);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_ARM);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_ARM);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_ARM);
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_ARM);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_ARM);
        }
        return shape;
    }
}

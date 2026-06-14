package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RedCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RedCableBoxBlock extends HbmEnergyNodeBlock {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 4);

    public RedCableBoxBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SIZE, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedCableBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
        double lower = 2.0D + state.getValue(SIZE);
        double upper = 14.0D - state.getValue(SIZE);

        VoxelShape core = box(lower, lower, lower, upper, upper, upper);
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);

        if ((east || west) && !north && !south && !up && !down) {
            return box(0.0D, lower, lower, 16.0D, upper, upper);
        }
        if ((up || down) && !north && !south && !east && !west) {
            return box(lower, 0.0D, lower, upper, 16.0D, upper);
        }
        if ((north || south) && !east && !west && !up && !down) {
            return box(lower, lower, 0.0D, upper, upper, 16.0D);
        }

        VoxelShape shape = core;
        if (north) shape = Shapes.or(shape, box(lower, lower, 0.0D, upper, upper, lower));
        if (east) shape = Shapes.or(shape, box(upper, lower, lower, 16.0D, upper, upper));
        if (south) shape = Shapes.or(shape, box(lower, lower, upper, upper, upper, 16.0D));
        if (west) shape = Shapes.or(shape, box(0.0D, lower, lower, lower, upper, upper));
        if (up) shape = Shapes.or(shape, box(lower, upper, lower, upper, 16.0D, upper));
        if (down) shape = Shapes.or(shape, box(lower, 0.0D, lower, upper, lower, upper));
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SIZE);
    }
}

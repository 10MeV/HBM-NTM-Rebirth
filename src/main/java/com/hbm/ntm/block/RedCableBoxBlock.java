package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyNodeBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    private static final VoxelShape[][] SHAPES_BY_SIZE_AND_MASK = buildShapesBySizeAndMask();

    public RedCableBoxBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SIZE, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedCableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.RED_CABLE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                HbmEnergyNodeBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (RedCableBlockEntity) blockEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
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
        return SHAPES_BY_SIZE_AND_MASK[state.getValue(SIZE)][connectionMask(state)];
    }

    private static VoxelShape[][] buildShapesBySizeAndMask() {
        VoxelShape[][] shapes = new VoxelShape[5][64];
        for (int size = 0; size < shapes.length; size++) {
            for (int mask = 0; mask < shapes[size].length; mask++) {
                shapes[size][mask] = buildShape(size, mask);
            }
        }
        return shapes;
    }

    private static VoxelShape buildShape(int size, int mask) {
        double lower = 2.0D + size;
        double upper = 14.0D - size;

        VoxelShape core = box(lower, lower, lower, upper, upper, upper);
        boolean north = (mask & 1) != 0;
        boolean east = (mask & 32) != 0;
        boolean south = (mask & 2) != 0;
        boolean west = (mask & 16) != 0;
        boolean up = (mask & 8) != 0;
        boolean down = (mask & 4) != 0;

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

    private static int connectionMask(BlockState state) {
        return (state.getValue(EAST) ? 32 : 0)
                | (state.getValue(WEST) ? 16 : 0)
                | (state.getValue(UP) ? 8 : 0)
                | (state.getValue(DOWN) ? 4 : 0)
                | (state.getValue(SOUTH) ? 2 : 0)
                | (state.getValue(NORTH) ? 1 : 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SIZE);
    }
}

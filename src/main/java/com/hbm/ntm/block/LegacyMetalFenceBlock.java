package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * State-backed replacement for 1.7.10 {@code BlockMetalFence}.  The forced-post bit is
 * deliberately visual-only: legacy collision was determined exclusively by the four fence
 * connections.
 */
@SuppressWarnings("deprecation")
public final class LegacyMetalFenceBlock extends FenceBlock {
    public static final BooleanProperty FORCE_POST = BooleanProperty.create("force_post");

    public LegacyMetalFenceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FORCE_POST, false)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(FORCE_POST, false)
                .setValue(BlockStateProperties.WATERLOGGED, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return legacyFenceShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return legacyFenceShape(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(FORCE_POST) ? 1 : 0)
                : ItemStack.EMPTY;
    }

    private static VoxelShape legacyFenceShape(BlockState state) {
        boolean north = state.getValue(BlockStateProperties.NORTH);
        boolean south = state.getValue(BlockStateProperties.SOUTH);
        boolean west = state.getValue(BlockStateProperties.WEST);
        boolean east = state.getValue(BlockStateProperties.EAST);

        // This is the exact two-pass shape assembly in BlockMetalFence#addCollisionBoxesToList:
        // north/south first, then west/east (or the centered fallback for an isolated fence).
        VoxelShape shape = Shapes.empty();
        if (north || south) {
            shape = Shapes.or(shape, box(6.0D, 0.0D, north ? 0.0D : 6.0D,
                    10.0D, 16.0D, south ? 16.0D : 10.0D));
        }
        if (west || east || (!north && !south)) {
            shape = Shapes.or(shape, box(west ? 0.0D : 6.0D, 0.0D, 6.0D,
                    east ? 16.0D : 10.0D, 16.0D, 10.0D));
        }
        return shape;
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighbourState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        // FenceBlock only schedules fluid ticks when WATERLOGGED is true; the placement override
        // above intentionally preserves the legacy non-waterloggable contract.
        return super.updateShape(state.setValue(BlockStateProperties.WATERLOGGED, false), direction, neighbourState,
                level, pos, neighbourPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORCE_POST);
    }
}

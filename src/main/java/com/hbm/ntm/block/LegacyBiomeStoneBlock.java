package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/**
 * State-backed equivalent of 1.7.10 {@code BlockBiomeStone}.  Legacy metadata
 * selects the biome stone type while the top-neighbour test selected the side
 * sprite, so the latter is intentionally a derived, non-item state.
 */
public final class LegacyBiomeStoneBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 1);
    public static final BooleanProperty SAME_ABOVE = BooleanProperty.create("same_above");

    public LegacyBiomeStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0).setValue(SAME_ABOVE, false));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = context.getItemInHand().getItem() instanceof LegacyStateBlockItem item
                ? item.getVariant(context.getItemInHand()) : 0;
        BlockPos pos = context.getClickedPos();
        BlockState state = defaultBlockState().setValue(VARIANT, variant);
        return state.setValue(SAME_ABOVE, isSameVariant(context.getLevel().getBlockState(pos.above()), variant));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.UP) {
            return state.setValue(SAME_ABOVE, isSameVariant(neighbourState, state.getValue(VARIANT)));
        }
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(VARIANT))
                : super.getCloneItemStack(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, SAME_ABOVE);
    }

    private boolean isSameVariant(BlockState state, int variant) {
        return state.is(this) && state.getValue(VARIANT) == variant;
    }
}

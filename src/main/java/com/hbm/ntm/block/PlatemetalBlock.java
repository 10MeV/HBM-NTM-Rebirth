package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PlatemetalBlockEntity;
import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/** Fifteen-state carrier for legacy BlockEnumMultiCT PlatemetalType. */
@SuppressWarnings("deprecation")
public final class PlatemetalBlock extends BaseEntityBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 14);
    private static final String[] VARIANT_NAMES = {
            "base", "black", "white", "red", "green", "light_gray", "blue", "purple", "cyan", "pink",
            "lime", "yellow", "light_blue", "magenta", "orange"
    };

    public PlatemetalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    public static String variantName(int variant) {
        return VARIANT_NAMES[Math.max(0, Math.min(VARIANT_NAMES.length - 1, variant))];
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = context.getItemInHand().getItem() instanceof LegacyStateBlockItem item
                ? item.getVariant(context.getItemInHand()) : 0;
        return defaultBlockState().setValue(VARIANT, variant);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(VARIANT))
                : super.getCloneItemStack(level, pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlatemetalBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock())) {
            refreshCtNeighborhood(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        refreshCtNeighborhood(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            refreshCtNeighborhood(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    private static void refreshCtNeighborhood(Level level, BlockPos center) {
        if (!level.isClientSide) {
            return;
        }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (level.getBlockEntity(pos) instanceof PlatemetalBlockEntity plate) {
                plate.refreshConnectedTextureModelData();
            }
        }
    }
}

package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Modern state-backed equivalent of 1.7.10 {@code BlockMultiSlab}/{@code BlockMultiSlabMeta}.
 * The legacy low three metadata bits select a material and bit 3 selects the top half; modern
 * items retain the low-bit material selection in {@link LegacyStateBlockItem#TAG_VARIANT}.
 */
public final class LegacyMultiSlabBlock extends SlabBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 7);

    private final List<Supplier<? extends Block>> materials;
    private final int variants;
    @Nullable
    private final Supplier<? extends LegacyMultiSlabBlock> singleSlab;
    private final boolean speedyAsphalt;

    public LegacyMultiSlabBlock(BlockBehaviour.Properties properties, List<Supplier<? extends Block>> materials,
                                @Nullable Supplier<? extends LegacyMultiSlabBlock> singleSlab,
                                boolean speedyAsphalt) {
        super(properties);
        if (materials.isEmpty() || materials.size() > 8) {
            throw new IllegalArgumentException("Legacy multi slab needs 1..8 material variants");
        }
        this.materials = List.copyOf(materials);
        this.variants = materials.size();
        this.singleSlab = singleSlab;
        this.speedyAsphalt = speedyAsphalt;
        registerDefaultState(defaultBlockState().setValue(TYPE, singleSlab == null ? SlabType.BOTTOM : SlabType.DOUBLE)
                .setValue(VARIANT, 0));
    }

    public int variants() {
        return variants;
    }

    public int normalizeVariant(int variant) {
        return Math.floorMod(variant & 7, variants);
    }

    private int stackVariant(ItemStack stack) {
        if (stack.getItem() instanceof LegacyStateBlockItem stateItem) {
            return normalizeVariant(stateItem.getVariant(stack));
        }
        return 0;
    }

    private BlockState materialState(BlockState state) {
        return materials.get(normalizeVariant(state.getValue(VARIANT))).get().defaultBlockState();
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = stackVariant(context.getItemInHand());
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this) && existing.getValue(TYPE) != SlabType.DOUBLE
                && normalizeVariant(existing.getValue(VARIANT)) == variant) {
            return existing.setValue(TYPE, SlabType.DOUBLE);
        }

        boolean bottom = context.getClickedFace() != Direction.DOWN
                && (context.getClickedFace() == Direction.UP || context.getClickLocation().y - context.getClickedPos().getY() <= 0.5D);
        return defaultBlockState().setValue(VARIANT, variant)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER)
                .setValue(TYPE, bottom ? SlabType.BOTTOM : SlabType.TOP);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.getItemInHand().is(asItem()) || state.getValue(TYPE) == SlabType.DOUBLE
                || normalizeVariant(state.getValue(VARIANT)) != stackVariant(context.getItemInHand())) {
            return false;
        }
        if (!context.replacingClickedOnBlock()) {
            return true;
        }
        return state.getValue(TYPE) == SlabType.BOTTOM
                ? context.getClickedFace() == Direction.UP
                : context.getClickedFace() == Direction.DOWN;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return materialState(state).getDestroyProgress(player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, @Nullable Explosion explosion) {
        return materialState(state).getExplosionResistance(level, pos, explosion);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (speedyAsphalt && level.isClientSide && entity instanceof Player player
                && (player.zza != 0.0F || player.xxa != 0.0F)) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(1.5D, 1.0D, 1.5D));
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        LegacyMultiSlabBlock single = singleSlab == null ? this : singleSlab.get();
        return single.asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, normalizeVariant(state.getValue(VARIANT)))
                : ItemStack.EMPTY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED, VARIANT);
    }
}

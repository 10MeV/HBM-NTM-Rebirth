package com.hbm.ntm.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.IntFunction;

public class LegacyStateBlockItem extends BlockItem {
    public static final String TAG_VARIANT = "hbmLegacyVariant";

    private final IntegerProperty property;
    private final BooleanProperty booleanProperty;
    private final int variants;
    private final IntFunction<Component> nameFactory;

    public LegacyStateBlockItem(Block block, Properties properties, IntegerProperty property, int variants, IntFunction<Component> nameFactory) {
        super(block, properties);
        this.property = property;
        this.booleanProperty = null;
        this.variants = variants;
        this.nameFactory = nameFactory;
    }

    /** Supports legacy two-metadata block items backed by a modern boolean state property. */
    public LegacyStateBlockItem(Block block, Properties properties, BooleanProperty property, int variants,
                                IntFunction<Component> nameFactory) {
        super(block, properties);
        if (variants != 2) {
            throw new IllegalArgumentException("A boolean legacy block item must expose exactly two variants");
        }
        this.property = null;
        this.booleanProperty = property;
        this.variants = variants;
        this.nameFactory = nameFactory;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        int variant = getVariant(context.getItemInHand());
        BlockState variantState = booleanProperty == null
                ? state.setValue(property, variant)
                : state.setValue(booleanProperty, variant == 1);
        return super.placeBlock(context, variantState);
    }

    @Override
    public Component getName(ItemStack stack) {
        return nameFactory.apply(getVariant(stack));
    }

    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (int variant = 0; variant < variants; variant++) {
            output.accept(createStack(this, variant));
        }
    }

    public int getVariant(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int variant = tag == null ? 0 : tag.getInt(TAG_VARIANT);
        return Math.max(0, Math.min(variants - 1, variant));
    }

    public int getVariants() {
        return variants;
    }

    public BlockState stateForVariant(int variant) {
        int clamped = Math.max(0, Math.min(variants - 1, variant));
        return booleanProperty == null
                ? getBlock().defaultBlockState().setValue(property, clamped)
                : getBlock().defaultBlockState().setValue(booleanProperty, clamped == 1);
    }

    public BlockState stateForStack(ItemStack stack) {
        return stateForVariant(getVariant(stack));
    }

    public static ItemStack createStack(LegacyStateBlockItem item, int variant) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_VARIANT, variant);
        return stack;
    }
}

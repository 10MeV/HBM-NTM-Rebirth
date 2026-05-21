package com.hbm.ntm.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.IntFunction;

public class LegacyStateBlockItem extends BlockItem {
    private static final String TAG_VARIANT = "hbmLegacyVariant";

    private final IntegerProperty property;
    private final int variants;
    private final IntFunction<Component> nameFactory;

    public LegacyStateBlockItem(Block block, Properties properties, IntegerProperty property, int variants, IntFunction<Component> nameFactory) {
        super(block, properties);
        this.property = property;
        this.variants = variants;
        this.nameFactory = nameFactory;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return super.placeBlock(context, state.setValue(property, getVariant(context.getItemInHand())));
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

    private int getVariant(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int variant = tag == null ? 0 : tag.getInt(TAG_VARIANT);
        return Math.max(0, Math.min(variants - 1, variant));
    }

    private static ItemStack createStack(LegacyStateBlockItem item, int variant) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_VARIANT, variant);
        return stack;
    }
}

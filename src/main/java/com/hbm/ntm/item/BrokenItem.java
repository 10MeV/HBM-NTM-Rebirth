package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Source-backed carrier for the faulty products emitted by precision assembly.
 * The legacy item stores the failed product's registry id and metadata in NBT;
 * keeping that contract is what lets the matching recycle recipes remain
 * specific to the product that failed.
 */
public final class BrokenItem extends Item {
    public static final String TAG_ITEM_ID = "itemID";
    public static final String TAG_ITEM_META = "itemMeta";

    public BrokenItem(Properties properties) {
        super(properties);
    }

    public static ItemStack make(ItemStack stack) {
        return make(stack.getItem(), stack.getCount(), stack.getDamageValue());
    }

    public static ItemStack make(Item item) {
        return make(item, 1, 0);
    }

    public static ItemStack make(Item item, int count, int meta) {
        ItemStack broken = new ItemStack(ModItems.BROKEN_ITEM.get(), count);
        CompoundTag tag = new CompoundTag();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        tag.putString(TAG_ITEM_ID, itemId.toString());
        tag.putInt(TAG_ITEM_META, meta);
        broken.setTag(tag);
        return broken;
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack source = sourceStack(stack);
        if (source.isEmpty()) {
            return super.getName(stack);
        }
        return Component.translatable(getDescriptionId() + ".prefix", source.getHoverName());
    }

    private static ItemStack sourceStack(ItemStack broken) {
        if (!broken.hasTag()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = broken.getTag();
        ResourceLocation itemId = ResourceLocation.tryParse(tag.getString(TAG_ITEM_ID));
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
            return ItemStack.EMPTY;
        }
        ItemStack source = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        source.setDamageValue(Math.max(0, tag.getInt(TAG_ITEM_META)));
        return source;
    }
}

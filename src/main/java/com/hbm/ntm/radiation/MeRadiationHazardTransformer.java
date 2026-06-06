package com.hbm.ntm.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MeRadiationHazardTransformer implements HazardTransformer {
    private static final String LEGACY_ITEM_TYPES_KEY = "it";

    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !isMeStorageItem(stack.getItem()) || !tag.contains(LEGACY_ITEM_TYPES_KEY, Tag.TAG_ANY_NUMERIC)) {
            return;
        }

        float radiation = readLegacyMeRadiation(tag);
        if (radiation > 0.0F) {
            entries.add(new HazardEntry(HazardType.RADIATION, radiation));
        }
    }

    private static boolean isMeStorageItem(Item item) {
        String name = item.getClass().getName();
        return name.equals("appeng.items.storage.ItemBasicStorageCell")
                || name.equals("appeng.items.tools.powered.ToolPortableCell");
    }

    private static float readLegacyMeRadiation(CompoundTag tag) {
        float radiation = 0.0F;
        int types = tag.getShort(LEGACY_ITEM_TYPES_KEY);
        for (int i = 0; i < types; i++) {
            String stackKey = "#" + i;
            if (!tag.contains(stackKey, Tag.TAG_COMPOUND)) {
                continue;
            }
            ItemStack held = ItemStack.of(tag.getCompound(stackKey));
            if (held.isEmpty()) {
                continue;
            }
            held.setCount(Math.max(0, tag.getInt("@" + i)));
            radiation += HazardRegistry.getStackRadiation(held);
        }
        return radiation;
    }
}

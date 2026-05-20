package com.hbm.ntm.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ContainerRadiationHazardTransformer implements HazardTransformer {
    private static final int MAX_CONTAINER_SCAN_SLOTS = 108;

    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        if (!stack.hasTag()) {
            return;
        }

        float radiation = readLegacySlotRadiation(stack.getTag()) + readContainerHelperRadiation(stack.getTag());
        if (radiation > 0.0F) {
            entries.add(new HazardEntry(HazardType.RADIATION, radiation));
        }
    }

    private static float readLegacySlotRadiation(CompoundTag tag) {
        float radiation = 0.0F;
        for (int i = 0; i < MAX_CONTAINER_SCAN_SLOTS; i++) {
            String key = "slot" + i;
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                ItemStack held = ItemStack.of(tag.getCompound(key));
                radiation += HazardRegistry.getStackRadiation(held);
            }
        }
        return radiation;
    }

    private static float readContainerHelperRadiation(CompoundTag tag) {
        if (!tag.contains("Items", Tag.TAG_LIST)) {
            return 0.0F;
        }
        net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(MAX_CONTAINER_SCAN_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        float radiation = 0.0F;
        for (ItemStack held : items) {
            radiation += HazardRegistry.getStackRadiation(held);
        }
        return radiation;
    }
}

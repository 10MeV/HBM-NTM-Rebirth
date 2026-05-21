package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;

public class ContainerRadiationHazardTransformer implements HazardTransformer {
    private static final int MAX_CONTAINER_SCAN_SLOTS = 108;
    private static final int STORAGE_CRATE_SCAN_SLOTS = 104;
    private static final int TOOLBOX_SCAN_SLOTS = 24;
    private static final int LEAD_BOX_SCAN_SLOTS = 20;
    private static final int PLASTIC_BAG_SCAN_SLOTS = 1;
    private static final Set<String> STORAGE_CRATE_NAMES = Set.of(
            "crate_iron",
            "crate_steel",
            "crate_desh",
            "crate_tungsten",
            "safe"
    );

    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        if (!stack.hasTag()) {
            return;
        }

        ContainerKind kind = ContainerKind.of(stack.getItem());
        float radiation = kind.readRadiation(stack.getTag());
        radiation = kind.transform(radiation);
        if (radiation > 0.0F) {
            entries.add(new HazardEntry(HazardType.RADIATION, radiation));
        }
    }

    private static float readLegacySlotRadiation(CompoundTag tag, int maxSlots) {
        float radiation = 0.0F;
        for (int i = 0; i < maxSlots; i++) {
            String key = "slot" + i;
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                ItemStack held = ItemStack.of(tag.getCompound(key));
                radiation += HazardRegistry.getStackRadiation(held);
            }
        }
        return radiation;
    }

    private static float readLegacyItemInventoryRadiation(CompoundTag tag, int maxSlots) {
        if (!tag.contains("items", Tag.TAG_LIST)) {
            return 0.0F;
        }
        ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        float radiation = 0.0F;
        int entries = Math.min(list.size(), maxSlots);
        for (int i = 0; i < entries; i++) {
            CompoundTag slotTag = list.getCompound(i);
            int slot = slotTag.getByte("slot") & 255;
            if (slot >= maxSlots) {
                continue;
            }
            radiation += HazardRegistry.getStackRadiation(ItemStack.of(slotTag));
        }
        return radiation;
    }

    private static float readContainerHelperRadiation(CompoundTag tag, int maxSlots) {
        if (!tag.contains("Items", Tag.TAG_LIST)) {
            return 0.0F;
        }
        net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(maxSlots, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        float radiation = 0.0F;
        for (ItemStack held : items) {
            radiation += HazardRegistry.getStackRadiation(held);
        }
        return radiation;
    }

    private static float squirt(float value) {
        return (float) (Math.sqrt(value + 1.0D / ((value + 2.0D) * (value + 2.0D))) - 1.0D / (value + 2.0D));
    }

    private static boolean hasLegacyName(Item item, Set<String> names) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null && HbmNtm.MOD_ID.equals(key.getNamespace()) && names.contains(key.getPath());
    }

    private enum ContainerKind {
        DEFAULT(MAX_CONTAINER_SCAN_SLOTS),
        STORAGE_CRATE(STORAGE_CRATE_SCAN_SLOTS),
        TOOLBOX(TOOLBOX_SCAN_SLOTS),
        LEAD_BOX(LEAD_BOX_SCAN_SLOTS),
        PLASTIC_BAG(PLASTIC_BAG_SCAN_SLOTS);

        private final int scanSlots;

        ContainerKind(int scanSlots) {
            this.scanSlots = scanSlots;
        }

        private static ContainerKind of(Item item) {
            if (hasLegacyName(item, STORAGE_CRATE_NAMES)) {
                return STORAGE_CRATE;
            }
            if (item == ModItems.TOOLBOX.get()) {
                return TOOLBOX;
            }
            if (item == ModItems.CONTAINMENT_BOX.get()) {
                return LEAD_BOX;
            }
            if (item == ModItems.PLASTIC_BAG.get()) {
                return PLASTIC_BAG;
            }
            return DEFAULT;
        }

        private int scanSlots() {
            return scanSlots;
        }

        private float readRadiation(CompoundTag tag) {
            return switch (this) {
                case STORAGE_CRATE -> readLegacySlotRadiation(tag, scanSlots);
                case TOOLBOX, LEAD_BOX, PLASTIC_BAG -> readLegacyItemInventoryRadiation(tag, scanSlots) + readContainerHelperRadiation(tag, scanSlots);
                default -> readLegacySlotRadiation(tag, scanSlots) + readLegacyItemInventoryRadiation(tag, scanSlots) + readContainerHelperRadiation(tag, scanSlots);
            };
        }

        private float transform(float radiation) {
            return switch (this) {
                case LEAD_BOX -> squirt(radiation);
                case PLASTIC_BAG -> radiation * 2.0F;
                default -> radiation;
            };
        }
    }
}

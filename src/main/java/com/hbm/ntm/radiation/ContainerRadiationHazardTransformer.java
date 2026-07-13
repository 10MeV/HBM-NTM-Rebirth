package com.hbm.ntm.radiation;

import com.hbm.ntm.block.CrateBlock;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ContainerRadiationHazardTransformer implements HazardTransformer {
    private static final int STORAGE_CRATE_SCAN_SLOTS = 104;
    private static final int TOOLBOX_SCAN_SLOTS = 24;
    private static final int LEAD_BOX_SCAN_SLOTS = 20;
    private static final int PLASTIC_BAG_SCAN_SLOTS = 1;
    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        if (!stack.hasTag()) {
            return;
        }

        ContainerKind kind = ContainerKind.of(stack.getItem());
        if (kind == null) {
            return;
        }
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
        if (!tag.contains(HbmItemStackUtil.LEGACY_ITEMS_TAG, Tag.TAG_LIST) && !tag.contains("Items", Tag.TAG_LIST)) {
            return 0.0F;
        }
        NonNullList<ItemStack> items = HbmItemStackUtil.loadLegacyOrForgeItems(tag, maxSlots);
        float radiation = 0.0F;
        for (ItemStack held : items) {
            radiation += HazardRegistry.getStackRadiation(held);
        }
        return radiation;
    }

    private static float squirt(float value) {
        return (float) (Math.sqrt(value + 1.0D / ((value + 2.0D) * (value + 2.0D))) - 1.0D / (value + 2.0D));
    }

    private enum ContainerKind {
        STORAGE_CRATE(STORAGE_CRATE_SCAN_SLOTS),
        TOOLBOX(TOOLBOX_SCAN_SLOTS),
        LEAD_BOX(LEAD_BOX_SCAN_SLOTS),
        PLASTIC_BAG(PLASTIC_BAG_SCAN_SLOTS);

        private final int scanSlots;

        ContainerKind(int scanSlots) {
            this.scanSlots = scanSlots;
        }

        private static ContainerKind of(Item item) {
            if (Block.byItem(item) instanceof CrateBlock) {
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
            return null;
        }

        private float readRadiation(CompoundTag tag) {
            return switch (this) {
                case STORAGE_CRATE -> readLegacySlotRadiation(tag, scanSlots);
                case TOOLBOX, LEAD_BOX, PLASTIC_BAG -> readLegacyItemInventoryRadiation(tag, scanSlots);
            };
        }

        private float transform(float radiation) {
            return switch (this) {
                case LEAD_BOX -> squirt(radiation);
                case PLASTIC_BAG -> radiation * 2.0F;
                case STORAGE_CRATE, TOOLBOX -> radiation;
            };
        }
    }
}

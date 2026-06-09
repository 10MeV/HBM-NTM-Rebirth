package com.hbm.ntm.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy-name bridge for HBM HE battery items.
 */
@Deprecated(forRemoval = false)
public interface IBatteryItem extends HbmChargeableItem {
    default String getChargeTagName() {
        return HbmBatteryItem.DEFAULT_CHARGE_TAG;
    }

    static String getChargeTagName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return HbmBatteryItem.DEFAULT_CHARGE_TAG;
        }
        if (stack.getItem() instanceof HbmBatteryItem battery) {
            return battery.getChargeTagName(stack);
        }
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getChargeTagName();
        }
        return HbmBatteryItem.DEFAULT_CHARGE_TAG;
    }

    static ItemStack emptyBattery(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof HbmChargeableItem) {
            ItemStack copy = stack.copy();
            CompoundTag tag = new CompoundTag();
            tag.putLong(getChargeTagName(stack), 0L);
            copy.setTag(tag);
            return copy;
        }
        return ItemStack.EMPTY;
    }

    static ItemStack emptyBattery(Item item) {
        return item instanceof HbmChargeableItem ? emptyBattery(new ItemStack(item)) : ItemStack.EMPTY;
    }
}

package com.hbm.ntm.api.energy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Modern API namespace alias for legacy HBM HE battery items.
 */
@Deprecated(forRemoval = false)
public interface IBatteryItem extends com.hbm.ntm.energy.IBatteryItem {
    static String getChargeTagName(ItemStack stack) {
        return com.hbm.ntm.energy.IBatteryItem.getChargeTagName(stack);
    }

    static ItemStack emptyBattery(ItemStack stack) {
        return com.hbm.ntm.energy.IBatteryItem.emptyBattery(stack);
    }

    static ItemStack emptyBattery(Item item) {
        return com.hbm.ntm.energy.IBatteryItem.emptyBattery(item);
    }
}

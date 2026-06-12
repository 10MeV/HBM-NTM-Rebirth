package api.hbm.energymk2;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for HBM HE battery items.
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

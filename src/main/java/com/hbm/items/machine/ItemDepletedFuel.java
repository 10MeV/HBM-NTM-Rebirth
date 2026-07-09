package com.hbm.items.machine;

import com.hbm.ntm.item.DepletedFuelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for depleted reactor waste.
 *
 * <p>The old item used damage {@code 0} for cooled waste and damage {@code 1}
 * for hot waste. The modern item keeps that damage-backed subtype contract and
 * this facade restores the old FQCN for source migrations.
 */
@Deprecated(forRemoval = false)
public class ItemDepletedFuel extends DepletedFuelItem {
    public ItemDepletedFuel() {
        this(new Item.Properties());
    }

    public ItemDepletedFuel(Item.Properties properties) {
        super(properties);
    }

    public int getColorFromItemStack(ItemStack stack, int tintIndex) {
        return isHot(stack) ? HOT_TINT : 0xFFFFFF;
    }
}

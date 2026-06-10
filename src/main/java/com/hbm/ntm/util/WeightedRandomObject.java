package com.hbm.ntm.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy-name weighted object wrapper.
 */
@Deprecated(forRemoval = false)
public class WeightedRandomObject extends HbmWeightedRandomGeneric<Object> {
    public WeightedRandomObject(Object item, int weight) {
        super(item, weight);
    }

    public ItemStack asStack() {
        return item instanceof ItemStack stack ? stack.copy() : null;
    }

    public Item asItem() {
        return item instanceof Item value ? value : null;
    }
}

package com.hbm.ntm.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HbmWeightedRandomObject extends HbmWeightedRandomGeneric<Object> {
    public HbmWeightedRandomObject(Object item, int weight) {
        super(item, weight);
    }

    public ItemStack asStack() {
        return item instanceof ItemStack stack ? stack.copy() : ItemStack.EMPTY;
    }

    public Item asItem() {
        return item instanceof Item value ? value : null;
    }
}

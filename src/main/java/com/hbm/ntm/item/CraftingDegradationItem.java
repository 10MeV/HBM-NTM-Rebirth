package com.hbm.ntm.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CraftingDegradationItem extends Item {
    public CraftingDegradationItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainder = stack.copy();
        if (stack.getMaxDamage() > 0) {
            remainder.setDamageValue(stack.getDamageValue() + 1);
        }
        return remainder;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }
}

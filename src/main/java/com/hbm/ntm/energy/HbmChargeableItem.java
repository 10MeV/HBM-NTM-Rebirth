package com.hbm.ntm.energy;

import net.minecraft.world.item.ItemStack;

public interface HbmChargeableItem {
    long getMaxCharge(ItemStack stack);

    long getChargeRate(ItemStack stack);

    long getDischargeRate(ItemStack stack);

    long getCharge(ItemStack stack);

    void setCharge(ItemStack stack, long charge);

    long chargeBattery(ItemStack stack, long amount);

    long dischargeBattery(ItemStack stack, long amount);
}

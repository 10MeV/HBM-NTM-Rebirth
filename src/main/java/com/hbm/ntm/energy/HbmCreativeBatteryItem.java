package com.hbm.ntm.energy;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class HbmCreativeBatteryItem extends HbmBatteryItem {
    private static final long CREATIVE_CHARGE = Long.MAX_VALUE / 2L;
    private static final long CREATIVE_CAPACITY = Long.MAX_VALUE;
    private static final long CREATIVE_TRANSFER = Long.MAX_VALUE / 100L;

    public HbmCreativeBatteryItem(Properties properties) {
        super(properties, CREATIVE_CAPACITY, CREATIVE_TRANSFER, CREATIVE_TRANSFER);
    }

    @Override
    public long getCharge(ItemStack stack) {
        return CREATIVE_CHARGE;
    }

    @Override
    public void setCharge(ItemStack stack, long charge) {
    }

    @Override
    public long chargeBattery(ItemStack stack, long amount) {
        return 0L;
    }

    @Override
    public long dischargeBattery(ItemStack stack, long amount) {
        return Math.max(0L, Math.min(amount, getDischargeRate(stack)));
    }

    @Override
    public void addCreativeStacks(CreativeModeTab.Output output, ItemStack stack) {
        output.accept(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }
}

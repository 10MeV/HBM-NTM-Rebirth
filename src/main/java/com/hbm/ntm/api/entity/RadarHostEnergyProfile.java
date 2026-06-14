package com.hbm.ntm.api.entity;

import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import net.minecraft.world.item.ItemStack;

public final class RadarHostEnergyProfile {
    private RadarHostEnergyProfile() {
    }

    public static void applyConfiguredLimits(HbmEnergyStorage energy, long powerCap) {
        if (energy == null) {
            return;
        }
        energy.setMaxPower(powerCap);
        energy.setTransferRates(powerCap, 0L);
    }

    public static void chargeFromBatterySlot(ItemStack battery, HbmEnergyStorage energy) {
        if (energy == null) {
            return;
        }
        HbmEnergyUtil.chargeStorageFromItem(battery, energy, energy.getReceiverSpeed());
    }
}

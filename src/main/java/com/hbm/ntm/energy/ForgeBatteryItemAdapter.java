package com.hbm.ntm.energy;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeBatteryItemAdapter implements IEnergyStorage {
    private final ItemStack stack;
    private final HbmChargeableItem battery;

    public ForgeBatteryItemAdapter(ItemStack stack, HbmChargeableItem battery) {
        this.stack = stack;
        this.battery = battery;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }
        long accepted = Math.min((long) maxReceive, battery.getChargeRate(stack));
        accepted = Math.min(accepted, Math.max(0L, battery.getMaxCharge(stack) - battery.peekCharge(stack)));
        if (!simulate && accepted > 0L) {
            accepted = battery.chargeBattery(stack, accepted);
        }
        return HbmEnergyUtil.toForgeInt(accepted);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0) {
            return 0;
        }
        long extracted = Math.min((long) maxExtract, battery.getDischargeRate(stack));
        extracted = Math.min(extracted, battery.peekCharge(stack));
        if (!simulate && extracted > 0L) {
            extracted = battery.dischargeBattery(stack, extracted);
        }
        return HbmEnergyUtil.toForgeInt(extracted);
    }

    @Override
    public int getEnergyStored() {
        return HbmEnergyUtil.toForgeInt(battery.peekCharge(stack));
    }

    @Override
    public int getMaxEnergyStored() {
        return HbmEnergyUtil.toForgeInt(battery.getMaxCharge(stack));
    }

    @Override
    public boolean canExtract() {
        return battery.getDischargeRate(stack) > 0L;
    }

    @Override
    public boolean canReceive() {
        return battery.getChargeRate(stack) > 0L;
    }
}

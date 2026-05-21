package com.hbm.ntm.energy;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Modern equivalent of the 1.7.10 Library battery transfer helpers.
 */
public final class HbmBatteryTransfer {
    private static Predicate<ItemStack> creativeBatteryPredicate = stack -> false;

    private HbmBatteryTransfer() {
    }

    public static void setCreativeBatteryPredicate(Predicate<ItemStack> predicate) {
        creativeBatteryPredicate = predicate == null ? stack -> false : predicate;
    }

    public static boolean isHbmBattery(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HbmBatteryItem;
    }

    public static boolean isCreativeBattery(ItemStack stack) {
        return !stack.isEmpty() && creativeBatteryPredicate.test(stack);
    }

    public static long chargeItemFromStorage(ItemStack stack, HbmEnergyProvider provider, long maxPower) {
        if (provider == null) {
            return 0L;
        }
        long remaining = chargeItemsFromPower(stack, provider.getPower(), maxPower);
        long used = provider.getPower() - remaining;
        if (used > 0L) {
            provider.usePower(used);
        }
        return used;
    }

    public static long chargeStorageFromItem(ItemStack stack, HbmEnergyReceiver receiver, long maxPower) {
        if (receiver == null) {
            return 0L;
        }
        long before = receiver.getPower();
        long after = chargePowerFromItem(stack, before, maxPower);
        long accepted = after - before;
        if (accepted > 0L) {
            receiver.transferPower(accepted);
        }
        return accepted;
    }

    public static long chargeItemsFromPower(ItemStack stack, long power, long maxPower) {
        if (power < 0L) {
            return 0L;
        }
        if (power > maxPower) {
            return maxPower;
        }
        if (isCreativeBattery(stack)) {
            return 0L;
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof HbmBatteryItem battery)) {
            return power;
        }

        long batMax = battery.getMaxCharge(stack);
        long batCharge = battery.getCharge(stack);
        long batRate = battery.getChargeRate(stack);
        long toCharge = Math.min(Math.min(power, batRate), batMax - batCharge);

        if (toCharge > 0L) {
            battery.setCharge(stack, batCharge + toCharge);
            power -= toCharge;
        }
        return power;
    }

    public static long chargePowerFromItem(ItemStack stack, long power, long maxPower) {
        if (isCreativeBattery(stack)) {
            return maxPower;
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof HbmBatteryItem battery)) {
            return power;
        }

        long batCharge = battery.getCharge(stack);
        long batRate = battery.getDischargeRate(stack);
        long toDischarge = Math.min(Math.min(maxPower - power, batRate), batCharge);

        if (toDischarge > 0L) {
            battery.setCharge(stack, batCharge - toDischarge);
            power += toDischarge;
        }
        return power;
    }
}

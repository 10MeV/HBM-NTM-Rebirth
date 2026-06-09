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
        return !stack.isEmpty() && stack.getItem() instanceof HbmChargeableItem;
    }

    public static boolean isCreativeBattery(ItemStack stack) {
        return !stack.isEmpty() && creativeBatteryPredicate.test(stack);
    }

    public static boolean isEmptyBattery(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof HbmChargeableItem battery
                && battery.getCharge(stack) <= 0L;
    }

    public static boolean isFullBattery(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof HbmChargeableItem battery
                && battery.getCharge(stack) >= battery.getMaxCharge(stack);
    }

    public static long chargeItemFromStorage(ItemStack stack, HbmEnergyProvider provider, long maxPower) {
        if (provider == null) {
            return 0L;
        }
        long before = provider.getPower();
        long after = chargeItemsFromPower(stack, before, maxPower);
        if (after != before) {
            provider.setPower(after);
        }
        return before - after;
    }

    public static long chargeStorageFromItem(ItemStack stack, HbmEnergyReceiver receiver, long maxPower) {
        if (receiver == null) {
            return 0L;
        }
        long before = receiver.getPower();
        long after = chargePowerFromItem(stack, before, maxPower);
        if (after != before) {
            receiver.setPower(after);
        }
        return after - before;
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
        if (stack.isEmpty() || !(stack.getItem() instanceof HbmChargeableItem battery)) {
            return power;
        }

        long batMax = battery.getMaxCharge(stack);
        long batCharge = battery.getCharge(stack);
        long batRate = battery.getChargeRate(stack);
        long toCharge = Math.min(Math.min(power, batRate), batMax - batCharge);

        power -= toCharge;
        chargeBatteryLegacy(battery, stack, toCharge);
        return power;
    }

    public static long chargePowerFromItem(ItemStack stack, long power, long maxPower) {
        if (isCreativeBattery(stack)) {
            return maxPower;
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof HbmChargeableItem battery)) {
            return power;
        }

        long batCharge = battery.getCharge(stack);
        long batRate = battery.getDischargeRate(stack);
        long toDischarge = Math.min(Math.min(maxPower - power, batRate), batCharge);

        dischargeBatteryLegacy(battery, stack, toDischarge);
        power += toDischarge;
        return power;
    }

    private static void chargeBatteryLegacy(HbmChargeableItem battery, ItemStack stack, long amount) {
        if (amount >= 0L) {
            battery.chargeBattery(stack, amount);
        } else {
            battery.dischargeBattery(stack, -amount);
        }
    }

    private static void dischargeBatteryLegacy(HbmChargeableItem battery, ItemStack stack, long amount) {
        if (amount >= 0L) {
            battery.dischargeBattery(stack, amount);
        } else {
            battery.chargeBattery(stack, -amount);
        }
    }
}

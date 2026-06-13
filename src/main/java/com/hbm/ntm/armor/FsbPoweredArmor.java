package com.hbm.ntm.armor;

import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.IBatteryItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface FsbPoweredArmor extends IBatteryItem {
    long getBaseMaxCharge(ItemStack stack);

    long getChargeRate(ItemStack stack);

    long getConsumption(ItemStack stack);

    default long getDrain(ItemStack stack) {
        return 0L;
    }

    ResourceLocation fsbMaterialId(ItemStack stack);

    default boolean noHelmetForFsbSet(ItemStack chestplate) {
        return false;
    }

    default boolean isArmorEnabled(ItemStack stack) {
        return getCharge(stack) > 0L;
    }

    @Override
    default long getMaxCharge(ItemStack stack) {
        double multiplier = 1.0D;
        if (ArmorModHandler.hasMods(stack)) {
            ItemStack mod = ArmorModHandler.pryMod(stack, ArmorModHandler.battery);
            if (mod.getItem() instanceof ArmorModItems.ArmorBattery battery) {
                multiplier = battery.multiplier();
            }
        }
        return Math.max(0L, Math.round(getBaseMaxCharge(stack) * multiplier));
    }

    @Override
    default long getDischargeRate(ItemStack stack) {
        return 0L;
    }

    @Override
    default long getCharge(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0L;
        }
        if (stack.hasTag()) {
            return Math.min(stack.getTag().getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG), getMaxCharge(stack));
        }
        long charge = getMaxCharge(stack);
        stack.getOrCreateTag().putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, charge);
        return charge;
    }

    @Override
    default long peekCharge(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0L;
        }
        return stack.hasTag() ? Math.min(stack.getTag().getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG), getMaxCharge(stack))
                : getMaxCharge(stack);
    }

    @Override
    default void setCharge(ItemStack stack, long charge) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, charge);
        }
    }

    @Override
    default long chargeBattery(ItemStack stack, long amount) {
        long before = getCharge(stack);
        setCharge(stack, before + amount);
        return getCharge(stack) - before;
    }

    @Override
    default long dischargeBattery(ItemStack stack, long amount) {
        long before = getCharge(stack);
        setCharge(stack, before - amount);
        if (getCharge(stack) < 0L) {
            setCharge(stack, 0L);
        }
        return before - getCharge(stack);
    }

    default void applyLegacyDamage(ItemStack stack, int damage) {
        dischargeBattery(stack, (long) damage * getConsumption(stack));
    }

    static boolean hasFullPoweredSet(Player player) {
        return hasFullPoweredSet(player, false);
    }

    static boolean hasFullPoweredSetIgnoreCharge(Player player) {
        return hasFullPoweredSet(player, true);
    }

    private static boolean hasFullPoweredSet(Player player, boolean ignoreCharge) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof FsbPoweredArmor chestplate)) {
            return false;
        }

        ResourceLocation material = chestplate.fsbMaterialId(chest);
        if (material == null) {
            return false;
        }

        EquipmentSlot[] slots = chestplate.noHelmetForFsbSet(chest)
                ? new EquipmentSlot[] {EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}
                : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (EquipmentSlot slot : slots) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!(armor.getItem() instanceof FsbPoweredArmor powered)) {
                return false;
            }
            if (!material.equals(powered.fsbMaterialId(armor))) {
                return false;
            }
            if (!ignoreCharge && !powered.isArmorEnabled(armor)) {
                return false;
            }
        }
        return true;
    }
}

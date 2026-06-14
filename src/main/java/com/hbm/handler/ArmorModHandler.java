package com.hbm.handler;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Legacy package facade for the 1.7.10 armor module NBT and slot helper.
 */
@Deprecated(forRemoval = false)
public final class ArmorModHandler {
    public static final int helmet_only = com.hbm.ntm.armor.ArmorModHandler.helmet_only;
    public static final int plate_only = com.hbm.ntm.armor.ArmorModHandler.plate_only;
    public static final int legs_only = com.hbm.ntm.armor.ArmorModHandler.legs_only;
    public static final int boots_only = com.hbm.ntm.armor.ArmorModHandler.boots_only;
    public static final int servos = com.hbm.ntm.armor.ArmorModHandler.servos;
    public static final int cladding = com.hbm.ntm.armor.ArmorModHandler.cladding;
    public static final int kevlar = com.hbm.ntm.armor.ArmorModHandler.kevlar;
    public static final int extra = com.hbm.ntm.armor.ArmorModHandler.extra;
    public static final int battery = com.hbm.ntm.armor.ArmorModHandler.battery;

    public static final int MOD_SLOTS = com.hbm.ntm.armor.ArmorModHandler.MOD_SLOTS;

    public static final UUID[] UUIDs = com.hbm.ntm.armor.ArmorModHandler.UUIDs;
    public static final UUID[] fixedUUIDs = com.hbm.ntm.armor.ArmorModHandler.fixedUUIDs;

    public static final String MOD_COMPOUND_KEY = com.hbm.ntm.armor.ArmorModHandler.MOD_COMPOUND_KEY;
    public static final String MOD_SLOT_KEY = com.hbm.ntm.armor.ArmorModHandler.MOD_SLOT_KEY;

    public static boolean isApplicable(ItemStack armor, ItemStack mod) {
        return com.hbm.ntm.armor.ArmorModHandler.isApplicable(armor, mod);
    }

    public static void applyMod(ItemStack armor, ItemStack mod) {
        com.hbm.ntm.armor.ArmorModHandler.applyMod(armor, mod);
    }

    public static void removeMod(ItemStack armor, int slot) {
        com.hbm.ntm.armor.ArmorModHandler.removeMod(armor, slot);
    }

    public static void clearMods(ItemStack armor) {
        com.hbm.ntm.armor.ArmorModHandler.clearMods(armor);
    }

    public static boolean hasMods(ItemStack armor) {
        return com.hbm.ntm.armor.ArmorModHandler.hasMods(armor);
    }

    public static ItemStack[] pryMods(ItemStack armor) {
        return com.hbm.ntm.armor.ArmorModHandler.pryMods(armor);
    }

    public static ItemStack pryMod(ItemStack armor, int slot) {
        return com.hbm.ntm.armor.ArmorModHandler.pryMod(armor, slot);
    }

    public static com.hbm.ntm.armor.ArmorModHandler.ArmorModSlot slotByLegacyIndex(int index) {
        return com.hbm.ntm.armor.ArmorModHandler.slotByLegacyIndex(index);
    }

    public static UUID modifierUuidFor(ArmorItem.Type type) {
        return com.hbm.ntm.armor.ArmorModHandler.modifierUuidFor(type);
    }

    public static int legacyArmorTypeIndex(ArmorItem.Type type) {
        return com.hbm.ntm.armor.ArmorModHandler.legacyArmorTypeIndex(type);
    }

    private ArmorModHandler() {
    }
}

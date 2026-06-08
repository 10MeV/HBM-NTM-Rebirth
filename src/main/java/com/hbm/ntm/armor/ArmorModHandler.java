package com.hbm.ntm.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public final class ArmorModHandler {
    public static final int helmet_only = 0;
    public static final int plate_only = 1;
    public static final int legs_only = 2;
    public static final int boots_only = 3;
    public static final int servos = 4;
    public static final int cladding = 5;
    public static final int kevlar = 6;
    public static final int extra = 7;
    public static final int battery = 8;

    public static final int MOD_SLOTS = 9;

    public static final UUID[] UUIDs = new UUID[] {
            UUID.fromString("8d6e5c77-133e-4056-9c80-a9e42a1a0b65"),
            UUID.fromString("b1b7ee0e-1d14-4400-8037-f7f2e02f21ca"),
            UUID.fromString("30b50d2a-4858-4e5b-88d4-3e3612224238"),
            UUID.fromString("426ee0d0-7587-4697-aaef-4772ab202e78")
    };

    public static final UUID[] fixedUUIDs = new UUID[] {
            UUID.fromString("e572caf4-3e65-4152-bc79-c4d4048cbd29"),
            UUID.fromString("bed30902-8a6a-4769-9f65-2a9b67469fff"),
            UUID.fromString("baebf7b3-1eda-4a14-b233-068e2493e9a2"),
            UUID.fromString("28016c1b-d992-4324-9409-a9f9f0ffb85c")
    };

    public static final String MOD_COMPOUND_KEY = "ntm_armor_mods";
    public static final String MOD_SLOT_KEY = "mod_slot_";

    public static boolean isApplicable(ItemStack armor, ItemStack mod) {
        if (armor == null || armor.isEmpty() || mod == null || mod.isEmpty()) {
            return false;
        }
        if (!(armor.getItem() instanceof ArmorItem armorItem) || !(mod.getItem() instanceof ArmorModItem armorMod)) {
            return false;
        }
        return armorMod.supports(armorItem.getType());
    }

    public static void applyMod(ItemStack armor, ItemStack mod) {
        if (armor == null || armor.isEmpty() || mod == null || mod.isEmpty()) {
            return;
        }
        if (!(mod.getItem() instanceof ArmorModItem armorMod)) {
            return;
        }
        CompoundTag mods = armor.getOrCreateTagElement(MOD_COMPOUND_KEY);
        mods.put(MOD_SLOT_KEY + armorMod.slot().legacyIndex(), mod.save(new CompoundTag()));
    }

    public static void removeMod(ItemStack armor, int slot) {
        if (armor == null || armor.isEmpty() || slot < 0 || slot >= MOD_SLOTS) {
            return;
        }
        CompoundTag mods = armor.getTagElement(MOD_COMPOUND_KEY);
        if (mods == null) {
            return;
        }
        mods.remove(MOD_SLOT_KEY + slot);
        removeEmptyModCompound(armor, mods);
    }

    public static void clearMods(ItemStack armor) {
        if (armor == null || armor.isEmpty() || !armor.hasTag()) {
            return;
        }
        armor.getOrCreateTag().remove(MOD_COMPOUND_KEY);
    }

    public static boolean hasMods(ItemStack armor) {
        if (armor == null || armor.isEmpty()) {
            return false;
        }
        CompoundTag mods = armor.getTagElement(MOD_COMPOUND_KEY);
        return mods != null && !mods.isEmpty();
    }

    public static ItemStack[] pryMods(ItemStack armor) {
        ItemStack[] slots = emptySlots();
        if (armor == null || armor.isEmpty()) {
            return slots;
        }
        CompoundTag mods = armor.getTagElement(MOD_COMPOUND_KEY);
        if (mods == null) {
            return slots;
        }

        for (int i = 0; i < MOD_SLOTS; i++) {
            String key = MOD_SLOT_KEY + i;
            if (!mods.contains(key, Tag.TAG_COMPOUND)) {
                continue;
            }
            ItemStack stack = ItemStack.of(mods.getCompound(key));
            if (!stack.isEmpty()) {
                slots[i] = stack;
            } else {
                mods.remove(key);
            }
        }
        removeEmptyModCompound(armor, mods);
        return slots;
    }

    public static ItemStack pryMod(ItemStack armor, int slot) {
        if (armor == null || armor.isEmpty() || slot < 0 || slot >= MOD_SLOTS) {
            return ItemStack.EMPTY;
        }
        CompoundTag mods = armor.getTagElement(MOD_COMPOUND_KEY);
        if (mods == null) {
            return ItemStack.EMPTY;
        }
        String key = MOD_SLOT_KEY + slot;
        if (!mods.contains(key, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = ItemStack.of(mods.getCompound(key));
        if (!stack.isEmpty()) {
            return stack;
        }
        mods.remove(key);
        removeEmptyModCompound(armor, mods);
        return ItemStack.EMPTY;
    }

    public static ArmorModSlot slotByLegacyIndex(int index) {
        for (ArmorModSlot slot : ArmorModSlot.values()) {
            if (slot.legacyIndex == index) {
                return slot;
            }
        }
        throw new IllegalArgumentException("Unknown armor mod slot index: " + index);
    }

    public static UUID modifierUuidFor(ArmorItem.Type type) {
        return UUIDs[legacyArmorTypeIndex(type)];
    }

    public static int legacyArmorTypeIndex(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 0;
            case CHESTPLATE -> 1;
            case LEGGINGS -> 2;
            case BOOTS -> 3;
        };
    }

    private static ItemStack[] emptySlots() {
        ItemStack[] slots = new ItemStack[MOD_SLOTS];
        Arrays.fill(slots, ItemStack.EMPTY);
        return slots;
    }

    private static void removeEmptyModCompound(ItemStack armor, CompoundTag mods) {
        if (mods.isEmpty() && armor.hasTag()) {
            armor.getOrCreateTag().remove(MOD_COMPOUND_KEY);
        }
    }

    public enum ArmorModSlot {
        HELMET_ONLY(helmet_only),
        PLATE_ONLY(plate_only),
        LEGS_ONLY(legs_only),
        BOOTS_ONLY(boots_only),
        SERVOS(servos),
        CLADDING(cladding),
        KEVLAR(kevlar),
        EXTRA(extra),
        BATTERY(battery);

        private final int legacyIndex;

        ArmorModSlot(int legacyIndex) {
            this.legacyIndex = legacyIndex;
        }

        public int legacyIndex() {
            return legacyIndex;
        }
    }

    private ArmorModHandler() {
    }
}

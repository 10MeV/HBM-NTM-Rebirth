package com.hbm.ntm.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class HbmLegacyLootUtil {
    public static final String LOOT_BOOKLET = "LOOT_BOOKLET";
    public static final String LOOT_CAPNUKE = "LOOT_CAPNUKE";
    public static final String LOOT_MEDICINE = "LOOT_MEDICINE";
    public static final String LOOT_CAPSTASH = "LOOT_CAPSTASH";
    public static final String LOOT_MAKESHIFT_GUN = "LOOT_MAKESHIFT_GUN";
    public static final String LOOT_NUKE_STORAGE = "LOOT_NUKE_STORAGE";
    public static final String LOOT_BONES = "LOOT_BONES";
    public static final String LOOT_GLYPHID_HIVE = "LOOT_GLYPHID_HIVE";
    public static final String LOOT_METEOR = "LOOT_METEOR";
    public static final String LOOT_FLAREGUN = "LOOT_FLAREGUN";
    public static final String LOOT_SHIT = "LOOT_SHIT";
    public static final String LOOT_MECHANICAL = "LOOT_MECHANICAL";
    public static final String LOOT_GEAR = "LOOT_GEAR";

    public static final List<String> LOOT_NAMES = List.of(
            LOOT_BOOKLET,
            LOOT_CAPNUKE,
            LOOT_MEDICINE,
            LOOT_CAPSTASH,
            LOOT_MAKESHIFT_GUN,
            LOOT_NUKE_STORAGE,
            LOOT_BONES,
            LOOT_GLYPHID_HIVE,
            LOOT_METEOR,
            LOOT_FLAREGUN,
            LOOT_MECHANICAL,
            LOOT_GEAR,
            LOOT_SHIT);

    private HbmLegacyLootUtil() {
    }

    public static String[] getLootNames() {
        return LOOT_NAMES.toArray(String[]::new);
    }

    public static PlacedLootStack withDeviation(ItemStack stack, double x, double y, double z, RandomSource random) {
        RandomSource roll = random == null ? RandomSource.create() : random;
        return new PlacedLootStack(stack, x + roll.nextGaussian() * 0.02D, y, z + roll.nextGaussian() * 0.02D);
    }

    public record PlacedLootStack(ItemStack stack, double x, double y, double z) {
        public PlacedLootStack {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }
}

package com.hbm.ntm.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Names and small data carriers retained for legacy source/command diagnostics.
 * The associated 1.7.10 reward-loot mechanics are globally excluded.
 */
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
            LOOT_BOOKLET, LOOT_CAPNUKE, LOOT_MEDICINE, LOOT_CAPSTASH, LOOT_MAKESHIFT_GUN,
            LOOT_NUKE_STORAGE, LOOT_BONES, LOOT_GLYPHID_HIVE, LOOT_METEOR, LOOT_FLAREGUN,
            LOOT_MECHANICAL, LOOT_GEAR, LOOT_SHIT);

    private static final Map<String, String> ITEM_POOL_LOOT_NAMES = Map.of();

    private HbmLegacyLootUtil() {
    }

    public static String[] getLootNames() {
        return LOOT_NAMES.toArray(String[]::new);
    }

    public static Optional<String> itemPoolIdForLootName(String lootName) {
        return Optional.ofNullable(ITEM_POOL_LOOT_NAMES.get(lootName));
    }

    public static Map<String, String> mappedItemPoolLootNames() {
        return ITEM_POOL_LOOT_NAMES;
    }

    public static List<String> deferredLootNames() {
        return LOOT_NAMES;
    }

    /** The legacy names remain queryable, but no excluded reward path can roll an item. */
    public static List<PlacedLootStack> rollMappedItemPoolLoot(ServerLevel level, String lootName, Vec3 origin,
            RandomSource random) {
        return List.of();
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

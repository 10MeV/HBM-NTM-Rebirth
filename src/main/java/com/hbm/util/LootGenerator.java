package com.hbm.util;

import com.hbm.ntm.util.HbmLegacyLootUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Legacy-name loot generator facade.
 */
@Deprecated(forRemoval = false)
public final class LootGenerator {
    public static final String LOOT_BOOKLET = HbmLegacyLootUtil.LOOT_BOOKLET;
    public static final String LOOT_CAPNUKE = HbmLegacyLootUtil.LOOT_CAPNUKE;
    public static final String LOOT_MEDICINE = HbmLegacyLootUtil.LOOT_MEDICINE;
    public static final String LOOT_CAPSTASH = HbmLegacyLootUtil.LOOT_CAPSTASH;
    public static final String LOOT_MAKESHIFT_GUN = HbmLegacyLootUtil.LOOT_MAKESHIFT_GUN;
    public static final String LOOT_NUKE_STORAGE = HbmLegacyLootUtil.LOOT_NUKE_STORAGE;
    public static final String LOOT_BONES = HbmLegacyLootUtil.LOOT_BONES;
    public static final String LOOT_GLYPHID_HIVE = HbmLegacyLootUtil.LOOT_GLYPHID_HIVE;
    public static final String LOOT_METEOR = HbmLegacyLootUtil.LOOT_METEOR;
    public static final String LOOT_FLAREGUN = HbmLegacyLootUtil.LOOT_FLAREGUN;
    public static final String LOOT_SHIT = HbmLegacyLootUtil.LOOT_SHIT;
    public static final String LOOT_MECHANICAL = HbmLegacyLootUtil.LOOT_MECHANICAL;
    public static final String LOOT_GEAR = HbmLegacyLootUtil.LOOT_GEAR;

    private LootGenerator() {
    }

    public static String[] getLootNames() {
        return HbmLegacyLootUtil.getLootNames();
    }

    public static Optional<String> itemPoolIdForLootName(String lootName) {
        return HbmLegacyLootUtil.itemPoolIdForLootName(lootName);
    }

    public static Map<String, String> mappedItemPoolLootNames() {
        return HbmLegacyLootUtil.mappedItemPoolLootNames();
    }

    public static List<String> deferredLootNames() {
        return HbmLegacyLootUtil.deferredLootNames();
    }

    public static List<HbmLegacyLootUtil.PlacedLootStack> rollMappedItemPoolLoot(ServerLevel level, String lootName,
            Vec3 origin, RandomSource random) {
        return HbmLegacyLootUtil.rollMappedItemPoolLoot(level, lootName, origin, random);
    }

    public static HbmLegacyLootUtil.PlacedLootStack addItemWithDeviation(RandomSource random,
            net.minecraft.world.item.ItemStack stack, double x, double y, double z) {
        return HbmLegacyLootUtil.withDeviation(stack, x, y, z, random);
    }
}

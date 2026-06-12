package com.hbm.ntm.util;

import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.itempool.HbmItemPoolRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

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

    private static final Map<String, String> ITEM_POOL_LOOT_NAMES = Map.ofEntries(
            entry(LOOT_BONES, HbmItemPoolIds.POOL_PILE_BONES),
            entry(LOOT_GLYPHID_HIVE, HbmItemPoolIds.POOL_PILE_HIVE),
            entry(LOOT_CAPSTASH, HbmItemPoolIds.POOL_PILE_CAPS),
            entry(LOOT_SHIT, HbmItemPoolIds.POOL_PILE_OF_GARBAGE),
            entry(LOOT_MECHANICAL, HbmItemPoolIds.POOL_PILE_MECHANICAL),
            entry(LOOT_GEAR, HbmItemPoolIds.POOL_PILE_MECHANICAL)
    );

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
        return LOOT_NAMES.stream()
                .filter(lootName -> !ITEM_POOL_LOOT_NAMES.containsKey(lootName))
                .toList();
    }

    public static List<PlacedLootStack> rollMappedItemPoolLoot(ServerLevel level, String lootName, Vec3 origin,
                                                               RandomSource random) {
        Optional<String> poolId = itemPoolIdForLootName(lootName);
        if (poolId.isEmpty()) {
            return List.of();
        }

        RandomSource roll = random == null ? RandomSource.create() : random;
        Vec3 lootOrigin = origin == null ? Vec3.ZERO : origin;
        if (LOOT_CAPSTASH.equals(lootName)) {
            return rollCapStash(level, poolId.get(), lootOrigin, roll);
        }

        int limit = mappedRollCount(lootName, roll);
        List<PlacedLootStack> stacks = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            ItemStack stack = HbmItemPoolRegistry.getStack(level, poolId.get(), lootOrigin);
            if (!stack.isEmpty()) {
                stacks.add(withDeviation(stack, roll.nextDouble() - 0.5D, i * 0.03125D, roll.nextDouble() - 0.5D, roll));
            }
        }
        return List.copyOf(stacks);
    }

    private static List<PlacedLootStack> rollCapStash(ServerLevel level, String poolId, Vec3 origin, RandomSource random) {
        List<PlacedLootStack> stacks = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                int count = random.nextInt(5) + 3;
                for (int y = 0; y < count; y++) {
                    ItemStack stack = HbmItemPoolRegistry.getStack(level, poolId, origin);
                    if (!stack.isEmpty()) {
                        stacks.add(withDeviation(stack, x * 0.3125D, y * 0.03125D, z * 0.3125D, random));
                    }
                }
            }
        }
        return List.copyOf(stacks);
    }

    private static int mappedRollCount(String lootName, RandomSource random) {
        if (LOOT_MECHANICAL.equals(lootName) || LOOT_GEAR.equals(lootName)) {
            return random.nextInt(6) + 1;
        }
        return random.nextInt(3) + 3;
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

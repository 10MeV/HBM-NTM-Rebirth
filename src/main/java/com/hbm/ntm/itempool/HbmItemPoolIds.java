package com.hbm.ntm.itempool;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

public final class HbmItemPoolIds {
    public static final String BACKUP_POOL = "BACKUP_POOL";
    public static final String POOL_SUPPLIES = "POOL_SUPPLIES";
    public static final String POOL_WEAPONS = "POOL_WEAPONS";
    public static final String POOL_AMMO = "POOL_AMMO";
    public static final String POOL_SODA = "POOL_SODA";
    public static final String POOL_SNACKS = "POOL_SNACKS";
    public static final String POOL_SAT_MINER = "POOL_SAT_MINER";
    public static final String POOL_SAT_LUNAR = "POOL_SAT_LUNAR";
    /** Ground-refresh meteorite treasure; not part of the excluded falling-impact event. */
    public static final String POOL_METEORITE_TREASURE = "POOL_METEORITE_TREASURE";

    private static final Map<String, ResourceLocation> EXPLICIT_TABLES = Map.ofEntries(
            entry(BACKUP_POOL, table("backup")),
            entry(POOL_SUPPLIES, table("c130/supplies")),
            entry(POOL_WEAPONS, table("c130/weapons")),
            entry(POOL_AMMO, table("c130/ammo")),
            entry(POOL_SODA, table("vending/soda")),
            entry(POOL_SNACKS, table("vending/snacks")),
            entry(POOL_SAT_MINER, table("satellite/miner")),
            entry(POOL_SAT_LUNAR, table("satellite/lunar")),
            entry(POOL_METEORITE_TREASURE, table("meteorite/treasure"))
    );

    public static ResourceLocation tableFor(String legacyPoolId) {
        ResourceLocation explicit = EXPLICIT_TABLES.get(legacyPoolId);
        return explicit != null ? explicit : table(normalize(legacyPoolId));
    }

    public static ResourceLocation backupTable() {
        return EXPLICIT_TABLES.get(BACKUP_POOL);
    }

    public static Map<String, ResourceLocation> explicitTables() {
        return EXPLICIT_TABLES;
    }

    public static java.util.Set<String> knownPoolIds() {
        return EXPLICIT_TABLES.keySet();
    }

    private static ResourceLocation table(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "item_pool/" + path);
    }

    private static String normalize(String legacyPoolId) {
        String normalized = legacyPoolId == null || legacyPoolId.isBlank() ? "missing" : legacyPoolId;
        if (normalized.startsWith("POOL_")) {
            normalized = normalized.substring("POOL_".length());
        }
        return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
    }

    private HbmItemPoolIds() {
    }
}

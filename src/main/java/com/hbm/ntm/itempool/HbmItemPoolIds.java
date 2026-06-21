package com.hbm.ntm.itempool;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

public final class HbmItemPoolIds {
    public static final String BACKUP_POOL = "BACKUP_POOL";
    public static final String POOL_VAULT_RUSTY = "POOL_VAULT_RUSTY";
    public static final String POOL_VAULT_STANDARD = "POOL_VAULT_STANDARD";
    public static final String POOL_VAULT_REINFORCED = "POOL_VAULT_REINFORCED";
    public static final String POOL_VAULT_UNBREAKABLE = "POOL_VAULT_UNBREAKABLE";
    public static final String POOL_BLUEPRINTS = "POOL_BLUEPRINTS";
    public static final String POOL_GENERIC = "POOL_GENERIC";
    public static final String POOL_ANTENNA = "POOL_ANTENNA";
    public static final String POOL_EXPENSIVE = "POOL_EXPENSIVE";
    public static final String POOL_NUKE_TRASH = "POOL_NUKE_TRASH";
    public static final String POOL_NUKE_MISC = "POOL_NUKE_MISC";
    public static final String POOL_VERTIBIRD = "POOL_VERTIBIRD";
    public static final String POOL_SPACESHIP = "POOL_SPACESHIP";
    public static final String POOL_MACHINE_PARTS = "POOL_MACHINE_PARTS";
    public static final String POOL_NUKE_FUEL = "POOL_NUKE_FUEL";
    public static final String POOL_SILO = "POOL_SILO";
    public static final String POOL_OFFICE_TRASH = "POOL_OFFICE_TRASH";
    public static final String POOL_FILING_CABINET = "POOL_FILING_CABINET";
    public static final String POOL_SOLID_FUEL = "POOL_SOLID_FUEL";
    public static final String POOL_VAULT_LAB = "POOL_VAULT_LAB";
    public static final String POOL_VAULT_LOCKERS = "POOL_VAULT_LOCKERS";
    public static final String POOL_OIL_RIG = "POOL_OIL_RIG";
    public static final String POOL_RTG = "POOL_RTG";
    public static final String POOL_REPAIR_MATERIALS = "POOL_REPAIR_MATERIALS";
    public static final String POOL_PILE_HIVE = "POOL_PILE_HIVE";
    public static final String POOL_PILE_BONES = "POOL_PILE_BONES";
    public static final String POOL_PILE_CAPS = "POOL_PILE_CAPS";
    public static final String POOL_PILE_MED_SYRINGE = "POOL_PILE_MED_SYRINGE";
    public static final String POOL_PILE_MED_PILLS = "POOL_PILE_MED_PILLS";
    public static final String POOL_PILE_MAKESHIFT_GUN = "POOL_PILE_MAKESHIFT_GUN";
    public static final String POOL_PILE_MAKESHIFT_WRENCH = "POOL_PILE_MAKESHIFT_WRENCH";
    public static final String POOL_PILE_MAKESHIFT_PLATES = "POOL_PILE_MAKESHIFT_PLATES";
    public static final String POOL_PILE_MAKESHIFT_WIRE = "POOL_PILE_MAKESHIFT_WIRE";
    public static final String POOL_PILE_NUKE_STORAGE = "POOL_PILE_NUKE_STORAGE";
    public static final String POOL_PILE_OF_GARBAGE = "POOL_PILE_OF_GARBAGE";
    public static final String POOL_PILE_MECHANICAL = "POOL_PILE_MECHANICAL";
    public static final String POOL_PILE_GEAR = "POOL_PILE_GEAR";
    public static final String POOL_SUPPLIES = "POOL_SUPPLIES";
    public static final String POOL_WEAPONS = "POOL_WEAPONS";
    public static final String POOL_AMMO = "POOL_AMMO";
    public static final String POOL_SODA = "POOL_SODA";
    public static final String POOL_SNACKS = "POOL_SNACKS";
    public static final String POOL_SAT_MINER = "POOL_SAT_MINER";
    public static final String POOL_SAT_LUNAR = "POOL_SAT_LUNAR";
    public static final String POOL_RED_PEDESTAL = "POOL_RED_PEDESTAL";
    public static final String POOL_BLACK_SLAB = "POOL_BLACK_SLAB";
    public static final String POOL_BLACK_PART = "POOL_BLACK_PART";

    private static final Map<String, ResourceLocation> EXPLICIT_TABLES = Map.ofEntries(
            entry(BACKUP_POOL, table("backup")),
            entry(POOL_VAULT_RUSTY, table("vault_rusty")),
            entry(POOL_VAULT_STANDARD, table("vault_standard")),
            entry(POOL_VAULT_REINFORCED, table("vault_reinforced")),
            entry(POOL_VAULT_UNBREAKABLE, table("vault_unbreakable")),
            entry(POOL_BLUEPRINTS, table("blueprints")),
            entry(POOL_GENERIC, table("legacy/generic")),
            entry(POOL_ANTENNA, table("legacy/antenna")),
            entry(POOL_EXPENSIVE, table("legacy/expensive")),
            entry(POOL_NUKE_TRASH, table("legacy/nuke_trash")),
            entry(POOL_NUKE_MISC, table("legacy/nuke_misc")),
            entry(POOL_VERTIBIRD, table("legacy/vertibird")),
            entry(POOL_SPACESHIP, table("legacy/spaceship")),
            entry(POOL_MACHINE_PARTS, table("component/machine_parts")),
            entry(POOL_NUKE_FUEL, table("component/nuke_fuel")),
            entry(POOL_SILO, table("component/silo")),
            entry(POOL_OFFICE_TRASH, table("component/office_trash")),
            entry(POOL_FILING_CABINET, table("component/filing_cabinet")),
            entry(POOL_SOLID_FUEL, table("component/solid_fuel")),
            entry(POOL_VAULT_LAB, table("component/vault_lab")),
            entry(POOL_VAULT_LOCKERS, table("component/vault_lockers")),
            entry(POOL_OIL_RIG, table("component/oil_rig")),
            entry(POOL_RTG, table("component/rtg")),
            entry(POOL_REPAIR_MATERIALS, table("component/repair_materials")),
            entry(POOL_PILE_HIVE, table("pile/hive")),
            entry(POOL_PILE_BONES, table("pile/bones")),
            entry(POOL_PILE_CAPS, table("pile/caps")),
            entry(POOL_PILE_MED_SYRINGE, table("pile/med_syringe")),
            entry(POOL_PILE_MED_PILLS, table("pile/med_pills")),
            entry(POOL_PILE_MAKESHIFT_GUN, table("pile/makeshift_gun")),
            entry(POOL_PILE_MAKESHIFT_WRENCH, table("pile/makeshift_wrench")),
            entry(POOL_PILE_MAKESHIFT_PLATES, table("pile/makeshift_plates")),
            entry(POOL_PILE_MAKESHIFT_WIRE, table("pile/makeshift_wire")),
            entry(POOL_PILE_NUKE_STORAGE, table("pile/nuke_storage")),
            entry(POOL_PILE_OF_GARBAGE, table("pile/garbage")),
            entry(POOL_PILE_MECHANICAL, table("pile/mechanical")),
            entry(POOL_PILE_GEAR, table("pile/gear")),
            entry(POOL_SUPPLIES, table("c130/supplies")),
            entry(POOL_WEAPONS, table("c130/weapons")),
            entry(POOL_AMMO, table("c130/ammo")),
            entry(POOL_SODA, table("vending/soda")),
            entry(POOL_SNACKS, table("vending/snacks")),
            entry(POOL_SAT_MINER, table("satellite/miner")),
            entry(POOL_SAT_LUNAR, table("satellite/lunar")),
            entry(POOL_RED_PEDESTAL, table("redroom/red_pedestal")),
            entry(POOL_BLACK_SLAB, table("redroom/black_slab")),
            entry(POOL_BLACK_PART, table("redroom/black_part"))
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

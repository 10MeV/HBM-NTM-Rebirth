package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RadiationConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_CONTAMINATION;
    public static ForgeConfigSpec.BooleanValue ENABLE_CHUNK_RADS;
    public static ForgeConfigSpec.IntValue FOG_RAD;
    public static ForgeConfigSpec.IntValue FOG_CHANCE;
    public static ForgeConfigSpec.DoubleValue HELL_RAD;
    public static ForgeConfigSpec.BooleanValue WORLD_RAD_EFFECTS;
    public static ForgeConfigSpec.IntValue WORLD_RAD;
    public static ForgeConfigSpec.IntValue WORLD_RAD_THRESHOLD;
    public static ForgeConfigSpec.BooleanValue CLEANUP_DEAD_DIRT;
    public static ForgeConfigSpec.BooleanValue ENABLE_MYCELIUM_SPREAD;

    public static ForgeConfigSpec.BooleanValue DISABLE_ASBESTOS;
    public static ForgeConfigSpec.BooleanValue DISABLE_BLINDING;
    public static ForgeConfigSpec.BooleanValue DISABLE_COAL;
    public static ForgeConfigSpec.BooleanValue DISABLE_EXPLOSIVE;
    public static ForgeConfigSpec.BooleanValue DISABLE_HOT;
    public static ForgeConfigSpec.BooleanValue DISABLE_HYDROACTIVE;
    public static ForgeConfigSpec.BooleanValue ENABLE_LESS_BULLSHIT_MODE;
    public static ForgeConfigSpec.BooleanValue LBSM_SAFE_CRATES;
    public static ForgeConfigSpec.BooleanValue LBSM_SAFE_ME_DRIVES;
    public static ForgeConfigSpec.BooleanValue ENABLE_MKU;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("radiation");
        FOG_RAD = builder
                .comment("Legacy FOG_00_threshold: chunk radiation in RADs required for radiation fog.")
                .defineInRange("fogRad", 100, 0, Integer.MAX_VALUE);
        FOG_CHANCE = builder
                .comment("Legacy FOG_01_threshold: 1:n chance for fog while the radiation system updates.")
                .defineInRange("fogChance", 20, 1, Integer.MAX_VALUE);
        HELL_RAD = builder
                .comment("Legacy AMBIENT_00_nether: RAD/s applied in the Nether.")
                .defineInRange("hellRad", 0.1D, 0.0D, Double.MAX_VALUE);
        WORLD_RAD_EFFECTS = builder
                .comment("Legacy RADWORLD_00_toggle: allow high radiation to modify terrain.")
                .define("worldRadEffects", true);
        WORLD_RAD = builder
                .comment("Legacy RADWORLD_01_amount: block operation batches per tick.")
                .defineInRange("worldRad", 10, 0, Integer.MAX_VALUE);
        WORLD_RAD_THRESHOLD = builder
                .comment("Legacy RADWORLD_02_minimum: minimum chunk radiation for terrain mutation.")
                .defineInRange("worldRadThreshold", 20, 0, Integer.MAX_VALUE);
        CLEANUP_DEAD_DIRT = builder
                .comment("Legacy RADWORLD_03_regrow: whether dead grass and mycelium should decay into dirt.")
                .define("cleanupDeadDirt", false);
        ENABLE_MYCELIUM_SPREAD = builder
                .comment("Legacy general 1.01_enableMyceliumSpread: allows glowing waste mycelium to spread.")
                .define("enableMyceliumSpread", false);
        ENABLE_CONTAMINATION = builder
                .comment("Legacy RADIATION_00_enableContamination: toggles entity radiation contamination.")
                .define("enableContamination", true);
        ENABLE_CHUNK_RADS = builder
                .comment("Legacy RADIATION_01_enableChunkRads: toggles chunk radiation.")
                .define("enableChunkRads", true);
        builder.pop();

        builder.push("hazards");
        DISABLE_ASBESTOS = builder.define("disableAsbestos", false);
        DISABLE_BLINDING = builder.define("disableBlinding", false);
        DISABLE_COAL = builder.define("disableCoal", false);
        DISABLE_EXPLOSIVE = builder.define("disableExplosive", false);
        DISABLE_HOT = builder.define("disableHot", false);
        DISABLE_HYDROACTIVE = builder.define("disableHydroactive", false);
        ENABLE_LESS_BULLSHIT_MODE = builder
                .comment("Legacy enableLessBullshitMode. Currently used for hazard compatibility gates migrated before the full LBSM config.")
                .define("enableLessBullshitMode", false);
        LBSM_SAFE_CRATES = builder
                .comment("Legacy LBSM_safeCrates: when Less Bullshit Mode is enabled, prevents crate-like item containers from becoming radioactive.")
                .define("lbsmSafeCrates", true);
        LBSM_SAFE_ME_DRIVES = builder
                .comment("Legacy LBSM_safeMEDrives: when Less Bullshit Mode is enabled, prevents ME Drives and Portable Cells from becoming radioactive.")
                .define("lbsmSafeMeDrives", true);
        ENABLE_MKU = builder
                .comment("Legacy ServerConfig.ENABLE_MKU: toggles MKU contagion ticking and infection behavior.")
                .define("enableMku", true);
        builder.pop();
    }

    private RadiationConfig() {
    }
}

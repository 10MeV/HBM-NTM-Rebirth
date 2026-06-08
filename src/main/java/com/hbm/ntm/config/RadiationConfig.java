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
    public static ForgeConfigSpec.BooleanValue ENABLE_CRATER_BIOME_RADIATION;
    public static ForgeConfigSpec.DoubleValue CRATER_BIOME_RAD;
    public static ForgeConfigSpec.DoubleValue CRATER_BIOME_INNER_RAD;
    public static ForgeConfigSpec.DoubleValue CRATER_BIOME_OUTER_RAD;
    public static ForgeConfigSpec.DoubleValue CRATER_BIOME_WATER_MULT;
    public static ForgeConfigSpec.BooleanValue ENABLE_POLLUTION;
    public static ForgeConfigSpec.BooleanValue ENABLE_POLLUTION_LEAD_FROM_BLOCKS;
    public static ForgeConfigSpec.BooleanValue ENABLE_POLLUTION_LEAD_POISONING;
    public static ForgeConfigSpec.BooleanValue ENABLE_POLLUTION_POISON;
    public static ForgeConfigSpec.BooleanValue ENABLE_POLLUTION_SOOT_FOG;
    public static ForgeConfigSpec.DoubleValue POLLUTION_MULT;
    public static ForgeConfigSpec.DoubleValue POLLUTION_BUFF_MOB_THRESHOLD;
    public static ForgeConfigSpec.DoubleValue POLLUTION_SOOT_FOG_THRESHOLD;
    public static ForgeConfigSpec.DoubleValue POLLUTION_SOOT_FOG_DIVISOR;
    public static ForgeConfigSpec.BooleanValue RAMPANT_GLYPHID_GUIDANCE;

    public static ForgeConfigSpec.BooleanValue DISABLE_ASBESTOS;
    public static ForgeConfigSpec.BooleanValue DISABLE_BLINDING;
    public static ForgeConfigSpec.BooleanValue DISABLE_COAL;
    public static ForgeConfigSpec.BooleanValue DISABLE_EXPLOSIVE;
    public static ForgeConfigSpec.BooleanValue DISABLE_HOT;
    public static ForgeConfigSpec.BooleanValue DISABLE_HYDROACTIVE;

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
        ENABLE_CRATER_BIOME_RADIATION = builder
                .comment("Legacy WorldConfig 17.B_toggle radiation side: nuclear fallout marks crater zones that apply ambient radiation.")
                .define("enableCraterBiomeRadiation", true);
        CRATER_BIOME_RAD = builder
                .comment("Legacy WorldConfig 17.R00_craterBiomeRad: RAD/s for the crater zone.")
                .defineInRange("craterBiomeRad", 5.0D, 0.0D, Double.MAX_VALUE);
        CRATER_BIOME_INNER_RAD = builder
                .comment("Legacy WorldConfig 17.R01_craterBiomeInnerRad: RAD/s for the inner crater zone.")
                .defineInRange("craterBiomeInnerRad", 25.0D, 0.0D, Double.MAX_VALUE);
        CRATER_BIOME_OUTER_RAD = builder
                .comment("Legacy WorldConfig 17.R02_craterBiomeOuterRad: RAD/s for the outer crater zone.")
                .defineInRange("craterBiomeOuterRad", 0.5D, 0.0D, Double.MAX_VALUE);
        CRATER_BIOME_WATER_MULT = builder
                .comment("Legacy WorldConfig 17.R03_craterBiomeWaterMult: multiplier while wet in crater zones.")
                .defineInRange("craterBiomeWaterMult", 5.0D, 0.0D, Double.MAX_VALUE);
        ENABLE_CONTAMINATION = builder
                .comment("Legacy RADIATION_00_enableContamination: toggles entity radiation contamination.")
                .define("enableContamination", true);
        ENABLE_CHUNK_RADS = builder
                .comment("Legacy RADIATION_01_enableChunkRads: toggles chunk radiation.")
                .define("enableChunkRads", true);
        builder.pop();

        builder.push("pollution");
        ENABLE_POLLUTION = builder
                .comment("Legacy POL_00_enablePollution: toggles soot, poison, heavy metal, and fallout pollution.")
                .define("enablePollution", true);
        ENABLE_POLLUTION_LEAD_FROM_BLOCKS = builder
                .comment("Legacy POL_01_enableLeadFromBlocks: breaking blocks in heavy metal polluted areas applies lead poisoning.")
                .define("enableLeadFromBlocks", true);
        ENABLE_POLLUTION_LEAD_POISONING = builder
                .comment("Legacy POL_02_enableLeadPoisoning: standing in heavy metal polluted areas applies lead poisoning.")
                .define("enableLeadPoisoning", true);
        ENABLE_POLLUTION_POISON = builder
                .comment("Legacy POL_04_enablePoison: standing in poisoned areas applies poison or wither.")
                .define("enablePoison", true);
        ENABLE_POLLUTION_SOOT_FOG = builder
                .comment("Legacy POL_03_enableSootFog: whether smog should be visible.")
                .define("enableSootFog", true);
        POLLUTION_MULT = builder
                .comment("Legacy MobConfig 12.R08_pollutionMult: multiplier applied to emitted pollution amounts.")
                .defineInRange("pollutionMult", 1.0D, 0.0D, Double.MAX_VALUE);
        POLLUTION_BUFF_MOB_THRESHOLD = builder
                .comment("Legacy POL_05_buffMobThreshold: soot required to buff naturally spawning hostile mobs.")
                .defineInRange("buffMobThreshold", 15.0D, 0.0D, Double.MAX_VALUE);
        POLLUTION_SOOT_FOG_THRESHOLD = builder
                .comment("Legacy POL_06_sootFogThreshold: soot required for smog to become visible.")
                .defineInRange("sootFogThreshold", 35.0D, 0.0D, Double.MAX_VALUE);
        POLLUTION_SOOT_FOG_DIVISOR = builder
                .comment("Legacy POL_07_sootFogDivisor: higher values require more soot for the same smog density.")
                .defineInRange("sootFogDivisor", 120.0D, 0.0001D, Double.MAX_VALUE);
        RAMPANT_GLYPHID_GUIDANCE = builder
                .comment("Legacy MobConfig 12.R05_rampantGlyphidGuidance: records a sleeping player's bed as the pollution/rampant target point.")
                .define("rampantGlyphidGuidance", false);
        builder.pop();

        builder.push("hazards");
        DISABLE_ASBESTOS = builder.define("disableAsbestos", false);
        DISABLE_BLINDING = builder.define("disableBlinding", false);
        DISABLE_COAL = builder.define("disableCoal", false);
        DISABLE_EXPLOSIVE = builder.define("disableExplosive", false);
        DISABLE_HOT = builder.define("disableHot", false);
        DISABLE_HYDROACTIVE = builder.define("disableHydroactive", false);
        builder.pop();
    }

    private RadiationConfig() {
    }
}

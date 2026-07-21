package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

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
    public static ForgeConfigSpec.BooleanValue ENABLE_PRISM;
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
    public static ForgeConfigSpec.DoubleValue POLLUTION_SMOKE_STACK_SOOT_MULT;
    public static ForgeConfigSpec.BooleanValue ENABLE_MOB_GEAR;
    public static ForgeConfigSpec.BooleanValue ENABLE_MOB_WEAPONS;
    public static ForgeConfigSpec.DoubleValue MOB_WEAPON_SOOT_REDUCTION;
    public static ForgeConfigSpec.BooleanValue ENABLE_GLYPHID_HIVES;
    public static ForgeConfigSpec.IntValue GLYPHID_HIVE_SPAWN_CHUNKS;
    public static ForgeConfigSpec.BooleanValue GLYPHID_WAYPOINT_DEBUG;
    public static ForgeConfigSpec.DoubleValue GLYPHID_TARGETING_THRESHOLD;
    public static ForgeConfigSpec.BooleanValue RAMPANT_MODE;
    public static ForgeConfigSpec.BooleanValue RAMPANT_NATURAL_SCOUT_SPAWN;
    public static ForgeConfigSpec.DoubleValue RAMPANT_SCOUT_SPAWN_THRESHOLD;
    public static ForgeConfigSpec.IntValue RAMPANT_SCOUT_SPAWN_CHANCE;
    public static ForgeConfigSpec.BooleanValue RAMPANT_EXTENDED_TARGETING;
    public static ForgeConfigSpec.BooleanValue RAMPANT_DIG;
    public static ForgeConfigSpec.BooleanValue RAMPANT_GLYPHID_GUIDANCE;
    public static ForgeConfigSpec.DoubleValue RAMPANT_SMOKE_STACK_OVERRIDE;
    public static ForgeConfigSpec.DoubleValue GLYPHID_SPAWN_MAX;
    public static ForgeConfigSpec.DoubleValue GLYPHID_SCOUT_THRESHOLD;
    public static ForgeConfigSpec.IntValue GLYPHID_SCOUT_SWARM_SPAWN_CHANCE;
    public static ForgeConfigSpec.IntValue GLYPHID_BASE_SWARM_SIZE;
    public static ForgeConfigSpec.DoubleValue GLYPHID_SWARM_SCALING_MULT;
    public static ForgeConfigSpec.IntValue GLYPHID_SOOT_STEP;
    public static ForgeConfigSpec.IntValue GLYPHID_SWARM_COOLDOWN_SECONDS;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_BRAWLER_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_BOMBARDIER_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_BLASTER_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_DIGGER_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_BEHEMOTH_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_BRENDA_CHANCE;
    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> GLYPHID_NUCLEAR_CHANCE;

    public static ForgeConfigSpec.BooleanValue DISABLE_ASBESTOS;
    public static ForgeConfigSpec.BooleanValue DISABLE_BLINDING;
    public static ForgeConfigSpec.BooleanValue DISABLE_COAL;
    public static ForgeConfigSpec.BooleanValue DISABLE_EXPLOSIVE;
    public static ForgeConfigSpec.BooleanValue DISABLE_HOT;
    public static ForgeConfigSpec.BooleanValue DISABLE_HYDROACTIVE;
    public static ForgeConfigSpec.BooleanValue DISABLE_FIBROSIS;

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
        ENABLE_PRISM = builder
                .comment("Legacy RADIATION_99_enablePRISM: enables the 3D resistance-aware PRISM chunk radiation handler instead of the default Simple handler.")
                .define("enablePRISM", false);
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
        POLLUTION_SMOKE_STACK_SOOT_MULT = builder
                .comment("Legacy POL_08_smokeStackSootMult: stored for old pollution config parity; legacy chimney tile entities use their own pollution multipliers.")
                .defineInRange("smokeStackSootMult", 0.8D, 0.0D, Double.MAX_VALUE);
        ENABLE_MOB_GEAR = builder
                .comment("Legacy MobConfig 12.D01_enableMobGear: allows naturally spawning mobs to receive old HBM gear branches.")
                .define("enableMobGear", true);
        ENABLE_MOB_WEAPONS = builder
                .comment("Legacy MobConfig 12.D02_enableMobWeapons: lets high-soot Skeletons replace bows with Sedna guns.")
                .define("enableMobWeapons", true);
        MOB_WEAPON_SOOT_REDUCTION = builder
                .comment("Legacy MobConfig 12.D03_mobWeaponSootReduction: subtracts soot before Skeleton gun selection.")
                .defineInRange("mobWeaponSootReduction", 0.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        ENABLE_GLYPHID_HIVES = builder
                .comment("Legacy MobConfig 12.G00_enableHives: allows ordinary overworld Glyphid hive generation.")
                .define("enableGlyphidHives", true);
        GLYPHID_HIVE_SPAWN_CHUNKS = builder
                .comment("Legacy MobConfig 12.G01_hiveSpawn: average generated chunks per ordinary Glyphid hive.")
                .defineInRange("glyphidHiveSpawnChunks", 256, 1, Integer.MAX_VALUE);
        GLYPHID_WAYPOINT_DEBUG = builder
                .comment("Legacy MobConfig 12.G13_waypointDebug: shows client-side Glyphid waypoint tower particles.")
                .define("glyphidWaypointDebug", false);
        GLYPHID_TARGETING_THRESHOLD = builder
                .comment("Legacy MobConfig 12.G08_targetingThreshold: soot required for glyphids' extended targeting range.")
                .defineInRange("glyphidTargetingThreshold", 1.0D, 0.0D, Double.MAX_VALUE);
        RAMPANT_MODE = builder
                .comment("Legacy MobConfig 12.R01_rampantMode: aggregate toggle for old rampant glyphid/pollution behavior.")
                .define("rampantMode", false);
        RAMPANT_NATURAL_SCOUT_SPAWN = builder
                .comment("Legacy MobConfig 12.R02_rampantScoutSpawn: allows scouts to spawn naturally in high-soot areas once glyphids exist.")
                .define("rampantNaturalScoutSpawn", false);
        RAMPANT_SCOUT_SPAWN_THRESHOLD = builder
                .comment("Legacy MobConfig 12.R02.1_rampantScoutSpawnThresh: soot required for natural rampant scouts.")
                .defineInRange("rampantScoutSpawnThreshold", 13.0D, 0.0D, Double.MAX_VALUE);
        RAMPANT_SCOUT_SPAWN_CHANCE = builder
                .comment("Legacy MobConfig 12.R02.2_rampantScoutSpawnChance: 1/x chance per potential spawn check.")
                .defineInRange("rampantScoutSpawnChance", 1400, 1, Integer.MAX_VALUE);
        RAMPANT_EXTENDED_TARGETING = builder
                .comment("Legacy MobConfig 12.R03_rampantExtendedTargeting: forces glyphid extended targeting once glyphids exist.")
                .define("rampantExtendedTargeting", false);
        RAMPANT_DIG = builder
                .comment("Legacy MobConfig 12.R04_rampantDig: allows rampant glyphids to dig toward waypoints once glyphids exist.")
                .define("rampantDig", false);
        RAMPANT_GLYPHID_GUIDANCE = builder
                .comment("Legacy MobConfig 12.R05_rampantGlyphidGuidance: records a sleeping player's bed as the pollution/rampant target point.")
                .define("rampantGlyphidGuidance", false);
        RAMPANT_SMOKE_STACK_OVERRIDE = builder
                .comment("Legacy MobConfig 12.R06_rampantSmokeStackOverride: chimney pollution multiplier during rampant mode.")
                .defineInRange("rampantSmokeStackOverride", 0.4D, 0.0D, Double.MAX_VALUE);
        GLYPHID_SPAWN_MAX = builder
                .comment("Legacy MobConfig 12.G07_spawnMax: global loaded-entity cap for glyphid-spawner swarms.")
                .defineInRange("glyphidSpawnMax", 50.0D, 0.0D, Double.MAX_VALUE);
        GLYPHID_SCOUT_THRESHOLD = builder
                .comment("Legacy MobConfig 12.G02_scoutThreshold: soot required for a spawner scout roll.")
                .defineInRange("glyphidScoutThreshold", 1.0D, 0.0D, Double.MAX_VALUE);
        GLYPHID_SCOUT_SWARM_SPAWN_CHANCE = builder
                .comment("Legacy MobConfig 12.G10_scoutSwarmSpawn: 1 in x source chance is evaluated with nextInt(x + 1).")
                .defineInRange("glyphidScoutSwarmSpawnChance", 3, 0, Integer.MAX_VALUE);
        GLYPHID_BASE_SWARM_SIZE = builder
                .comment("Legacy MobConfig 12.GS01_baseSwarmSize: soot-less glyphid swarm size.")
                .defineInRange("glyphidBaseSwarmSize", 5, 0, Integer.MAX_VALUE);
        GLYPHID_SWARM_SCALING_MULT = builder
                .comment("Legacy MobConfig 12.GS02_swarmScalingMult: swarm-size scaling multiplier.")
                .defineInRange("glyphidSwarmScalingMult", 1.2D, 0.0D, Double.MAX_VALUE);
        GLYPHID_SOOT_STEP = builder
                .comment("Legacy MobConfig 12.GS03_sootStep: soot denominator for swarm-size scaling.")
                .defineInRange("glyphidSootStep", 50, 1, Integer.MAX_VALUE);
        GLYPHID_SWARM_COOLDOWN_SECONDS = builder
                .comment("Legacy MobConfig 12.GS04_swarmCooldown: spawner cooldown in seconds; runtime converts it to ticks.")
                .defineInRange("glyphidSwarmCooldownSeconds", 120, 1, Integer.MAX_VALUE);
        GLYPHID_CHANCE = glyphidChance(builder, "glyphidChance", "12.GC01_glyphidChance", List.of(50, -45, 0));
        GLYPHID_BRAWLER_CHANCE = glyphidChance(builder, "glyphidBrawlerChance", "12.GC02_brawlerChance", List.of(10, 30, 1));
        GLYPHID_BOMBARDIER_CHANCE = glyphidChance(builder, "glyphidBombardierChance", "12.GC03_bombardierChance", List.of(20, -15, 1));
        GLYPHID_BLASTER_CHANCE = glyphidChance(builder, "glyphidBlasterChance", "12.GC04_blasterChance", List.of(-5, 40, 5));
        GLYPHID_DIGGER_CHANCE = glyphidChance(builder, "glyphidDiggerChance", "12.GC05_diggerChance", List.of(-15, 25, 5));
        GLYPHID_BEHEMOTH_CHANCE = glyphidChance(builder, "glyphidBehemothChance", "12.GC06_behemothChance", List.of(-30, 45, 10));
        GLYPHID_BRENDA_CHANCE = glyphidChance(builder, "glyphidBrendaChance", "12.GC07_brendaChance", List.of(-50, 60, 20));
        GLYPHID_NUCLEAR_CHANCE = glyphidChance(builder, "glyphidNuclearChance", "12.GC08_johnsonChance", List.of(-50, 60, 50));
        builder.pop();

        builder.push("hazards");
        DISABLE_ASBESTOS = builder.define("disableAsbestos", false);
        DISABLE_BLINDING = builder.define("disableBlinding", false);
        DISABLE_COAL = builder.define("disableCoal", false);
        DISABLE_EXPLOSIVE = builder.define("disableExplosive", false);
        DISABLE_HOT = builder.define("disableHot", false);
        DISABLE_HYDROACTIVE = builder.define("disableHydroactive", false);
        DISABLE_FIBROSIS = builder
                .comment("Legacy HAZ_06_disableFibrosis mirror. The 1.7.10 source declares this setting but no hazard or event consumes it.")
                .define("disableFibrosis", false);
        builder.pop();
    }

    public static boolean pollutionEnabled() {
        return ENABLE_POLLUTION.get();
    }

    public static boolean contaminationEnabled() {
        return ENABLE_CONTAMINATION.get();
    }

    public static boolean chunkRadiationEnabled() {
        return ENABLE_CHUNK_RADS.get();
    }

    public static boolean prismRadiationEnabled() {
        return ENABLE_PRISM.get();
    }

    public static int radiationFogThreshold() {
        return FOG_RAD.get();
    }

    public static int radiationFogChance() {
        return FOG_CHANCE.get();
    }

    public static float hellRadiation() {
        return HELL_RAD.get().floatValue();
    }

    public static boolean worldRadiationEffectsEnabled() {
        return WORLD_RAD_EFFECTS.get();
    }

    public static int worldRadiation() {
        return WORLD_RAD.get();
    }

    public static int worldRadiationThreshold() {
        return WORLD_RAD_THRESHOLD.get();
    }

    public static boolean cleanupDeadDirtEnabled() {
        return CLEANUP_DEAD_DIRT.get();
    }

    public static boolean myceliumSpreadEnabled() {
        return ENABLE_MYCELIUM_SPREAD.get();
    }

    public static boolean craterBiomeRadiationEnabled() {
        return ENABLE_CRATER_BIOME_RADIATION.get();
    }

    public static float craterBiomeRadiation() {
        return CRATER_BIOME_RAD.get().floatValue();
    }

    public static float craterBiomeInnerRadiation() {
        return CRATER_BIOME_INNER_RAD.get().floatValue();
    }

    public static float craterBiomeOuterRadiation() {
        return CRATER_BIOME_OUTER_RAD.get().floatValue();
    }

    public static float craterBiomeWaterMultiplier() {
        return CRATER_BIOME_WATER_MULT.get().floatValue();
    }

    public static boolean pollutionLeadFromBlocksEnabled() {
        return ENABLE_POLLUTION_LEAD_FROM_BLOCKS.get();
    }

    public static boolean pollutionLeadPoisoningEnabled() {
        return ENABLE_POLLUTION_LEAD_POISONING.get();
    }

    public static boolean pollutionPoisonEnabled() {
        return ENABLE_POLLUTION_POISON.get();
    }

    public static boolean pollutionSootFogEnabled() {
        return ENABLE_POLLUTION_SOOT_FOG.get();
    }

    public static float pollutionMultiplier() {
        double multiplier = POLLUTION_MULT.get();
        if (rampantModeEnabled() && multiplier == 1.0D) {
            multiplier = 3.0D;
        }
        return (float) multiplier;
    }

    public static float pollutionBuffMobThreshold() {
        return POLLUTION_BUFF_MOB_THRESHOLD.get().floatValue();
    }

    public static float pollutionSootFogThreshold() {
        double threshold = POLLUTION_SOOT_FOG_THRESHOLD.get();
        if (rampantModeEnabled()) {
            threshold *= pollutionMultiplier();
        }
        return (float) threshold;
    }

    public static float pollutionSootFogDivisor() {
        return POLLUTION_SOOT_FOG_DIVISOR.get().floatValue();
    }

    public static double pollutionSmokeStackSootMultiplier() {
        return POLLUTION_SMOKE_STACK_SOOT_MULT.get();
    }

    public static boolean mobGearEnabled() {
        return ENABLE_MOB_GEAR.get();
    }

    public static boolean mobWeaponsEnabled() {
        return ENABLE_MOB_WEAPONS.get();
    }

    public static float mobWeaponSootReduction() {
        return MOB_WEAPON_SOOT_REDUCTION.get().floatValue();
    }

    public static boolean glyphidHivesEnabled() {
        return ENABLE_GLYPHID_HIVES.get();
    }

    public static int glyphidHiveSpawnChunks() {
        return GLYPHID_HIVE_SPAWN_CHUNKS.get();
    }

    public static boolean glyphidWaypointDebugEnabled() {
        return GLYPHID_WAYPOINT_DEBUG.get();
    }

    public static float glyphidTargetingThreshold() {
        return GLYPHID_TARGETING_THRESHOLD.get().floatValue();
    }

    public static boolean rampantModeEnabled() {
        return RAMPANT_MODE.get();
    }

    public static boolean rampantNaturalScoutSpawnEnabled() {
        return rampantModeEnabled() || RAMPANT_NATURAL_SCOUT_SPAWN.get();
    }

    public static float rampantScoutSpawnThreshold() {
        return RAMPANT_SCOUT_SPAWN_THRESHOLD.get().floatValue();
    }

    public static int rampantScoutSpawnChance() {
        return RAMPANT_SCOUT_SPAWN_CHANCE.get();
    }

    public static boolean rampantExtendedTargetingEnabled() {
        return rampantModeEnabled() || RAMPANT_EXTENDED_TARGETING.get();
    }

    public static boolean rampantDigEnabled() {
        return rampantModeEnabled() || RAMPANT_DIG.get();
    }

    public static boolean rampantGlyphidGuidanceEnabled() {
        return rampantModeEnabled() || RAMPANT_GLYPHID_GUIDANCE.get();
    }

    public static double rampantSmokeStackOverride() {
        return RAMPANT_SMOKE_STACK_OVERRIDE.get();
    }

    public static double glyphidSpawnMax() { return GLYPHID_SPAWN_MAX.get(); }

    /**
     * The legacy rampant aggregate toggle made the first scout roll effectively
     * unconditional by assigning {@code scoutThreshold = 0.1}.
     */
    public static double glyphidScoutThreshold() {
        return rampantModeEnabled() ? 0.1D : GLYPHID_SCOUT_THRESHOLD.get();
    }

    /**
     * The old runtime rolls {@code nextInt(chance + 1)}; Rampant Mode assigns
     * one here, preserving its one-in-two first-scout result.
     */
    public static int glyphidScoutSwarmSpawnChance() {
        return rampantModeEnabled() ? 1 : GLYPHID_SCOUT_SWARM_SPAWN_CHANCE.get();
    }
    public static int glyphidBaseSwarmSize() { return GLYPHID_BASE_SWARM_SIZE.get(); }
    public static double glyphidSwarmScalingMultiplier() { return GLYPHID_SWARM_SCALING_MULT.get(); }
    public static int glyphidSootStep() { return GLYPHID_SOOT_STEP.get(); }
    public static int glyphidSwarmCooldownTicks() { return GLYPHID_SWARM_COOLDOWN_SECONDS.get() * 20; }
    public static int[] glyphidChance() { return chanceArray(GLYPHID_CHANCE, 50, -45, 0); }
    public static int[] glyphidBrawlerChance() { return chanceArray(GLYPHID_BRAWLER_CHANCE, 10, 30, 1); }
    public static int[] glyphidBombardierChance() {
        int[] chance = chanceArray(GLYPHID_BOMBARDIER_CHANCE, 20, -15, 1);
        // MobConfig#loadFromConfig only relaxes the default one-soot minimum.
        if (rampantModeEnabled() && chance[2] == 1) {
            chance[2] = 0;
        }
        return chance;
    }
    public static int[] glyphidBlasterChance() { return chanceArray(GLYPHID_BLASTER_CHANCE, -5, 40, 5); }
    public static int[] glyphidDiggerChance() { return chanceArray(GLYPHID_DIGGER_CHANCE, -15, 25, 5); }
    public static int[] glyphidBehemothChance() { return chanceArray(GLYPHID_BEHEMOTH_CHANCE, -30, 45, 10); }
    public static int[] glyphidBrendaChance() { return chanceArray(GLYPHID_BRENDA_CHANCE, -50, 60, 20); }
    public static int[] glyphidNuclearChance() { return chanceArray(GLYPHID_NUCLEAR_CHANCE, -50, 60, 50); }

    private static ForgeConfigSpec.ConfigValue<List<? extends Integer>> glyphidChance(ForgeConfigSpec.Builder builder,
            String key, String legacyKey, List<Integer> defaults) {
        return builder.comment("Legacy MobConfig " + legacyKey
                        + ": [base chance, soot modifier, minimum soot]. Exactly three integers are required.")
                .defineList(key, defaults, value -> value instanceof Integer);
    }

    private static int[] chanceArray(ForgeConfigSpec.ConfigValue<List<? extends Integer>> configured,
            int first, int second, int third) {
        List<? extends Integer> values = configured.get();
        if (values.size() != 3) return new int[] {first, second, third};
        return new int[] {values.get(0), values.get(1), values.get(2)};
    }

    public static double chimneyPollutionMultiplier(boolean industrial) {
        if (rampantModeEnabled()) {
            double override = rampantSmokeStackOverride();
            return industrial ? override / 2.0D : override;
        }
        return industrial ? 0.1D : 0.25D;
    }

    public static boolean asbestosHazardDisabled() {
        return DISABLE_ASBESTOS.get();
    }

    public static boolean blindingHazardDisabled() {
        return DISABLE_BLINDING.get();
    }

    public static boolean coalHazardDisabled() {
        return DISABLE_COAL.get();
    }

    public static boolean explosiveHazardDisabled() {
        return DISABLE_EXPLOSIVE.get();
    }

    public static boolean hotHazardDisabled() {
        return DISABLE_HOT.get();
    }

    public static boolean hydroactiveHazardDisabled() {
        return DISABLE_HYDROACTIVE.get();
    }

    public static boolean fibrosisHazardDisabled() {
        return DISABLE_FIBROSIS.get();
    }

    private RadiationConfig() {
    }
}

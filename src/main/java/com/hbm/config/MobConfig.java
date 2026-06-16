package com.hbm.config;

/**
 * Legacy package facade for the pollution-related MobConfig surface.
 */
@Deprecated(forRemoval = false)
public final class MobConfig {
    public static boolean enableMobGear = true;
    public static boolean rampantMode = false;
    public static boolean rampantNaturalScoutSpawn = false;
    public static double rampantScoutSpawnThresh = 13.0D;
    public static int rampantScoutSpawnChance = 1400;
    public static boolean rampantExtendedTargetting = false;
    public static boolean rampantDig = false;
    public static boolean rampantGlyphidGuidance = false;
    public static double rampantSmokeStackOverride = 0.4D;
    public static double pollutionMult = 1.0D;
    public static double targetingThreshold = 1.0D;

    static {
        syncFromModern();
    }

    public static void syncFromModern() {
        try {
            enableMobGear = com.hbm.ntm.config.RadiationConfig.mobGearEnabled();
            rampantMode = com.hbm.ntm.config.RadiationConfig.rampantModeEnabled();
            rampantNaturalScoutSpawn = com.hbm.ntm.config.RadiationConfig.rampantNaturalScoutSpawnEnabled();
            rampantScoutSpawnThresh = com.hbm.ntm.config.RadiationConfig.rampantScoutSpawnThreshold();
            rampantScoutSpawnChance = com.hbm.ntm.config.RadiationConfig.rampantScoutSpawnChance();
            rampantExtendedTargetting = com.hbm.ntm.config.RadiationConfig.rampantExtendedTargetingEnabled();
            rampantDig = com.hbm.ntm.config.RadiationConfig.rampantDigEnabled();
            rampantGlyphidGuidance = com.hbm.ntm.config.RadiationConfig.rampantGlyphidGuidanceEnabled();
            rampantSmokeStackOverride = com.hbm.ntm.config.RadiationConfig.rampantSmokeStackOverride();
            pollutionMult = com.hbm.ntm.config.RadiationConfig.pollutionMultiplier();
            targetingThreshold = com.hbm.ntm.config.RadiationConfig.glyphidTargetingThreshold();
        } catch (IllegalStateException ignored) {
            // Keep legacy defaults until Forge finishes loading the modern config.
        }
    }

    public static boolean trueRam() {
        syncFromModern();
        return rampantMode && rampantNaturalScoutSpawn && rampantExtendedTargetting && rampantDig && rampantGlyphidGuidance;
    }

    public static boolean enableMobGear() {
        syncFromModern();
        return enableMobGear;
    }

    public static boolean rampantMode() {
        syncFromModern();
        return rampantMode;
    }

    public static boolean rampantNaturalScoutSpawn() {
        syncFromModern();
        return rampantNaturalScoutSpawn;
    }

    public static float rampantScoutSpawnThresh() {
        syncFromModern();
        return (float) rampantScoutSpawnThresh;
    }

    public static int rampantScoutSpawnChance() {
        syncFromModern();
        return rampantScoutSpawnChance;
    }

    public static boolean rampantExtendedTargetting() {
        syncFromModern();
        return rampantExtendedTargetting;
    }

    public static boolean rampantExtendedTargeting() {
        return rampantExtendedTargetting();
    }

    public static boolean rampantDig() {
        syncFromModern();
        return rampantDig;
    }

    public static boolean rampantGlyphidGuidance() {
        syncFromModern();
        return rampantGlyphidGuidance;
    }

    public static float pollutionMult() {
        syncFromModern();
        return (float) pollutionMult;
    }

    public static float targetingThreshold() {
        syncFromModern();
        return (float) targetingThreshold;
    }

    private MobConfig() {
    }
}

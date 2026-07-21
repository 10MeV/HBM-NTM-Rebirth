package com.hbm.config;

/**
 * Legacy package facade for the pollution-related MobConfig surface.
 */
@Deprecated(forRemoval = false)
public final class MobConfig {
    public static boolean enableMobGear = true;
    public static boolean enableMobWeapons = true;
    public static boolean enableHives = true;
    public static int hiveSpawn = 256;
    public static boolean waypointDebug = false;
    public static double mobWeaponSootReduction;
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
    public static int swarmCooldown = 2400, baseSwarmSize = 5, sootStep = 50, scoutSwarmSpawnChance = 3;
    public static double swarmScalingMult = 1.2D, spawnMax = 50D, scoutThreshold = 1D;
    public static int[] glyphidChance = {50,-45,0}, brawlerChance = {10,30,1}, bombardierChance = {20,-15,1}, blasterChance = {-5,40,5}, diggerChance = {-15,25,5}, behemothChance = {-30,45,10}, brendaChance = {-50,60,20}, johnsonChance = {-50,60,50};

    static {
        syncFromModern();
    }

    public static void syncFromModern() {
        try {
            enableMobGear = com.hbm.ntm.config.RadiationConfig.mobGearEnabled();
            enableMobWeapons = com.hbm.ntm.config.RadiationConfig.mobWeaponsEnabled();
            enableHives = com.hbm.ntm.config.RadiationConfig.glyphidHivesEnabled();
            hiveSpawn = com.hbm.ntm.config.RadiationConfig.glyphidHiveSpawnChunks();
            waypointDebug = com.hbm.ntm.config.RadiationConfig.glyphidWaypointDebugEnabled();
            mobWeaponSootReduction = com.hbm.ntm.config.RadiationConfig.mobWeaponSootReduction();
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
            spawnMax = com.hbm.ntm.config.RadiationConfig.glyphidSpawnMax();
            scoutThreshold = com.hbm.ntm.config.RadiationConfig.glyphidScoutThreshold();
            scoutSwarmSpawnChance = com.hbm.ntm.config.RadiationConfig.glyphidScoutSwarmSpawnChance();
            baseSwarmSize = com.hbm.ntm.config.RadiationConfig.glyphidBaseSwarmSize();
            swarmScalingMult = com.hbm.ntm.config.RadiationConfig.glyphidSwarmScalingMultiplier();
            sootStep = com.hbm.ntm.config.RadiationConfig.glyphidSootStep();
            swarmCooldown = com.hbm.ntm.config.RadiationConfig.glyphidSwarmCooldownTicks();
            glyphidChance = com.hbm.ntm.config.RadiationConfig.glyphidChance();
            brawlerChance = com.hbm.ntm.config.RadiationConfig.glyphidBrawlerChance();
            bombardierChance = com.hbm.ntm.config.RadiationConfig.glyphidBombardierChance();
            blasterChance = com.hbm.ntm.config.RadiationConfig.glyphidBlasterChance();
            diggerChance = com.hbm.ntm.config.RadiationConfig.glyphidDiggerChance();
            behemothChance = com.hbm.ntm.config.RadiationConfig.glyphidBehemothChance();
            brendaChance = com.hbm.ntm.config.RadiationConfig.glyphidBrendaChance();
            johnsonChance = com.hbm.ntm.config.RadiationConfig.glyphidNuclearChance();
        } catch (IllegalStateException | NullPointerException ignored) {
            // Keep legacy defaults until Forge finishes loading the modern config.
        }
    }

    public static boolean trueRam() {
        syncFromModern();
        return rampantMode && rampantNaturalScoutSpawn && scoutThreshold <= 0.1D
                && rampantExtendedTargetting && rampantDig && rampantGlyphidGuidance;
    }

    public static boolean enableMobGear() {
        syncFromModern();
        return enableMobGear;
    }

    public static boolean enableMobWeapons() {
        syncFromModern();
        return enableMobWeapons;
    }

    public static float mobWeaponSootReduction() {
        syncFromModern();
        return (float) mobWeaponSootReduction;
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

    public static boolean waypointDebug() {
        syncFromModern();
        return waypointDebug;
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

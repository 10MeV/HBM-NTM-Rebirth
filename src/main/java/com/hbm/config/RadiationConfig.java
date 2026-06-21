package com.hbm.config;

/**
 * Legacy package facade for radiation and pollution config reads.
 */
@Deprecated(forRemoval = false)
public final class RadiationConfig {
    public static int fogRad = 100;
    public static int fogCh = 20;
    public static double hellRad = 0.1D;
    public static int worldRad = 10;
    public static int worldRadThreshold = 20;
    public static boolean worldRadEffects = true;
    public static boolean cleanupDeadDirt = false;
    public static boolean enableContamination = true;
    public static boolean enableChunkRads = true;
    public static boolean enablePRISM = false;
    public static boolean disableAsbestos = false;
    public static boolean disableCoal = false;
    public static boolean disableHot = false;
    public static boolean disableExplosive = false;
    public static boolean disableHydro = false;
    public static boolean disableBlinding = false;
    public static boolean disableFibrosis = false;
    public static boolean enablePollution = true;
    public static boolean enableLeadFromBlocks = true;
    public static boolean enableLeadPoisoning = true;
    public static boolean enableSootFog = true;
    public static boolean enablePoison = true;
    public static double buffMobThreshold = 15.0D;
    public static double sootFogThreshold = 35.0D;
    public static double sootFogDivisor = 120.0D;
    public static double smokeStackSootMult = 0.8D;

    static {
        syncFromModern();
    }

    public static void syncFromModern() {
        try {
            fogRad = com.hbm.ntm.config.RadiationConfig.radiationFogThreshold();
            fogCh = com.hbm.ntm.config.RadiationConfig.radiationFogChance();
            hellRad = com.hbm.ntm.config.RadiationConfig.hellRadiation();
            worldRad = com.hbm.ntm.config.RadiationConfig.worldRadiation();
            worldRadThreshold = com.hbm.ntm.config.RadiationConfig.worldRadiationThreshold();
            worldRadEffects = com.hbm.ntm.config.RadiationConfig.worldRadiationEffectsEnabled();
            cleanupDeadDirt = com.hbm.ntm.config.RadiationConfig.cleanupDeadDirtEnabled();
            enableContamination = com.hbm.ntm.config.RadiationConfig.contaminationEnabled();
            enableChunkRads = com.hbm.ntm.config.RadiationConfig.chunkRadiationEnabled();
            enablePRISM = false;
            disableAsbestos = com.hbm.ntm.config.RadiationConfig.asbestosHazardDisabled();
            disableCoal = com.hbm.ntm.config.RadiationConfig.coalHazardDisabled();
            disableHot = com.hbm.ntm.config.RadiationConfig.hotHazardDisabled();
            disableExplosive = com.hbm.ntm.config.RadiationConfig.explosiveHazardDisabled();
            disableHydro = com.hbm.ntm.config.RadiationConfig.hydroactiveHazardDisabled();
            disableBlinding = com.hbm.ntm.config.RadiationConfig.blindingHazardDisabled();
            disableFibrosis = com.hbm.ntm.config.RadiationConfig.fibrosisHazardDisabled();
            enablePollution = com.hbm.ntm.config.RadiationConfig.pollutionEnabled();
            enableLeadFromBlocks = com.hbm.ntm.config.RadiationConfig.pollutionLeadFromBlocksEnabled();
            enableLeadPoisoning = com.hbm.ntm.config.RadiationConfig.pollutionLeadPoisoningEnabled();
            enableSootFog = com.hbm.ntm.config.RadiationConfig.pollutionSootFogEnabled();
            enablePoison = com.hbm.ntm.config.RadiationConfig.pollutionPoisonEnabled();
            buffMobThreshold = com.hbm.ntm.config.RadiationConfig.pollutionBuffMobThreshold();
            sootFogThreshold = com.hbm.ntm.config.RadiationConfig.pollutionSootFogThreshold();
            sootFogDivisor = com.hbm.ntm.config.RadiationConfig.pollutionSootFogDivisor();
            smokeStackSootMult = com.hbm.ntm.config.RadiationConfig.pollutionSmokeStackSootMultiplier();
        } catch (IllegalStateException ignored) {
            // Keep legacy defaults until Forge finishes loading the modern config.
        }
    }

    public static boolean enablePollution() {
        syncFromModern();
        return enablePollution;
    }

    public static boolean enableLeadFromBlocks() {
        syncFromModern();
        return enableLeadFromBlocks;
    }

    public static boolean enableLeadPoisoning() {
        syncFromModern();
        return enableLeadPoisoning;
    }

    public static boolean enableSootFog() {
        syncFromModern();
        return enableSootFog;
    }

    public static boolean enablePoison() {
        syncFromModern();
        return enablePoison;
    }

    public static float buffMobThreshold() {
        syncFromModern();
        return (float) buffMobThreshold;
    }

    public static float sootFogThreshold() {
        syncFromModern();
        return (float) sootFogThreshold;
    }

    public static float sootFogDivisor() {
        syncFromModern();
        return (float) sootFogDivisor;
    }

    public static double smokeStackSootMult() {
        syncFromModern();
        return smokeStackSootMult;
    }

    public static boolean enableContamination() {
        syncFromModern();
        return enableContamination;
    }

    public static boolean enableChunkRads() {
        syncFromModern();
        return enableChunkRads;
    }

    public static int fogRad() {
        syncFromModern();
        return fogRad;
    }

    public static int fogCh() {
        syncFromModern();
        return fogCh;
    }

    public static float hellRad() {
        syncFromModern();
        return (float) hellRad;
    }

    public static boolean worldRadEffects() {
        syncFromModern();
        return worldRadEffects;
    }

    public static int worldRad() {
        syncFromModern();
        return worldRad;
    }

    public static int worldRadThreshold() {
        syncFromModern();
        return worldRadThreshold;
    }

    public static boolean cleanupDeadDirt() {
        syncFromModern();
        return cleanupDeadDirt;
    }

    public static boolean disableAsbestos() {
        syncFromModern();
        return disableAsbestos;
    }

    public static boolean disableCoal() {
        syncFromModern();
        return disableCoal;
    }

    public static boolean disableHot() {
        syncFromModern();
        return disableHot;
    }

    public static boolean disableExplosive() {
        syncFromModern();
        return disableExplosive;
    }

    public static boolean disableHydro() {
        syncFromModern();
        return disableHydro;
    }

    public static boolean disableBlinding() {
        syncFromModern();
        return disableBlinding;
    }

    public static boolean disableFibrosis() {
        syncFromModern();
        return disableFibrosis;
    }

    public static void loadFromConfig(Object ignored) {
        syncFromModern();
    }

    private RadiationConfig() {
    }
}

package com.hbm.config;

/**
 * Legacy package facade for world config reads that have modern radiation consumers.
 */
@Deprecated(forRemoval = false)
public final class WorldConfig {
    public static boolean enableCraterBiomes() {
        return com.hbm.ntm.config.RadiationConfig.craterBiomeRadiationEnabled();
    }

    public static float craterBiomeRad() {
        return com.hbm.ntm.config.RadiationConfig.craterBiomeRadiation();
    }

    public static float craterBiomeInnerRad() {
        return com.hbm.ntm.config.RadiationConfig.craterBiomeInnerRadiation();
    }

    public static float craterBiomeOuterRad() {
        return com.hbm.ntm.config.RadiationConfig.craterBiomeOuterRadiation();
    }

    public static float craterBiomeWaterMult() {
        return com.hbm.ntm.config.RadiationConfig.craterBiomeWaterMultiplier();
    }

    public static boolean overworldOre() {
        return true;
    }

    public static boolean netherOre() {
        return true;
    }

    public static boolean endOre() {
        return true;
    }

    public static boolean newBedrockOres() {
        return true;
    }

    public static boolean enableMeteorStrikes() {
        return true;
    }

    public static boolean enableMeteorShowers() {
        return true;
    }

    public static boolean enableMeteorTails() {
        return true;
    }

    public static boolean enableSpecialMeteors() {
        return true;
    }

    public static void loadFromConfig(Object ignored) {
    }

    private WorldConfig() {
    }
}

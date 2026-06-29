package com.hbm.config;

/**
 * Legacy package facade for world config reads that have modern consumers.
 */
@Deprecated(forRemoval = false)
public final class WorldConfig {
    public static boolean overworldOre = true;
    public static boolean netherOre = true;
    public static boolean endOre = true;
    public static int uraniumSpawn = 6;
    public static int thoriumSpawn = 7;
    public static int titaniumSpawn = 8;
    public static int sulfurSpawn = 5;
    public static int aluminiumSpawn = 7;
    public static int copperSpawn = 12;
    public static int fluoriteSpawn = 6;
    public static int niterSpawn = 6;
    public static int tungstenSpawn = 10;
    public static int leadSpawn = 6;
    public static int berylliumSpawn = 6;
    public static int ligniteSpawn = 2;
    public static int asbestosSpawn = 4;
    public static int rareSpawn = 6;
    public static int lithiumSpawn = 6;
    public static int cinnebarSpawn = 1;
    public static int gassshaleSpawn = 5;
    public static int gasbubbleSpawn = 12;
    public static int explosivebubbleSpawn = 0;
    public static int cobaltSpawn = 2;
    public static int oilSpawn = 100;
    public static int bedrockOilSpawn = 200;
    public static boolean newBedrockOres = true;
    public static int bedrockIronSpawn = 100;
    public static int bedrockCopperSpawn = 200;
    public static int bedrockBoraxSpawn = 50;
    public static int bedrockChlorocalciteSpawn = 35;
    public static int bedrockAsbestosSpawn = 50;
    public static int bedrockNiobiumSpawn = 50;
    public static int bedrockNeodymiumSpawn = 50;
    public static int bedrockTitaniumSpawn = 100;
    public static int bedrockTungstenSpawn = 100;
    public static int bedrockGoldSpawn = 50;
    public static int bedrockUraniumSpawn = 35;
    public static int bedrockThoriumSpawn = 50;
    public static int bedrockCoalSpawn = 200;
    public static int bedrockNiterSpawn = 50;
    public static int bedrockFluoriteSpawn = 50;
    public static int bedrockRedstoneSpawn = 50;
    public static int bedrockRareEarthSpawn = 50;
    public static int bedrockBauxiteSpawn = 100;
    public static int bedrockEmeraldSpawn = 50;
    public static int bedrockGlowstoneSpawn = 100;
    public static int bedrockPhosphorusSpawn = 50;
    public static int bedrockQuartzSpawn = 100;
    public static int ironClusterSpawn = 4;
    public static int titaniumClusterSpawn = 2;
    public static int aluminiumClusterSpawn = 3;
    public static int copperClusterSpawn = 4;
    public static int alexandriteSpawn = 100;
    public static int limestoneSpawn = 1;
    public static int netherUraniumuSpawn = 8;
    public static int netherTungstenSpawn = 10;
    public static int netherSulfurSpawn = 26;
    public static int netherPhosphorusSpawn = 24;
    public static int netherCoalSpawn = 8;
    public static int netherPlutoniumSpawn = 8;
    public static int netherCobaltSpawn = 2;
    public static int endTikiteSpawn = 8;
    public static boolean enableHematite = true;
    public static boolean enableMalachite = true;
    public static boolean enableBauxite = true;
    public static boolean enableSulfurCave = true;
    public static boolean enableAsbestosCave = true;
    public static int antennaStructure = 250;
    public static int atomStructure = 500;
    public static int dungeonStructure = 64;
    public static int satelliteStructure = 500;
    public static int dudStructure = 500;
    public static int spaceshipStructure = 1000;
    public static int barrelStructure = 5000;
    public static int geyserChlorine = 3000;
    public static int geyserVapor = 250;
    public static int capsuleStructure = 100;
    public static int arcticStructure = 500;
    public static int jungleStructure = 2000;
    public static int pyramidStructure = 4000;
    public static int broadcaster = 5000;
    public static int minefreq = 64;
    public static int radfreq = 5000;
    public static int vaultfreq = 2500;
    public static boolean enableCraterBiomes = true;
    public static int craterBiomeId = 80;
    public static int craterBiomeInnerId = 81;
    public static int craterBiomeOuterId = 82;
    public static float craterBiomeRad = 5.0F;
    public static float craterBiomeInnerRad = 25.0F;
    public static float craterBiomeOuterRad = 0.5F;
    public static float craterBiomeWaterMult = 5.0F;

    static {
        syncFromModern();
    }

    public static void syncFromModern() {
        try {
            enableCraterBiomes = com.hbm.ntm.config.RadiationConfig.craterBiomeRadiationEnabled();
            craterBiomeRad = com.hbm.ntm.config.RadiationConfig.craterBiomeRadiation();
            craterBiomeInnerRad = com.hbm.ntm.config.RadiationConfig.craterBiomeInnerRadiation();
            craterBiomeOuterRad = com.hbm.ntm.config.RadiationConfig.craterBiomeOuterRadiation();
            craterBiomeWaterMult = com.hbm.ntm.config.RadiationConfig.craterBiomeWaterMultiplier();
        } catch (IllegalStateException ignored) {
            // Keep legacy defaults until Forge finishes loading the modern config.
        }
    }

    public static boolean enableCraterBiomes() {
        syncFromModern();
        return enableCraterBiomes;
    }

    public static float craterBiomeRad() {
        syncFromModern();
        return craterBiomeRad;
    }

    public static float craterBiomeInnerRad() {
        syncFromModern();
        return craterBiomeInnerRad;
    }

    public static float craterBiomeOuterRad() {
        syncFromModern();
        return craterBiomeOuterRad;
    }

    public static float craterBiomeWaterMult() {
        syncFromModern();
        return craterBiomeWaterMult;
    }

    public static boolean overworldOre() {
        return overworldOre;
    }

    public static boolean netherOre() {
        return netherOre;
    }

    public static boolean endOre() {
        return endOre;
    }

    public static boolean newBedrockOres() {
        return newBedrockOres;
    }

    public static void loadFromConfig(Object ignored) {
        syncFromModern();
    }

    private WorldConfig() {
    }
}

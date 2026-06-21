package com.hbm.config;

/**
 * Legacy package facade for nuclear explosion config reads.
 */
@Deprecated(forRemoval = false)
public final class BombConfig {
    public static int gadgetRadius = com.hbm.ntm.config.BombConfig.GADGET_RADIUS_DEFAULT;
    public static int boyRadius = com.hbm.ntm.config.BombConfig.BOY_RADIUS_DEFAULT;
    public static int manRadius = com.hbm.ntm.config.BombConfig.MAN_RADIUS_DEFAULT;
    public static int mikeRadius = com.hbm.ntm.config.BombConfig.MIKE_RADIUS_DEFAULT;
    public static int tsarRadius = com.hbm.ntm.config.BombConfig.TSAR_RADIUS_DEFAULT;
    public static int prototypeRadius = com.hbm.ntm.config.BombConfig.PROTOTYPE_RADIUS_DEFAULT;
    public static int fleijaRadius = com.hbm.ntm.config.BombConfig.FLEIJA_RADIUS_DEFAULT;
    public static int soliniumRadius = com.hbm.ntm.config.BombConfig.SOLINIUM_RADIUS_DEFAULT;
    public static int n2Radius = com.hbm.ntm.config.BombConfig.N2_RADIUS_DEFAULT;
    public static int missileRadius = com.hbm.ntm.config.BombConfig.MISSILE_RADIUS_DEFAULT;
    public static int mirvRadius = com.hbm.ntm.config.BombConfig.MIRV_RADIUS_DEFAULT;
    public static int fatmanRadius = com.hbm.ntm.config.BombConfig.FATMAN_RADIUS_DEFAULT;
    public static int nukaRadius = com.hbm.ntm.config.BombConfig.NUKA_RADIUS_DEFAULT;
    public static int aSchrabRadius = com.hbm.ntm.config.BombConfig.A_SCHRAB_RADIUS_DEFAULT;

    public static int mk5 = 50;
    public static int blastSpeed = 1024;
    public static int falloutRange = 100;
    public static int fDelay = 4;
    public static int limitExplosionLifespan = 0;
    public static boolean chunkloading = true;
    public static int explosionAlgorithm = 2;

    static {
        syncFromModern();
    }

    public static void syncFromModern() {
        try {
            gadgetRadius = com.hbm.ntm.config.BombConfig.gadgetRadius();
            boyRadius = com.hbm.ntm.config.BombConfig.boyRadius();
            manRadius = com.hbm.ntm.config.BombConfig.manRadius();
            mikeRadius = com.hbm.ntm.config.BombConfig.mikeRadius();
            tsarRadius = com.hbm.ntm.config.BombConfig.tsarRadius();
            prototypeRadius = com.hbm.ntm.config.BombConfig.prototypeRadius();
            fleijaRadius = com.hbm.ntm.config.BombConfig.fleijaRadius();
            soliniumRadius = com.hbm.ntm.config.BombConfig.soliniumRadius();
            n2Radius = com.hbm.ntm.config.BombConfig.n2Radius();
            missileRadius = com.hbm.ntm.config.BombConfig.missileRadius();
            mirvRadius = com.hbm.ntm.config.BombConfig.mirvRadius();
            fatmanRadius = com.hbm.ntm.config.BombConfig.fatmanRadius();
            nukaRadius = com.hbm.ntm.config.BombConfig.nukaRadius();
            aSchrabRadius = com.hbm.ntm.config.BombConfig.antiSchrabidiumRadius();
            mk5 = com.hbm.ntm.config.BombConfig.mk5BudgetMs();
            blastSpeed = com.hbm.ntm.config.BombConfig.blastSpeed();
            falloutRange = com.hbm.ntm.config.BombConfig.falloutRangePercent();
            fDelay = com.hbm.ntm.config.BombConfig.falloutDelayTicks();
            limitExplosionLifespan = com.hbm.ntm.config.BombConfig.explosionLifespanLimitSeconds();
            chunkloading = com.hbm.ntm.config.BombConfig.chunkLoadingEnabled();
            explosionAlgorithm = com.hbm.ntm.config.BombConfig.explosionAlgorithm();
        } catch (IllegalStateException ignored) {
            // Keep legacy defaults until Forge finishes loading the modern config.
        }
    }

    public static int gadgetRadius() {
        syncFromModern();
        return gadgetRadius;
    }

    public static int boyRadius() {
        syncFromModern();
        return boyRadius;
    }

    public static int manRadius() {
        syncFromModern();
        return manRadius;
    }

    public static int mikeRadius() {
        syncFromModern();
        return mikeRadius;
    }

    public static int tsarRadius() {
        syncFromModern();
        return tsarRadius;
    }

    public static int prototypeRadius() {
        syncFromModern();
        return prototypeRadius;
    }

    public static int fleijaRadius() {
        syncFromModern();
        return fleijaRadius;
    }

    public static int soliniumRadius() {
        syncFromModern();
        return soliniumRadius;
    }

    public static int n2Radius() {
        syncFromModern();
        return n2Radius;
    }

    public static int missileRadius() {
        syncFromModern();
        return missileRadius;
    }

    public static int mirvRadius() {
        syncFromModern();
        return mirvRadius;
    }

    public static int fatmanRadius() {
        syncFromModern();
        return fatmanRadius;
    }

    public static int nukaRadius() {
        syncFromModern();
        return nukaRadius;
    }

    public static int antiSchrabidiumRadius() {
        syncFromModern();
        return aSchrabRadius;
    }

    public static int mk5BudgetMs() {
        syncFromModern();
        return mk5;
    }

    public static int blastSpeed() {
        syncFromModern();
        return blastSpeed;
    }

    public static int falloutRangePercent() {
        syncFromModern();
        return falloutRange;
    }

    public static int falloutDelayTicks() {
        syncFromModern();
        return fDelay;
    }

    public static int explosionLifespanLimitSeconds() {
        syncFromModern();
        return limitExplosionLifespan;
    }

    public static boolean chunkLoadingEnabled() {
        syncFromModern();
        return chunkloading;
    }

    public static int explosionAlgorithm() {
        syncFromModern();
        return explosionAlgorithm;
    }

    public static void loadFromConfig(Object ignored) {
        syncFromModern();
    }

    private BombConfig() {
    }
}

package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BombConfig {
    public static final int GADGET_RADIUS_DEFAULT = 150;
    public static final int BOY_RADIUS_DEFAULT = 120;
    public static final int MAN_RADIUS_DEFAULT = 175;
    public static final int MIKE_RADIUS_DEFAULT = 250;
    public static final int TSAR_RADIUS_DEFAULT = 500;
    public static final int PROTOTYPE_RADIUS_DEFAULT = 150;
    public static final int FLEIJA_RADIUS_DEFAULT = 50;
    public static final int SOLINIUM_RADIUS_DEFAULT = 150;
    public static final int N2_RADIUS_DEFAULT = 200;
    public static final int MISSILE_RADIUS_DEFAULT = 100;
    public static final int MIRV_RADIUS_DEFAULT = 100;
    public static final int FATMAN_RADIUS_DEFAULT = 35;
    public static final int NUKA_RADIUS_DEFAULT = 25;
    public static final int A_SCHRAB_RADIUS_DEFAULT = 20;

    public static ForgeConfigSpec.IntValue GADGET_RADIUS;
    public static ForgeConfigSpec.IntValue BOY_RADIUS;
    public static ForgeConfigSpec.IntValue MAN_RADIUS;
    public static ForgeConfigSpec.IntValue MIKE_RADIUS;
    public static ForgeConfigSpec.IntValue TSAR_RADIUS;
    public static ForgeConfigSpec.IntValue PROTOTYPE_RADIUS;
    public static ForgeConfigSpec.IntValue FLEIJA_RADIUS;
    public static ForgeConfigSpec.IntValue SOLINIUM_RADIUS;
    public static ForgeConfigSpec.IntValue N2_RADIUS;
    public static ForgeConfigSpec.IntValue MISSILE_RADIUS;
    public static ForgeConfigSpec.IntValue MIRV_RADIUS;
    public static ForgeConfigSpec.IntValue FATMAN_RADIUS;
    public static ForgeConfigSpec.IntValue NUKA_RADIUS;
    public static ForgeConfigSpec.IntValue A_SCHRAB_RADIUS;

    public static ForgeConfigSpec.IntValue MK5_BUDGET_MS;
    public static ForgeConfigSpec.IntValue BLAST_SPEED;
    public static ForgeConfigSpec.IntValue FALLOUT_RANGE_PERCENT;
    public static ForgeConfigSpec.IntValue FALLOUT_DELAY;
    public static ForgeConfigSpec.IntValue LIMIT_EXPLOSION_LIFESPAN;
    public static ForgeConfigSpec.BooleanValue CHUNK_LOADING;
    public static ForgeConfigSpec.IntValue EXPLOSION_ALGORITHM;
    public static ForgeConfigSpec.IntValue LBSM_SCHRAB_ORE_RATE;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("nukes");
        GADGET_RADIUS = radius(builder, "gadgetRadius", GADGET_RADIUS_DEFAULT, "Legacy 3.00_gadgetRadius: radius of the Gadget.");
        BOY_RADIUS = radius(builder, "boyRadius", BOY_RADIUS_DEFAULT, "Legacy 3.01_boyRadius: radius of Little Boy.");
        MAN_RADIUS = radius(builder, "manRadius", MAN_RADIUS_DEFAULT, "Legacy 3.02_manRadius: radius of Fat Man.");
        MIKE_RADIUS = radius(builder, "mikeRadius", MIKE_RADIUS_DEFAULT, "Legacy 3.03_mikeRadius: radius of Ivy Mike.");
        TSAR_RADIUS = radius(builder, "tsarRadius", TSAR_RADIUS_DEFAULT, "Legacy 3.04_tsarRadius: radius of the Tsar Bomba.");
        PROTOTYPE_RADIUS = radius(builder, "prototypeRadius", PROTOTYPE_RADIUS_DEFAULT, "Legacy 3.05_prototypeRadius: radius of the Prototype.");
        FLEIJA_RADIUS = radius(builder, "fleijaRadius", FLEIJA_RADIUS_DEFAULT, "Legacy 3.06_fleijaRadius: radius of F.L.E.I.J.A.");
        MISSILE_RADIUS = radius(builder, "missileRadius", MISSILE_RADIUS_DEFAULT, "Legacy 3.07_missileRadius: radius of the nuclear missile.");
        MIRV_RADIUS = radius(builder, "mirvRadius", MIRV_RADIUS_DEFAULT, "Legacy 3.08_mirvRadius: radius of a MIRV.");
        FATMAN_RADIUS = radius(builder, "fatmanRadius", FATMAN_RADIUS_DEFAULT, "Legacy 3.09_fatmanRadius: radius of the Fatman launcher.");
        NUKA_RADIUS = radius(builder, "nukaRadius", NUKA_RADIUS_DEFAULT, "Legacy 3.10_nukaRadius: radius of the Nuka grenade.");
        A_SCHRAB_RADIUS = radius(builder, "aSchrabRadius", A_SCHRAB_RADIUS_DEFAULT, "Legacy 3.11_aSchrabRadius: radius of dropped anti-schrabidium.");
        SOLINIUM_RADIUS = radius(builder, "soliniumRadius", SOLINIUM_RADIUS_DEFAULT, "Legacy 3.12_soliniumRadius: radius of the blue rinse.");
        N2_RADIUS = radius(builder, "n2Radius", N2_RADIUS_DEFAULT, "Legacy 3.13_n2Radius: radius of the N2 mine.");
        builder.pop();

        builder.push("explosions");
        LIMIT_EXPLOSION_LIFESPAN = builder
                .comment("Legacy 6.00_limitExplosionLifespan: seconds an unloaded procedural explosion can live. 0 disables the effect.")
                .defineInRange("limitExplosionLifespan", 0, 0, Integer.MAX_VALUE);
        BLAST_SPEED = builder
                .comment("Legacy 6.01_blastSpeed: base speed of MK3 system detonations in blocks per tick.")
                .defineInRange("blastSpeed", 1024, 1, Integer.MAX_VALUE);
        MK5_BUDGET_MS = builder
                .comment("Legacy 6.02_mk5BlastTime: minimum milliseconds per tick allocated for MK5 chunk processing.")
                .defineInRange("mk5BlastTime", 50, 1, 1000);
        FALLOUT_RANGE_PERCENT = builder
                .comment("Legacy 6.03_falloutRange: radius of fallout area as percent of base radius.")
                .defineInRange("falloutRange", 100, 0, 1000);
        FALLOUT_DELAY = builder
                .comment("Legacy 6.04_falloutDelay: ticks to wait before the next fallout chunk computation.")
                .defineInRange("falloutDelay", 4, 0, Integer.MAX_VALUE);
        CHUNK_LOADING = builder
                .comment("Legacy 6.05_enableChunkLoading: allows procedural nuclear explosions to keep their work chunks loaded.")
                .define("enableChunkLoading", true);
        EXPLOSION_ALGORITHM = builder
                .comment("Legacy 6.06_explosionAlgorithm: 0 legacy, 1 threaded DDA, 2 threaded DDA with damage accumulation. Modern port safely delegates threaded mode to the batched worker.")
                .defineInRange("explosionAlgorithm", 2, 0, 2);
        builder.pop();

        builder.push("lessBullshitMode");
        LBSM_SCHRAB_ORE_RATE = builder
                .comment("Legacy LBSM_schrabOreRate: average uranium ore blocks needed to create one schrabidium ore through nuclear waste mutation while Less Bullshit Mode is enabled.")
                .defineInRange("schrabOreRate", 20, 1, Integer.MAX_VALUE);
        builder.pop();
    }

    private static ForgeConfigSpec.IntValue radius(ForgeConfigSpec.Builder builder, String name, int defaultValue, String comment) {
        return builder.comment(comment).defineInRange(name, defaultValue, 0, Integer.MAX_VALUE);
    }

    private BombConfig() {
    }
}

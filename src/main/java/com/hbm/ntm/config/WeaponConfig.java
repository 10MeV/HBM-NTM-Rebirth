package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WeaponConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_GUNS;
    public static ForgeConfigSpec.BooleanValue DROP_ANTIMATTER_CELLS;
    public static ForgeConfigSpec.BooleanValue DROP_SINGULARITY;
    public static ForgeConfigSpec.BooleanValue DROP_XEN_CRYSTALS;
    public static ForgeConfigSpec.IntValue CIWS_HITRATE;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("guns");
        ENABLE_GUNS = builder
                .comment("Legacy GeneralConfig 1.20_enableGuns: allows new-system guns to fire.")
                .define("enableGuns", true);
        builder.pop();

        builder.push("drops");
        DROP_ANTIMATTER_CELLS = builder
                .comment("Legacy 10.00_dropCell: whether antimatter cells and clusters should explode when dropped.")
                .define("dropAntimatterCells", true);
        DROP_SINGULARITY = builder
                .comment("Legacy 10.01_dropBHole: whether singularities and black holes should spawn when dropped.")
                .define("dropBlackHoleSingularities", true);
        DROP_XEN_CRYSTALS = builder
                .comment("Legacy 10.04_dropCrys: whether artificial xen crystals should move nearby blocks when dropped.")
                .define("dropXenCrystals", true);
        builder.pop();

        builder.push("turrets");
        CIWS_HITRATE = builder
                .comment("Legacy ciwsHitrate: percent chance for each Howard CIWS damage pulse to hit.")
                .defineInRange("ciwsHitrate", 50, 0, 100);
        builder.pop();
    }

    public static boolean gunsEnabled() {
        return booleanValue(ENABLE_GUNS, true);
    }

    public static boolean droppedAntimatterCellsEnabled() {
        return booleanValue(DROP_ANTIMATTER_CELLS, true);
    }

    public static boolean droppedSingularitiesEnabled() {
        return booleanValue(DROP_SINGULARITY, true);
    }

    public static boolean droppedXenCrystalsEnabled() {
        return booleanValue(DROP_XEN_CRYSTALS, true);
    }

    public static int ciwsHitrate() {
        try {
            return CIWS_HITRATE == null ? 50 : CIWS_HITRATE.get();
        } catch (IllegalStateException ignored) {
            return 50;
        }
    }

    private static boolean booleanValue(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private WeaponConfig() {
    }
}

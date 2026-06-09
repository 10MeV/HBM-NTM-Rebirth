package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WeaponConfig {
    public static ForgeConfigSpec.BooleanValue DROP_ANTIMATTER_CELLS;
    public static ForgeConfigSpec.BooleanValue DROP_SINGULARITY;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("drops");
        DROP_ANTIMATTER_CELLS = builder
                .comment("Legacy 10.00_dropCell: whether antimatter cells and clusters should explode when dropped.")
                .define("dropAntimatterCells", true);
        DROP_SINGULARITY = builder
                .comment("Legacy 10.01_dropBHole: whether singularities and black holes should spawn when dropped.")
                .define("dropBlackHoleSingularities", true);
        builder.pop();
    }

    public static boolean droppedAntimatterCellsEnabled() {
        return booleanValue(DROP_ANTIMATTER_CELLS, true);
    }

    public static boolean droppedSingularitiesEnabled() {
        return booleanValue(DROP_SINGULARITY, true);
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

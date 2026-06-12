package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ServerConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_MKU;
    public static ForgeConfigSpec.IntValue ITEM_HAZARD_DROP_TICKRATE;
    public static ForgeConfigSpec.BooleanValue TAINT_TRAILS;
    public static ForgeConfigSpec.IntValue AUTOCAL_MAX_CLOCK;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("server");
        ENABLE_MKU = builder
                .comment("Legacy ServerConfig.ENABLE_MKU: toggles MKU contagion ticking and infection behavior.")
                .define("enableMku", true);
        ITEM_HAZARD_DROP_TICKRATE = builder
                .comment("Legacy ServerConfig.ITEM_HAZARD_DROP_TICKRATE: tick interval for dropped item hazard updates. Clamped to at least 1 tick.")
                .defineInRange("itemHazardDropTickrate", 2, 1, 20 * 60);
        TAINT_TRAILS = builder
                .comment("Legacy ServerConfig.TAINT_TRAILS: tainted living entities leave level 14 taint on solid blocks underfoot.")
                .define("taintTrails", false);
        AUTOCAL_MAX_CLOCK = builder
                .comment("Legacy ServerConfig.AUTOCAL_MAX_CLOCK: maximum AUTOCAL MS-ES1 clockspeed command value, in script lines per tick.")
                .defineInRange("autocalMaxClockSpeed", 20, 1, 100);
        builder.pop();
    }

    public static int droppedItemHazardTickRate() {
        return ITEM_HAZARD_DROP_TICKRATE == null ? 2 : Math.max(1, ITEM_HAZARD_DROP_TICKRATE.get());
    }

    public static boolean mkuEnabled() {
        return booleanValue(ENABLE_MKU, true);
    }

    public static boolean taintTrailsEnabled() {
        return booleanValue(TAINT_TRAILS, false);
    }

    public static int autocalMaxClockSpeed() {
        return intValue(AUTOCAL_MAX_CLOCK, 20, 1);
    }

    private static boolean booleanValue(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static int intValue(ForgeConfigSpec.IntValue value, int fallback, int min) {
        try {
            return value == null ? fallback : Math.max(min, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private ServerConfig() {
    }
}

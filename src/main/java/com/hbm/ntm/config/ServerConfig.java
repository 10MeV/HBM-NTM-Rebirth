package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ServerConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_MKU;
    public static ForgeConfigSpec.IntValue ITEM_HAZARD_DROP_TICKRATE;
    public static ForgeConfigSpec.BooleanValue TAINT_TRAILS;
    public static ForgeConfigSpec.BooleanValue CRATE_OPEN_HELD;
    public static ForgeConfigSpec.BooleanValue CRATE_KEEP_CONTENTS;
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
        CRATE_OPEN_HELD = builder
                .comment("Legacy ServerConfig.CRATE_OPEN_HELD: a single held storage crate or safe opens before it can be placed unless the player sneaks. Mass storage is intentionally excluded, as in 1.7.10.")
                .define("crateOpenHeld", true);
        CRATE_KEEP_CONTENTS = builder
                .comment("Legacy ServerConfig.CRATE_KEEP_CONTENTS: mined unlocked storage crates and mass storage keep their inventory in the dropped block item instead of ejecting it.")
                .define("crateKeepContents", true);
        AUTOCAL_MAX_CLOCK = builder
                .comment("Legacy ServerConfig.AUTOCAL_MAX_CLOCK: maximum AUTOCAL MS-ES1 clockspeed command value, in script lines per tick.")
                .defineInRange("autocalMaxClockSpeed", 20, 1, 100);
        builder.pop();
    }

    public static int droppedItemHazardTickRate() {
        return intValue(ITEM_HAZARD_DROP_TICKRATE, 2, 1);
    }

    public static boolean mkuEnabled() {
        return booleanValue(ENABLE_MKU, true);
    }

    public static boolean taintTrailsEnabled() {
        return booleanValue(TAINT_TRAILS, false);
    }

    public static boolean crateOpenHeldEnabled() {
        return booleanValue(CRATE_OPEN_HELD, true);
    }

    public static boolean crateKeepContentsEnabled() {
        return booleanValue(CRATE_KEEP_CONTENTS, true);
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

package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ServerConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_MKU;
    public static ForgeConfigSpec.IntValue ITEM_HAZARD_DROP_TICKRATE;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("server");
        ENABLE_MKU = builder
                .comment("Legacy ServerConfig.ENABLE_MKU: toggles MKU contagion ticking and infection behavior.")
                .define("enableMku", true);
        ITEM_HAZARD_DROP_TICKRATE = builder
                .comment("Legacy ServerConfig.ITEM_HAZARD_DROP_TICKRATE: tick interval for dropped item hazard updates. Clamped to at least 1 tick.")
                .defineInRange("itemHazardDropTickrate", 2, 1, 20 * 60);
        builder.pop();
    }

    public static int droppedItemHazardTickRate() {
        return ITEM_HAZARD_DROP_TICKRATE == null ? 2 : Math.max(1, ITEM_HAZARD_DROP_TICKRATE.get());
    }

    public static boolean mkuEnabled() {
        return booleanValue(ENABLE_MKU, true);
    }

    private static boolean booleanValue(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private ServerConfig() {
    }
}

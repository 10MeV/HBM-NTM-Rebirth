package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SteamEngineConfig {
    public static final int STEAM_CAPACITY_DEFAULT = 2_000;
    public static final int SPENT_STEAM_CAPACITY_DEFAULT = 20;
    public static final double EFFICIENCY_DEFAULT = 0.85D;

    public static ForgeConfigSpec.IntValue STEAM_CAPACITY;
    public static ForgeConfigSpec.IntValue SPENT_STEAM_CAPACITY;
    public static ForgeConfigSpec.DoubleValue EFFICIENCY;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("steamengine");
        STEAM_CAPACITY = positiveInt(builder, "steamCap", STEAM_CAPACITY_DEFAULT,
                "Legacy steamengine I:steamCap: steam tank capacity in millibuckets.");
        SPENT_STEAM_CAPACITY = positiveInt(builder, "ldsCap", SPENT_STEAM_CAPACITY_DEFAULT,
                "Legacy steamengine I:ldsCap: spent steam tank capacity in millibuckets.");
        EFFICIENCY = builder
                .comment("Legacy steamengine D:efficiency: multiplier applied to turbine power output.")
                .defineInRange("efficiency", EFFICIENCY_DEFAULT, 0.000001D, Double.MAX_VALUE);
        builder.pop();
        builder.pop();
    }

    public static int steamCapacity() {
        return positiveInt(STEAM_CAPACITY, STEAM_CAPACITY_DEFAULT);
    }

    public static int spentSteamCapacity() {
        return positiveInt(SPENT_STEAM_CAPACITY, SPENT_STEAM_CAPACITY_DEFAULT);
    }

    public static double efficiency() {
        try {
            return EFFICIENCY == null ? EFFICIENCY_DEFAULT : Math.max(0.000001D, EFFICIENCY.get());
        } catch (IllegalStateException ignored) {
            return EFFICIENCY_DEFAULT;
        }
    }

    private static ForgeConfigSpec.IntValue positiveInt(ForgeConfigSpec.Builder builder, String name, int fallback,
            String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 1, Integer.MAX_VALUE);
    }

    private static int positiveInt(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value == null ? fallback : Math.max(1, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private SteamEngineConfig() {
    }
}

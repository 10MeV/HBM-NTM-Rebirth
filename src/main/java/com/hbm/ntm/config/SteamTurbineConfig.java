package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SteamTurbineConfig {
    public static final long STEAM_TURBINE_MAX_POWER_DEFAULT = 1_000_000L;
    public static final int STEAM_TURBINE_INPUT_TANK_SIZE_DEFAULT = 64_000;
    public static final int STEAM_TURBINE_OUTPUT_TANK_SIZE_DEFAULT = 128_000;
    public static final int STEAM_TURBINE_MAX_STEAM_PER_TICK_DEFAULT = 6_000;
    public static final double STEAM_TURBINE_EFFICIENCY_DEFAULT = 0.85D;
    public static final long LARGE_TURBINE_MAX_POWER_DEFAULT = 100_000_000L;
    public static final int LARGE_TURBINE_INPUT_TANK_SIZE_DEFAULT = 512_000;
    public static final int LARGE_TURBINE_OUTPUT_TANK_SIZE_DEFAULT = 10_240_000;
    public static final double LARGE_TURBINE_EFFICIENCY_DEFAULT = 1.0D;
    public static final int INDUSTRIAL_INPUT_TANK_SIZE_DEFAULT = 750_000;
    public static final int INDUSTRIAL_OUTPUT_TANK_SIZE_DEFAULT = 3_000_000;
    public static final double INDUSTRIAL_EFFICIENCY_DEFAULT = 1.0D;

    public static ForgeConfigSpec.LongValue STEAM_TURBINE_MAX_POWER;
    public static ForgeConfigSpec.IntValue STEAM_TURBINE_INPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue STEAM_TURBINE_OUTPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue STEAM_TURBINE_MAX_STEAM_PER_TICK;
    public static ForgeConfigSpec.DoubleValue STEAM_TURBINE_EFFICIENCY;
    public static ForgeConfigSpec.LongValue LARGE_TURBINE_MAX_POWER;
    public static ForgeConfigSpec.IntValue LARGE_TURBINE_INPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue LARGE_TURBINE_OUTPUT_TANK_SIZE;
    public static ForgeConfigSpec.DoubleValue LARGE_TURBINE_EFFICIENCY;
    public static ForgeConfigSpec.IntValue INDUSTRIAL_INPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue INDUSTRIAL_OUTPUT_TANK_SIZE;
    public static ForgeConfigSpec.DoubleValue INDUSTRIAL_EFFICIENCY;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("steamturbine");
        STEAM_TURBINE_MAX_POWER = positiveLong(builder, "maxPower", STEAM_TURBINE_MAX_POWER_DEFAULT,
                "Legacy steamturbine L:maxPower: internal HE storage capacity.");
        STEAM_TURBINE_INPUT_TANK_SIZE = positiveInt(builder, "inputTankSize",
                STEAM_TURBINE_INPUT_TANK_SIZE_DEFAULT,
                "Legacy steamturbine I:inputTankSize: steam input tank capacity in millibuckets.");
        STEAM_TURBINE_OUTPUT_TANK_SIZE = positiveInt(builder, "outputTankSize",
                STEAM_TURBINE_OUTPUT_TANK_SIZE_DEFAULT,
                "Legacy steamturbine I:outputTankSize: spent steam output tank capacity in millibuckets.");
        STEAM_TURBINE_MAX_STEAM_PER_TICK = positiveInt(builder, "maxSteamPerTick",
                STEAM_TURBINE_MAX_STEAM_PER_TICK_DEFAULT,
                "Legacy steamturbine I:maxSteamPerTick: maximum steam consumed per tick.");
        STEAM_TURBINE_EFFICIENCY = positiveDouble(builder, "efficiency", STEAM_TURBINE_EFFICIENCY_DEFAULT,
                "Legacy steamturbine D:efficiency: multiplier applied to turbine power output.");
        builder.pop();

        builder.push("largeTurbineLegacy");
        LARGE_TURBINE_MAX_POWER = positiveLong(builder, "maxPower", LARGE_TURBINE_MAX_POWER_DEFAULT,
                "Legacy machine_large_turbine maxPower: internal HE storage capacity.");
        LARGE_TURBINE_INPUT_TANK_SIZE = positiveInt(builder, "inputTankSize",
                LARGE_TURBINE_INPUT_TANK_SIZE_DEFAULT,
                "Legacy machine_large_turbine inputTankSize: steam input tank capacity in millibuckets.");
        LARGE_TURBINE_OUTPUT_TANK_SIZE = positiveInt(builder, "outputTankSize",
                LARGE_TURBINE_OUTPUT_TANK_SIZE_DEFAULT,
                "Legacy machine_large_turbine outputTankSize: spent steam output tank capacity in millibuckets.");
        LARGE_TURBINE_EFFICIENCY = positiveDouble(builder, "efficiency", LARGE_TURBINE_EFFICIENCY_DEFAULT,
                "Legacy machine_large_turbine efficiency: multiplier applied to turbine power output.");
        builder.pop();

        builder.push("steamturbineIndustrialMk2");
        INDUSTRIAL_INPUT_TANK_SIZE = positiveInt(builder, "inputTankSize", INDUSTRIAL_INPUT_TANK_SIZE_DEFAULT,
                "Legacy steamturbineIndustrialMk2 I:inputTankSize: steam input tank capacity in millibuckets.");
        INDUSTRIAL_OUTPUT_TANK_SIZE = positiveInt(builder, "outputTankSize", INDUSTRIAL_OUTPUT_TANK_SIZE_DEFAULT,
                "Legacy steamturbineIndustrialMk2 I:outputTankSize: spent steam output tank capacity in millibuckets.");
        INDUSTRIAL_EFFICIENCY = positiveDouble(builder, "efficiency", INDUSTRIAL_EFFICIENCY_DEFAULT,
                "Legacy steamturbineIndustrialMk2 D:efficiency: multiplier applied to turbine power output.");
        builder.pop();
        builder.pop();
    }

    public static long steamTurbineMaxPower() {
        return positiveLong(STEAM_TURBINE_MAX_POWER, STEAM_TURBINE_MAX_POWER_DEFAULT);
    }

    public static int steamTurbineInputTankSize() {
        return positiveInt(STEAM_TURBINE_INPUT_TANK_SIZE, STEAM_TURBINE_INPUT_TANK_SIZE_DEFAULT);
    }

    public static int steamTurbineOutputTankSize() {
        return positiveInt(STEAM_TURBINE_OUTPUT_TANK_SIZE, STEAM_TURBINE_OUTPUT_TANK_SIZE_DEFAULT);
    }

    public static int steamTurbineMaxSteamPerTick() {
        return positiveInt(STEAM_TURBINE_MAX_STEAM_PER_TICK, STEAM_TURBINE_MAX_STEAM_PER_TICK_DEFAULT);
    }

    public static double steamTurbineEfficiency() {
        return positiveDouble(STEAM_TURBINE_EFFICIENCY, STEAM_TURBINE_EFFICIENCY_DEFAULT);
    }

    public static long largeTurbineMaxPower() {
        return positiveLong(LARGE_TURBINE_MAX_POWER, LARGE_TURBINE_MAX_POWER_DEFAULT);
    }

    public static int largeTurbineInputTankSize() {
        return positiveInt(LARGE_TURBINE_INPUT_TANK_SIZE, LARGE_TURBINE_INPUT_TANK_SIZE_DEFAULT);
    }

    public static int largeTurbineOutputTankSize() {
        return positiveInt(LARGE_TURBINE_OUTPUT_TANK_SIZE, LARGE_TURBINE_OUTPUT_TANK_SIZE_DEFAULT);
    }

    public static double largeTurbineEfficiency() {
        return positiveDouble(LARGE_TURBINE_EFFICIENCY, LARGE_TURBINE_EFFICIENCY_DEFAULT);
    }

    public static int industrialInputTankSize() {
        return positiveInt(INDUSTRIAL_INPUT_TANK_SIZE, INDUSTRIAL_INPUT_TANK_SIZE_DEFAULT);
    }

    public static int industrialOutputTankSize() {
        return positiveInt(INDUSTRIAL_OUTPUT_TANK_SIZE, INDUSTRIAL_OUTPUT_TANK_SIZE_DEFAULT);
    }

    public static double industrialEfficiency() {
        return positiveDouble(INDUSTRIAL_EFFICIENCY, INDUSTRIAL_EFFICIENCY_DEFAULT);
    }

    private static ForgeConfigSpec.IntValue positiveInt(ForgeConfigSpec.Builder builder, String name, int fallback,
            String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 1, Integer.MAX_VALUE);
    }

    private static ForgeConfigSpec.LongValue positiveLong(ForgeConfigSpec.Builder builder, String name, long fallback,
            String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 1L, Long.MAX_VALUE);
    }

    private static ForgeConfigSpec.DoubleValue positiveDouble(ForgeConfigSpec.Builder builder, String name,
            double fallback, String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 0.000001D, Double.MAX_VALUE);
    }

    private static int positiveInt(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value == null ? fallback : Math.max(1, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static long positiveLong(ForgeConfigSpec.LongValue value, long fallback) {
        try {
            return value == null ? fallback : Math.max(1L, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static double positiveDouble(ForgeConfigSpec.DoubleValue value, double fallback) {
        try {
            return value == null ? fallback : Math.max(0.000001D, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private SteamTurbineConfig() {
    }
}

package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class PoweredCondenserConfig {
    public static final long MAX_POWER_DEFAULT = 10_000_000L;
    public static final int INPUT_TANK_SIZE_DEFAULT = 1_000_000;
    public static final int OUTPUT_TANK_SIZE_DEFAULT = 1_000_000;
    public static final int POWER_CONSUMPTION_DEFAULT = 10;

    public static ForgeConfigSpec.LongValue MAX_POWER;
    public static ForgeConfigSpec.IntValue INPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue OUTPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue POWER_CONSUMPTION;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("condenserPowered");
        MAX_POWER = builder
                .comment("Legacy condenserPowered L:maxPower: internal HE storage capacity.")
                .defineInRange("maxPower", MAX_POWER_DEFAULT, 1L, Long.MAX_VALUE);
        INPUT_TANK_SIZE = positiveInt(builder, "inputTankSize", INPUT_TANK_SIZE_DEFAULT,
                "Legacy condenserPowered I:inputTankSize: spent steam input tank capacity in millibuckets.");
        OUTPUT_TANK_SIZE = positiveInt(builder, "outputTankSize", OUTPUT_TANK_SIZE_DEFAULT,
                "Legacy condenserPowered I:outputTankSize: water output tank capacity in millibuckets.");
        POWER_CONSUMPTION = positiveInt(builder, "powerConsumption", POWER_CONSUMPTION_DEFAULT,
                "Legacy condenserPowered I:powerConsumption: HE consumed per converted millibucket.");
        builder.pop();
        builder.pop();
    }

    public static long maxPower() {
        try {
            return MAX_POWER == null ? MAX_POWER_DEFAULT : Math.max(1L, MAX_POWER.get());
        } catch (IllegalStateException ignored) {
            return MAX_POWER_DEFAULT;
        }
    }

    public static int inputTankSize() {
        return positiveInt(INPUT_TANK_SIZE, INPUT_TANK_SIZE_DEFAULT);
    }

    public static int outputTankSize() {
        return positiveInt(OUTPUT_TANK_SIZE, OUTPUT_TANK_SIZE_DEFAULT);
    }

    public static int powerConsumption() {
        return positiveInt(POWER_CONSUMPTION, POWER_CONSUMPTION_DEFAULT);
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

    private PoweredCondenserConfig() {
    }
}

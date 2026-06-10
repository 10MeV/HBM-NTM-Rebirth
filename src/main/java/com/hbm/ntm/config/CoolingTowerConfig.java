package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CoolingTowerConfig {
    public static final int SMALL_INPUT_TANK_SIZE_DEFAULT = 1_000;
    public static final int SMALL_OUTPUT_TANK_SIZE_DEFAULT = 1_000;
    public static final int LARGE_INPUT_TANK_SIZE_DEFAULT = 10_000;
    public static final int LARGE_OUTPUT_TANK_SIZE_DEFAULT = 10_000;

    public static ForgeConfigSpec.IntValue SMALL_INPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue SMALL_OUTPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue LARGE_INPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue LARGE_OUTPUT_TANK_SIZE;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("condenserTowerSmall");
        SMALL_INPUT_TANK_SIZE = positiveInt(builder, "inputTankSize", SMALL_INPUT_TANK_SIZE_DEFAULT,
                "Legacy condenserTowerSmall I:inputTankSize: spent steam input tank capacity in millibuckets.");
        SMALL_OUTPUT_TANK_SIZE = positiveInt(builder, "outputTankSize", SMALL_OUTPUT_TANK_SIZE_DEFAULT,
                "Legacy condenserTowerSmall I:outputTankSize: water output tank capacity in millibuckets.");
        builder.pop();

        builder.push("condenserTowerLarge");
        LARGE_INPUT_TANK_SIZE = positiveInt(builder, "inputTankSize", LARGE_INPUT_TANK_SIZE_DEFAULT,
                "Legacy condenserTowerLarge I:inputTankSize: spent steam input tank capacity in millibuckets.");
        LARGE_OUTPUT_TANK_SIZE = positiveInt(builder, "outputTankSize", LARGE_OUTPUT_TANK_SIZE_DEFAULT,
                "Legacy condenserTowerLarge I:outputTankSize: water output tank capacity in millibuckets.");
        builder.pop();
        builder.pop();
    }

    public static int smallInputTankSize() {
        return positiveInt(SMALL_INPUT_TANK_SIZE, SMALL_INPUT_TANK_SIZE_DEFAULT);
    }

    public static int smallOutputTankSize() {
        return positiveInt(SMALL_OUTPUT_TANK_SIZE, SMALL_OUTPUT_TANK_SIZE_DEFAULT);
    }

    public static int largeInputTankSize() {
        return positiveInt(LARGE_INPUT_TANK_SIZE, LARGE_INPUT_TANK_SIZE_DEFAULT);
    }

    public static int largeOutputTankSize() {
        return positiveInt(LARGE_OUTPUT_TANK_SIZE, LARGE_OUTPUT_TANK_SIZE_DEFAULT);
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

    private CoolingTowerConfig() {
    }
}

package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CondenserConfig {
    public static final int INPUT_TANK_SIZE_DEFAULT = 100;
    public static final int OUTPUT_TANK_SIZE_DEFAULT = 100;

    public static ForgeConfigSpec.IntValue INPUT_TANK_SIZE;
    public static ForgeConfigSpec.IntValue OUTPUT_TANK_SIZE;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("condenser");
        INPUT_TANK_SIZE = positiveInt(builder, "inputTankSize", INPUT_TANK_SIZE_DEFAULT,
                "Legacy condenser I:inputTankSize: spent steam input tank capacity in millibuckets.");
        OUTPUT_TANK_SIZE = positiveInt(builder, "outputTankSize", OUTPUT_TANK_SIZE_DEFAULT,
                "Legacy condenser I:outputTankSize: water output tank capacity in millibuckets.");
        builder.pop();
        builder.pop();
    }

    public static int inputTankSize() {
        return positiveInt(INPUT_TANK_SIZE, INPUT_TANK_SIZE_DEFAULT);
    }

    public static int outputTankSize() {
        return positiveInt(OUTPUT_TANK_SIZE, OUTPUT_TANK_SIZE_DEFAULT);
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

    private CondenserConfig() {
    }
}

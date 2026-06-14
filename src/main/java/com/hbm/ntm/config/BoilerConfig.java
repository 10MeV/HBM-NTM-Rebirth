package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BoilerConfig {
    public static final int MAX_HEAT_DEFAULT = 3_200_000;
    public static final int INDUSTRIAL_MAX_HEAT_DEFAULT = 12_800_000;
    public static final double DIFFUSION_DEFAULT = 0.1D;
    public static final boolean CAN_EXPLODE_DEFAULT = true;

    public static ForgeConfigSpec.IntValue MAX_HEAT;
    public static ForgeConfigSpec.IntValue INDUSTRIAL_MAX_HEAT;
    public static ForgeConfigSpec.DoubleValue DIFFUSION;
    public static ForgeConfigSpec.BooleanValue CAN_EXPLODE;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("boiler");
        MAX_HEAT = builder
                .comment("Legacy boiler I:maxHeat: maximum stored heat for the basic heat boiler.")
                .defineInRange("maxHeat", MAX_HEAT_DEFAULT, 1, Integer.MAX_VALUE);
        INDUSTRIAL_MAX_HEAT = builder
                .comment("Legacy boilerIndustrial I:maxHeat: maximum stored heat for the industrial heat boiler.")
                .defineInRange("industrialMaxHeat", INDUSTRIAL_MAX_HEAT_DEFAULT, 1, Integer.MAX_VALUE);
        DIFFUSION = builder
                .comment("Legacy boiler D:diffusion: fraction of positive heat difference pulled from the heat source below.")
                .defineInRange("diffusion", DIFFUSION_DEFAULT, 0.0D, 1.0D);
        CAN_EXPLODE = builder
                .comment("Legacy boiler B:canExplode: whether the basic heat boiler bursts when its output is full.")
                .define("canExplode", CAN_EXPLODE_DEFAULT);
        builder.pop();
        builder.pop();
    }

    public static int maxHeat() {
        return maxHeat(false);
    }

    public static int maxHeat(boolean industrial) {
        try {
            if (industrial) {
                return INDUSTRIAL_MAX_HEAT == null
                        ? INDUSTRIAL_MAX_HEAT_DEFAULT
                        : Math.max(1, INDUSTRIAL_MAX_HEAT.get());
            }
            return MAX_HEAT == null ? MAX_HEAT_DEFAULT : Math.max(1, MAX_HEAT.get());
        } catch (IllegalStateException ignored) {
            return industrial ? INDUSTRIAL_MAX_HEAT_DEFAULT : MAX_HEAT_DEFAULT;
        }
    }

    public static double diffusion() {
        try {
            return DIFFUSION == null ? DIFFUSION_DEFAULT : Math.max(0.0D, Math.min(1.0D, DIFFUSION.get()));
        } catch (IllegalStateException ignored) {
            return DIFFUSION_DEFAULT;
        }
    }

    public static boolean canExplode() {
        try {
            return CAN_EXPLODE == null ? CAN_EXPLODE_DEFAULT : CAN_EXPLODE.get();
        } catch (IllegalStateException ignored) {
            return CAN_EXPLODE_DEFAULT;
        }
    }

    private BoilerConfig() {
    }
}

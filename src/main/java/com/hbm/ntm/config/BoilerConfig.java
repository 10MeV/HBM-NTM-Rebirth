package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BoilerConfig {
    public static final int MAX_HEAT_DEFAULT = 3_200_000;
    public static final double DIFFUSION_DEFAULT = 0.1D;

    public static ForgeConfigSpec.IntValue MAX_HEAT;
    public static ForgeConfigSpec.DoubleValue DIFFUSION;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("boiler");
        MAX_HEAT = builder
                .comment("Legacy boiler I:maxHeat: maximum stored heat for the basic heat boiler.")
                .defineInRange("maxHeat", MAX_HEAT_DEFAULT, 1, Integer.MAX_VALUE);
        DIFFUSION = builder
                .comment("Legacy boiler D:diffusion: fraction of positive heat difference pulled from the heat source below.")
                .defineInRange("diffusion", DIFFUSION_DEFAULT, 0.0D, 1.0D);
        builder.pop();
        builder.pop();
    }

    public static int maxHeat() {
        try {
            return MAX_HEAT == null ? MAX_HEAT_DEFAULT : Math.max(1, MAX_HEAT.get());
        } catch (IllegalStateException ignored) {
            return MAX_HEAT_DEFAULT;
        }
    }

    public static double diffusion() {
        try {
            return DIFFUSION == null ? DIFFUSION_DEFAULT : Math.max(0.0D, Math.min(1.0D, DIFFUSION.get()));
        } catch (IllegalStateException ignored) {
            return DIFFUSION_DEFAULT;
        }
    }

    private BoilerConfig() {
    }
}

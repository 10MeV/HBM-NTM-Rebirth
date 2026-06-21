package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RtgConfig {
    public static final boolean DO_RTGS_DECAY_DEFAULT = true;
    public static final boolean SCALE_RTG_POWER_DEFAULT = false;

    public static ForgeConfigSpec.BooleanValue DO_RTGS_DECAY;
    public static ForgeConfigSpec.BooleanValue SCALE_RTG_POWER;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("rtg");
        DO_RTGS_DECAY = builder
                .comment("Legacy MachineConfig 9.01_doRTGsDecay: whether RTG and betavoltaic fuel decays while used.")
                .define("doRtgsDecay", DO_RTGS_DECAY_DEFAULT);
        SCALE_RTG_POWER = builder
                .comment("Legacy MachineConfig 9.00_scaleRTGPower: whether RTG and betavoltaic fuel output scales down as it decays.")
                .define("scaleRtgPower", SCALE_RTG_POWER_DEFAULT);
        builder.pop();
        builder.pop();
    }

    public static boolean doRtgsDecay() {
        return booleanValue(DO_RTGS_DECAY, DO_RTGS_DECAY_DEFAULT);
    }

    public static boolean scaleRtgPower() {
        return booleanValue(SCALE_RTG_POWER, SCALE_RTG_POWER_DEFAULT);
    }

    private static boolean booleanValue(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private RtgConfig() {
    }
}

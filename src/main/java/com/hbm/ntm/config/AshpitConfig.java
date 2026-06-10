package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AshpitConfig {
    public static final int THRESHOLD_WOOD_DEFAULT = 2_000;
    public static final int THRESHOLD_COAL_DEFAULT = 2_000;
    public static final int THRESHOLD_MISC_DEFAULT = 2_000;
    public static final int THRESHOLD_FLY_DEFAULT = 2_000;
    public static final int THRESHOLD_SOOT_DEFAULT = 8_000;

    public static ForgeConfigSpec.IntValue THRESHOLD_WOOD;
    public static ForgeConfigSpec.IntValue THRESHOLD_COAL;
    public static ForgeConfigSpec.IntValue THRESHOLD_MISC;
    public static ForgeConfigSpec.IntValue THRESHOLD_FLY;
    public static ForgeConfigSpec.IntValue THRESHOLD_SOOT;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("ashpit");
        THRESHOLD_WOOD = threshold(builder, "thresholdWood", THRESHOLD_WOOD_DEFAULT,
                "Legacy ashpit I:thresholdWood: wood ash level required to produce one wood ash powder.");
        THRESHOLD_COAL = threshold(builder, "thresholdCoal", THRESHOLD_COAL_DEFAULT,
                "Legacy ashpit I:thresholdCoal: coal ash level required to produce one coal ash powder.");
        THRESHOLD_MISC = threshold(builder, "thresholdMisc", THRESHOLD_MISC_DEFAULT,
                "Legacy ashpit I:thresholdMisc: miscellaneous ash level required to produce one miscellaneous ash powder.");
        THRESHOLD_FLY = threshold(builder, "thresholdFly", THRESHOLD_FLY_DEFAULT,
                "Legacy ashpit I:thresholdFly: fly ash level required to produce one fly ash powder.");
        THRESHOLD_SOOT = threshold(builder, "thresholdSoot", THRESHOLD_SOOT_DEFAULT,
                "Legacy ashpit I:thresholdSoot: soot level required to produce one soot ash powder.");
        builder.pop();
        builder.pop();
    }

    public static int thresholdWood() {
        return positiveInt(THRESHOLD_WOOD, THRESHOLD_WOOD_DEFAULT);
    }

    public static int thresholdCoal() {
        return positiveInt(THRESHOLD_COAL, THRESHOLD_COAL_DEFAULT);
    }

    public static int thresholdMisc() {
        return positiveInt(THRESHOLD_MISC, THRESHOLD_MISC_DEFAULT);
    }

    public static int thresholdFly() {
        return positiveInt(THRESHOLD_FLY, THRESHOLD_FLY_DEFAULT);
    }

    public static int thresholdSoot() {
        return positiveInt(THRESHOLD_SOOT, THRESHOLD_SOOT_DEFAULT);
    }

    private static ForgeConfigSpec.IntValue threshold(ForgeConfigSpec.Builder builder, String name, int fallback,
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

    private AshpitConfig() {
    }
}

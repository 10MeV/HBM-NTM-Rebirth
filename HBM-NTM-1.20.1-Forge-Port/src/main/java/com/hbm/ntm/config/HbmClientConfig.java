package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HbmClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue GEIGER_OFFSET_HORIZONTAL;
    public static final ForgeConfigSpec.IntValue GEIGER_OFFSET_VERTICAL;
    public static final ForgeConfigSpec.BooleanValue NUKE_HUD_FLASH;
    public static final ForgeConfigSpec.BooleanValue NUKE_HUD_SHAKE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("hud");
        GEIGER_OFFSET_HORIZONTAL = builder
                .comment("Legacy ClientConfig.GEIGER_OFFSET_HORIZONTAL: shifts the Geiger counter HUD to the right.")
                .defineInRange("geigerOffsetHorizontal", 0, -10000, 10000);
        GEIGER_OFFSET_VERTICAL = builder
                .comment("Legacy ClientConfig.GEIGER_OFFSET_VERTICAL: shifts the Geiger counter HUD upward.")
                .defineInRange("geigerOffsetVertical", 0, -10000, 10000);
        NUKE_HUD_FLASH = builder
                .comment("Legacy ClientConfig.NUKE_HUD_FLASH: enables the white nuclear explosion HUD flash.")
                .define("nukeHudFlash", true);
        NUKE_HUD_SHAKE = builder
                .comment("Legacy ClientConfig.NUKE_HUD_SHAKE: enables the nuclear explosion HUD shake.")
                .define("nukeHudShake", true);
        builder.pop();

        SPEC = builder.build();
    }

    private HbmClientConfig() {
    }
}

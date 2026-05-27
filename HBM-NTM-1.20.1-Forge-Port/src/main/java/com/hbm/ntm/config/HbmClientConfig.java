package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HbmClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue GEIGER_OFFSET_HORIZONTAL;
    public static final ForgeConfigSpec.IntValue GEIGER_OFFSET_VERTICAL;
    public static final ForgeConfigSpec.BooleanValue NUKE_HUD_FLASH;
    public static final ForgeConfigSpec.BooleanValue NUKE_HUD_SHAKE;
    public static final ForgeConfigSpec.BooleanValue NUKE_WARP_SHOCKWAVE;
    public static final ForgeConfigSpec.DoubleValue NUKE_WARP_SHOCKWAVE_INTENSITY;
    public static final ForgeConfigSpec.IntValue NUKE_WARP_SHOCKWAVE_MESH_SEGMENTS;
    public static final ForgeConfigSpec.BooleanValue DEBUG_NUKE_WARP_SHOCKWAVE_WIREFRAME;

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

        builder.push("effects");
        NUKE_WARP_SHOCKWAVE = builder
                .comment("Enables the warp_world shader shockwave that follows nuclear shock fronts.")
                .define("nukeWarpShockwave", true);
        NUKE_WARP_SHOCKWAVE_INTENSITY = builder
                .comment("Multiplier for the warp_world nuclear shockwave distortion intensity.")
                .defineInRange("nukeWarpShockwaveIntensity", 1.75D, 0.0D, 8.0D);
        NUKE_WARP_SHOCKWAVE_MESH_SEGMENTS = builder
                .comment("Horizontal sphere segments used by the warp_world nuclear shockwave mesh.")
                .defineInRange("nukeWarpShockwaveMeshSegments", 48, 12, 96);
        DEBUG_NUKE_WARP_SHOCKWAVE_WIREFRAME = builder
                .comment("Debug only: renders a visible wireframe over the nuclear warp shockwave to verify the API/render pass is firing.")
                .define("debugNukeWarpShockwaveWireframe", false);
        builder.pop();

        SPEC = builder.build();
    }

    private HbmClientConfig() {
    }
}

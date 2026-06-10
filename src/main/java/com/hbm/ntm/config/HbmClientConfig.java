package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HbmClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue GEIGER_OFFSET_HORIZONTAL;
    public static final ForgeConfigSpec.IntValue GEIGER_OFFSET_VERTICAL;
    public static final ForgeConfigSpec.IntValue INFO_OFFSET_HORIZONTAL;
    public static final ForgeConfigSpec.IntValue INFO_OFFSET_VERTICAL;
    public static final ForgeConfigSpec.IntValue INFO_POSITION;
    public static final ForgeConfigSpec.IntValue TOOL_HUD_INDICATOR_X;
    public static final ForgeConfigSpec.IntValue TOOL_HUD_INDICATOR_Y;
    public static final ForgeConfigSpec.BooleanValue NUKE_HUD_FLASH;
    public static final ForgeConfigSpec.BooleanValue NUKE_HUD_SHAKE;
    public static final ForgeConfigSpec.BooleanValue LEGACY_LOOK_OVERLAY;
    public static final ForgeConfigSpec.BooleanValue SHOW_BLOCK_STATE_OVERLAY;
    public static final ForgeConfigSpec.DoubleValue GUN_ANIMATION_SPEED;
    public static final ForgeConfigSpec.BooleanValue NUKE_WARP_SHOCKWAVE;
    public static final ForgeConfigSpec.DoubleValue NUKE_WARP_SHOCKWAVE_INTENSITY;
    public static final ForgeConfigSpec.IntValue NUKE_WARP_SHOCKWAVE_MESH_SEGMENTS;
    public static final ForgeConfigSpec.BooleanValue DEBUG_NUKE_WARP_SHOCKWAVE_WIREFRAME;
    public static final ForgeConfigSpec.BooleanValue COOLING_TOWER_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue ITEM_TOOLTIP_SHOW_CUSTOM_NUKE;
    public static final ForgeConfigSpec.BooleanValue NEI_HIDE_SECRETS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("hud");
        GEIGER_OFFSET_HORIZONTAL = builder
                .comment("Legacy ClientConfig.GEIGER_OFFSET_HORIZONTAL: shifts the Geiger counter HUD to the right.")
                .defineInRange("geigerOffsetHorizontal", 0, -10000, 10000);
        GEIGER_OFFSET_VERTICAL = builder
                .comment("Legacy ClientConfig.GEIGER_OFFSET_VERTICAL: shifts the Geiger counter HUD upward.")
                .defineInRange("geigerOffsetVertical", 0, -10000, 10000);
        INFO_OFFSET_HORIZONTAL = builder
                .comment("Legacy ClientConfig.INFO_OFFSET_HORIZONTAL: shifts the client inform message overlay horizontally.")
                .defineInRange("infoOffsetHorizontal", 0, -10000, 10000);
        INFO_OFFSET_VERTICAL = builder
                .comment("Legacy ClientConfig.INFO_OFFSET_VERTICAL: shifts the client inform message overlay vertically.")
                .defineInRange("infoOffsetVertical", 0, -10000, 10000);
        INFO_POSITION = builder
                .comment("Legacy ClientConfig.INFO_POSITION: 0=upper left, 1=upper right, 2=right of crosshair, 3=left of crosshair.")
                .defineInRange("infoPosition", 0, 0, 3);
        TOOL_HUD_INDICATOR_X = builder
                .comment("Legacy ClientConfig.TOOL_HUD_INDICATOR_X: shifts the active tool area ability HUD icon horizontally.")
                .defineInRange("toolHudIndicatorX", 0, -10000, 10000);
        TOOL_HUD_INDICATOR_Y = builder
                .comment("Legacy ClientConfig.TOOL_HUD_INDICATOR_Y: shifts the active tool area ability HUD icon vertically.")
                .defineInRange("toolHudIndicatorY", 0, -10000, 10000);
        NUKE_HUD_FLASH = builder
                .comment("Legacy ClientConfig.NUKE_HUD_FLASH: enables the white nuclear explosion HUD flash.")
                .define("nukeHudFlash", true);
        NUKE_HUD_SHAKE = builder
                .comment("Legacy ClientConfig.NUKE_HUD_SHAKE: enables the nuclear explosion HUD shake.")
                .define("nukeHudShake", true);
        LEGACY_LOOK_OVERLAY = builder
                .comment("Legacy ClientConfig.DODD_RBMK_DIAGNOSTIC: enables crosshair look overlays for migrated blocks, items, and entities.")
                .define("legacyLookOverlay", true);
        SHOW_BLOCK_STATE_OVERLAY = builder
                .comment("Modernized ClientConfig.SHOW_BLOCK_META_OVERLAY: shows registry id and BlockState properties for the block under the crosshair.")
                .define("showBlockStateOverlay", false);
        builder.pop();

        builder.push("animations");
        GUN_ANIMATION_SPEED = builder
                .comment("Legacy ClientConfig.GUN_ANIMATION_SPEED: divides legacy item/gun animation keyframe durations. Values above 1 play faster.")
                .defineInRange("gunAnimationSpeed", 1.0D, 0.001D, 100.0D);
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
        COOLING_TOWER_PARTICLES = builder
                .comment("Legacy ClientConfig.COOLING_TOWER_PARTICLES: enables steam plume particles for active cooling towers.")
                .define("coolingTowerParticles", true);
        builder.pop();

        builder.push("tooltips");
        ITEM_TOOLTIP_SHOW_CUSTOM_NUKE = builder
                .comment("Legacy ClientConfig.ITEM_TOOLTIP_SHOW_CUSTOM_NUKE: shows custom nuke stage contribution lines for items used by the custom nuke.")
                .define("showCustomNukeTooltips", true);
        builder.pop();

        builder.push("jei");
        NEI_HIDE_SECRETS = builder
                .comment("Legacy ClientConfig.NEI_HIDE_SECRETS: hides secret blueprint-pool machine recipes from JEI display.")
                .define("hideSecretRecipes", true);
        builder.pop();

        SPEC = builder.build();
    }

    public static double legacyGunAnimationTimeMultiplier() {
        double speed = GUN_ANIMATION_SPEED == null ? 1.0D : GUN_ANIMATION_SPEED.get();
        return 1.0D / Math.max(0.001D, speed);
    }

    public static int infoPosition() {
        return INFO_POSITION == null ? 0 : Math.max(0, Math.min(3, INFO_POSITION.get()));
    }

    public static int geigerOffsetHorizontal() {
        return GEIGER_OFFSET_HORIZONTAL == null ? 0 : GEIGER_OFFSET_HORIZONTAL.get();
    }

    public static int geigerOffsetVertical() {
        return GEIGER_OFFSET_VERTICAL == null ? 0 : GEIGER_OFFSET_VERTICAL.get();
    }

    public static int infoOffsetHorizontal() {
        return INFO_OFFSET_HORIZONTAL == null ? 0 : INFO_OFFSET_HORIZONTAL.get();
    }

    public static int infoOffsetVertical() {
        return INFO_OFFSET_VERTICAL == null ? 0 : INFO_OFFSET_VERTICAL.get();
    }

    public static int toolHudIndicatorX() {
        return TOOL_HUD_INDICATOR_X == null ? 0 : TOOL_HUD_INDICATOR_X.get();
    }

    public static int toolHudIndicatorY() {
        return TOOL_HUD_INDICATOR_Y == null ? 0 : TOOL_HUD_INDICATOR_Y.get();
    }

    public static boolean coolingTowerParticles() {
        return COOLING_TOWER_PARTICLES == null || COOLING_TOWER_PARTICLES.get();
    }

    public static boolean nukeHudFlash() {
        return NUKE_HUD_FLASH == null || NUKE_HUD_FLASH.get();
    }

    public static boolean nukeHudShake() {
        return NUKE_HUD_SHAKE == null || NUKE_HUD_SHAKE.get();
    }

    public static boolean legacyLookOverlay() {
        return LEGACY_LOOK_OVERLAY == null || LEGACY_LOOK_OVERLAY.get();
    }

    public static boolean showBlockStateOverlay() {
        return SHOW_BLOCK_STATE_OVERLAY != null && SHOW_BLOCK_STATE_OVERLAY.get();
    }

    public static boolean nukeWarpShockwaveEnabled() {
        return booleanValue(NUKE_WARP_SHOCKWAVE, true);
    }

    public static float nukeWarpShockwaveIntensity() {
        return (float) doubleValue(NUKE_WARP_SHOCKWAVE_INTENSITY, 1.0D);
    }

    public static int nukeWarpShockwaveMeshSegments() {
        return Math.max(12, Math.min(96, intValue(NUKE_WARP_SHOCKWAVE_MESH_SEGMENTS, 48)));
    }

    public static boolean debugNukeWarpShockwaveWireframe() {
        return booleanValue(DEBUG_NUKE_WARP_SHOCKWAVE_WIREFRAME, false);
    }

    public static boolean customNukeTooltips() {
        return booleanValue(ITEM_TOOLTIP_SHOW_CUSTOM_NUKE, true);
    }

    public static boolean hideSecretJeiRecipes() {
        return booleanValue(NEI_HIDE_SECRETS, true);
    }

    private static boolean booleanValue(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static int intValue(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static double doubleValue(ForgeConfigSpec.DoubleValue value, double fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private HbmClientConfig() {
    }
}

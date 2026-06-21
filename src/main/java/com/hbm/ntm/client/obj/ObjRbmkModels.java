package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class ObjRbmkModels {
    public static final int FUEL_CHANNEL_CHERENKOV_COLOR = 0x66E5FF;
    public static final int FUEL_CHANNEL_CHERENKOV_ALPHA = 26;
    public static final double FUEL_CHANNEL_CHERENKOV_START_Y = 0.75D;
    public static final double FUEL_CHANNEL_CHERENKOV_STEP = 0.25D;

    public static final LegacyWavefrontModel ELEMENT = model("rbmk_element", iconTexture("rbmk_element")).noSmooth().mixedMode();
    public static final LegacyWavefrontModel ELEMENT_RODS = model("rbmk_element_rods", iconTexture("rbmk_element_fuel")).noSmooth();
    public static final LegacyWavefrontModel ELEMENT_RODS_VBO = model("rbmk_element_rods", iconTexture("rbmk_element_fuel")).noSmooth().asVBO();
    public static final LegacyWavefrontModel RODS = model("rbmk_rods", iconTexture("rbmk_control")).noSmooth();
    public static final LegacyWavefrontModel RODS_VBO = model("rbmk_rods", iconTexture("rbmk_control")).noSmooth().asVBO();
    public static final LegacyWavefrontModel CRANE_CONSOLE = model("crane_console", modelTexture("crane_console")).asVBO();
    public static final LegacyWavefrontModel CRANE = model("crane", modelTexture("rbmk_crane")).asVBO();
    public static final LegacyWavefrontModel AUTOLOADER = model("autoloader", modelTexture("rbmk_autoloader")).asVBO();
    public static final LegacyWavefrontModel CONSOLE = model("rbmk_console", modelTexture("rbmk_control")).asVBO();
    public static final LegacyWavefrontModel BUTTON = model("button", modelTexture("keypad")).asVBO();
    public static final LegacyWavefrontModel GAUGE = model("gauge", modelTexture("gauge")).asVBO();
    public static final LegacyWavefrontModel NUMITRON = model("numitron", modelTexture("numitron")).asVBO();
    public static final LegacyWavefrontModel LEVER = model("lever", modelTexture("lever")).asVBO();
    public static final LegacyWavefrontModel INDICATOR = model("indicator", modelTexture("indicator")).asVBO();
    public static final LegacyWavefrontModel TERMINAL = model("terminal", modelTexture("terminal")).asVBO();
    public static final LegacyWavefrontModel DEBRIS = model("debris", iconTexture("rbmk_debris")).noSmooth();

    public static final ResourceLocation CRANE_CONSOLE_TEXTURE = modelTexture("crane_console");
    public static final ResourceLocation CRANE_TEXTURE = modelTexture("rbmk_crane");
    public static final ResourceLocation AUTOLOADER_TEXTURE = modelTexture("rbmk_autoloader");
    public static final ResourceLocation CONSOLE_TEXTURE = modelTexture("rbmk_control");
    public static final ResourceLocation KEYPAD_TEXTURE = modelTexture("keypad");
    public static final ResourceLocation GAUGE_TEXTURE = modelTexture("gauge");
    public static final ResourceLocation NUMITRON_TEXTURE = modelTexture("numitron");
    public static final ResourceLocation NUMITRON_LIGHTS_TEXTURE = modelTexture("numitron_lights");
    public static final ResourceLocation LEVER_TEXTURE = modelTexture("lever");
    public static final ResourceLocation INDICATOR_TEXTURE = modelTexture("indicator");
    public static final ResourceLocation TERMINAL_TEXTURE = modelTexture("terminal");

    public static final ResourceLocation ELEMENT_TEXTURE = iconTexture("rbmk_element");
    public static final ResourceLocation ELEMENT_INNER_TEXTURE = iconTexture("rbmk_element_inner");
    public static final ResourceLocation ELEMENT_FUEL_TEXTURE = iconTexture("rbmk_element_fuel");
    public static final ResourceLocation CONTROL_STANDARD_TEXTURE = iconTexture("rbmk_control");
    public static final ResourceLocation CONTROL_AUTO_TEXTURE = iconTexture("rbmk_control_auto");
    public static final ResourceLocation CONTROL_RED_TEXTURE = iconTexture("rbmk_control_red");
    public static final ResourceLocation CONTROL_YELLOW_TEXTURE = iconTexture("rbmk_control_yellow");
    public static final ResourceLocation CONTROL_GREEN_TEXTURE = iconTexture("rbmk_control_green");
    public static final ResourceLocation CONTROL_BLUE_TEXTURE = iconTexture("rbmk_control_blue");
    public static final ResourceLocation CONTROL_PURPLE_TEXTURE = iconTexture("rbmk_control_purple");
    public static final ResourceLocation DEBRIS_TEXTURE = iconTexture("rbmk_debris");
    public static final ResourceLocation DEBRIS_BURNING_TEXTURE = iconTexture("rbmk_debris_burning");
    public static final ResourceLocation DEBRIS_RADIATING_TEXTURE = iconTexture("rbmk_debris_radiating");
    public static final ResourceLocation DEBRIS_DIGAMMA_TEXTURE = iconTexture("rbmk_debris_digamma");

    public static void renderFuelChannelRods(int argbColor, int height, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        int red = argbColor >> 16 & 255;
        int green = argbColor >> 8 & 255;
        int blue = argbColor & 255;
        poseStack.pushPose();
        for (int i = 0; i <= height; i++) {
            ELEMENT_RODS.renderPart("Rods", ELEMENT_FUEL_TEXTURE, poseStack, buffer, packedLight, packedOverlay, red, green, blue, 255);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }
        poseStack.popPose();
    }

    public static void renderFuelChannelCherenkov(ObjRenderContext context, int height) {
        LegacyUntexturedQuadRenderer.horizontalSlices(context.withAdditiveTranslucency(),
                -0.5D, -0.5D, 0.5D, 0.5D,
                FUEL_CHANNEL_CHERENKOV_START_Y,
                FUEL_CHANNEL_CHERENKOV_START_Y + Math.max(0, height),
                FUEL_CHANNEL_CHERENKOV_STEP,
                FUEL_CHANNEL_CHERENKOV_COLOR,
                FUEL_CHANNEL_CHERENKOV_ALPHA);
    }

    public static void renderControlLid(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        RODS_VBO.renderPart("Lid", texture, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderTerminal(ObjRenderContext context) {
        TERMINAL.renderAll(TERMINAL_TEXTURE, context);
    }

    public static void renderCraneConsolePart(String partName, ObjRenderContext context) {
        CRANE_CONSOLE.renderPart(partName, CRANE_CONSOLE_TEXTURE, context);
    }

    public static void renderCranePart(String partName, ObjRenderContext context) {
        CRANE.renderPart(partName, CRANE_TEXTURE, context);
    }

    public static void renderAutoloaderPart(String partName, ObjRenderContext context) {
        AUTOLOADER.renderPart(partName, AUTOLOADER_TEXTURE, context);
    }

    public static void renderDebris(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        DEBRIS.renderAll(texture, poseStack, buffer, packedLight, packedOverlay);
    }

    public static ResourceLocation manualControlTexture(int colorOrdinal) {
        return switch (colorOrdinal) {
            case 0 -> CONTROL_RED_TEXTURE;
            case 1 -> CONTROL_YELLOW_TEXTURE;
            case 2 -> CONTROL_GREEN_TEXTURE;
            case 3 -> CONTROL_BLUE_TEXTURE;
            case 4 -> CONTROL_PURPLE_TEXTURE;
            default -> CONTROL_STANDARD_TEXTURE;
        };
    }

    private static LegacyWavefrontModel model(String name, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/rbmk/" + name + ".obj"),
                texture);
    }

    public static ResourceLocation modelTexture(String name) {
        return switch (name) {
            case "crane_console", "rbmk_crane", "rbmk_autoloader", "rbmk_control" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/" + name + ".png");
            case "keypad", "gauge", "numitron", "numitron_lights", "lever", "indicator", "terminal" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "textures/models/network/" + name + ".png");
            default -> new ResourceLocation(HbmNtm.MOD_ID, "textures/block/rbmk/models/" + name + ".png");
        };
    }

    public static ResourceLocation iconTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/rbmk/icons/" + name + ".png");
    }

    private ObjRbmkModels() {
    }
}

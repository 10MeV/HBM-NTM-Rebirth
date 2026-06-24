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
    private static final String ELEMENT_RODS_PART = "Rods";
    private static final String CONTROL_LID_PART = "Lid";
    private static final LegacyWavefrontModel.SelectionHandle ELEMENT_RODS_HANDLE =
            ELEMENT_RODS_VBO.prepareRenderOnlyInCallOrder(ELEMENT_RODS_PART);
    private static final LegacyWavefrontModel.SelectionHandle CONTROL_LID_HANDLE =
            RODS_VBO.prepareRenderOnlyInCallOrder(CONTROL_LID_PART);
    public static final LegacyWavefrontModel CRANE_CONSOLE = model("crane_console", modelTexture("crane_console")).asVBO();
    public static final LegacyWavefrontModel CRANE = model("crane", modelTexture("rbmk_crane")).asVBO();
    public static final LegacyWavefrontModel AUTOLOADER = model("autoloader", modelTexture("rbmk_autoloader")).asVBO();
    private static final LegacyWavefrontModel.SelectionHandle CRANE_MAIN =
            CRANE.prepareRenderOnlyInCallOrder("Main");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_GIRDER =
            CRANE.prepareRenderOnlyInCallOrder("Girder");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_TUBE =
            CRANE.prepareRenderOnlyInCallOrder("Tube");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_CARRIAGE =
            CRANE.prepareRenderOnlyInCallOrder("Carriage");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_LIFT =
            CRANE.prepareRenderOnlyInCallOrder("Lift");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_CONSOLE_BODY =
            CRANE_CONSOLE.prepareRenderOnlyInCallOrder("Console_Coonsole");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_CONSOLE_JOYSTICK =
            CRANE_CONSOLE.prepareRenderOnlyInCallOrder("Joystick");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_CONSOLE_METER_1 =
            CRANE_CONSOLE.prepareRenderOnlyInCallOrder("Meter1");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_CONSOLE_METER_2 =
            CRANE_CONSOLE.prepareRenderOnlyInCallOrder("Meter2");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_CONSOLE_LAMP_1 =
            CRANE_CONSOLE.prepareRenderOnlyInCallOrder("Lamp1");
    private static final LegacyWavefrontModel.SelectionHandle CRANE_CONSOLE_LAMP_2 =
            CRANE_CONSOLE.prepareRenderOnlyInCallOrder("Lamp2");
    private static final LegacyWavefrontModel.SelectionHandle AUTOLOADER_BASE =
            AUTOLOADER.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle AUTOLOADER_PISTON =
            AUTOLOADER.prepareRenderOnlyInCallOrder("Piston");
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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, null, packedLight, packedOverlay)
                .withRgb(red, green, blue);
        for (int i = 0; i <= height; i++) {
            renderFuelRodPart(ELEMENT_RODS_PART, ELEMENT_FUEL_TEXTURE, context);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }
        poseStack.popPose();
    }

    public static void renderFuelRodPart(String partName, ResourceLocation texture, ObjRenderContext context) {
        if (ELEMENT_RODS_PART.equals(partName)) {
            ELEMENT_RODS_VBO.renderOnlyInCallOrder(texture, context, ELEMENT_RODS_HANDLE);
            return;
        }
        ELEMENT_RODS_VBO.renderPart(partName, texture, context);
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
        renderControlRodPart(CONTROL_LID_PART, texture,
                new ObjRenderContext(poseStack, buffer, null, packedLight, packedOverlay));
    }

    public static void renderControlRodPart(String partName, ResourceLocation texture, ObjRenderContext context) {
        if (CONTROL_LID_PART.equals(partName)) {
            RODS_VBO.renderOnlyInCallOrder(texture, context, CONTROL_LID_HANDLE);
            return;
        }
        RODS_VBO.renderPart(partName, texture, context);
    }

    public static void renderTerminal(ObjRenderContext context) {
        TERMINAL.renderAll(TERMINAL_TEXTURE, context);
    }

    public static void renderCraneConsolePart(String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = craneConsoleHandle(partName);
        if (handle != null) {
            CRANE_CONSOLE.renderOnlyInCallOrder(CRANE_CONSOLE_TEXTURE, context, handle);
            return;
        }
        CRANE_CONSOLE.renderPart(partName, CRANE_CONSOLE_TEXTURE, context);
    }

    public static void renderCraneConsolePartUntextured(String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = craneConsoleHandle(partName);
        if (handle != null) {
            CRANE_CONSOLE.renderOnlyUntextured(context, handle);
            return;
        }
        CRANE_CONSOLE.renderPartUntextured(partName, context);
    }

    public static void renderCranePart(String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = craneHandle(partName);
        if (handle != null) {
            CRANE.renderOnlyInCallOrder(CRANE_TEXTURE, context, handle);
            return;
        }
        CRANE.renderPart(partName, CRANE_TEXTURE, context);
    }

    public static void renderAutoloaderPart(String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = autoloaderHandle(partName);
        if (handle != null) {
            AUTOLOADER.renderOnlyInCallOrder(AUTOLOADER_TEXTURE, context, handle);
            return;
        }
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

    private static LegacyWavefrontModel.SelectionHandle craneConsoleHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Console_Coonsole" -> CRANE_CONSOLE_BODY;
            case "Joystick" -> CRANE_CONSOLE_JOYSTICK;
            case "Meter1" -> CRANE_CONSOLE_METER_1;
            case "Meter2" -> CRANE_CONSOLE_METER_2;
            case "Lamp1" -> CRANE_CONSOLE_LAMP_1;
            case "Lamp2" -> CRANE_CONSOLE_LAMP_2;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle craneHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Main" -> CRANE_MAIN;
            case "Girder" -> CRANE_GIRDER;
            case "Tube" -> CRANE_TUBE;
            case "Carriage" -> CRANE_CARRIAGE;
            case "Lift" -> CRANE_LIFT;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle autoloaderHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> AUTOLOADER_BASE;
            case "Piston" -> AUTOLOADER_PISTON;
            default -> null;
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

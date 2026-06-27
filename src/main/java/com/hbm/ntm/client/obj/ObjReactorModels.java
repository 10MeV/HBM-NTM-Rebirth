package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class ObjReactorModels {
    public static final LegacyWavefrontModel SMALL_BASE = model("reactor_small_base").asVBO();
    public static final LegacyWavefrontModel SMALL_RODS = model("reactor_small_rods").asVBO();
    public static final LegacyWavefrontModel BREEDER = model("breeder").asVBO();
    public static final LegacyWavefrontModel ICF = model("icf").asVBO();
    public static final LegacyWavefrontModel WATZ = model("watz").asVBO();
    public static final LegacyWavefrontModel WATZ_PUMP = model("watz_pump").asVBO();
    public static final LegacyWavefrontModel LPW2 = model("lpw2").asVBO();
    public static final LegacyWavefrontModel ZIRNOX = model("zirnox").asVBO();
    public static final LegacyWavefrontModel ZIRNOX_DESTROYED = model("zirnox_destroyed").asVBO();
    private static final LegacyWavefrontModel.SelectionHandle LPW2_FRAME =
            LPW2.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_WIRE_LEFT =
            LPW2.prepareRenderOnlyInCallOrder("WireLeft");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_WIRE_RIGHT =
            LPW2.prepareRenderOnlyInCallOrder("WireRight");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_COVER =
            LPW2.prepareRenderOnlyInCallOrder("Cover");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_COVER_FRONT =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionCoverFront");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_COVER_BACK =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionCoverBack");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_BACK_OUTER =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionBackOuter");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_BACK_CENTER =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionBackCenter");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SERVER_1 =
            LPW2.prepareRenderOnlyInCallOrder("Server1");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SERVER_2 =
            LPW2.prepareRenderOnlyInCallOrder("Server2");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SERVER_3 =
            LPW2.prepareRenderOnlyInCallOrder("Server3");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SERVER_4 =
            LPW2.prepareRenderOnlyInCallOrder("Server4");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_MONITOR =
            LPW2.prepareRenderOnlyInCallOrder("Monitor");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SCREEN =
            LPW2.prepareRenderOnlyInCallOrder("Screen");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_CENTER =
            LPW2.prepareRenderOnlyInCallOrder("Center");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_ROTOR =
            LPW2.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_TURBINE_FRONT =
            LPW2.prepareRenderOnlyInCallOrder("TurbineFront");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_TURBINE_BACK =
            LPW2.prepareRenderOnlyInCallOrder("TurbineBack");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_PISTON =
            LPW2.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_ENGINE =
            LPW2.prepareRenderOnlyInCallOrder("Engine");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SHROUD_H =
            LPW2.prepareRenderOnlyInCallOrder("ShroudH");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SHROUD_V =
            LPW2.prepareRenderOnlyInCallOrder("ShroudV");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_LEFT =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionLeft");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_RIGHT =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionRight");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_TOP =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionTop");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_SUSPENSION_BOTTOM =
            LPW2.prepareRenderOnlyInCallOrder("SuspensionBottom");
    private static final LegacyWavefrontModel.SelectionHandle LPW2_FLAP =
            LPW2.prepareRenderOnlyInCallOrder("Flap");

    public static final ResourceLocation SMALL_BASE_TEXTURE = texture("reactor_small_base");
    public static final ResourceLocation SMALL_RODS_TEXTURE = texture("reactor_small_rods");
    public static final ResourceLocation BREEDER_TEXTURE = texture("breeder");
    public static final ResourceLocation ICF_TEXTURE = texture("icf");
    public static final ResourceLocation WATZ_TEXTURE = texture("watz");
    public static final ResourceLocation WATZ_PUMP_TEXTURE = texture("watz_pump");
    public static final ResourceLocation LPW2_TEXTURE = texture("lpw2");
    public static final ResourceLocation LPW2_TERM_TEXTURE = texture("lpw2_term");
    public static final ResourceLocation LPW2_TERM_ERROR_TEXTURE = texture("lpw2_term_error");
    public static final ResourceLocation ZIRNOX_TEXTURE = texture("zirnox");
    public static final ResourceLocation ZIRNOX_DESTROYED_TEXTURE = texture("zirnox_destroyed");
    public static final ResourceLocation ZIRNOX_DEBRIS_ELEMENT_TEXTURE = texture("zirnox_deb_element");

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                modelLocation(name),
                texture(name)).asVBO();
    }

    private static ResourceLocation modelLocation(String name) {
        return switch (name) {
            case "reactor_small_base", "reactor_small_rods", "breeder", "icf", "watz" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "models/reactors/" + name + ".obj");
            case "lpw2", "watz_pump" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "models/machines/" + name + ".obj");
            case "zirnox", "zirnox_destroyed" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "models/" + name + ".obj");
            default -> new ResourceLocation(HbmNtm.MOD_ID, "models/block/reactors/" + name + ".obj");
        };
    }

    public static ResourceLocation texture(String name) {
        return switch (name) {
            case "breeder", "icf", "lpw2", "lpw2_term", "lpw2_term_error", "watz", "watz_pump",
                    "zirnox_deb_element" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/" + name + ".png");
            case "reactor_small_base", "reactor_small_rods", "zirnox", "zirnox_destroyed" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
            default -> new ResourceLocation(HbmNtm.MOD_ID, "textures/block/reactors/" + name + ".png");
        };
    }

    public static void renderLpw2Part(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = lpw2Handle(partName);
        if (handle != null) {
            LPW2.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle);
            return;
        }
        LPW2.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderLpw2PartWithLegacyTextureMatrixCull(String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green,
            int blue, int alpha, float uScale, float vScale, float uTranslate, float vTranslate) {
        LegacyWavefrontModel.SelectionHandle handle = lpw2Handle(partName);
        if (handle != null) {
            LPW2.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    red, green, blue, alpha, false, LegacyTexturedRenderMode.CUTOUT_CULL,
                    LegacyWavefrontModel.legacyTextureMatrixDynamic(uScale, vScale, uTranslate, vTranslate), handle);
            return;
        }
        LPW2.renderPartWithLegacyTextureMatrixCull(partName, texture, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha, uScale, vScale, uTranslate, vTranslate);
    }

    private static LegacyWavefrontModel.SelectionHandle lpw2Handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Frame" -> LPW2_FRAME;
            case "WireLeft" -> LPW2_WIRE_LEFT;
            case "WireRight" -> LPW2_WIRE_RIGHT;
            case "Cover" -> LPW2_COVER;
            case "SuspensionCoverFront" -> LPW2_SUSPENSION_COVER_FRONT;
            case "SuspensionCoverBack" -> LPW2_SUSPENSION_COVER_BACK;
            case "SuspensionBackOuter" -> LPW2_SUSPENSION_BACK_OUTER;
            case "SuspensionBackCenter" -> LPW2_SUSPENSION_BACK_CENTER;
            case "Server1" -> LPW2_SERVER_1;
            case "Server2" -> LPW2_SERVER_2;
            case "Server3" -> LPW2_SERVER_3;
            case "Server4" -> LPW2_SERVER_4;
            case "Monitor" -> LPW2_MONITOR;
            case "Screen" -> LPW2_SCREEN;
            case "Center" -> LPW2_CENTER;
            case "Rotor" -> LPW2_ROTOR;
            case "TurbineFront" -> LPW2_TURBINE_FRONT;
            case "TurbineBack" -> LPW2_TURBINE_BACK;
            case "Piston" -> LPW2_PISTON;
            case "Engine" -> LPW2_ENGINE;
            case "ShroudH" -> LPW2_SHROUD_H;
            case "ShroudV" -> LPW2_SHROUD_V;
            case "SuspensionLeft" -> LPW2_SUSPENSION_LEFT;
            case "SuspensionRight" -> LPW2_SUSPENSION_RIGHT;
            case "SuspensionTop" -> LPW2_SUSPENSION_TOP;
            case "SuspensionBottom" -> LPW2_SUSPENSION_BOTTOM;
            case "Flap" -> LPW2_FLAP;
            default -> null;
        };
    }

    private ObjReactorModels() {
    }
}

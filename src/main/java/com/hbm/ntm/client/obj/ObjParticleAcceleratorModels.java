package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class ObjParticleAcceleratorModels {
    public static final LegacyWavefrontModel SOURCE = model("source").asVBO();
    public static final LegacyWavefrontModel BEAMLINE = model("beamline").asVBO();
    public static final LegacyWavefrontModel RFC = model("rfc").asVBO();
    public static final LegacyWavefrontModel QUADRUPOLE = model("quadrupole").asVBO();
    public static final LegacyWavefrontModel DIPOLE = model("dipole").asVBO();
    public static final LegacyWavefrontModel DETECTOR = model("detector").asVBO();

    private static final LegacyWavefrontModel.SelectionHandle BEAMLINE_BODY =
            BEAMLINE.prepareRenderOnlyInCallOrder("Beamline");
    private static final LegacyWavefrontModel.SelectionHandle BEAMLINE_WINDOW =
            BEAMLINE.prepareRenderOnlyInCallOrder("BeamlineWindow");
    private static final LegacyWavefrontModel.SelectionHandle BEAMLINE_GLASS =
            BEAMLINE.prepareRenderOnlyInCallOrder("BeamlineGlass");

    public static final ResourceLocation SOURCE_TEXTURE = texture("source");
    public static final ResourceLocation BEAMLINE_TEXTURE = texture("beamline");
    public static final ResourceLocation RFC_TEXTURE = texture("rfc");
    public static final ResourceLocation QUADRUPOLE_TEXTURE = texture("quadrupole");
    public static final ResourceLocation DIPOLE_TEXTURE = texture("dipole");
    public static final ResourceLocation DETECTOR_TEXTURE = texture("detector");

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/particleaccelerator/" + name + ".obj"),
                texture(name)).asVBO();
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/particleaccelerator/" + name + ".png");
    }

    public static void renderBeamlinePart(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = beamlineHandle(partName);
        if (handle != null) {
            BEAMLINE.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
            return;
        }
        BEAMLINE.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, 255, 255, 255, 255,
                false, renderMode, LegacyWavefrontModel.UvTransform.DEFAULT, partName);
    }

    public static void renderBeamlinePartUntextured(String partName, PoseStack poseStack, MultiBufferSource buffer,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = beamlineHandle(partName);
        if (handle != null) {
            BEAMLINE.renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha, renderMode, handle);
            return;
        }
        BEAMLINE.renderOnlyUntextured(poseStack, buffer, red, green, blue, alpha, renderMode, partName);
    }

    private static LegacyWavefrontModel.SelectionHandle beamlineHandle(String partName) {
        return switch (partName) {
            case "Beamline" -> BEAMLINE_BODY;
            case "BeamlineWindow" -> BEAMLINE_WINDOW;
            case "BeamlineGlass" -> BEAMLINE_GLASS;
            default -> null;
        };
    }

    private ObjParticleAcceleratorModels() {
    }
}

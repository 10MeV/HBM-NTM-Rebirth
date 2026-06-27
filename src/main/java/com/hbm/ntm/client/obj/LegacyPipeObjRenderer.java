package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Modern entrypoint for the old RenderPipe OBJ/icon combinations.
 */
public final class LegacyPipeObjRenderer {
    public static void renderStandard(ResourceLocation topTexture, ResourceLocation sideTexture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, float yawRadians, float pitchRadians) {
        renderPipe(ObjBlockModels.PIPE, topTexture, sideTexture, poseStack, buffer, packedLight, packedOverlay,
                yawRadians, pitchRadians);
    }

    public static void renderRim(ResourceLocation topTexture, ResourceLocation sideTexture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, float yawRadians, float pitchRadians) {
        renderPipe(ObjBlockModels.PIPE_RIM, topTexture, sideTexture, poseStack, buffer, packedLight, packedOverlay,
                yawRadians, pitchRadians);
    }

    public static void renderQuad(ResourceLocation topTexture, ResourceLocation sideTexture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, float yawRadians, float pitchRadians) {
        renderPipe(ObjBlockModels.PIPE_QUAD, topTexture, sideTexture, poseStack, buffer, packedLight, packedOverlay,
                yawRadians, pitchRadians);
    }

    public static void renderFramed(ResourceLocation topTexture, ResourceLocation sideTexture, ResourceLocation frameTexture,
            ResourceLocation meshTexture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, float yawRadians, float pitchRadians) {
        renderRim(topTexture, sideTexture, poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.PIPE_FRAME, "Frame", frameTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.PIPE_FRAME, "Mesh", meshTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, 0.0F);
    }

    public static void renderByLegacyType(int rType, ResourceLocation topTexture, ResourceLocation sideTexture,
            ResourceLocation frameTexture, ResourceLocation meshTexture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians) {
        switch (rType) {
            case 0 -> renderStandard(topTexture, sideTexture, poseStack, buffer, packedLight, packedOverlay,
                    yawRadians, pitchRadians);
            case 1 -> renderRim(topTexture, sideTexture, poseStack, buffer, packedLight, packedOverlay,
                    yawRadians, pitchRadians);
            case 2 -> renderQuad(topTexture, sideTexture, poseStack, buffer, packedLight, packedOverlay,
                    yawRadians, pitchRadians);
            case 3 -> renderFramed(topTexture, sideTexture, frameTexture, meshTexture, poseStack, buffer, packedLight,
                    packedOverlay, yawRadians, pitchRadians);
            default -> {
            }
        }
    }

    private static void renderPipe(LegacyWavefrontModel model, ResourceLocation topTexture, ResourceLocation sideTexture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float yawRadians,
            float pitchRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(model, "Top", topTexture, poseStack, buffer, packedLight,
                packedOverlay, yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(model, "Side", sideTexture, poseStack, buffer, packedLight,
                packedOverlay, yawRadians, pitchRadians, 0.0F);
    }

    private LegacyPipeObjRenderer() {
    }
}

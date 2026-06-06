package com.hbm.ntm.client.obj;

import net.minecraft.resources.ResourceLocation;

/**
 * Modern entrypoint for the old RenderPipe OBJ/icon combinations.
 */
public final class LegacyPipeObjRenderer {
    public static void renderStandard(ResourceLocation topTexture, ResourceLocation sideTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians) {
        renderPipe(ObjBlockModels.PIPE, topTexture, sideTexture, context, yawRadians, pitchRadians);
    }

    public static void renderRim(ResourceLocation topTexture, ResourceLocation sideTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians) {
        renderPipe(ObjBlockModels.PIPE_RIM, topTexture, sideTexture, context, yawRadians, pitchRadians);
    }

    public static void renderQuad(ResourceLocation topTexture, ResourceLocation sideTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians) {
        renderPipe(ObjBlockModels.PIPE_QUAD, topTexture, sideTexture, context, yawRadians, pitchRadians);
    }

    public static void renderFramed(ResourceLocation topTexture, ResourceLocation sideTexture, ResourceLocation frameTexture,
            ResourceLocation meshTexture, ObjRenderContext context, float yawRadians, float pitchRadians) {
        renderRim(topTexture, sideTexture, context, yawRadians, pitchRadians);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.PIPE_FRAME, "Frame", frameTexture, context,
                yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.PIPE_FRAME, "Mesh", meshTexture, context,
                yawRadians, pitchRadians, 0.0F);
    }

    public static void renderByLegacyType(int rType, ResourceLocation topTexture, ResourceLocation sideTexture,
            ResourceLocation frameTexture, ResourceLocation meshTexture, ObjRenderContext context, float yawRadians, float pitchRadians) {
        switch (rType) {
            case 0 -> renderStandard(topTexture, sideTexture, context, yawRadians, pitchRadians);
            case 1 -> renderRim(topTexture, sideTexture, context, yawRadians, pitchRadians);
            case 2 -> renderQuad(topTexture, sideTexture, context, yawRadians, pitchRadians);
            case 3 -> renderFramed(topTexture, sideTexture, frameTexture, meshTexture, context, yawRadians, pitchRadians);
            default -> {
            }
        }
    }

    private static void renderPipe(LegacyWavefrontModel model, ResourceLocation topTexture, ResourceLocation sideTexture,
            ObjRenderContext context, float yawRadians, float pitchRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(model, "Top", topTexture, context, yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(model, "Side", sideTexture, context, yawRadians, pitchRadians, 0.0F);
    }

    private LegacyPipeObjRenderer() {
    }
}

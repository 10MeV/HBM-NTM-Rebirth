package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Modern entrypoint for old RenderBarrel and RenderFluidBarrel OBJ group rendering.
 */
public final class LegacyBarrelObjRenderer {
    public static void renderBody(ResourceLocation barrelTexture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.BARREL, "Barrel", barrelTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians);
    }

    public static void renderConnector(ResourceLocation barrelTexture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float yawRadians, float pitchRadians, float rollRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.BARREL, "Connector", barrelTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, rollRadians);
    }

    private LegacyBarrelObjRenderer() {
    }
}

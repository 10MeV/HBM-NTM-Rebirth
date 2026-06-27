package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Modern entrypoint for old RenderCapacitor group/icon rendering.
 */
public final class LegacyCapacitorObjRenderer {
    public static void render(ResourceLocation topTexture, ResourceLocation sideTexture, ResourceLocation bottomTexture,
            ResourceLocation innerTopTexture, ResourceLocation innerSideTexture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, float yawRadians, float pitchRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "Top", topTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "Side", sideTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "Bottom", bottomTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "InnerTop", innerTopTexture, poseStack,
                buffer, packedLight, packedOverlay, yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "InnerSide", innerSideTexture,
                poseStack, buffer, packedLight, packedOverlay, yawRadians, pitchRadians, 0.0F);
    }

    private LegacyCapacitorObjRenderer() {
    }
}

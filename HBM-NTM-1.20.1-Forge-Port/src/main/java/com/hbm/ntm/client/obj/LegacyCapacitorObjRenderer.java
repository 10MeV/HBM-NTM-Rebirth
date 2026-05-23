package com.hbm.ntm.client.obj;

import net.minecraft.resources.ResourceLocation;

/**
 * Modern entrypoint for old RenderCapacitor group/icon rendering.
 */
public final class LegacyCapacitorObjRenderer {
    public static void render(ResourceLocation topTexture, ResourceLocation sideTexture, ResourceLocation bottomTexture,
            ResourceLocation innerTopTexture, ResourceLocation innerSideTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "Top", topTexture, context,
                yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "Side", sideTexture, context,
                yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "Bottom", bottomTexture, context,
                yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "InnerTop", innerTopTexture, context,
                yawRadians, pitchRadians, 0.0F);
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.CAPACITOR, "InnerSide", innerSideTexture, context,
                yawRadians, pitchRadians, 0.0F);
    }

    private LegacyCapacitorObjRenderer() {
    }
}

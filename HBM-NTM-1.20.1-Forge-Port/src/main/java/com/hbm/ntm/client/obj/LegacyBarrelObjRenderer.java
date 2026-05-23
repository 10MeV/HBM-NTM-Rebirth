package com.hbm.ntm.client.obj;

import net.minecraft.resources.ResourceLocation;

/**
 * Modern entrypoint for old RenderBarrel and RenderFluidBarrel OBJ group rendering.
 */
public final class LegacyBarrelObjRenderer {
    public static void renderBody(ResourceLocation barrelTexture, ObjRenderContext context) {
        renderBody(barrelTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderBody(ResourceLocation barrelTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians, float rollRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.BARREL, "Barrel", barrelTexture, context,
                yawRadians, pitchRadians, rollRadians);
    }

    public static void renderConnector(ResourceLocation barrelTexture, ObjRenderContext context) {
        renderConnector(barrelTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderConnector(ResourceLocation barrelTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians, float rollRadians) {
        LegacyIsbrhObjRenderer.renderPartWithTexture(ObjBlockModels.BARREL, "Connector", barrelTexture, context,
                yawRadians, pitchRadians, rollRadians);
    }

    private LegacyBarrelObjRenderer() {
    }
}

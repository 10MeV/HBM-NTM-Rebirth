package com.hbm.ntm.client.obj;

import net.minecraft.resources.ResourceLocation;

public final class LegacyObjGlintRenderer {
    public static void renderClassicGlint(LegacyWavefrontModel model, ResourceLocation texture,
            ObjRenderContext context, String partName, float age, float red, float green, float blue, float speed, float scale) {
        renderClassicGlint(model, texture, context, partName, age, 0.5F, red, green, blue, speed, scale);
    }

    public static void renderClassicGlint(LegacyWavefrontModel model, ResourceLocation texture,
            ObjRenderContext context, String partName, float age, float colorMod,
            float red, float green, float blue, float speed, float scale) {
        float glintColor = 0.76F;
        ObjRenderContext base = context.withGlintEqualDepth()
                .withColor(red * glintColor, green * glintColor, blue * glintColor, 1.0F);

        for (int layer = 0; layer < 2; layer++) {
            float movement = age * (0.001F + layer * 0.003F) * speed;
            ObjRenderContext layerContext = base.withLegacyTextureMatrix(scale, scale, 30.0F - layer * 60.0F, 0.0F, movement);
            renderPartOrAll(model, texture, layerContext, partName);
        }
    }

    private static void renderPartOrAll(LegacyWavefrontModel model, ResourceLocation texture,
            ObjRenderContext context, String partName) {
        if ("all".equals(partName)) {
            model.renderAll(texture, context);
        } else {
            model.renderPart(partName, texture, context);
        }
    }

    private LegacyObjGlintRenderer() {
    }
}

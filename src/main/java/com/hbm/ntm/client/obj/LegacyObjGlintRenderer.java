package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class LegacyObjGlintRenderer {
    public static final ResourceLocation GLINT_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/misc/glint.png");
    public static final ResourceLocation BALEFIRE_GLINT_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/misc/glintBF.png");
    public static final float DEFAULT_COLOR_MOD = 0.5F;
    public static final float DEFAULT_RED = 0.25F;
    public static final float DEFAULT_GREEN = 0.8F;
    public static final float DEFAULT_BLUE = 20.0F;
    public static final float DEFAULT_SPEED = 1.0F;
    public static final float DEFAULT_SCALE = 1.0F / 3.0F;
    public static final float GLINT_COLOR_MULTIPLIER = 0.76F;
    public static final int LAYERS = 2;

    public static void renderClassicGlint(LegacyWavefrontModel model, ResourceLocation texture,
            ObjRenderContext context, String partName, float age, float red, float green, float blue, float speed, float scale) {
        renderClassicGlint(model, texture, context, partName, age, DEFAULT_COLOR_MOD, red, green, blue, speed, scale);
    }

    public static void renderClassicGlint(LegacyWavefrontModel model, ResourceLocation texture,
            ObjRenderContext context, String partName, float age, float colorMod,
            float red, float green, float blue, float speed, float scale) {
        ObjRenderContext base = context.withGlintEqualDepth()
                .withColor(red * GLINT_COLOR_MULTIPLIER, green * GLINT_COLOR_MULTIPLIER,
                        blue * GLINT_COLOR_MULTIPLIER, 1.0F);

        for (GlintLayerPlan layer : classicGlintPlan(age, colorMod, red, green, blue, speed, scale).layers()) {
            ObjRenderContext layerContext = base.withLegacyTextureMatrix(layer.scale(), layer.scale(),
                    layer.rotationDegrees(), 0.0F, layer.movement());
            renderPartOrAll(model, texture, layerContext, partName);
        }
    }

    public static GlintPlan defaultClassicGlintPlan(float age) {
        return classicGlintPlan(age, DEFAULT_COLOR_MOD, DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE,
                DEFAULT_SPEED, DEFAULT_SCALE);
    }

    public static GlintPlan classicGlintPlan(float age, float colorMod,
            float red, float green, float blue, float speed, float scale) {
        return new GlintPlan(colorMod, red, green, blue, speed, scale,
                red * GLINT_COLOR_MULTIPLIER, green * GLINT_COLOR_MULTIPLIER, blue * GLINT_COLOR_MULTIPLIER,
                List.of(layerPlan(age, 0, speed, scale), layerPlan(age, 1, speed, scale)));
    }

    private static GlintLayerPlan layerPlan(float age, int layer, float speed, float scale) {
        return new GlintLayerPlan(layer,
                (float) LegacyUvAnimation.classicGlintMovement(age, layer, speed),
                (float) LegacyUvAnimation.classicGlintRotation(layer),
                scale);
    }

    private static void renderPartOrAll(LegacyWavefrontModel model, ResourceLocation texture,
            ObjRenderContext context, String partName) {
        if ("all".equals(partName)) {
            model.renderAll(texture, context);
        } else {
            model.renderPart(partName, texture, context);
        }
    }

    public record GlintPlan(
            float colorMod,
            float red,
            float green,
            float blue,
            float speed,
            float scale,
            float finalRed,
            float finalGreen,
            float finalBlue,
            List<GlintLayerPlan> layers) {
    }

    public record GlintLayerPlan(int layer, float movement, float rotationDegrees, float scale) {
    }

    private LegacyObjGlintRenderer() {
    }
}

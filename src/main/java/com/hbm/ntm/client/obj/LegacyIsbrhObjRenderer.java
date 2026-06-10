package com.hbm.ntm.client.obj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector3f;

/**
 * Bridge for old ISimpleBlockRenderingHandler OBJ paths that used ObjUtil with block icons.
 */
public final class LegacyIsbrhObjRenderer {
    public static final Direction LEGACY_MODEL_FACING = Direction.EAST;

    public static void renderWithTexture(LegacyWavefrontModel model, ResourceLocation spriteTexture, ObjRenderContext context) {
        renderWithTexture(model, spriteTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderWithTexture(LegacyWavefrontModel model, ResourceLocation spriteTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians, float rollRadians) {
        model.renderWithSprite(sprite(spriteTexture), context, yawRadians, pitchRadians, rollRadians);
    }

    public static void renderWithTexture(LegacyWavefrontModel model, ObjUtilIconPlan plan, ObjRenderContext context) {
        model.renderWithSprite(sprite(plan.spriteTexture()), applyPlan(context, plan), plan.yawRadians(), plan.pitchRadians(), plan.rollRadians());
    }

    public static void renderWithTextureAdditive(LegacyWavefrontModel model, ResourceLocation spriteTexture, ObjRenderContext context) {
        renderWithTextureAdditive(model, spriteTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderWithTextureAdditive(LegacyWavefrontModel model, ResourceLocation spriteTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians, float rollRadians) {
        renderWithTexture(model, spriteTexture, context.withAdditiveTranslucency(), yawRadians, pitchRadians, rollRadians);
    }

    public static void renderPartWithTexture(LegacyWavefrontModel model, String partName, ResourceLocation spriteTexture,
            ObjRenderContext context) {
        renderPartWithTexture(model, partName, spriteTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderPartWithTexture(LegacyWavefrontModel model, String partName, ResourceLocation spriteTexture,
            ObjRenderContext context, float yawRadians, float pitchRadians, float rollRadians) {
        model.renderPartWithSprite(partName, sprite(spriteTexture), context, yawRadians, pitchRadians, rollRadians);
    }

    public static void renderPartWithTexture(LegacyWavefrontModel model, String partName, ObjUtilIconPlan plan, ObjRenderContext context) {
        model.renderPartWithSprite(partName, sprite(plan.spriteTexture()), applyPlan(context, plan.asPartBrightness()),
                plan.yawRadians(), plan.pitchRadians(), plan.rollRadians());
    }

    public static void renderPartWithTextureAdditive(LegacyWavefrontModel model, String partName, ResourceLocation spriteTexture,
            ObjRenderContext context) {
        renderPartWithTextureAdditive(model, partName, spriteTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderPartWithTextureAdditive(LegacyWavefrontModel model, String partName, ResourceLocation spriteTexture,
            ObjRenderContext context, float yawRadians, float pitchRadians, float rollRadians) {
        renderPartWithTexture(model, partName, spriteTexture, context.withAdditiveTranslucency(), yawRadians, pitchRadians, rollRadians);
    }

    public static TextureAtlasSprite sprite(ResourceLocation textureLocation) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureLocation);
    }

    public static ObjUtilIconPlan allPlan(ResourceLocation spriteTexture, float yawRadians, boolean shadow) {
        return allPlan(spriteTexture, yawRadians, 0.0F, 0.0F, shadow);
    }

    public static ObjUtilIconPlan allPlan(ResourceLocation spriteTexture, float yawRadians, float pitchRadians, boolean shadow) {
        return allPlan(spriteTexture, yawRadians, pitchRadians, 0.0F, shadow);
    }

    public static ObjUtilIconPlan allPlan(ResourceLocation spriteTexture, float yawRadians, float pitchRadians, float rollRadians, boolean shadow) {
        return new ObjUtilIconPlan(spriteTexture, yawRadians, pitchRadians, rollRadians, shadow, false, false,
                0xFFFFFF, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public static ObjUtilIconPlan partPlan(ResourceLocation spriteTexture, float yawRadians, boolean shadow) {
        return partPlan(spriteTexture, yawRadians, 0.0F, 0.0F, shadow);
    }

    public static ObjUtilIconPlan partPlan(ResourceLocation spriteTexture, float yawRadians, float pitchRadians, boolean shadow) {
        return partPlan(spriteTexture, yawRadians, pitchRadians, 0.0F, shadow);
    }

    public static ObjUtilIconPlan partPlan(ResourceLocation spriteTexture, float yawRadians, float pitchRadians, float rollRadians, boolean shadow) {
        return new ObjUtilIconPlan(spriteTexture, yawRadians, pitchRadians, rollRadians, shadow, true, false,
                0xFFFFFF, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    public static ObjUtilIconPlan allPlan(ResourceLocation spriteTexture, Direction direction, boolean shadow) {
        return allPlan(spriteTexture, LegacyObjTransforms.yawRadians(direction), LegacyObjTransforms.pitchRadians(direction), 0.0F, shadow);
    }

    public static ObjUtilIconPlan partPlan(ResourceLocation spriteTexture, Direction direction, boolean shadow) {
        return partPlan(spriteTexture, LegacyObjTransforms.yawRadians(direction), LegacyObjTransforms.pitchRadians(direction), 0.0F, shadow);
    }

    public static ObjUtilIconPlan partPlan(ResourceLocation spriteTexture, Direction direction, float rollRadians, boolean shadow) {
        return partPlan(spriteTexture, LegacyObjTransforms.yawRadians(direction), LegacyObjTransforms.pitchRadians(direction), rollRadians, shadow);
    }

    public static ObjUtilRotationPlan rotationPlan(Direction direction) {
        return rotationPlan(direction, 0.0F);
    }

    public static ObjUtilRotationPlan rotationPlan(Direction direction, float rollRadians) {
        return new ObjUtilRotationPlan(LegacyObjTransforms.yawRadians(direction), LegacyObjTransforms.pitchRadians(direction), rollRadians);
    }

    public static LegacyObjTransforms.ObjUtilDirectionPlan directionPlan(Direction direction) {
        return LegacyObjTransforms.directionPlan(direction);
    }

    public static LegacyObjTransforms.ObjUtilRotationPlan transformPlan(Direction direction) {
        return LegacyObjTransforms.objUtilRotationPlan(direction);
    }

    public static LegacyObjTransforms.ObjUtilRotationPlan transformPlan(Direction direction, float rollRadians) {
        return LegacyObjTransforms.objUtilRotationPlan(direction, rollRadians);
    }

    private static ObjRenderContext applyPlan(ObjRenderContext context, ObjUtilIconPlan plan) {
        ObjRenderContext effective = context.withRenderMode(plan.renderMode());
        effective = plan.shadow() ? effective.withLegacyShadow() : effective.withoutLegacyShadow();
        return plan.hasColor() ? effective.withColor(plan.color()) : effective;
    }

    private LegacyIsbrhObjRenderer() {
    }

    public record ObjUtilIconPlan(ResourceLocation spriteTexture, float yawRadians, float pitchRadians, float rollRadians,
            boolean shadow, boolean partBrightness, boolean hasColor, int color, LegacyTexturedRenderMode renderMode) {
        public ObjUtilIconPlan withColor(int color) {
            return new ObjUtilIconPlan(spriteTexture, yawRadians, pitchRadians, rollRadians, shadow, partBrightness,
                    true, color & 0xFFFFFF, renderMode);
        }

        public ObjUtilIconPlan withColor(int red, int green, int blue) {
            return withColor(clampColor(red) << 16 | clampColor(green) << 8 | clampColor(blue));
        }

        public ObjUtilIconPlan clearColor() {
            return new ObjUtilIconPlan(spriteTexture, yawRadians, pitchRadians, rollRadians, shadow, partBrightness,
                    false, 0xFFFFFF, renderMode);
        }

        public ObjUtilIconPlan withRenderMode(LegacyTexturedRenderMode renderMode) {
            return new ObjUtilIconPlan(spriteTexture, yawRadians, pitchRadians, rollRadians, shadow, partBrightness,
                    hasColor, color, renderMode);
        }

        public ObjUtilIconPlan additive() {
            return withRenderMode(LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
        }

        public ObjUtilIconPlan translucent() {
            return withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
        }

        public ObjUtilIconPlan asPartBrightness() {
            return new ObjUtilIconPlan(spriteTexture, yawRadians, pitchRadians, rollRadians, shadow, true,
                    hasColor, color, renderMode);
        }

        public ObjUtilIconPlan asAllBrightness() {
            return new ObjUtilIconPlan(spriteTexture, yawRadians, pitchRadians, rollRadians, shadow, false,
                    hasColor, color, renderMode);
        }

        public Vector3f rotateVertex(Vector3f vertex) {
            return LegacyObjTransforms.rotateObjUtilVertex(vertex, yawRadians, pitchRadians, rollRadians);
        }

        public Vector3f rotateNormal(Vector3f normal) {
            return LegacyObjTransforms.rotateObjUtilNormal(normal, yawRadians, pitchRadians);
        }

        public float shadowFactor(Vector3f rotatedNormal) {
            return partBrightness
                    ? LegacyObjTransforms.objUtilPartShadowFactor(rotatedNormal)
                    : LegacyObjTransforms.objUtilAllShadowFactor(rotatedNormal);
        }

        public LegacyObjTransforms.ObjUtilRotationPlan transformPlan() {
            return LegacyObjTransforms.objUtilRotationPlan(yawRadians, pitchRadians, rollRadians);
        }

        public LegacyObjTransforms.ObjUtilShadowPlan shadowPlan(Vector3f normal) {
            return LegacyObjTransforms.objUtilShadowPlan(normal, yawRadians, pitchRadians);
        }

        public LegacyObjTransforms.ObjUtilColorPlan colorPlan() {
            return hasColor
                    ? LegacyObjTransforms.objUtilColorPlan(color)
                    : LegacyObjTransforms.clearObjUtilColorPlan();
        }

        private static int clampColor(int color) {
            return Math.max(0, Math.min(255, color));
        }
    }

    public record ObjUtilRotationPlan(float yawRadians, float pitchRadians, float rollRadians) {
        public Vector3f rotateVertex(Vector3f vertex) {
            return LegacyObjTransforms.rotateObjUtilVertex(vertex, yawRadians, pitchRadians, rollRadians);
        }

        public Vector3f rotateNormal(Vector3f normal) {
            return LegacyObjTransforms.rotateObjUtilNormal(normal, yawRadians, pitchRadians);
        }
    }
}

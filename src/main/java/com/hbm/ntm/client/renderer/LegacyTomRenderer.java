package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyRenderRandom;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class LegacyTomRenderer {
    public static final float MODEL_SCALE = 100.0F;
    public static final double HMF_MODULO = 50000.0D;
    public static final double HMF_QUOTIENT = 2500.0D;
    public static final FlameScale INITIAL_FLAME_SCALE = new FlameScale(0.8F, 5.0F, 0.8F);
    public static final FlameScale LAYER_POST_SCALE = new FlameScale(-1.015F, 0.9F, 1.015F);
    public static final int FLAME_LAYERS = 20;
    public static final int FLAME_RANDOM_BOUND = 90;
    private static final int[] FLAME_RANDOM_YAWS = createFlameRandomYaws();
    private static final ThreadLocal<LegacyWavefrontModel.TexturedPreparedSequence> FLAME_SEQUENCE =
            ThreadLocal.withInitial(LegacyWavefrontModel.TexturedPreparedSequence::new);

    public static TomRenderPlan plan(long currentMillis) {
        float baseYaw = flameBaseYaw(currentMillis);
        return new TomRenderPlan(MODEL_SCALE, HMF_MODULO, HMF_QUOTIENT, INITIAL_FLAME_SCALE, baseYaw,
                flameLayers(baseYaw));
    }

    public static TomStatePlan statePlan() {
        return new TomStatePlan(false, false, true, false);
    }

    public static void renderMain(PoseStack poseStack, MultiBufferSource buffer, int packedOverlay) {
        renderAll(ObjWeaponModels.TOM_MAIN, ObjWeaponModels.TOM_MAIN_TEXTURE, poseStack, buffer,
                LightTexture.FULL_BRIGHT, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    public static void renderTom(PoseStack poseStack, MultiBufferSource buffer, int packedOverlay,
            long currentMillis) {
        poseStack.pushPose();
        try {
            applyModelScale(poseStack);
            renderMain(poseStack, buffer, packedOverlay);
            renderFlames(poseStack, buffer, packedOverlay, currentMillis);
        } finally {
            poseStack.popPose();
        }
    }

    public static void renderFlames(PoseStack poseStack, MultiBufferSource buffer, int packedOverlay,
            long currentMillis) {
        LegacyWavefrontModel.UvTransform uvTransform = hmfTransform(currentMillis);
        LegacyWavefrontModel.TexturedPreparedSequence flameSequence = FLAME_SEQUENCE.get();
        if (!ObjWeaponModels.TOM_FLAME.prepareTexturedAllSequence(flameSequence, ObjWeaponModels.TOM_FLAME_TEXTURE,
                buffer, LightTexture.FULL_BRIGHT, packedOverlay, 255, 255, 255, 255, false,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, uvTransform,
                LegacyWavefrontModel.RenderBackendFallbackReason.GPU_UNSUPPORTED_UV_TRANSFORM)) {
            return;
        }
        poseStack.pushPose();
        try {
            applyInitialFlameScale(poseStack);
            float baseYaw = flameBaseYaw(currentMillis);
            for (int i = 0; i < FLAME_LAYERS; i++) {
                LegacyPoseRotations.rotateYDegrees(poseStack, baseYaw + FLAME_RANDOM_YAWS[i]);
                flameSequence.render(poseStack);
                LegacyPoseRotations.rotateYDegrees(poseStack, -baseYaw);
                applyScale(poseStack, LAYER_POST_SCALE);
            }
        } finally {
            flameSequence.clear();
            poseStack.popPose();
        }
    }

    public static void applyModelScale(PoseStack poseStack) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    public static void applyInitialFlameScale(PoseStack poseStack) {
        applyScale(poseStack, INITIAL_FLAME_SCALE);
    }

    public static void applyBeforeFlameLayer(PoseStack poseStack, FlameLayer layer) {
        LegacyPoseRotations.rotateYDegrees(poseStack, layer.renderYaw());
    }

    public static void applyAfterFlameLayer(PoseStack poseStack, FlameLayer layer) {
        LegacyPoseRotations.rotateYDegrees(poseStack, -layer.undoYaw());
        applyScale(poseStack, layer.postScale());
    }

    public static float flameBaseYaw(long currentMillis) {
        return (float) (-(currentMillis / 10L) % 360L);
    }

    public static double hmfOffset(double currentTime) {
        return LegacyUvAnimation.legacyHmfOffset(currentTime, HMF_MODULO, HMF_QUOTIENT);
    }

    public static List<FlameLayer> flameLayers(float baseYaw) {
        List<FlameLayer> layers = new ArrayList<>(FLAME_LAYERS);
        for (int i = 0; i < FLAME_LAYERS; i++) {
            int randomYaw = FLAME_RANDOM_YAWS[i];
            layers.add(new FlameLayer(i, randomYaw, baseYaw + randomYaw, baseYaw, LAYER_POST_SCALE));
        }
        return Collections.unmodifiableList(layers);
    }

    private static int[] createFlameRandomYaws() {
        Random random = LegacyRenderRandom.seeded(0L);
        int[] yaws = new int[FLAME_LAYERS];
        for (int i = 0; i < FLAME_LAYERS; i++) {
            yaws[i] = random.nextInt(FLAME_RANDOM_BOUND);
        }
        return yaws;
    }

    private static void applyScale(PoseStack poseStack, FlameScale scale) {
        poseStack.scale(scale.x(), scale.y(), scale.z());
    }

    private static void renderAll(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            LegacyWavefrontModel.UvTransform uvTransform) {
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, false, renderMode, uvTransform);
    }

    private static LegacyWavefrontModel.UvTransform hmfTransform(long currentMillis) {
        return LegacyWavefrontModel.UvTransform.dynamic(
                1.0F,
                0.0F,
                0.0F,
                1.0F,
                0.0F,
                (float) LegacyUvAnimation.legacyHmfOffset(currentMillis, HMF_MODULO, HMF_QUOTIENT),
                LegacyUvAnimation.LEGACY_HMF_TEXTURE_OFFSET);
    }

    private LegacyTomRenderer() {
    }

    public record TomRenderPlan(
            float modelScale,
            double hmfModulo,
            double hmfQuotient,
            FlameScale initialFlameScale,
            float baseFlameYaw,
            List<FlameLayer> flameLayers) {
    }

    public record TomStatePlan(
            boolean cullEnabled,
            boolean lightingEnabled,
            boolean additiveBlend,
            boolean alphaTestEnabled) {
    }

    public record FlameScale(float x, float y, float z) {
    }

    public record FlameLayer(int index, int randomYaw, float renderYaw, float undoYaw, FlameScale postScale) {
    }
}

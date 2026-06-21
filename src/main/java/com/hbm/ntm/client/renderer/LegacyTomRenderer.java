package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

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

    public static TomRenderPlan plan(long currentMillis) {
        float baseYaw = flameBaseYaw(currentMillis);
        return new TomRenderPlan(MODEL_SCALE, HMF_MODULO, HMF_QUOTIENT, INITIAL_FLAME_SCALE, baseYaw,
                flameLayers(baseYaw));
    }

    public static TomStatePlan statePlan() {
        return new TomStatePlan(false, false, true, false);
    }

    public static void renderMain(ObjRenderContext context) {
        ObjWeaponModels.TOM_MAIN.renderAll(ObjWeaponModels.TOM_MAIN_TEXTURE, context.fullBright());
    }

    public static void renderTom(ObjRenderContext context, long currentMillis) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        try {
            applyModelScale(poseStack);
            renderMain(context);
            renderFlames(context, currentMillis);
        } finally {
            poseStack.popPose();
        }
    }

    public static void renderFlames(ObjRenderContext context, long currentMillis) {
        PoseStack poseStack = context.poseStack();
        ObjRenderContext flameContext = context.fullBright()
                .withAdditiveTranslucency()
                .withLegacyHmfAnimation((float) currentMillis, HMF_MODULO, HMF_QUOTIENT);
        poseStack.pushPose();
        try {
            applyInitialFlameScale(poseStack);
            TomRenderPlan plan = plan(currentMillis);
            for (FlameLayer layer : plan.flameLayers()) {
                applyBeforeFlameLayer(poseStack, layer);
                ObjWeaponModels.TOM_FLAME.renderAll(ObjWeaponModels.TOM_FLAME_TEXTURE, flameContext);
                applyAfterFlameLayer(poseStack, layer);
            }
        } finally {
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
        poseStack.mulPose(Axis.YP.rotationDegrees(layer.renderYaw()));
    }

    public static void applyAfterFlameLayer(PoseStack poseStack, FlameLayer layer) {
        poseStack.mulPose(Axis.YN.rotationDegrees(layer.undoYaw()));
        applyScale(poseStack, layer.postScale());
    }

    public static float flameBaseYaw(long currentMillis) {
        return (float) (-(currentMillis / 10L) % 360L);
    }

    public static double hmfOffset(double currentTime) {
        return LegacyUvAnimation.legacyHmfOffset(currentTime, HMF_MODULO, HMF_QUOTIENT);
    }

    public static List<FlameLayer> flameLayers(float baseYaw) {
        Random random = new Random(0L);
        List<FlameLayer> layers = new ArrayList<>(FLAME_LAYERS);
        for (int i = 0; i < FLAME_LAYERS; i++) {
            int randomYaw = random.nextInt(FLAME_RANDOM_BOUND);
            layers.add(new FlameLayer(i, randomYaw, baseYaw + randomYaw, baseYaw, LAYER_POST_SCALE));
        }
        return Collections.unmodifiableList(layers);
    }

    private static void applyScale(PoseStack poseStack, FlameScale scale) {
        poseStack.scale(scale.x(), scale.y(), scale.z());
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

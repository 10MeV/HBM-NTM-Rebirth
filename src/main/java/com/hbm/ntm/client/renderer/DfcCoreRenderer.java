package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.DfcCoreBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Modern RenderCore carrier.  The core has no baked body: its standby orb, active shells and
 * blocked-meltdown flare are all the original TESR visual contract.
 */
public final class DfcCoreRenderer implements BlockEntityRenderer<DfcCoreBlockEntity> {
    public DfcCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(DfcCoreBlockEntity core, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(core, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(core, getViewDistance());
    }

    @Override
    public void render(DfcCoreBlockEntity core, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(core, getViewDistance())) {
            return;
        }
        long millis = System.currentTimeMillis();
        long worldTime = gameTime(core);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        try (var scope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(core)) {
            if (core.getHeat() == 0) {
                renderStandby(millis, poseStack, buffer);
            } else if (core.isMeltdownTick()) {
                renderFlare(core.getColor(), worldTime, poseStack, buffer);
            } else {
                renderOrb(core, worldTime, poseStack, buffer);
            }
        }
        poseStack.popPose();
    }

    private static void renderStandby(long millis, PoseStack poseStack, MultiBufferSource buffer) {
        var plan = LegacyTileRenderPlans.coreStandbyPlan(millis);
        renderSphere(ObjEffectModels.SPHERE_UV, plan.base(), poseStack, buffer, false);
        renderSphere(ObjEffectModels.SPHERE_UV, plan.glow(), poseStack, buffer, true);
        if (plan.sparkTick()) {
            LegacyMachineEffectPresenter.enqueueSparkGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                    LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, group -> plan.sparks().forEach(spark ->
                            group.add(spark.seed(), spark.x(), spark.y(), spark.z(), 0.0F, spark.length(),
                                    spark.width(), spark.steps(), spark.outerColor(), spark.innerColor())));
        }
    }

    private static void renderOrb(DfcCoreBlockEntity core, long worldTime, PoseStack poseStack,
            MultiBufferSource buffer) {
        int fill = core.getFuel1().getFill() + core.getFuel2().getFill();
        int capacity = core.getFuel1().getMaxFill() + core.getFuel2().getMaxFill();
        var plan = LegacyTileRenderPlans.coreOrbPlan(core.getColor(), fill, capacity, worldTime);
        renderSphere(ObjEffectModels.SPHERE_RUV, plan.base(), poseStack, buffer, false);
        for (var shell : plan.glowShells()) {
            renderSphere(ObjEffectModels.SPHERE_RUV, shell, poseStack, buffer, true);
        }
    }

    private static void renderSphere(com.hbm.ntm.client.obj.LegacyWavefrontModel model,
            LegacyTileRenderPlans.ModelSpherePlan plan, PoseStack poseStack, MultiBufferSource buffer,
            boolean additive) {
        poseStack.pushPose();
        float scale = (float) plan.scale();
        poseStack.scale(scale, scale, scale);
        model.renderAllUntextured(poseStack, buffer, plan.color().redByte(), plan.color().greenByte(),
                plan.color().blueByte(), plan.color().alphaByte(), additive);
        poseStack.popPose();
    }

    private static void renderFlare(int color, long worldTime, PoseStack poseStack, MultiBufferSource buffer) {
        var plan = LegacyTileRenderPlans.coreFlarePlan(color, worldTime);
        poseStack.pushPose();
        float scale = (float) plan.scale();
        poseStack.scale(scale, scale, scale);
        for (var ray : plan.rays()) {
            poseStack.pushPose();
            com.hbm.ntm.client.render.LegacyPoseRotations.rotateXDegrees(poseStack, ray.rotateX0());
            com.hbm.ntm.client.render.LegacyPoseRotations.rotateYDegrees(poseStack, ray.rotateY0());
            com.hbm.ntm.client.render.LegacyPoseRotations.rotateZDegrees(poseStack, ray.rotateZ0());
            com.hbm.ntm.client.render.LegacyPoseRotations.rotateXDegrees(poseStack, ray.rotateX1());
            com.hbm.ntm.client.render.LegacyPoseRotations.rotateYDegrees(poseStack, ray.rotateY1());
            com.hbm.ntm.client.render.LegacyPoseRotations.rotateZDegrees(poseStack, (float) ray.rotateZ1());
            renderRay(LegacyUntexturedQuadRenderer.directTriangleBatch(poseStack, buffer,
                    LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE), ray);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderRay(LegacyUntexturedQuadRenderer.DirectTriangleBatch triangles,
            LegacyTileRenderPlans.CoreFlareRayPlan ray) {
        double halfWidth = ray.radius() * 0.866D;
        double halfDepth = ray.radius() * 0.5D;
        int center = ray.centerColor().redByte() << 16 | ray.centerColor().greenByte() << 8
                | ray.centerColor().blueByte();
        int edge = ray.edgeColor().redByte() << 16 | ray.edgeColor().greenByte() << 8 | ray.edgeColor().blueByte();
        int centerAlpha = ray.centerColor().alphaByte();
        int edgeAlpha = ray.edgeColor().alphaByte();
        LegacyUntexturedQuadRenderer.triangleDirect(triangles,
                0.0D, 0.0D, 0.0D, center, centerAlpha,
                -halfWidth, ray.length(), -halfDepth, edge, edgeAlpha,
                halfWidth, ray.length(), -halfDepth, edge, edgeAlpha);
        LegacyUntexturedQuadRenderer.triangleDirect(triangles,
                0.0D, 0.0D, 0.0D, center, centerAlpha,
                halfWidth, ray.length(), -halfDepth, edge, edgeAlpha,
                0.0D, ray.length(), ray.radius(), edge, edgeAlpha);
        LegacyUntexturedQuadRenderer.triangleDirect(triangles,
                0.0D, 0.0D, 0.0D, center, centerAlpha,
                0.0D, ray.length(), ray.radius(), edge, edgeAlpha,
                -halfWidth, ray.length(), -halfDepth, edge, edgeAlpha);
    }

    private static long gameTime(DfcCoreBlockEntity core) {
        Level level = core.getLevel();
        return level == null ? 0L : level.getGameTime();
    }
}

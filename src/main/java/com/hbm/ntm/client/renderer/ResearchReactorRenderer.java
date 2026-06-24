package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.ResearchReactorBlockEntity;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ResearchReactorRenderer implements BlockEntityRenderer<ResearchReactorBlockEntity> {
    private static final int CHERENKOV_RED = Math.round(0.4F * 255.0F);
    private static final int CHERENKOV_GREEN = Math.round(0.9F * 255.0F);
    private static final int CHERENKOV_BLUE = Math.round(1.0F * 255.0F);

    public ResearchReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ResearchReactorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(ResearchReactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int light = LegacyRenderLighting.resolveBoundsLight(blockEntity, blockEntity.getRenderBoundingBox(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        ObjReactorModels.SMALL_BASE.renderAll(ObjReactorModels.SMALL_BASE_TEXTURE, poseStack, buffer,
                light, packedOverlay);
        double level = Mth.lerp(partialTick, blockEntity.getLastLevel(), blockEntity.getLevelValue());
        poseStack.pushPose();
        poseStack.translate(0.0D, level, 0.0D);
        ObjReactorModels.SMALL_RODS.renderAll(ObjReactorModels.SMALL_RODS_TEXTURE, poseStack, buffer,
                light, packedOverlay);
        poseStack.popPose();
        renderCherenkov(blockEntity, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderCherenkov(ResearchReactorBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer) {
        int totalFlux = blockEntity.getTotalFlux();
        if (totalFlux <= 10 || !blockEntity.isSubmerged()) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.additiveNoCull(buffer);
        PoseStack.Pose pose = poseStack.last();
        for (double d = LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_START;
                d < LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_END;
                d += LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_STEP) {
            float alpha = (float) (0.025D + random.nextDouble() * 0.015D + 0.125D * totalFlux / 1000.0D);
            int alphaByte = LegacyUntexturedQuadRenderer.alpha(alpha);
            double bottom = LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_CENTER_Y - d;
            double top = LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_CENTER_Y + d;
            renderCherenkovBox(consumer, pose, d, bottom, top, alphaByte);
        }
    }

    private static void renderCherenkovBox(VertexConsumer consumer, PoseStack.Pose pose,
            double d, double bottom, double top, int alpha) {
        vertex(consumer, pose, d, bottom, -d, alpha);
        vertex(consumer, pose, d, top, -d, alpha);
        vertex(consumer, pose, d, top, d, alpha);
        vertex(consumer, pose, d, bottom, d, alpha);

        vertex(consumer, pose, -d, bottom, -d, alpha);
        vertex(consumer, pose, -d, top, -d, alpha);
        vertex(consumer, pose, -d, top, d, alpha);
        vertex(consumer, pose, -d, bottom, d, alpha);

        vertex(consumer, pose, -d, bottom, d, alpha);
        vertex(consumer, pose, -d, top, d, alpha);
        vertex(consumer, pose, d, top, d, alpha);
        vertex(consumer, pose, d, bottom, d, alpha);

        vertex(consumer, pose, -d, bottom, -d, alpha);
        vertex(consumer, pose, -d, top, -d, alpha);
        vertex(consumer, pose, d, top, -d, alpha);
        vertex(consumer, pose, d, bottom, -d, alpha);

        vertex(consumer, pose, -d, top, -d, alpha);
        vertex(consumer, pose, -d, top, d, alpha);
        vertex(consumer, pose, d, top, d, alpha);
        vertex(consumer, pose, d, top, -d, alpha);

        vertex(consumer, pose, -d, bottom, -d, alpha);
        vertex(consumer, pose, -d, bottom, d, alpha);
        vertex(consumer, pose, d, bottom, d, alpha);
        vertex(consumer, pose, d, bottom, -d, alpha);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
            double x, double y, double z, int alpha) {
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x, y, z,
                CHERENKOV_RED, CHERENKOV_GREEN, CHERENKOV_BLUE, alpha);
    }
}

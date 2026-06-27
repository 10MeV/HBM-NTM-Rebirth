package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.ResearchReactorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
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
    private static final int CHERENKOV_COLOR = CHERENKOV_RED << 16 | CHERENKOV_GREEN << 8 | CHERENKOV_BLUE;

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
        int totalFlux = blockEntity.getTotalFlux();
        if (totalFlux > 10 && blockEntity.isSubmerged()) {
            LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                    queuedPose -> renderCherenkov(totalFlux, queuedPose, buffer));
        }
        poseStack.popPose();
    }

    private static void renderCherenkov(int totalFlux, PoseStack poseStack,
            MultiBufferSource buffer) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (double d = LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_START;
                d < LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_END;
                d += LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_STEP) {
            float alpha = (float) (0.025D + random.nextDouble() * 0.015D + 0.125D * totalFlux / 1000.0D);
            int alphaByte = LegacyUntexturedQuadRenderer.alpha(alpha);
            double bottom = LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_CENTER_Y - d;
            double top = LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_CENTER_Y + d;
            renderCherenkovBox(poseStack, buffer, d, bottom, top, alphaByte);
        }
    }

    private static void renderCherenkovBox(PoseStack poseStack, MultiBufferSource buffer,
            double d, double bottom, double top, int alpha) {
        quad(poseStack, buffer, d, bottom, -d, d, top, -d, d, top, d, d, bottom, d, alpha);
        quad(poseStack, buffer, -d, bottom, -d, -d, top, -d, -d, top, d, -d, bottom, d, alpha);
        quad(poseStack, buffer, -d, bottom, d, -d, top, d, d, top, d, d, bottom, d, alpha);
        quad(poseStack, buffer, -d, bottom, -d, -d, top, -d, d, top, -d, d, bottom, -d, alpha);
        quad(poseStack, buffer, -d, top, -d, -d, top, d, d, top, d, d, top, -d, alpha);
        quad(poseStack, buffer, -d, bottom, -d, -d, bottom, d, d, bottom, d, d, bottom, -d, alpha);
    }

    private static void quad(PoseStack poseStack, MultiBufferSource buffer,
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3, int alpha) {
        LegacyUntexturedQuadRenderer.quad(poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                x0, y0, z0,
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3,
                CHERENKOV_COLOR, alpha, alpha, alpha, alpha);
    }
}

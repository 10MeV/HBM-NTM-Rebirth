package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.ResearchReactorBlockEntity;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class ResearchReactorRenderer implements BlockEntityRenderer<ResearchReactorBlockEntity> {
    public ResearchReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ResearchReactorBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
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
        List<Double> randomUnits = new ArrayList<>(smallReactorCherenkovShellCount());
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < smallReactorCherenkovShellCount(); i++) {
            randomUnits.add(random.nextDouble());
        }
        LegacyTileRenderPlans.CherenkovShellPlan plan =
                LegacyTileRenderPlans.smallReactorCherenkovPlan(totalFlux, true, randomUnits);
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.additiveNoCull(buffer);
        PoseStack.Pose pose = poseStack.last();
        for (LegacyTileRenderPlans.UntexturedQuadPlan quad : plan.shells()) {
            for (LegacyTileRenderPlans.UntexturedVertexPlan vertex : quad.vertices()) {
                LegacyTileRenderPlans.RgbaPlan color = vertex.color();
                LegacyUntexturedQuadRenderer.vertexRgbaF(consumer, pose,
                        vertex.x(), vertex.y(), vertex.z(),
                        color.red(), color.green(), color.blue(), color.alpha());
            }
        }
    }

    private static int smallReactorCherenkovShellCount() {
        return (int) Math.ceil((LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_END
                - LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_START)
                / LegacyTileRenderPlans.SMALL_REACTOR_CHERENKOV_STEP);
    }
}

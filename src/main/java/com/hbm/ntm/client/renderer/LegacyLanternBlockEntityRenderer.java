package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.LegacyLanternBlockEntity;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LegacyLanternBlockEntityRenderer implements BlockEntityRenderer<LegacyLanternBlockEntity> {
    private static final float[][] LIGHT_VERTICES = {
            {0.6875F, 4.0625F, 0.6875F},
            {0.8125F, 4.8125F, 0.8125F},
            {0.6875F, 4.0625F, 0.3125F},
            {0.8125F, 4.8125F, 0.1875F},
            {0.3183F, 4.0625F, 0.6875F},
            {0.1933F, 4.8125F, 0.8125F},
            {0.3183F, 4.0625F, 0.3125F},
            {0.1933F, 4.8125F, 0.1875F}
    };
    private static final int[][] LIGHT_FACES = {
            {1, 4, 3, 2},
            {7, 6, 5, 8},
            {3, 8, 4, 7},
            {1, 2, 6, 5}
    };

    public LegacyLanternBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LegacyLanternBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.solid(buffer);
        LegacyTileRenderPlans.LanternLightPlan plan =
                LegacyTileRenderPlans.lanternLightPlan(System.currentTimeMillis());
        PoseStack.Pose pose = poseStack.last();

        for (int[] face : LIGHT_FACES) {
            renderFace(consumer, pose, face, plan.red(), plan.green(), plan.blue(), plan.alpha());
            for (int i = face.length - 1; i >= 0; i--) {
                renderVertex(consumer, pose, face[i], plan.red(), plan.green(), plan.blue(), plan.alpha());
            }
        }
    }

    private static void renderFace(VertexConsumer consumer, PoseStack.Pose pose, int[] face,
            int red, int green, int blue, int alpha) {
        for (int index : face) {
            renderVertex(consumer, pose, index, red, green, blue, alpha);
        }
    }

    private static void renderVertex(VertexConsumer consumer, PoseStack.Pose pose, int index,
            int red, int green, int blue, int alpha) {
        float[] vertex = LIGHT_VERTICES[index - 1];
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, vertex[0], vertex[1], vertex[2], red, green, blue, alpha);
    }
}

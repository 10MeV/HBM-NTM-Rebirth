package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.LegacyLanternBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

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
    public boolean shouldRender(LegacyLanternBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(LegacyLanternBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            float multiplier = ((float) (Math.sin(System.currentTimeMillis()
                    / LegacyTileRenderPlans.LANTERN_LIGHT_PERIOD_DIVISOR) / 2.0D + 0.5D))
                    * LegacyTileRenderPlans.LANTERN_LIGHT_RANGE + LegacyTileRenderPlans.LANTERN_LIGHT_BASE;
            int red = Math.round(LegacyTileRenderPlans.LANTERN_LIGHT_RED * multiplier);
            int green = Math.round(LegacyTileRenderPlans.LANTERN_LIGHT_GREEN * multiplier);
            int blue = Math.round(LegacyTileRenderPlans.LANTERN_LIGHT_BLUE * multiplier);
            int color = red << 16 | green << 8 | blue;
            LegacyUntexturedQuadRenderer.DirectQuadBatch batch =
                    LegacyUntexturedQuadRenderer.directQuadBatch(poseStack, buffer,
                            LegacyTexturedRenderMode.CUTOUT_NO_CULL);

            for (int[] face : LIGHT_FACES) {
                renderFace(batch, face[0], face[1], face[2], face[3], color, 255);
                renderFace(batch, face[3], face[2], face[1], face[0], color, 255);
            }
        }
    }

    private static void renderFace(LegacyUntexturedQuadRenderer.DirectQuadBatch batch,
            int i0, int i1, int i2, int i3, int color, int alpha) {
        float[] v0 = LIGHT_VERTICES[i0 - 1];
        float[] v1 = LIGHT_VERTICES[i1 - 1];
        float[] v2 = LIGHT_VERTICES[i2 - 1];
        float[] v3 = LIGHT_VERTICES[i3 - 1];
        LegacyUntexturedQuadRenderer.quadDirect(batch,
                v0[0], v0[1], v0[2],
                v1[0], v1[1], v1[2],
                v2[0], v2[1], v2[2],
                v3[0], v3[1], v3[2],
                color, alpha, alpha, alpha, alpha);
    }
}

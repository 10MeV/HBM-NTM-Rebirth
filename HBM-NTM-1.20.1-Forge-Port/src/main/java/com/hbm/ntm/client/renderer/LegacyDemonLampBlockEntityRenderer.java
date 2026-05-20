package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyDemonLampBlock;
import com.hbm.ntm.blockentity.LegacyDemonLampBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyDemonLampBlockEntityRenderer implements BlockEntityRenderer<LegacyDemonLampBlockEntity> {
    public LegacyDemonLampBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LegacyDemonLampBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyDemonLampBlock)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyObjTransforms.applySixFaceAttachmentRotation(poseStack, state.getValue(LegacyDemonLampBlock.FACE));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        ObjModelLibrary.DEMON_LAMP.render(new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay));
        renderAura(poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderAura(PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.lightning(buffer);
        PoseStack.Pose pose = poseStack.last();
        double near = 0.375D;
        double far = 15.0D;
        int segments = 16;

        for (int j = 0; j < 2; j++) {
            double h = 0.5D;
            double height = j == 0 ? -h : h;
            double yNear = 0.5D + j * 0.125D;
            double yFar = 1.0D + j * 0.125D + height;

            renderAuraRing(consumer, pose, near, far, yNear, yFar, segments, 64);
        }
    }

    private static void renderAuraRing(VertexConsumer consumer, PoseStack.Pose pose, double near, double far,
                                       double yNear, double yFar, int segments, int nearAlpha) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0D / segments * i;
            double a1 = Math.PI * 2.0D / segments * (i + 1);
            double x0 = Math.cos(a0);
            double z0 = Math.sin(a0);
            double x1 = Math.cos(a1);
            double z1 = Math.sin(a1);

            renderAuraQuad(
                    consumer,
                    pose,
                    x0 * near + 0.5D, yNear, z0 * near + 0.5D,
                    x0 * far + 0.5D, yFar, z0 * far + 0.5D,
                    x1 * far + 0.5D, yFar, z1 * far + 0.5D,
                    x1 * near + 0.5D, yNear, z1 * near + 0.5D,
                    nearAlpha);
        }
    }

    private static void renderAuraQuad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3,
            int nearAlpha) {
        LegacyUntexturedQuadRenderer.doubleSidedQuad(
                consumer,
                pose,
                x0, y0, z0,
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3,
                0, 191, 255,
                nearAlpha, 0, 0, nearAlpha);
    }
}

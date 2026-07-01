package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyDemonLampBlock;
import com.hbm.ntm.blockentity.LegacyDemonLampBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjLightModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyDemonLampBlockEntityRenderer implements BlockEntityRenderer<LegacyDemonLampBlockEntity> {
    public LegacyDemonLampBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(LegacyDemonLampBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(LegacyDemonLampBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyDemonLampBlock)) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyObjTransforms.applySixFaceAttachmentRotation(poseStack, state.getValue(LegacyDemonLampBlock.FACE));
        poseStack.translate(0.0D, -0.5D, 0.0D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjLightModels.DEMON_LAMP_LEGACY.renderAll(poseStack, buffer, modelLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL);
        }
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> renderAura(queuedPose, buffer));
        poseStack.popPose();
    }

    private static void renderAura(PoseStack poseStack, MultiBufferSource buffer) {
        double near = 0.375D;
        double far = 15.0D;
        int segments = 16;

        for (int j = 0; j < 2; j++) {
            double h = 0.5D;
            double height = j == 0 ? -h : h;
            double yNear = 0.5D + j * 0.125D;
            double yFar = 1.0D + j * 0.125D + height;

            renderAuraRing(poseStack, buffer, near, far, yNear, yFar, segments, 64);
        }
    }

    private static void renderAuraRing(PoseStack poseStack, MultiBufferSource buffer, double near, double far,
                                       double yNear, double yFar, int segments, int nearAlpha) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0D / segments * i;
            double a1 = Math.PI * 2.0D / segments * (i + 1);
            double x0 = Math.cos(a0);
            double z0 = Math.sin(a0);
            double x1 = Math.cos(a1);
            double z1 = Math.sin(a1);

            renderAuraQuad(
                    poseStack,
                    buffer,
                    x0 * near, yNear, z0 * near,
                    x0 * far, yFar, z0 * far,
                    x1 * far, yFar, z1 * far,
                    x1 * near, yNear, z1 * near,
                    nearAlpha);
        }
    }

    private static void renderAuraQuad(
            PoseStack poseStack,
            MultiBufferSource buffer,
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3,
            int nearAlpha) {
        LegacyUntexturedQuadRenderer.quad(
                poseStack,
                buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                x0, y0, z0,
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3,
                0x00BFFF,
                nearAlpha, 0, 0, nearAlpha);
    }
}

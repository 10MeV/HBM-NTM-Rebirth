package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyDemonLampBlock;
import com.hbm.ntm.blockentity.LegacyDemonLampBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjLightModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.UntexturedQuadGroup;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LegacyDemonLampBlockEntityRenderer implements BlockEntityRenderer<LegacyDemonLampBlockEntity> {
    private static final double AURA_NEAR = 0.375D;
    private static final double AURA_FAR = 15.0D;
    private static final int AURA_SEGMENTS = 16;
    private static final int AURA_COLOR = 0x00BFFF;
    private static final int AURA_NEAR_ALPHA = 64;
    private static final double[] AURA_UNIT_X = new double[AURA_SEGMENTS + 1];
    private static final double[] AURA_UNIT_Z = new double[AURA_SEGMENTS + 1];

    static {
        double step = Math.PI * 2.0D / AURA_SEGMENTS;
        for (int i = 0; i <= AURA_SEGMENTS; i++) {
            double angle = step * i;
            AURA_UNIT_X[i] = Math.cos(angle);
            AURA_UNIT_Z[i] = Math.sin(angle);
        }
    }

    public LegacyDemonLampBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(LegacyDemonLampBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(LegacyDemonLampBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
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
        LegacyMachineEffectPresenter.enqueueUntexturedQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0, LegacyDemonLampBlockEntityRenderer::renderAura);
        poseStack.popPose();
    }

    private static void renderAura(UntexturedQuadGroup group) {
        for (int j = 0; j < 2; j++) {
            double h = 0.5D;
            double height = j == 0 ? -h : h;
            double yNear = 0.5D + j * 0.125D;
            double yFar = 1.0D + j * 0.125D + height;

            renderAuraRing(group, yNear, yFar);
        }
    }

    private static void renderAuraRing(UntexturedQuadGroup group, double yNear, double yFar) {
        for (int i = 0; i < AURA_SEGMENTS; i++) {
            double x0 = AURA_UNIT_X[i];
            double z0 = AURA_UNIT_Z[i];
            double x1 = AURA_UNIT_X[i + 1];
            double z1 = AURA_UNIT_Z[i + 1];

            group.add(
                    x0 * AURA_NEAR, yNear, z0 * AURA_NEAR,
                    x0 * AURA_FAR, yFar, z0 * AURA_FAR,
                    x1 * AURA_FAR, yFar, z1 * AURA_FAR,
                    x1 * AURA_NEAR, yNear, z1 * AURA_NEAR,
                    AURA_COLOR, AURA_NEAR_ALPHA, 0, 0, AURA_NEAR_ALPHA);
        }
    }
}

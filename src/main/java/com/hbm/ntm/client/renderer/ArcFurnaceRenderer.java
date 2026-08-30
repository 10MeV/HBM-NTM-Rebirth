package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ArcFurnaceBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ArcFurnaceRenderer implements BlockEntityRenderer<ArcFurnaceBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_ARC_FURNACE;

    public ArcFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ArcFurnaceBlockEntity blockEntity) {
        return HbmShaderCompatibilityDetector.shouldRenderBlockEntityOffScreen();
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ArcFurnaceBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(ArcFurnaceBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }
        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, definition.yRotation(state));
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));

            LegacyTexturedRenderMode renderMode = LegacyMachinePartRenderContexts.renderMode(definition.renderMode());
            if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                LegacyArcFurnaceRenderHelper.renderStaticShell(MODEL, poseStack, buffer, modelLight, packedOverlay,
                        renderMode);
            }
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                LegacyArcFurnaceRenderHelper.renderAnimatedDirect(MODEL,
                        blockEntity.getPreviousLid(),
                        blockEntity.getLid(),
                        blockEntity.isProgressing(),
                        blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime(),
                        partialTick,
                        blockEntity.getLiquidAmount(),
                        blockEntity.getMaxLiquid(),
                        blockEntity.hasMaterial(),
                        blockEntity.electrodeStateInSlot(ArcFurnaceBlockEntity.SLOT_ELECTRODE_0),
                        blockEntity.electrodeStateInSlot(ArcFurnaceBlockEntity.SLOT_ELECTRODE_1),
                        blockEntity.electrodeStateInSlot(ArcFurnaceBlockEntity.SLOT_ELECTRODE_2),
                        poseStack, buffer, packedLight, packedOverlay, renderMode);
            }
            poseStack.popPose();
        }
    }
}

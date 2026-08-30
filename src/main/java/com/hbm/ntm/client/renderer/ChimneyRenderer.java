package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.ChimneyBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.blockentity.ChimneyBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Shared 1.7.10-aligned BER for the brick and industrial smokestacks. */
public final class ChimneyRenderer implements BlockEntityRenderer<ChimneyBlockEntity> {
    public ChimneyRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChimneyBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(ChimneyBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(ChimneyBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof ChimneyBlock chimney)) {
            return;
        }
        LegacyMachineDefinition definition = chimney.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        LegacyWavefrontModel model = state.is(ModBlocks.CHIMNEY_INDUSTRIAL.get())
                ? ObjMachineModels.CHIMNEY_INDUSTRIAL_LEGACY
                : ObjMachineModels.CHIMNEY_BRICK_LEGACY;

        try (var submission = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity);
                var lighting = LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight,
                    OverlayTexture.NO_OVERLAY, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            poseStack.popPose();
        }
    }
}

package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyFanBlock;
import com.hbm.ntm.blockentity.LegacyFanBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyFanRenderer implements BlockEntityRenderer<LegacyFanBlockEntity> {
    private static final LegacyWavefrontModel.SelectionHandle FRAME =
            ObjMachineModels.FAN_LEGACY.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle BLADES =
            ObjMachineModels.FAN_LEGACY.prepareRenderOnlyInCallOrder("Blades");

    public LegacyFanRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(LegacyFanBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyFanBlock) || !state.hasProperty(LegacyFanBlock.FACING)) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            applyLegacyFacingTransform(poseStack, state.getValue(LegacyFanBlock.FACING));
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.spin(partialTick)));
                renderPart(BLADES, poseStack, buffer, modelLight, packedOverlay);
            }
            poseStack.popPose();
        }
    }

    public static void renderItemModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, BlockState state) {
        ObjMachineModels.FAN_LEGACY.renderAll(poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_CULL);
    }

    private static void applyLegacyFacingTransform(PoseStack poseStack, Direction direction) {
        poseStack.translate(0.0D, 0.5D, 0.0D);
        switch (direction) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
            default -> {
            }
        }
        poseStack.translate(0.0D, -0.5D, 0.0D);
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjMachineModels.FAN_LEGACY.renderOnlyInCallOrder(ObjMachineModels.FAN_TEXTURE, poseStack, buffer,
                packedLight, packedOverlay, handle, LegacyTexturedRenderMode.CUTOUT_CULL);
    }
}

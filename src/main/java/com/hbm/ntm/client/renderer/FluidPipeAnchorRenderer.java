package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class FluidPipeAnchorRenderer implements BlockEntityRenderer<FluidPipeAnchorBlockEntity> {
    public FluidPipeAnchorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(FluidPipeAnchorBlockEntity anchor, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(anchor, packedLight);
        Direction facing = anchor.getBlockState().hasProperty(FluidPipeAnchorBlock.FACING)
                ? anchor.getBlockState().getValue(FluidPipeAnchorBlock.FACING)
                : Direction.UP;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        rotateToFacing(poseStack, facing);
        poseStack.translate(0.0D, -0.5D, 0.0D);
        ObjNetworkModels.PIPE_ANCHOR_LEGACY.renderPart("Anchor", ObjNetworkModels.texture("pipe_anchor"),
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    private static void rotateToFacing(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case UP -> {
            }
            case NORTH -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            case EAST -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(270.0F));
            }
        }
    }
}

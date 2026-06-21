package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.hbm.ntm.util.ColorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

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
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(anchor, packedLight);
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

        renderRemoteConnections(anchor, poseStack, buffer, modelLight, packedOverlay);
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

    private static void renderRemoteConnections(FluidPipeAnchorBlockEntity anchor, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = anchor.getLevel();
        if (level == null) {
            return;
        }

        Vec3 anchorPoint = center(anchor.getBlockPos());
        for (BlockPos remotePos : anchor.getRemoteConnections()) {
            BlockEntity blockEntity = level.getBlockEntity(remotePos);
            if (!(blockEntity instanceof FluidPipeAnchorBlockEntity other)
                    || anchor.getFluidType() != other.getFluidType()
                    || !isDominant(anchorPoint, center(remotePos))) {
                continue;
            }
            renderConnection(anchorPoint, center(remotePos), anchor.getFluidType().getColor(),
                    poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderConnection(Vec3 anchorPoint, Vec3 connectionPoint, int fluidColor,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double dX = connectionPoint.x - anchorPoint.x;
        double dY = connectionPoint.y - anchorPoint.y;
        double dZ = connectionPoint.z - anchorPoint.z;
        double hyp = Math.sqrt(dX * dX + dZ * dZ);
        double yaw = Math.toDegrees(Math.atan2(dX, dZ));
        double pitch = Math.toDegrees(Math.atan2(dY, hyp));
        double length = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        if (length <= 0.0D) {
            return;
        }

        int color = ColorUtil.lightenColor(fluidColor, 0.25D);
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (90.0D - pitch)));

        poseStack.pushPose();
        poseStack.scale(1.0F, (float) length, 1.0F);
        poseStack.translate(0.0D, -0.5D, 0.0D);
        ObjNetworkModels.PIPE_ANCHOR_LEGACY.renderPart("Pipe", ObjNetworkModels.texture("pipe_anchor"),
                poseStack, buffer, packedLight, packedOverlay, red, green, blue, 255);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, length / 2.0D - 1.5D, 0.0D);
        ObjNetworkModels.PIPE_ANCHOR_LEGACY.renderPart("Ring", ObjNetworkModels.texture("pipe_anchor"),
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static Vec3 center(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    /**
     * Matches the legacy renderer's single-owner rule so a connected pair does not render the pipe twice.
     */
    public static boolean isDominant(Vec3 first, Vec3 second) {
        if (first.x < second.x) {
            return true;
        }
        if (first.x > second.x) {
            return false;
        }
        if (first.y < second.y) {
            return true;
        }
        if (first.y > second.y) {
            return false;
        }
        return first.z < second.z;
    }
}

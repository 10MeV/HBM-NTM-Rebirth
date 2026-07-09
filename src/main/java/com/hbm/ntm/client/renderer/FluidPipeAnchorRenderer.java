package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.hbm.ntm.util.ColorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class FluidPipeAnchorRenderer implements BlockEntityRenderer<FluidPipeAnchorBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjNetworkModels.PIPE_ANCHOR_LEGACY;
    private static final ResourceLocation TEXTURE = ObjNetworkModels.texture("pipe_anchor");
    private static final LegacyWavefrontModel.SelectionHandle ANCHOR =
            MODEL.prepareRenderOnlyInCallOrder("Anchor");
    private static final LegacyWavefrontModel.SelectionHandle PIPE =
            MODEL.prepareRenderOnlyInCallOrder("Pipe");
    private static final LegacyWavefrontModel.SelectionHandle RING =
            MODEL.prepareRenderOnlyInCallOrder("Ring");

    public FluidPipeAnchorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FluidPipeAnchorBlockEntity anchor) {
        return false;
    }

    @Override
    public boolean shouldRender(FluidPipeAnchorBlockEntity anchor, Vec3 cameraPos) {
        return hasBerVisuals(anchor)
                && BlockEntityRenderer.super.shouldRender(anchor, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(anchor, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(FluidPipeAnchorBlockEntity anchor, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean renderStaticAnchor = LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
        boolean renderRemoteConnections = anchor.hasRemoteConnections();
        if (!renderStaticAnchor && !renderRemoteConnections) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(anchor, getViewDistance())) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(anchor, packedLight);
        Direction facing = anchor.getBlockState().hasProperty(FluidPipeAnchorBlock.FACING)
                ? anchor.getBlockState().getValue(FluidPipeAnchorBlock.FACING)
                : Direction.UP;

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(anchor)) {
            if (renderStaticAnchor) {
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.5D, 0.5D);
                rotateToFacing(poseStack, facing);
                poseStack.translate(0.0D, -0.5D, 0.0D);
                renderPart(ANCHOR, poseStack, buffer, modelLight, packedOverlay);
                poseStack.popPose();
            }

            if (renderRemoteConnections) {
                renderRemoteConnections(anchor, poseStack, buffer, modelLight, packedOverlay);
            }
        }
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

        BlockPos anchorPos = anchor.getBlockPos();
        double anchorX = anchorPos.getX() + 0.5D;
        double anchorY = anchorPos.getY() + 0.5D;
        double anchorZ = anchorPos.getZ() + 0.5D;
        var fluidType = anchor.getFluidType();
        int fluidColor = fluidType.getColor();
        for (BlockPos remotePos : anchor.getRemoteConnectionsView()) {
            BlockEntity blockEntity = level.getBlockEntity(remotePos);
            double connectionX = remotePos.getX() + 0.5D;
            double connectionY = remotePos.getY() + 0.5D;
            double connectionZ = remotePos.getZ() + 0.5D;
            if (!(blockEntity instanceof FluidPipeAnchorBlockEntity other)
                    || fluidType != other.getFluidType()
                    || !isDominant(anchorX, anchorY, anchorZ, connectionX, connectionY, connectionZ)) {
                continue;
            }
            renderConnection(anchorX, anchorY, anchorZ, connectionX, connectionY, connectionZ,
                    fluidColor,
                    poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static boolean hasBerVisuals(FluidPipeAnchorBlockEntity anchor) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer() || anchor.hasRemoteConnections();
    }

    private static void renderConnection(double anchorX, double anchorY, double anchorZ,
            double connectionX, double connectionY, double connectionZ, int fluidColor,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double dX = connectionX - anchorX;
        double dY = connectionY - anchorY;
        double dZ = connectionZ - anchorZ;
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
        renderPart(PIPE, poseStack, buffer, packedLight, packedOverlay, red, green, blue);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, length / 2.0D - 1.5D, 0.0D);
        renderPart(RING, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay, handle);
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int red, int green, int blue) {
        MODEL.renderOnlyInCallOrder(TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, 255, false, handle);
    }

    /**
     * Matches the legacy renderer's single-owner rule so a connected pair does not render the pipe twice.
     */
    public static boolean isDominant(double firstX, double firstY, double firstZ,
            double secondX, double secondY, double secondZ) {
        if (firstX < secondX) {
            return true;
        }
        if (firstX > secondX) {
            return false;
        }
        if (firstY < secondY) {
            return true;
        }
        if (firstY > secondY) {
            return false;
        }
        return firstZ < secondZ;
    }
}

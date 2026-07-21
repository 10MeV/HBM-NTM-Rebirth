package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyPoleSatelliteReceiverBlock;
import com.hbm.ntm.blockentity.PoleSatelliteReceiverBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Modern OBJ-backed carrier for {@code RenderPoleSatelliteReceiver}. */
public final class PoleSatelliteReceiverRenderer implements BlockEntityRenderer<PoleSatelliteReceiverBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjBlockModels.POLE_SATELLITE_RECEIVER;
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/pole_satellite_receiver.png");

    public PoleSatelliteReceiverRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(PoleSatelliteReceiverBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(PoleSatelliteReceiverBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack,
                legacyYaw(blockEntity.getBlockState().getValue(LegacyPoleSatelliteReceiverBlock.FACING)));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            MODEL.renderAll(TEXTURE, poseStack, buffer,
                    LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight), packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        }
        poseStack.popPose();
    }

    private static float legacyYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}

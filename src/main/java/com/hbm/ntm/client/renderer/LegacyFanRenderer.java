package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyFanBlock;
import com.hbm.ntm.blockentity.LegacyFanBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Direct OBJ renderer matching RenderFan's Frame + interpolated Blades sequence. */
public class LegacyFanRenderer implements BlockEntityRenderer<LegacyFanBlockEntity> {
    static final LegacyWavefrontModel MODEL = ObjMachineModels.FAN;
    static final LegacyWavefrontModel.SelectionHandle FRAME = MODEL.prepareRenderOnlyInCallOrder("Frame");
    static final LegacyWavefrontModel.SelectionHandle BLADES = MODEL.prepareRenderOnlyInCallOrder("Blades");

    public LegacyFanRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(LegacyFanBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(LegacyFanBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(LegacyFanBlock.FACING)
                ? state.getValue(LegacyFanBlock.FACING) : Direction.SOUTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        orientLikeLegacyMetadata(poseStack, facing);
        poseStack.translate(0.0D, -0.5D, 0.0D);
        try (var scope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            renderPart(FRAME, poseStack, buffer, packedLight, packedOverlay);
            try (var animated = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                LegacyPoseRotations.rotateYDegrees(poseStack, -blockEntity.getSpin(partialTick));
                renderPart(BLADES, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    static void renderPart(LegacyWavefrontModel.SelectionHandle part, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MODEL.renderOnlyInCallOrder(ObjMachineModels.FAN_TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                part, LegacyTexturedRenderMode.CUTOUT_CULL);
    }

    static void orientLikeLegacyMetadata(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
            case NORTH -> LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
            case SOUTH -> LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
            case WEST -> LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
            case EAST -> LegacyPoseRotations.rotateZDegrees(poseStack, -90.0F);
            case UP -> {
                // Legacy metadata 1 is the unrotated orientation.
            }
        }
    }
}

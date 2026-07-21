package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BoxcarBlock;
import com.hbm.ntm.blockentity.BoxcarBlockEntity;
import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Exact legacy RenderDecoBlock boxcar transform, routed through unified OBJ culling. */
public class BoxcarBlockEntityRenderer implements BlockEntityRenderer<BoxcarBlockEntity> {
    public BoxcarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(BoxcarBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(BoxcarBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())
                || !(blockEntity.getBlockState().getBlock() instanceof BoxcarBlock)) {
            return;
        }
        Direction facing = blockEntity.getBlockState().getValue(BoxcarBlock.FACING);
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, -1.5D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, rotationFor(facing));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjEntityModels.BOXCAR.renderAll(ObjEntityModels.BOXCAR_TEXTURE, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private static float rotationFor(Direction direction) {
        return switch (direction) {
            case SOUTH -> 90.0F; // old metadata 3
            case WEST -> 0.0F;   // old metadata 4
            case NORTH -> 270.0F; // old metadata 2
            case EAST -> 180.0F; // old metadata 5
            default -> 0.0F;
        };
    }
}

package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BlastDoorBlock;
import com.hbm.ntm.blockentity.BlastDoorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjDoorModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** 1.7.10 RenderBlastDoor: base/top block remain fixed; tooth and four sliders travel vertically. */
public class BlastDoorRenderer implements BlockEntityRenderer<BlastDoorBlockEntity> {
    private static final double MAX_TRAVEL = 5.0D;
    private static final long ANIMATION_MILLIS = 5_000L;

    public BlastDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(BlastDoorBlockEntity door) {
        return false;
    }

    @Override
    public boolean shouldRender(BlastDoorBlockEntity door, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(door, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(door, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(BlastDoorBlockEntity door, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(door, getViewDistance())) return;
        int light = LegacyRenderLighting.resolveBoundsLight(door, door.getRenderBoundingBox(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        // old glRotate(180,Y), then metadata 2 added +90 degrees; modern facing preserves the two old axes.
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        Direction facing = door.getBlockState().hasProperty(BlastDoorBlock.FACING)
                ? door.getBlockState().getValue(BlastDoorBlock.FACING) : Direction.NORTH;
        if (facing == Direction.NORTH) LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);

        double travel = travel(door);
        try (var scope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(door)) {
            renderAll(ObjDoorModels.BLAST_DOOR_BASE_LEGACY, ObjDoorModels.BLAST_DOOR_BASE_TEXTURE,
                    poseStack, buffer, light, packedOverlay);
            poseStack.pushPose();
            poseStack.translate(0.0D, 3.0D, 0.0D);
            renderAll(ObjDoorModels.BLAST_DOOR_BLOCK_LEGACY, ObjDoorModels.BLAST_DOOR_BLOCK_TEXTURE,
                    poseStack, buffer, light, packedOverlay);
            poseStack.translate(0.0D, -travel, 0.0D);
            poseStack.translate(0.0D, 2.0D, 0.0D);
            try (var animated = LegacyBlockEntityRenderCulling.animatedModelFadeScope(door)) {
                renderAll(ObjDoorModels.BLAST_DOOR_TOOTH_LEGACY, ObjDoorModels.BLAST_DOOR_TOOTH_TEXTURE,
                        poseStack, buffer, packedLight, packedOverlay);
                if (travel > 1.0D) {
                    renderAll(ObjDoorModels.BLAST_DOOR_SLIDER_LEGACY, ObjDoorModels.BLAST_DOOR_SLIDER_TEXTURE,
                            poseStack, buffer, packedLight, packedOverlay);
                }
                for (int slider = 2; slider < 5; slider++) {
                    if (travel > slider) {
                        poseStack.translate(0.0D, 1.0D, 0.0D);
                        renderAll(ObjDoorModels.BLAST_DOOR_SLIDER_LEGACY, ObjDoorModels.BLAST_DOOR_SLIDER_TEXTURE,
                                poseStack, buffer, packedLight, packedOverlay);
                    }
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static double travel(BlastDoorBlockEntity door) {
        if (door.state() == BlastDoorBlockEntity.STATE_CLOSED) return MAX_TRAVEL;
        if (door.state() == BlastDoorBlockEntity.STATE_OPEN) return 0.0D;
        long start = door.clientAnimationStartMillis();
        if (start <= 0L) return door.isOpening() ? MAX_TRAVEL : 0.0D;
        double progress = Math.max(0.0D, Math.min(1.0D, (System.currentTimeMillis() - start) / (double) ANIMATION_MILLIS));
        return door.isOpening() ? MAX_TRAVEL * (1.0D - progress) : MAX_TRAVEL * progress;
    }

    private static void renderAll(LegacyWavefrontModel model, net.minecraft.resources.ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        model.renderAll(texture, poseStack, buffer, light, overlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }
}

package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LaunchPadBlock;
import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LaunchPadRenderer implements BlockEntityRenderer<LaunchPadBlockEntity> {
    public LaunchPadRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(LaunchPadBlockEntity launchPad, Vec3 cameraPos) {
        return hasMissile(launchPad)
                && BlockEntityRenderer.super.shouldRender(launchPad, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(launchPad, getViewDistance());
    }

    @Override
    public void render(LaunchPadBlockEntity launchPad, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack missile = missileStack(launchPad);
        if (missile.isEmpty()) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(launchPad, getViewDistance())) {
            return;
        }
        Direction facing = launchPad.getBlockState().hasProperty(LaunchPadBlock.FACING)
                ? launchPad.getBlockState().getValue(LaunchPadBlock.FACING)
                : Direction.NORTH;
        int modelLight = LegacyRenderLighting.resolveBoundsLight(launchPad,
                launchPadLightingBounds(launchPad.getBlockPos(), true), packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, yRotation(facing));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(launchPad)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.0D, 0.0D);
            MissileItemRenderer.renderRawMissile(missile, poseStack, buffer, modelLight, packedOverlay);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static boolean hasMissile(LaunchPadBlockEntity launchPad) {
        return !missileStack(launchPad).isEmpty();
    }

    private static ItemStack missileStack(LaunchPadBlockEntity launchPad) {
        return launchPad.getItems().getStackInSlot(LaunchPadBlockEntity.SLOT_MISSILE);
    }

    private static AABB launchPadLightingBounds(BlockPos pos, boolean missileLoaded) {
        return new AABB(
                pos.getX() - 1,
                pos.getY(),
                pos.getZ() - 1,
                pos.getX() + 2,
                pos.getY() + (missileLoaded ? 12 : 1),
                pos.getZ() + 2);
    }

    private static float yRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}

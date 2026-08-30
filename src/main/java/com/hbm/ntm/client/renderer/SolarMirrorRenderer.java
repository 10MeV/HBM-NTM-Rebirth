package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.SolarMirrorBlockEntity;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class SolarMirrorRenderer implements BlockEntityRenderer<SolarMirrorBlockEntity> {
    private static final LegacyWavefrontModel SOLAR_MIRROR = ObjMachineModels.SOLAR_MIRROR_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle MIRROR =
            SOLAR_MIRROR.prepareRenderOnlyInCallOrder("Mirror");

    public SolarMirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SolarMirrorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(SolarMirrorBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(SolarMirrorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                if (blockEntity.isTargetAbove()) {
                    BlockPos target = blockEntity.getTarget();
                    BlockPos origin = blockEntity.getBlockPos();
                    aimAt(target.getX() - origin.getX(), target.getY() - origin.getY(), target.getZ() - origin.getZ(), poseStack);
                }
                SOLAR_MIRROR.renderOnlyInCallOrder(poseStack, buffer, packedLight, packedOverlay, MIRROR);
            }
        }
        poseStack.popPose();
    }

    private static void aimAt(double dx, double dy, double dz, PoseStack poseStack) {
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance <= 0.0D) {
            return;
        }
        double pitch = -Math.asin(dy / distance) + Math.PI / 2.0D;
        double yaw = -Math.atan2(dz, dx) - Math.PI / 2.0D;
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateYRadians(poseStack, (float) yaw);
        LegacyPoseRotations.rotateXRadians(poseStack, (float) pitch);
        poseStack.translate(0.0D, -1.0D, 0.0D);
    }
}

package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.SolarMirrorBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class SolarMirrorRenderer implements BlockEntityRenderer<SolarMirrorBlockEntity> {
    private static final LegacyWavefrontModel SOLAR_MIRROR = ObjMachineModels.SOLAR_MIRROR_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            SOLAR_MIRROR.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle MIRROR =
            SOLAR_MIRROR.prepareRenderOnlyInCallOrder("Mirror");

    public SolarMirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SolarMirrorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(SolarMirrorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer,
                blockEntity.getBlockState(), packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        SOLAR_MIRROR.renderOnlyInCallOrder(context, BASE);
        if (blockEntity.isTargetAbove()) {
            Vec3 delta = Vec3.atLowerCornerOf(blockEntity.getTarget().subtract(blockEntity.getBlockPos()));
            aimAt(delta, poseStack);
        }
        SOLAR_MIRROR.renderOnlyInCallOrder(context, MIRROR);
        poseStack.popPose();
    }

    private static void aimAt(Vec3 delta, PoseStack poseStack) {
        double distance = delta.length();
        if (distance <= 0.0D) {
            return;
        }
        double pitch = -Math.asin(delta.y / distance) + Math.PI / 2.0D;
        double yaw = -Math.atan2(delta.z, delta.x) - Math.PI / 2.0D;
        poseStack.translate(0.0D, 1.0D, 0.0D);
        poseStack.mulPose(Axis.YP.rotation((float) yaw));
        poseStack.mulPose(Axis.XP.rotation((float) pitch));
        poseStack.translate(0.0D, -1.0D, 0.0D);
    }
}

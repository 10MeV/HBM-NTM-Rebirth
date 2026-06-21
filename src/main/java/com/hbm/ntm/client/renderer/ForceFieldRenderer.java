package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.ForceFieldBlockEntity;
import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class ForceFieldRenderer implements BlockEntityRenderer<ForceFieldBlockEntity> {
    public ForceFieldRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ForceFieldBlockEntity forceField, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        ObjModelLibrary.MACHINE_RADAR_BODY_LEGACY.renderAll(ObjUtilityModels.FORCEFIELD_BASE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);

        poseStack.translate(0.0D, 0.5D, 0.0D);
        if (forceField.isOn() && forceField.getHealth() > 0 && forceField.getPower() > 0
                && forceField.getCooldown() == 0) {
            int segments = (int) (16 + forceField.getRadius() * 0.125F);
            renderSphere(poseStack, buffer, segments, segments * 2, forceField.getRadius(), forceField.getColor());
            double rotation = (System.currentTimeMillis() / 10.0D) % 360.0D;
            poseStack.mulPose(Axis.YP.rotationDegrees((float) -rotation));
        }

        poseStack.translate(0.0D, 0.5D, 0.0D);
        ObjUtilityModels.FORCEFIELD_TOP.renderAll(ObjUtilityModels.FORCEFIELD_TOP_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ForceFieldBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static void renderSphere(PoseStack poseStack, MultiBufferSource buffer, int latitudes, int segments,
            float radius, int color) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        double segmentRot = Math.PI * 2.0D / segments;
        double latitudeRot = Math.PI / latitudes;

        for (int k = 0; k < segments; k++) {
            double yaw = segmentRot * (k + 1);
            Vec3 prev = rotateY(new Vec3(0.0D, radius, 0.0D), yaw);
            for (int i = 0; i < latitudes; i++) {
                Vec3 next = rotateY(rotateX(new Vec3(0.0D, radius, 0.0D), latitudeRot * (i + 1)), yaw);
                LegacyLineRenderer.line(consumer, pose, prev.x, prev.y, prev.z, next.x, next.y, next.z, color, 255);
                prev = next;
            }
        }

        Vec3 ring = new Vec3(0.0D, radius, 0.0D);
        for (int k = 0; k < latitudes; k++) {
            ring = rotateZ(ring, latitudeRot);
            Vec3 prev = ring;
            for (int i = 0; i < segments; i++) {
                Vec3 next = rotateY(ring, segmentRot * (i + 1));
                LegacyLineRenderer.line(consumer, pose, prev.x, prev.y, prev.z, next.x, next.y, next.z, color, 255);
                prev = next;
            }
        }
    }

    private static Vec3 rotateX(Vec3 vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vec.x, vec.y * cos - vec.z * sin, vec.y * sin + vec.z * cos);
    }

    private static Vec3 rotateY(Vec3 vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    private static Vec3 rotateZ(Vec3 vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vec.x * cos - vec.y * sin, vec.x * sin + vec.y * cos, vec.z);
    }
}

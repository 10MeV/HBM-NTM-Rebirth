package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.ForceFieldBlockEntity;
import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ForceFieldRenderer implements BlockEntityRenderer<ForceFieldBlockEntity> {
    public ForceFieldRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ForceFieldBlockEntity forceField, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(forceField, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        ObjModelLibrary.MACHINE_RADAR_BODY_LEGACY.renderAll(ObjUtilityModels.FORCEFIELD_BASE_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);

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
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ForceFieldBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    private static void renderSphere(PoseStack poseStack, MultiBufferSource buffer, int latitudes, int segments,
            float radius, int color) {
        double segmentRot = Math.PI * 2.0D / segments;
        double latitudeRot = Math.PI / latitudes;
        List<LegacyWavefrontModel.UntexturedLineTransient> lines = new ArrayList<>(segments * latitudes * 2);

        for (int k = 0; k < segments; k++) {
            double yaw = segmentRot * (k + 1);
            Vec3 prev = rotateY(new Vec3(0.0D, radius, 0.0D), yaw);
            for (int i = 0; i < latitudes; i++) {
                Vec3 next = rotateY(rotateX(new Vec3(0.0D, radius, 0.0D), latitudeRot * (i + 1)), yaw);
                lines.add(line(prev, next, color));
                prev = next;
            }
        }

        Vec3 ring = new Vec3(0.0D, radius, 0.0D);
        for (int k = 0; k < latitudes; k++) {
            ring = rotateZ(ring, latitudeRot);
            Vec3 prev = ring;
            for (int i = 0; i < segments; i++) {
                Vec3 next = rotateY(ring, segmentRot * (i + 1));
                lines.add(line(prev, next, color));
                prev = next;
            }
        }
        LegacyLineRenderer.lines(poseStack, buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                LegacyLineRenderer.DEFAULT_LINE_WIDTH, lines);
    }

    private static LegacyWavefrontModel.UntexturedLineTransient line(Vec3 start, Vec3 end, int color) {
        return new LegacyWavefrontModel.UntexturedLineTransient(
                start.x, start.y, start.z, end.x, end.y, end.z, color, 255);
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

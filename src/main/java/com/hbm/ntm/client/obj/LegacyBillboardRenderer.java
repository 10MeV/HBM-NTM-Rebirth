package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class LegacyBillboardRenderer {
    public static CameraBasis currentCameraBasis() {
        return cameraBasis(Minecraft.getInstance().gameRenderer.getMainCamera());
    }

    public static CameraBasis cameraBasis(Camera camera) {
        return cameraBasis(camera.rotation());
    }

    public static CameraBasis cameraBasis(Quaternionf rotation) {
        return new CameraBasis(
                new Vector3f(1.0F, 0.0F, 0.0F).rotate(rotation),
                new Vector3f(0.0F, 1.0F, 0.0F).rotate(rotation));
    }

    public static void billboardRgbaF(VertexConsumer consumer, PoseStack.Pose pose, CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight,
            float red, float green, float blue, float alpha, int packedLight) {
        BillboardQuad quad = quad(basis, x, y, z, halfWidth, halfHeight);
        emitBillboard(consumer, pose, quad, red, green, blue, alpha, packedLight);
    }

    public static void billboardRgbaF(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight,
            float red, float green, float blue, float alpha, int packedLight) {
        BillboardQuad quad = quad(basis, x, y, z, halfWidth, halfHeight);
        int color = channel(red) << 16 | channel(green) << 8 | channel(blue);
        int alphaByte = channel(alpha);
        if (LegacyWavefrontModel.renderTexturedTransientBillboard(texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, renderMode, 0.0F, 1.0F, 0.0F,
                quad.x0(), quad.y0(), quad.z0(), 1.0F, 1.0F,
                quad.x1(), quad.y1(), quad.z1(), 1.0F, 0.0F,
                quad.x2(), quad.y2(), quad.z2(), 0.0F, 0.0F,
                quad.x3(), quad.y3(), quad.z3(), 0.0F, 1.0F,
                color, alphaByte)) {
            return;
        }
        emitBillboard(buffer.getBuffer(renderMode.renderType(texture)), poseStack.last(), quad,
                red, green, blue, alpha, packedLight);
    }

    private static BillboardQuad quad(CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight) {
        Vector3f right = basis.right();
        Vector3f up = basis.up();
        float rightX = right.x() * (float) halfWidth;
        float rightY = right.y() * (float) halfWidth;
        float rightZ = right.z() * (float) halfWidth;
        float upX = up.x() * (float) halfHeight;
        float upY = up.y() * (float) halfHeight;
        float upZ = up.z() * (float) halfHeight;
        float centerX = (float) x;
        float centerY = (float) y;
        float centerZ = (float) z;
        return new BillboardQuad(
                centerX - rightX - upX, centerY - rightY - upY, centerZ - rightZ - upZ,
                centerX - rightX + upX, centerY - rightY + upY, centerZ - rightZ + upZ,
                centerX + rightX + upX, centerY + rightY + upY, centerZ + rightZ + upZ,
                centerX + rightX - upX, centerY + rightY - upY, centerZ + rightZ - upZ);
    }

    private static void emitBillboard(VertexConsumer consumer, PoseStack.Pose pose, BillboardQuad quad,
            float red, float green, float blue, float alpha, int packedLight) {
        vertex(consumer, pose, quad.x0(), quad.y0(), quad.z0(),
                1.0F, 1.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, quad.x1(), quad.y1(), quad.z1(),
                1.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, quad.x2(), quad.y2(), quad.z2(),
                0.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, quad.x3(), quad.y3(), quad.z3(),
                0.0F, 1.0F, red, green, blue, alpha, packedLight);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
            float u, float v, float red, float green, float blue, float alpha, int packedLight) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static int channel(float value) {
        return (int) (Math.max(0.0F, Math.min(1.0F, value)) * 255.0F);
    }

    private record BillboardQuad(
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3) {
    }

    public record CameraBasis(Vector3f right, Vector3f up) {
    }

    private LegacyBillboardRenderer() {
    }
}

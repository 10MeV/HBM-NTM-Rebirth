package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

public final class LegacyBillboardRenderer {
    private static final ThreadLocal<CameraBasis> CAMERA_BASIS_SCRATCH =
            ThreadLocal.withInitial(CameraBasis::new);

    public static CameraBasis currentCameraBasis() {
        return cameraBasis(Minecraft.getInstance().gameRenderer.getMainCamera());
    }

    public static CameraBasis currentCameraBasisScratch() {
        return cameraBasisScratch(Minecraft.getInstance().gameRenderer.getMainCamera());
    }

    public static CameraBasis cameraBasis(Camera camera) {
        return cameraBasis(camera.rotation());
    }

    public static CameraBasis cameraBasisScratch(Camera camera) {
        return cameraBasisScratch(camera.rotation());
    }

    public static CameraBasis cameraBasis(Quaternionf rotation) {
        return new CameraBasis().set(rotation);
    }

    public static CameraBasis cameraBasisScratch(Quaternionf rotation) {
        return CAMERA_BASIS_SCRATCH.get().set(rotation);
    }

    private static CameraBasis writeCameraBasis(Quaternionf rotation, CameraBasis basis) {
        float x = rotation.x();
        float y = rotation.y();
        float z = rotation.z();
        float w = rotation.w();
        float x2 = x + x;
        float y2 = y + y;
        float z2 = z + z;
        float xx = x * x2;
        float xy = x * y2;
        float xz = x * z2;
        float yy = y * y2;
        float yz = y * z2;
        float zz = z * z2;
        float wx = w * x2;
        float wy = w * y2;
        float wz = w * z2;
        return basis.set(
                1.0F - yy - zz, xy + wz, xz - wy,
                xy - wz, 1.0F - xx - zz, yz + wx);
    }

    public static void billboardRgbaF(VertexConsumer consumer, PoseStack.Pose pose, CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight,
            float red, float green, float blue, float alpha, int packedLight) {
        float rightX = basis.rightX() * (float) halfWidth;
        float rightY = basis.rightY() * (float) halfWidth;
        float rightZ = basis.rightZ() * (float) halfWidth;
        float upX = basis.upX() * (float) halfHeight;
        float upY = basis.upY() * (float) halfHeight;
        float upZ = basis.upZ() * (float) halfHeight;
        float centerX = (float) x;
        float centerY = (float) y;
        float centerZ = (float) z;
        emitBillboard(consumer, pose,
                centerX - rightX - upX, centerY - rightY - upY, centerZ - rightZ - upZ,
                centerX - rightX + upX, centerY - rightY + upY, centerZ - rightZ + upZ,
                centerX + rightX + upX, centerY + rightY + upY, centerZ + rightZ + upZ,
                centerX + rightX - upX, centerY + rightY - upY, centerZ + rightZ - upZ,
                red, green, blue, alpha, packedLight);
    }

    public static void billboardRgbaF(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight,
            float red, float green, float blue, float alpha, int packedLight) {
        float rightX = basis.rightX() * (float) halfWidth;
        float rightY = basis.rightY() * (float) halfWidth;
        float rightZ = basis.rightZ() * (float) halfWidth;
        float upX = basis.upX() * (float) halfHeight;
        float upY = basis.upY() * (float) halfHeight;
        float upZ = basis.upZ() * (float) halfHeight;
        float centerX = (float) x;
        float centerY = (float) y;
        float centerZ = (float) z;
        float x0 = centerX - rightX - upX;
        float y0 = centerY - rightY - upY;
        float z0 = centerZ - rightZ - upZ;
        float x1 = centerX - rightX + upX;
        float y1 = centerY - rightY + upY;
        float z1 = centerZ - rightZ + upZ;
        float x2 = centerX + rightX + upX;
        float y2 = centerY + rightY + upY;
        float z2 = centerZ + rightZ + upZ;
        float x3 = centerX + rightX - upX;
        float y3 = centerY + rightY - upY;
        float z3 = centerZ + rightZ - upZ;
        int color = channel(red) << 16 | channel(green) << 8 | channel(blue);
        int alphaByte = channel(alpha);
        if (renderTransient(texture, poseStack, buffer, renderMode, packedLight,
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, color, alphaByte)) {
            return;
        }
        emitBillboard(buffer.getBuffer(renderMode.renderType(texture)), poseStack.last(),
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                red, green, blue, alpha, packedLight);
    }

    public static void billboardPairRgbaF(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, CameraBasis basis,
            double x, double y, double z,
            double firstHalfWidth, double firstHalfHeight,
            float firstRed, float firstGreen, float firstBlue, float firstAlpha,
            double secondHalfWidth, double secondHalfHeight,
            float secondRed, float secondGreen, float secondBlue, float secondAlpha,
            int packedLight) {
        VertexConsumer fallbackConsumer = null;
        PoseStack.Pose fallbackPose = null;

        float centerX = (float) x;
        float centerY = (float) y;
        float centerZ = (float) z;

        float rightX = basis.rightX() * (float) firstHalfWidth;
        float rightY = basis.rightY() * (float) firstHalfWidth;
        float rightZ = basis.rightZ() * (float) firstHalfWidth;
        float upX = basis.upX() * (float) firstHalfHeight;
        float upY = basis.upY() * (float) firstHalfHeight;
        float upZ = basis.upZ() * (float) firstHalfHeight;
        float x0 = centerX - rightX - upX;
        float y0 = centerY - rightY - upY;
        float z0 = centerZ - rightZ - upZ;
        float x1 = centerX - rightX + upX;
        float y1 = centerY - rightY + upY;
        float z1 = centerZ - rightZ + upZ;
        float x2 = centerX + rightX + upX;
        float y2 = centerY + rightY + upY;
        float z2 = centerZ + rightZ + upZ;
        float x3 = centerX + rightX - upX;
        float y3 = centerY + rightY - upY;
        float z3 = centerZ + rightZ - upZ;
        int color = channel(firstRed) << 16 | channel(firstGreen) << 8 | channel(firstBlue);
        int alphaByte = channel(firstAlpha);
        if (!renderTransient(texture, poseStack, buffer, renderMode, packedLight,
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, color, alphaByte)) {
            fallbackConsumer = buffer.getBuffer(renderMode.renderType(texture));
            fallbackPose = poseStack.last();
            emitBillboard(fallbackConsumer, fallbackPose,
                    x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                    firstRed, firstGreen, firstBlue, firstAlpha, packedLight);
        }

        rightX = basis.rightX() * (float) secondHalfWidth;
        rightY = basis.rightY() * (float) secondHalfWidth;
        rightZ = basis.rightZ() * (float) secondHalfWidth;
        upX = basis.upX() * (float) secondHalfHeight;
        upY = basis.upY() * (float) secondHalfHeight;
        upZ = basis.upZ() * (float) secondHalfHeight;
        x0 = centerX - rightX - upX;
        y0 = centerY - rightY - upY;
        z0 = centerZ - rightZ - upZ;
        x1 = centerX - rightX + upX;
        y1 = centerY - rightY + upY;
        z1 = centerZ - rightZ + upZ;
        x2 = centerX + rightX + upX;
        y2 = centerY + rightY + upY;
        z2 = centerZ + rightZ + upZ;
        x3 = centerX + rightX - upX;
        y3 = centerY + rightY - upY;
        z3 = centerZ + rightZ - upZ;
        color = channel(secondRed) << 16 | channel(secondGreen) << 8 | channel(secondBlue);
        alphaByte = channel(secondAlpha);
        if (!renderTransient(texture, poseStack, buffer, renderMode, packedLight,
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, color, alphaByte)) {
            if (fallbackConsumer == null) {
                fallbackConsumer = buffer.getBuffer(renderMode.renderType(texture));
                fallbackPose = poseStack.last();
            }
            emitBillboard(fallbackConsumer, fallbackPose,
                    x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                    secondRed, secondGreen, secondBlue, secondAlpha, packedLight);
        }
    }

    private static boolean renderTransient(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int packedLight,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            int color, int alphaByte) {
        return LegacyWavefrontModel.renderTexturedTransientBillboard(texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, renderMode, 0.0F, 1.0F, 0.0F,
                x0, y0, z0, 1.0F, 1.0F,
                x1, y1, z1, 1.0F, 0.0F,
                x2, y2, z2, 0.0F, 0.0F,
                x3, y3, z3, 0.0F, 1.0F,
                color, alphaByte);
    }

    private static void emitBillboard(VertexConsumer consumer, PoseStack.Pose pose,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float red, float green, float blue, float alpha, int packedLight) {
        vertex(consumer, pose, x0, y0, z0, 1.0F, 1.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, x1, y1, z1, 1.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, x2, y2, z2, 0.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, x3, y3, z3, 0.0F, 1.0F, red, green, blue, alpha, packedLight);
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

    public static final class CameraBasis {
        private float rightX;
        private float rightY;
        private float rightZ;
        private float upX;
        private float upY;
        private float upZ;

        public CameraBasis() {
        }

        public CameraBasis(float rightX, float rightY, float rightZ, float upX, float upY, float upZ) {
            set(rightX, rightY, rightZ, upX, upY, upZ);
        }

        public CameraBasis set(Quaternionf rotation) {
            return writeCameraBasis(rotation, this);
        }

        public CameraBasis set(float rightX, float rightY, float rightZ, float upX, float upY, float upZ) {
            this.rightX = rightX;
            this.rightY = rightY;
            this.rightZ = rightZ;
            this.upX = upX;
            this.upY = upY;
            this.upZ = upZ;
            return this;
        }

        public float rightX() {
            return rightX;
        }

        public float rightY() {
            return rightY;
        }

        public float rightZ() {
            return rightZ;
        }

        public float upX() {
            return upX;
        }

        public float upY() {
            return upY;
        }

        public float upZ() {
            return upZ;
        }
    }

    private LegacyBillboardRenderer() {
    }
}

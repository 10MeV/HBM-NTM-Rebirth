package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

    public static void billboard(ResourceLocation texture, ObjRenderContext context,
            double x, double y, double z, double halfWidth, double halfHeight) {
        billboard(texture, context, currentCameraBasis(), x, y, z, halfWidth, halfHeight, 0xFFFFFF, 255);
    }

    public static void billboard(ResourceLocation texture, ObjRenderContext context, CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight, int color, int alpha) {
        LegacyTexturedQuadRenderer.Vertex[] vertices = vertices(basis, x, y, z, halfWidth, halfHeight, color, alpha);
        LegacyTexturedQuadRenderer.quadWithComputedNormal(texture, context,
                vertices[0], vertices[1], vertices[2], vertices[3]);
    }

    public static void spriteBillboard(TextureAtlasSprite sprite, ObjRenderContext context,
            double x, double y, double z, double halfWidth, double halfHeight) {
        spriteBillboard(sprite, context, currentCameraBasis(), x, y, z, halfWidth, halfHeight, 0xFFFFFF, 255);
    }

    public static void spriteBillboard(TextureAtlasSprite sprite, ObjRenderContext context, CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight, int color, int alpha) {
        LegacyTexturedQuadRenderer.Vertex[] vertices = vertices(basis, x, y, z, halfWidth, halfHeight, color, alpha);
        LegacyTexturedQuadRenderer.spriteUnitQuadWithComputedNormal(sprite, context,
                vertices[0], vertices[1], vertices[2], vertices[3]);
    }

    public static void billboardRgbaF(VertexConsumer consumer, PoseStack.Pose pose, CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight,
            float red, float green, float blue, float alpha, int packedLight) {
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

        vertex(consumer, pose, centerX - rightX - upX, centerY - rightY - upY, centerZ - rightZ - upZ,
                1.0F, 1.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, centerX - rightX + upX, centerY - rightY + upY, centerZ - rightZ + upZ,
                1.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, centerX + rightX + upX, centerY + rightY + upY, centerZ + rightZ + upZ,
                0.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(consumer, pose, centerX + rightX - upX, centerY + rightY - upY, centerZ + rightZ - upZ,
                0.0F, 1.0F, red, green, blue, alpha, packedLight);
    }

    private static LegacyTexturedQuadRenderer.Vertex[] vertices(CameraBasis basis,
            double x, double y, double z, double halfWidth, double halfHeight, int color, int alpha) {
        Vector3f right = basis.right();
        Vector3f up = basis.up();
        double rightX = right.x() * halfWidth;
        double rightY = right.y() * halfWidth;
        double rightZ = right.z() * halfWidth;
        double upX = up.x() * halfHeight;
        double upY = up.y() * halfHeight;
        double upZ = up.z() * halfHeight;
        return new LegacyTexturedQuadRenderer.Vertex[] {
                LegacyTexturedQuadRenderer.vertex(x - rightX - upX, y - rightY - upY, z - rightZ - upZ,
                        1.0D, 1.0D, color, alpha),
                LegacyTexturedQuadRenderer.vertex(x - rightX + upX, y - rightY + upY, z - rightZ + upZ,
                        1.0D, 0.0D, color, alpha),
                LegacyTexturedQuadRenderer.vertex(x + rightX + upX, y + rightY + upY, z + rightZ + upZ,
                        0.0D, 0.0D, color, alpha),
                LegacyTexturedQuadRenderer.vertex(x + rightX - upX, y + rightY - upY, z + rightZ - upZ,
                        0.0D, 1.0D, color, alpha)
        };
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

    public record CameraBasis(Vector3f right, Vector3f up) {
    }

    private LegacyBillboardRenderer() {
    }
}

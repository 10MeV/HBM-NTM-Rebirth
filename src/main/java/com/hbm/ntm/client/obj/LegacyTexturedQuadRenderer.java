package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LegacyTexturedQuadRenderer {
    public static Vertex vertex(double x, double y, double z, double u, double v) {
        return new Vertex(x, y, z, (float) u, (float) v, 0xFFFFFF, 255);
    }

    public static Vertex vertex(double x, double y, double z, double u, double v, int color, int alpha) {
        return new Vertex(x, y, z, (float) u, (float) v, color & 0xFFFFFF, clampAlpha(alpha));
    }

    public static Vertex spritePixelVertex(double x, double y, double z, double pixelU, double pixelV) {
        return vertex(x, y, z, pixelU, pixelV);
    }

    public static Vertex spritePixelVertex(double x, double y, double z, double pixelU, double pixelV, int color, int alpha) {
        return vertex(x, y, z, pixelU, pixelV, color, alpha);
    }

    public static Vertex spriteUnitVertex(double x, double y, double z, double u, double v) {
        return vertex(x, y, z, u, v);
    }

    public static Vertex spriteUnitVertex(double x, double y, double z, double u, double v, int color, int alpha) {
        return vertex(x, y, z, u, v, color, alpha);
    }

    public static TextureAtlasSprite blockSprite(ResourceLocation textureLocation) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureLocation);
    }

    public static void quad(ResourceLocation texture, ObjRenderContext context, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        quad(texture, context, 0.0F, 1.0F, 0.0F, v0, v1, v2, v3);
    }

    public static void quad(ResourceLocation texture, ObjRenderContext context,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vertex[] vertices = {v0, v1, v2, v3};
        VertexConsumer consumer = context.buffer().getBuffer(renderMode(context, vertices).renderType(texture));
        emitQuad(consumer, context.poseStack().last(), context, normalX, normalY, normalZ, vertices);
    }

    public static void quadWithComputedNormal(ResourceLocation texture, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vector3f normal = computedNormal(v0, v1, v2);
        quad(texture, context, normal.x(), normal.y(), normal.z(), v0, v1, v2, v3);
    }

    public static void spriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, context, 0.0F, 1.0F, 0.0F, v0, v1, v2, v3);
    }

    public static void spriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, context, normalX, normalY, normalZ, false, v0, v1, v2, v3);
    }

    public static void fullSpriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3) {
        fullSpriteQuad(sprite, context, 0.0F, 1.0F, 0.0F,
                x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, 0xFFFFFF, 255);
    }

    public static void fullSpriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3,
            int color, int alpha) {
        spriteQuad(sprite, context, normalX, normalY, normalZ,
                spritePixelVertex(x0, y0, z0, 0.0D, 16.0D, color, alpha),
                spritePixelVertex(x1, y1, z1, 16.0D, 16.0D, color, alpha),
                spritePixelVertex(x2, y2, z2, 16.0D, 0.0D, color, alpha),
                spritePixelVertex(x3, y3, z3, 0.0D, 0.0D, color, alpha));
    }

    public static void spriteUnitQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteUnitQuad(sprite, context, 0.0F, 1.0F, 0.0F, v0, v1, v2, v3);
    }

    public static void spriteUnitQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, context, normalX, normalY, normalZ, true, v0, v1, v2, v3);
    }

    private static void spriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ, boolean unitUv, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vertex[] vertices = {v0, v1, v2, v3};
        VertexConsumer consumer = context.buffer().getBuffer(renderMode(context, vertices).renderType(InventoryMenu.BLOCK_ATLAS));
        emitSpriteQuad(consumer, context.poseStack().last(), context, sprite, normalX, normalY, normalZ, unitUv, vertices);
    }

    public static void spriteQuadWithComputedNormal(TextureAtlasSprite sprite, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vector3f normal = computedNormal(v0, v1, v2);
        spriteQuad(sprite, context, normal.x(), normal.y(), normal.z(), v0, v1, v2, v3);
    }

    public static void doubleSidedQuad(ResourceLocation texture, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        doubleSidedQuad(texture, context, 0.0F, 1.0F, 0.0F, v0, v1, v2, v3);
    }

    public static void doubleSidedQuad(ResourceLocation texture, ObjRenderContext context,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        quad(texture, context, normalX, normalY, normalZ, v0, v1, v2, v3);
        quad(texture, context, -normalX, -normalY, -normalZ, v3, v2, v1, v0);
    }

    public static void doubleSidedQuadWithComputedNormal(ResourceLocation texture, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vector3f normal = computedNormal(v0, v1, v2);
        doubleSidedQuad(texture, context, normal.x(), normal.y(), normal.z(), v0, v1, v2, v3);
    }

    public static void doubleSidedSpriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, context, normalX, normalY, normalZ, v0, v1, v2, v3);
        spriteQuad(sprite, context, -normalX, -normalY, -normalZ, v3, v2, v1, v0);
    }

    public static void doubleSidedSpriteQuadWithComputedNormal(TextureAtlasSprite sprite, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vector3f normal = computedNormal(v0, v1, v2);
        doubleSidedSpriteQuad(sprite, context, normal.x(), normal.y(), normal.z(), v0, v1, v2, v3);
    }

    public static void quad(VertexConsumer consumer, PoseStack.Pose pose, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        emitQuad(consumer, pose, context, 0.0F, 1.0F, 0.0F, new Vertex[] {v0, v1, v2, v3});
    }

    public static VertexConsumer consumer(ResourceLocation texture, ObjRenderContext context) {
        return context.buffer().getBuffer(context.renderMode().withAlpha(context.alpha()).renderType(texture));
    }

    public static VertexConsumer consumer(ResourceLocation texture, ObjRenderContext context, Vertex... vertices) {
        return context.buffer().getBuffer(renderMode(context, vertices).renderType(texture));
    }

    public static VertexConsumer atlasConsumer(ObjRenderContext context, Vertex... vertices) {
        return context.buffer().getBuffer(renderMode(context, vertices).renderType(InventoryMenu.BLOCK_ATLAS));
    }

    private static void emitQuad(VertexConsumer consumer, PoseStack.Pose pose, ObjRenderContext context,
            float normalX, float normalY, float normalZ, Vertex[] vertices) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        float averageU = averageU(vertices);
        float averageV = averageV(vertices);
        for (Vertex vertex : vertices) {
            emitVertex(consumer, position, normal, context, normalX, normalY, normalZ, averageU, averageV, vertex);
        }
    }

    private static void emitSpriteQuad(VertexConsumer consumer, PoseStack.Pose pose, ObjRenderContext context,
            TextureAtlasSprite sprite, float normalX, float normalY, float normalZ, boolean unitUv, Vertex[] vertices) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        float averageU = averageU(vertices);
        float averageV = averageV(vertices);
        for (Vertex vertex : vertices) {
            emitSpriteVertex(consumer, position, normal, context, sprite, normalX, normalY, normalZ,
                    averageU, averageV, unitUv, vertex);
        }
    }

    private static void emitVertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal, ObjRenderContext context,
            float normalX, float normalY, float normalZ, float averageU, float averageV, Vertex vertex) {
        int color = multipliedColor(context, vertex.color());
        consumer.vertex(position, (float) vertex.x(), (float) vertex.y(), (float) vertex.z())
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, multipliedAlpha(context, vertex.alpha()))
                .uv(transformU(context, vertex.u(), vertex.v(), averageU), transformV(context, vertex.u(), vertex.v(), averageV))
                .overlayCoords(context.packedOverlay() == 0 ? OverlayTexture.NO_OVERLAY : context.packedOverlay())
                .uv2(context.packedLight())
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    private static void emitSpriteVertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal, ObjRenderContext context,
            TextureAtlasSprite sprite, float normalX, float normalY, float normalZ, float averageU, float averageV,
            boolean unitUv, Vertex vertex) {
        int color = multipliedColor(context, vertex.color());
        float u = transformU(context, vertex.u(), vertex.v(), averageU);
        float v = transformV(context, vertex.u(), vertex.v(), averageV);
        float pixelScale = unitUv ? 16.0F : 1.0F;
        consumer.vertex(position, (float) vertex.x(), (float) vertex.y(), (float) vertex.z())
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, multipliedAlpha(context, vertex.alpha()))
                .uv(sprite.getU(u * pixelScale), sprite.getV(v * pixelScale))
                .overlayCoords(context.packedOverlay() == 0 ? OverlayTexture.NO_OVERLAY : context.packedOverlay())
                .uv2(context.packedLight())
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    private static LegacyTexturedRenderMode renderMode(ObjRenderContext context, Vertex[] vertices) {
        int alpha = context.alpha();
        for (Vertex vertex : vertices) {
            alpha = Math.min(alpha, multipliedAlpha(context, vertex.alpha()));
        }
        return context.renderMode().withAlpha(alpha);
    }

    private static int multipliedColor(ObjRenderContext context, int vertexColor) {
        if (!context.hasColor()) {
            return vertexColor & 0xFFFFFF;
        }
        int contextColor = context.color();
        int red = (contextColor >> 16 & 255) * (vertexColor >> 16 & 255) / 255;
        int green = (contextColor >> 8 & 255) * (vertexColor >> 8 & 255) / 255;
        int blue = (contextColor & 255) * (vertexColor & 255) / 255;
        return red << 16 | green << 8 | blue;
    }

    private static int multipliedAlpha(ObjRenderContext context, int vertexAlpha) {
        return clampAlpha(context.alpha() * clampAlpha(vertexAlpha) / 255);
    }

    private static float transformU(ObjRenderContext context, float u, float v, float averageU) {
        return u * context.uScale()
                + v * context.uFromV()
                + context.uOffset()
                + legacyTextureOffset(u, averageU, context.legacyTextureOffset());
    }

    private static float transformV(ObjRenderContext context, float u, float v, float averageV) {
        return u * context.vFromU()
                + v * context.vScale()
                + context.vOffset()
                + legacyTextureOffset(v, averageV, context.legacyTextureOffset());
    }

    private static float legacyTextureOffset(float value, float average, float textureOffset) {
        if (textureOffset == 0.0F) {
            return 0.0F;
        }
        return value > average ? -textureOffset : textureOffset;
    }

    private static float averageU(Vertex[] vertices) {
        float total = 0.0F;
        for (Vertex vertex : vertices) {
            total += vertex.u();
        }
        return total / vertices.length;
    }

    private static float averageV(Vertex[] vertices) {
        float total = 0.0F;
        for (Vertex vertex : vertices) {
            total += vertex.v();
        }
        return total / vertices.length;
    }

    private static int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    public static Vector3f normal(float x, float y, float z) {
        Vector3f normal = new Vector3f(x, y, z);
        if (normal.lengthSquared() <= 1.0E-6F) {
            return new Vector3f(0.0F, 1.0F, 0.0F);
        }
        return normal.normalize();
    }

    public static Vector3f computedNormal(Vertex v0, Vertex v1, Vertex v2) {
        float edgeAX = (float) (v1.x() - v0.x());
        float edgeAY = (float) (v1.y() - v0.y());
        float edgeAZ = (float) (v1.z() - v0.z());
        float edgeBX = (float) (v2.x() - v0.x());
        float edgeBY = (float) (v2.y() - v0.y());
        float edgeBZ = (float) (v2.z() - v0.z());
        return normal(
                edgeAY * edgeBZ - edgeAZ * edgeBY,
                edgeAZ * edgeBX - edgeAX * edgeBZ,
                edgeAX * edgeBY - edgeAY * edgeBX);
    }

    public record Vertex(double x, double y, double z, float u, float v, int color, int alpha) {
        public Vertex withColor(int color) {
            return new Vertex(x, y, z, u, v, color & 0xFFFFFF, alpha);
        }

        public Vertex withAlpha(int alpha) {
            return new Vertex(x, y, z, u, v, color, clampAlpha(alpha));
        }

        public Vertex withRgba(int red, int green, int blue, int alpha) {
            return new Vertex(x, y, z, u, v,
                    clampAlpha(red) << 16 | clampAlpha(green) << 8 | clampAlpha(blue), clampAlpha(alpha));
        }
    }

    private LegacyTexturedQuadRenderer() {
    }
}

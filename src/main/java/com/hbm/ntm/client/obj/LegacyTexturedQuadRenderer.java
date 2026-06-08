package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LegacyTexturedQuadRenderer {
    public static final int INHERIT_LIGHT = -1;
    public static final int INHERIT_OVERLAY = -1;

    public static Vertex vertex(double x, double y, double z, double u, double v) {
        return new Vertex(x, y, z, (float) u, (float) v, 0xFFFFFF, 255, INHERIT_LIGHT, INHERIT_OVERLAY);
    }

    public static Vertex vertex(double x, double y, double z, double u, double v, int color, int alpha) {
        return vertex(x, y, z, u, v, color, alpha, INHERIT_LIGHT, INHERIT_OVERLAY);
    }

    public static Vertex vertex(double x, double y, double z, double u, double v, int color, int alpha, int packedLight) {
        return vertex(x, y, z, u, v, color, alpha, packedLight, INHERIT_OVERLAY);
    }

    public static Vertex vertex(double x, double y, double z, double u, double v,
            int color, int alpha, int packedLight, int packedOverlay) {
        return new Vertex(x, y, z, (float) u, (float) v, color & 0xFFFFFF, clampAlpha(alpha), packedLight, packedOverlay);
    }

    public static Vertex vertexRgbaF(double x, double y, double z, double u, double v,
            float red, float green, float blue, float alpha) {
        return vertex(x, y, z, u, v, rgb(red, green, blue), alpha(alpha));
    }

    public static Vertex vertexRgbF(double x, double y, double z, double u, double v,
            float red, float green, float blue) {
        return vertex(x, y, z, u, v, rgb(red, green, blue), 255);
    }

    public static Vertex spritePixelVertex(double x, double y, double z, double pixelU, double pixelV) {
        return vertex(x, y, z, pixelU, pixelV);
    }

    public static Vertex spritePixelVertex(double x, double y, double z, double pixelU, double pixelV, int color, int alpha) {
        return vertex(x, y, z, pixelU, pixelV, color, alpha);
    }

    public static Vertex spritePixelVertex(double x, double y, double z, double pixelU, double pixelV,
            int color, int alpha, int packedLight) {
        return vertex(x, y, z, pixelU, pixelV, color, alpha, packedLight);
    }

    public static Vertex spritePixelVertex(double x, double y, double z, double pixelU, double pixelV,
            int color, int alpha, int packedLight, int packedOverlay) {
        return vertex(x, y, z, pixelU, pixelV, color, alpha, packedLight, packedOverlay);
    }

    public static Vertex spritePixelVertexRgbaF(double x, double y, double z, double pixelU, double pixelV,
            float red, float green, float blue, float alpha) {
        return spritePixelVertex(x, y, z, pixelU, pixelV, rgb(red, green, blue), alpha(alpha));
    }

    public static Vertex spritePixelVertexRgbF(double x, double y, double z, double pixelU, double pixelV,
            float red, float green, float blue) {
        return spritePixelVertex(x, y, z, pixelU, pixelV, rgb(red, green, blue), 255);
    }

    public static Vertex spriteUnitVertex(double x, double y, double z, double u, double v) {
        return vertex(x, y, z, u, v);
    }

    public static Vertex spriteUnitVertex(double x, double y, double z, double u, double v, int color, int alpha) {
        return vertex(x, y, z, u, v, color, alpha);
    }

    public static Vertex spriteUnitVertex(double x, double y, double z, double u, double v,
            int color, int alpha, int packedLight) {
        return vertex(x, y, z, u, v, color, alpha, packedLight);
    }

    public static Vertex spriteUnitVertex(double x, double y, double z, double u, double v,
            int color, int alpha, int packedLight, int packedOverlay) {
        return vertex(x, y, z, u, v, color, alpha, packedLight, packedOverlay);
    }

    public static Vertex spriteUnitVertexRgbaF(double x, double y, double z, double u, double v,
            float red, float green, float blue, float alpha) {
        return spriteUnitVertex(x, y, z, u, v, rgb(red, green, blue), alpha(alpha));
    }

    public static Vertex spriteUnitVertexRgbF(double x, double y, double z, double u, double v,
            float red, float green, float blue) {
        return spriteUnitVertex(x, y, z, u, v, rgb(red, green, blue), 255);
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

    public static void quad(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        quad(texture, context,
                vertex(x0, y0, z0, u0, v0, color, alpha),
                vertex(x1, y1, z1, u1, v1, color, alpha),
                vertex(x2, y2, z2, u2, v2, color, alpha),
                vertex(x3, y3, z3, u3, v3, color, alpha));
    }

    public static void quad(ResourceLocation texture, ObjRenderContext context,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        quad(texture, context, normalX, normalY, normalZ,
                vertex(x0, y0, z0, u0, v0, color, alpha),
                vertex(x1, y1, z1, u1, v1, color, alpha),
                vertex(x2, y2, z2, u2, v2, color, alpha),
                vertex(x3, y3, z3, u3, v3, color, alpha));
    }

    public static void pixelQuad(ResourceLocation texture, ObjRenderContext context,
            double textureWidth, double textureHeight,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        pixelQuad(texture, context, 0.0F, 1.0F, 0.0F, textureWidth, textureHeight,
                x0, y0, z0, pixelU0, pixelV0,
                x1, y1, z1, pixelU1, pixelV1,
                x2, y2, z2, pixelU2, pixelV2,
                x3, y3, z3, pixelU3, pixelV3,
                color, alpha);
    }

    public static void pixelQuad(ResourceLocation texture, ObjRenderContext context,
            float normalX, float normalY, float normalZ, double textureWidth, double textureHeight,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        if (textureWidth == 0.0D || textureHeight == 0.0D) {
            return;
        }
        quad(texture, context, normalX, normalY, normalZ,
                x0, y0, z0, pixelU0 / textureWidth, pixelV0 / textureHeight,
                x1, y1, z1, pixelU1 / textureWidth, pixelV1 / textureHeight,
                x2, y2, z2, pixelU2 / textureWidth, pixelV2 / textureHeight,
                x3, y3, z3, pixelU3 / textureWidth, pixelV3 / textureHeight,
                color, alpha);
    }

    public static void quadWithVertexAlpha(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        quad(texture, context,
                vertex(x0, y0, z0, u0, v0, color, alpha0),
                vertex(x1, y1, z1, u1, v1, color, alpha1),
                vertex(x2, y2, z2, u2, v2, color, alpha2),
                vertex(x3, y3, z3, u3, v3, color, alpha3));
    }

    public static void quadWithComputedNormal(ResourceLocation texture, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vector3f normal = computedNormal(v0, v1, v2);
        quad(texture, context, normal.x(), normal.y(), normal.z(), v0, v1, v2, v3);
    }

    public static void quadWithComputedNormal(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        quadWithComputedNormal(texture, context,
                vertex(x0, y0, z0, u0, v0, color, alpha),
                vertex(x1, y1, z1, u1, v1, color, alpha),
                vertex(x2, y2, z2, u2, v2, color, alpha),
                vertex(x3, y3, z3, u3, v3, color, alpha));
    }

    public static void quadWithComputedNormalAndVertexAlpha(ResourceLocation texture, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        quadWithComputedNormal(texture, context,
                vertex(x0, y0, z0, u0, v0, color, alpha0),
                vertex(x1, y1, z1, u1, v1, color, alpha1),
                vertex(x2, y2, z2, u2, v2, color, alpha2),
                vertex(x3, y3, z3, u3, v3, color, alpha3));
    }

    public static void spriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, context, 0.0F, 1.0F, 0.0F, v0, v1, v2, v3);
    }

    public static void spriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, context, normalX, normalY, normalZ, false, v0, v1, v2, v3);
    }

    public static void spriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        spriteQuad(sprite, context,
                spritePixelVertex(x0, y0, z0, pixelU0, pixelV0, color, alpha),
                spritePixelVertex(x1, y1, z1, pixelU1, pixelV1, color, alpha),
                spritePixelVertex(x2, y2, z2, pixelU2, pixelV2, color, alpha),
                spritePixelVertex(x3, y3, z3, pixelU3, pixelV3, color, alpha));
    }

    public static void spriteQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        spriteQuad(sprite, context, normalX, normalY, normalZ,
                spritePixelVertex(x0, y0, z0, pixelU0, pixelV0, color, alpha),
                spritePixelVertex(x1, y1, z1, pixelU1, pixelV1, color, alpha),
                spritePixelVertex(x2, y2, z2, pixelU2, pixelV2, color, alpha),
                spritePixelVertex(x3, y3, z3, pixelU3, pixelV3, color, alpha));
    }

    public static void spriteQuadWithVertexAlpha(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double pixelU0, double pixelV0, int alpha0,
            double x1, double y1, double z1, double pixelU1, double pixelV1, int alpha1,
            double x2, double y2, double z2, double pixelU2, double pixelV2, int alpha2,
            double x3, double y3, double z3, double pixelU3, double pixelV3, int alpha3,
            int color) {
        spriteQuad(sprite, context,
                spritePixelVertex(x0, y0, z0, pixelU0, pixelV0, color, alpha0),
                spritePixelVertex(x1, y1, z1, pixelU1, pixelV1, color, alpha1),
                spritePixelVertex(x2, y2, z2, pixelU2, pixelV2, color, alpha2),
                spritePixelVertex(x3, y3, z3, pixelU3, pixelV3, color, alpha3));
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

    public static void spriteUnitQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        spriteUnitQuad(sprite, context,
                spriteUnitVertex(x0, y0, z0, u0, v0, color, alpha),
                spriteUnitVertex(x1, y1, z1, u1, v1, color, alpha),
                spriteUnitVertex(x2, y2, z2, u2, v2, color, alpha),
                spriteUnitVertex(x3, y3, z3, u3, v3, color, alpha));
    }

    public static void spriteUnitQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        spriteUnitQuad(sprite, context, normalX, normalY, normalZ,
                spriteUnitVertex(x0, y0, z0, u0, v0, color, alpha),
                spriteUnitVertex(x1, y1, z1, u1, v1, color, alpha),
                spriteUnitVertex(x2, y2, z2, u2, v2, color, alpha),
                spriteUnitVertex(x3, y3, z3, u3, v3, color, alpha));
    }

    public static void spriteUnitQuadWithVertexAlpha(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        spriteUnitQuad(sprite, context,
                spriteUnitVertex(x0, y0, z0, u0, v0, color, alpha0),
                spriteUnitVertex(x1, y1, z1, u1, v1, color, alpha1),
                spriteUnitVertex(x2, y2, z2, u2, v2, color, alpha2),
                spriteUnitVertex(x3, y3, z3, u3, v3, color, alpha3));
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

    public static void spriteQuadWithComputedNormal(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        spriteQuadWithComputedNormal(sprite, context,
                spritePixelVertex(x0, y0, z0, pixelU0, pixelV0, color, alpha),
                spritePixelVertex(x1, y1, z1, pixelU1, pixelV1, color, alpha),
                spritePixelVertex(x2, y2, z2, pixelU2, pixelV2, color, alpha),
                spritePixelVertex(x3, y3, z3, pixelU3, pixelV3, color, alpha));
    }

    public static void spriteQuadWithComputedNormalAndVertexAlpha(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double pixelU0, double pixelV0, int alpha0,
            double x1, double y1, double z1, double pixelU1, double pixelV1, int alpha1,
            double x2, double y2, double z2, double pixelU2, double pixelV2, int alpha2,
            double x3, double y3, double z3, double pixelU3, double pixelV3, int alpha3,
            int color) {
        spriteQuadWithComputedNormal(sprite, context,
                spritePixelVertex(x0, y0, z0, pixelU0, pixelV0, color, alpha0),
                spritePixelVertex(x1, y1, z1, pixelU1, pixelV1, color, alpha1),
                spritePixelVertex(x2, y2, z2, pixelU2, pixelV2, color, alpha2),
                spritePixelVertex(x3, y3, z3, pixelU3, pixelV3, color, alpha3));
    }

    public static void spriteUnitQuadWithComputedNormal(TextureAtlasSprite sprite, ObjRenderContext context,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vector3f normal = computedNormal(v0, v1, v2);
        spriteUnitQuad(sprite, context, normal.x(), normal.y(), normal.z(), v0, v1, v2, v3);
    }

    public static void spriteUnitQuadWithComputedNormal(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        spriteUnitQuadWithComputedNormal(sprite, context,
                spriteUnitVertex(x0, y0, z0, u0, v0, color, alpha),
                spriteUnitVertex(x1, y1, z1, u1, v1, color, alpha),
                spriteUnitVertex(x2, y2, z2, u2, v2, color, alpha),
                spriteUnitVertex(x3, y3, z3, u3, v3, color, alpha));
    }

    public static void spriteUnitQuadWithComputedNormalAndVertexAlpha(TextureAtlasSprite sprite, ObjRenderContext context,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        spriteUnitQuadWithComputedNormal(sprite, context,
                spriteUnitVertex(x0, y0, z0, u0, v0, color, alpha0),
                spriteUnitVertex(x1, y1, z1, u1, v1, color, alpha1),
                spriteUnitVertex(x2, y2, z2, u2, v2, color, alpha2),
                spriteUnitVertex(x3, y3, z3, u3, v3, color, alpha3));
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
                .overlayCoords(packedOverlay(context, vertex))
                .uv2(packedLight(context, vertex))
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
                .overlayCoords(packedOverlay(context, vertex))
                .uv2(packedLight(context, vertex))
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

    private static int packedLight(ObjRenderContext context, Vertex vertex) {
        return vertex.packedLight() == INHERIT_LIGHT ? context.packedLight() : vertex.packedLight();
    }

    private static int packedOverlay(ObjRenderContext context, Vertex vertex) {
        int overlay = vertex.packedOverlay() == INHERIT_OVERLAY ? context.packedOverlay() : vertex.packedOverlay();
        return overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay;
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

    private static int clampColor(float value) {
        return clampAlpha(Math.round(value * 255.0F));
    }

    public static int alpha(float alpha) {
        return clampColor(alpha);
    }

    public static int rgb(float red, float green, float blue) {
        return clampColor(red) << 16 | clampColor(green) << 8 | clampColor(blue);
    }

    public static int rgb(int red, int green, int blue) {
        return clampAlpha(red) << 16 | clampAlpha(green) << 8 | clampAlpha(blue);
    }

    public static int legacyColor3ub(byte red, byte green, byte blue) {
        return (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }

    public static int legacyLightmap(float lightmapX, float lightmapY) {
        return ObjRenderContext.legacyLightmap(lightmapX, lightmapY);
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

    public record Vertex(double x, double y, double z, float u, float v, int color, int alpha,
            int packedLight, int packedOverlay) {
        public Vertex withColor(int color) {
            return new Vertex(x, y, z, u, v, color & 0xFFFFFF, alpha, packedLight, packedOverlay);
        }

        public Vertex withRgb(int red, int green, int blue) {
            return withColor(LegacyTexturedQuadRenderer.rgb(red, green, blue));
        }

        public Vertex withRgb(float red, float green, float blue) {
            return withColor(LegacyTexturedQuadRenderer.rgb(red, green, blue));
        }

        public Vertex withAlpha(int alpha) {
            return new Vertex(x, y, z, u, v, color, clampAlpha(alpha), packedLight, packedOverlay);
        }

        public Vertex withAlpha(float alpha) {
            return withAlpha(LegacyTexturedQuadRenderer.alpha(alpha));
        }

        public Vertex withRgba(int red, int green, int blue, int alpha) {
            return new Vertex(x, y, z, u, v,
                    clampAlpha(red) << 16 | clampAlpha(green) << 8 | clampAlpha(blue), clampAlpha(alpha),
                    packedLight, packedOverlay);
        }

        public Vertex withRgba(float red, float green, float blue, float alpha) {
            return new Vertex(x, y, z, u, v,
                    LegacyTexturedQuadRenderer.rgb(red, green, blue), LegacyTexturedQuadRenderer.alpha(alpha),
                    packedLight, packedOverlay);
        }

        public Vertex withArgb(int argb) {
            return new Vertex(x, y, z, u, v, argb & 0xFFFFFF, argb >>> 24 & 255, packedLight, packedOverlay);
        }

        public Vertex withLegacyColor3ub(byte red, byte green, byte blue) {
            return withColor(LegacyTexturedQuadRenderer.legacyColor3ub(red, green, blue));
        }

        public Vertex withPackedLight(int packedLight) {
            return new Vertex(x, y, z, u, v, color, alpha, packedLight, packedOverlay);
        }

        public Vertex withLegacyLightmap(float lightmapX, float lightmapY) {
            return withPackedLight(LegacyTexturedQuadRenderer.legacyLightmap(lightmapX, lightmapY));
        }

        public Vertex fullBright() {
            return withPackedLight(LightTexture.FULL_BRIGHT);
        }

        public Vertex withPackedOverlay(int packedOverlay) {
            return new Vertex(x, y, z, u, v, color, alpha, packedLight, packedOverlay);
        }

        public Vertex inheritPackedLight() {
            return withPackedLight(INHERIT_LIGHT);
        }

        public Vertex inheritPackedOverlay() {
            return withPackedOverlay(INHERIT_OVERLAY);
        }
    }

    private LegacyTexturedQuadRenderer() {
    }
}

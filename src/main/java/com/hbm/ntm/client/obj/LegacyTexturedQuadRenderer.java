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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LegacyTexturedQuadRenderer {
    public static final int INHERIT_LIGHT = -1;
    public static final int INHERIT_OVERLAY = -1;
    private static final Map<ResourceLocation, TextureAtlasSprite> BLOCK_SPRITE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, TextureAtlasSprite> BLOCK_SPRITE_PATH_CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<float[]> NORMAL_SCRATCH = ThreadLocal.withInitial(() -> new float[3]);

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
        return BLOCK_SPRITE_CACHE.computeIfAbsent(textureLocation, LegacyTexturedQuadRenderer::resolveBlockSprite);
    }

    public static TextureAtlasSprite blockSprite(String namespace, String path) {
        return BLOCK_SPRITE_PATH_CACHE.computeIfAbsent(namespace + '\n' + path,
                ignored -> resolveBlockSprite(new ResourceLocation(namespace, path)));
    }

    public static void clearSpriteCache() {
        BLOCK_SPRITE_CACHE.clear();
        BLOCK_SPRITE_PATH_CACHE.clear();
    }

    private static TextureAtlasSprite resolveBlockSprite(ResourceLocation textureLocation) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureLocation);
    }

    public static void quad(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vertex[] vertices = {v0, v1, v2, v3};
        if (tryTransientQuad(texture, poseStack, buffer, packedLight, packedOverlay, renderMode, normalX, normalY,
                normalZ, vertices)) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(renderMode(renderMode, vertices).renderType(texture));
        emitQuad(consumer, poseStack.last(), normalX, normalY, normalZ, packedLight, packedOverlay, vertices);
    }

    public static void quadDirect(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        quadWithVertexAlpha(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, alpha,
                x1, y1, z1, u1, v1, alpha,
                x2, y2, z2, u2, v2, alpha,
                x3, y3, z3, u3, v3, alpha,
                color);
    }

    public static void pixelQuad(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, double textureWidth, double textureHeight,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        if (textureWidth == 0.0D || textureHeight == 0.0D) {
            return;
        }
        quad(texture, poseStack, buffer, packedLight, packedOverlay, renderMode, normalX, normalY, normalZ,
                vertex(x0, y0, z0, pixelU0 / textureWidth, pixelV0 / textureHeight, color, alpha),
                vertex(x1, y1, z1, pixelU1 / textureWidth, pixelV1 / textureHeight, color, alpha),
                vertex(x2, y2, z2, pixelU2 / textureWidth, pixelV2 / textureHeight, color, alpha),
                vertex(x3, y3, z3, pixelU3 / textureWidth, pixelV3 / textureHeight, color, alpha));
    }

    public static void pixelQuadDirect(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, double textureWidth, double textureHeight,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        if (textureWidth == 0.0D || textureHeight == 0.0D) {
            return;
        }
        quadWithVertexAlpha(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, normalY, normalZ,
                x0, y0, z0, pixelU0 / textureWidth, pixelV0 / textureHeight, alpha,
                x1, y1, z1, pixelU1 / textureWidth, pixelV1 / textureHeight, alpha,
                x2, y2, z2, pixelU2 / textureWidth, pixelV2 / textureHeight, alpha,
                x3, y3, z3, pixelU3 / textureWidth, pixelV3 / textureHeight, alpha,
                color);
    }

    public static TexturedQuadBatch texturedQuadBatch(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, int alpha) {
        return new TexturedQuadBatch(texture, poseStack, buffer, renderMode, clampAlpha(alpha));
    }

    public static void quadDirect(TexturedQuadBatch batch, int packedLight, int packedOverlay,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        if (batch == null) {
            return;
        }
        int resolvedAlpha = clampAlpha(alpha);
        if (resolvedAlpha != batch.alpha()) {
            quadDirect(batch.texture(), batch.poseStack(), batch.buffer(), packedLight, packedOverlay,
                    batch.renderMode(), normalX, normalY, normalZ,
                    x0, y0, z0, u0, v0,
                    x1, y1, z1, u1, v1,
                    x2, y2, z2, u2, v2,
                    x3, y3, z3, u3, v3,
                    color, alpha);
            return;
        }
        int rgb = color & 0xFFFFFF;
        int resolvedOverlay = resolvedOverlay(packedOverlay);
        if (LegacyWavefrontModel.renderTexturedTransientQuad(batch.texture(), batch.poseStack(), batch.buffer(),
                packedLight, resolvedOverlay, batch.renderMode(), normalX, normalY, normalZ,
                x0, y0, z0, (float) u0, (float) v0,
                x1, y1, z1, (float) u1, (float) v1,
                x2, y2, z2, (float) u2, (float) v2,
                x3, y3, z3, (float) u3, (float) v3,
                rgb, resolvedAlpha)) {
            return;
        }
        PoseStack.Pose pose = batch.pose();
        quadWithVertexAlpha(batch.consumer(), pose.pose(), pose.normal(), packedLight, resolvedOverlay,
                normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, resolvedAlpha,
                x1, y1, z1, u1, v1, resolvedAlpha,
                x2, y2, z2, u2, v2, resolvedAlpha,
                x3, y3, z3, u3, v3, resolvedAlpha,
                rgb);
    }

    public static void pixelQuadDirect(TexturedQuadBatch batch, int packedLight, int packedOverlay,
            float normalX, float normalY, float normalZ, double textureWidth, double textureHeight,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        if (textureWidth == 0.0D || textureHeight == 0.0D) {
            return;
        }
        quadDirect(batch, packedLight, packedOverlay, normalX, normalY, normalZ,
                x0, y0, z0, pixelU0 / textureWidth, pixelV0 / textureHeight,
                x1, y1, z1, pixelU1 / textureWidth, pixelV1 / textureHeight,
                x2, y2, z2, pixelU2 / textureWidth, pixelV2 / textureHeight,
                x3, y3, z3, pixelU3 / textureWidth, pixelV3 / textureHeight,
                color, alpha);
    }

    public static void quadWithComputedNormal(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        float[] normal = computedNormalScratch(v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(),
                v2.x(), v2.y(), v2.z());
        quad(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normal[0], normal[1], normal[2], v0, v1, v2, v3);
    }

    public static void quadWithComputedNormalAndVertexAlpha(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        quadWithComputedNormal(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                vertex(x0, y0, z0, u0, v0, color, alpha0),
                vertex(x1, y1, z1, u1, v1, color, alpha1),
                vertex(x2, y2, z2, u2, v2, color, alpha2),
                vertex(x3, y3, z3, u3, v3, color, alpha3));
    }

    public static void quadWithComputedNormalDirect(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        float[] normal = computedNormalScratch(x0, y0, z0, x1, y1, z1, x2, y2, z2);
        quadWithVertexAlpha(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normal[0], normal[1], normal[2],
                x0, y0, z0, u0, v0, alpha0,
                x1, y1, z1, u1, v1, alpha1,
                x2, y2, z2, u2, v2, alpha2,
                x3, y3, z3, u3, v3, alpha3,
                color);
    }

    public static void quadWithComputedNormalDirect(VertexConsumer consumer, PoseStack.Pose pose,
            int packedLight, int packedOverlay,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        float[] normal = computedNormalScratch(x0, y0, z0, x1, y1, z1, x2, y2, z2);
        quadWithVertexAlpha(consumer, pose, packedLight, packedOverlay,
                normal[0], normal[1], normal[2],
                x0, y0, z0, u0, v0, alpha0,
                x1, y1, z1, u1, v1, alpha1,
                x2, y2, z2, u2, v2, alpha2,
                x3, y3, z3, u3, v3, alpha3,
                color);
    }

    public static void quadWithVertexAlpha(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        VertexConsumer consumer = vertexAlphaConsumer(texture, buffer, renderMode);
        PoseStack.Pose pose = poseStack.last();
        quadWithVertexAlpha(consumer, pose, packedLight, packedOverlay, normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, alpha0,
                x1, y1, z1, u1, v1, alpha1,
                x2, y2, z2, u2, v2, alpha2,
                x3, y3, z3, u3, v3, alpha3,
                color);
    }

    public static VertexConsumer vertexAlphaConsumer(ResourceLocation texture, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode) {
        return buffer.getBuffer(renderMode.renderType(texture));
    }

    public static void quadWithVertexAlpha(VertexConsumer consumer, PoseStack.Pose pose,
            int packedLight, int packedOverlay, float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        quadWithVertexAlpha(consumer, pose.pose(), pose.normal(), packedLight, packedOverlay,
                normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, alpha0,
                x1, y1, z1, u1, v1, alpha1,
                x2, y2, z2, u2, v2, alpha2,
                x3, y3, z3, u3, v3, alpha3,
                color);
    }

    public static void quadWithVertexAlpha(VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            int packedLight, int packedOverlay, float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int alpha3,
            int color) {
        int resolvedOverlay = resolvedOverlay(packedOverlay);
        int rgb = color & 0xFFFFFF;
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x0, y0, z0, u0, v0, rgb, alpha0);
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x1, y1, z1, u1, v1, rgb, alpha1);
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x2, y2, z2, u2, v2, rgb, alpha2);
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x3, y3, z3, u3, v3, rgb, alpha3);
    }

    public static void quadWithVertexColors(VertexConsumer consumer, PoseStack.Pose pose,
            int packedLight, int packedOverlay, float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0, int color0, int alpha0,
            double x1, double y1, double z1, double u1, double v1, int color1, int alpha1,
            double x2, double y2, double z2, double u2, double v2, int color2, int alpha2,
            double x3, double y3, double z3, double u3, double v3, int color3, int alpha3) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        int resolvedOverlay = resolvedOverlay(packedOverlay);
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x0, y0, z0, u0, v0, color0 & 0xFFFFFF, alpha0);
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x1, y1, z1, u1, v1, color1 & 0xFFFFFF, alpha1);
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x2, y2, z2, u2, v2, color2 & 0xFFFFFF, alpha2);
        emitPrimitiveVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, resolvedOverlay,
                x3, y3, z3, u3, v3, color3 & 0xFFFFFF, alpha3);
    }

    public static void spriteQuad(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, normalY, normalZ, false, v0, v1, v2, v3);
    }

    public static void spriteQuad(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, Vertex[] vertices) {
        spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, normalY, normalZ, false, vertices);
    }

    public static void spritePixelQuadDirect(TextureAtlasSprite sprite, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        spriteQuadDirect(sprite, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, normalY, normalZ, false,
                x0, y0, z0, pixelU0, pixelV0,
                x1, y1, z1, pixelU1, pixelV1,
                x2, y2, z2, pixelU2, pixelV2,
                x3, y3, z3, pixelU3, pixelV3,
                color, alpha);
    }

    public static SpriteQuadBatch spriteQuadBatch(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int alpha) {
        return new SpriteQuadBatch(poseStack, buffer, renderMode, clampAlpha(alpha));
    }

    public static VertexConsumer spriteAtlasConsumer(MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int alpha) {
        return buffer.getBuffer(renderMode.withAlpha(clampAlpha(alpha)).renderType(InventoryMenu.BLOCK_ATLAS));
    }

    public static void spritePixelQuadDirect(TextureAtlasSprite sprite, SpritePixelQuadSink sink,
            int packedLight, int packedOverlay,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        if (sink == null) {
            return;
        }
        sink.add(sprite, packedLight, packedOverlay, normalX, normalY, normalZ,
                x0, y0, z0, pixelU0, pixelV0,
                x1, y1, z1, pixelU1, pixelV1,
                x2, y2, z2, pixelU2, pixelV2,
                x3, y3, z3, pixelU3, pixelV3,
                color, alpha);
    }

    public static void spritePixelQuadDirect(TextureAtlasSprite sprite, VertexConsumer consumer,
            Matrix4f position, Matrix3f normalMatrix, int packedLight, int packedOverlay,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        int rgb = color & 0xFFFFFF;
        int resolvedAlpha = clampAlpha(alpha);
        int resolvedOverlay = resolvedOverlay(packedOverlay);
        emitPrimitiveSpriteQuad(consumer, position, normalMatrix, sprite, normalX, normalY, normalZ, false,
                packedLight, resolvedOverlay,
                x0, y0, z0, pixelU0, pixelV0,
                x1, y1, z1, pixelU1, pixelV1,
                x2, y2, z2, pixelU2, pixelV2,
                x3, y3, z3, pixelU3, pixelV3,
                rgb, resolvedAlpha);
    }

    public static void spritePixelQuadDirect(TextureAtlasSprite sprite, SpriteQuadBatch batch,
            int packedLight, int packedOverlay,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double pixelU0, double pixelV0,
            double x1, double y1, double z1, double pixelU1, double pixelV1,
            double x2, double y2, double z2, double pixelU2, double pixelV2,
            double x3, double y3, double z3, double pixelU3, double pixelV3,
            int color, int alpha) {
        spriteQuadDirect(sprite, batch, packedLight, packedOverlay, normalX, normalY, normalZ, false,
                x0, y0, z0, pixelU0, pixelV0,
                x1, y1, z1, pixelU1, pixelV1,
                x2, y2, z2, pixelU2, pixelV2,
                x3, y3, z3, pixelU3, pixelV3,
                color, alpha);
    }

    private static void spriteQuad(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, boolean unitUv, Vertex v0, Vertex v1, Vertex v2,
            Vertex v3) {
        Vertex[] vertices = {v0, v1, v2, v3};
        spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, normalY, normalZ, unitUv, vertices);
    }

    private static void spriteQuad(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, boolean unitUv, Vertex[] vertices) {
        if (tryTransientSpriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay, renderMode, normalX,
                normalY, normalZ, unitUv, vertices)) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(renderMode(renderMode, vertices).renderType(InventoryMenu.BLOCK_ATLAS));
        emitSpriteQuad(consumer, poseStack.last(), sprite, normalX, normalY, normalZ, unitUv, packedLight,
                packedOverlay, vertices);
    }

    private static void spriteQuadDirect(TextureAtlasSprite sprite, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, boolean unitUv,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        int rgb = color & 0xFFFFFF;
        int resolvedAlpha = clampAlpha(alpha);
        int resolvedOverlay = resolvedOverlay(packedOverlay);
        if (LegacyWavefrontModel.renderSpriteTransientQuad(sprite, poseStack, buffer, packedLight, resolvedOverlay,
                renderMode, normalX, normalY, normalZ, unitUv,
                x0, y0, z0, (float) u0, (float) v0,
                x1, y1, z1, (float) u1, (float) v1,
                x2, y2, z2, (float) u2, (float) v2,
                x3, y3, z3, (float) u3, (float) v3,
                rgb, resolvedAlpha)) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(renderMode.withAlpha(resolvedAlpha)
                .renderType(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose pose = poseStack.last();
        emitPrimitiveSpriteQuad(consumer, pose, sprite, normalX, normalY, normalZ, unitUv, packedLight,
                resolvedOverlay,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                rgb, resolvedAlpha);
    }

    private static void spriteQuadDirect(TextureAtlasSprite sprite, SpriteQuadBatch batch,
            int packedLight, int packedOverlay,
            float normalX, float normalY, float normalZ, boolean unitUv,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        int resolvedAlpha = clampAlpha(alpha);
        if (resolvedAlpha != batch.alpha()) {
            spriteQuadDirect(sprite, batch.poseStack(), batch.buffer(), packedLight, packedOverlay,
                    batch.renderMode(), normalX, normalY, normalZ, unitUv,
                    x0, y0, z0, u0, v0,
                    x1, y1, z1, u1, v1,
                    x2, y2, z2, u2, v2,
                    x3, y3, z3, u3, v3,
                    color, alpha);
            return;
        }
        int rgb = color & 0xFFFFFF;
        int resolvedOverlay = resolvedOverlay(packedOverlay);
        if (LegacyWavefrontModel.renderSpriteTransientQuad(sprite, batch.poseStack(), batch.buffer(), packedLight,
                resolvedOverlay, batch.renderMode(), normalX, normalY, normalZ, unitUv,
                x0, y0, z0, (float) u0, (float) v0,
                x1, y1, z1, (float) u1, (float) v1,
                x2, y2, z2, (float) u2, (float) v2,
                x3, y3, z3, (float) u3, (float) v3,
                rgb, resolvedAlpha)) {
            return;
        }
        emitPrimitiveSpriteQuad(batch.consumer(), batch.pose(), sprite, normalX, normalY, normalZ, unitUv,
                packedLight, resolvedOverlay,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                rgb, resolvedAlpha);
    }

    private static void emitPrimitiveSpriteQuad(VertexConsumer consumer, PoseStack.Pose pose,
            TextureAtlasSprite sprite, float normalX, float normalY, float normalZ, boolean unitUv,
            int packedLight, int resolvedOverlay,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int rgb, int resolvedAlpha) {
        emitPrimitiveSpriteQuad(consumer, pose.pose(), pose.normal(), sprite, normalX, normalY, normalZ, unitUv,
                packedLight, resolvedOverlay,
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                rgb, resolvedAlpha);
    }

    private static void emitPrimitiveSpriteQuad(VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            TextureAtlasSprite sprite, float normalX, float normalY, float normalZ, boolean unitUv,
            int packedLight, int resolvedOverlay,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int rgb, int resolvedAlpha) {
        float pixelScale = unitUv ? 16.0F : 1.0F;
        emitPrimitiveSpriteVertex(consumer, position, normal, sprite, normalX, normalY, normalZ, pixelScale,
                packedLight, resolvedOverlay, x0, y0, z0, u0, v0, rgb, resolvedAlpha);
        emitPrimitiveSpriteVertex(consumer, position, normal, sprite, normalX, normalY, normalZ, pixelScale,
                packedLight, resolvedOverlay, x1, y1, z1, u1, v1, rgb, resolvedAlpha);
        emitPrimitiveSpriteVertex(consumer, position, normal, sprite, normalX, normalY, normalZ, pixelScale,
                packedLight, resolvedOverlay, x2, y2, z2, u2, v2, rgb, resolvedAlpha);
        emitPrimitiveSpriteVertex(consumer, position, normal, sprite, normalX, normalY, normalZ, pixelScale,
                packedLight, resolvedOverlay, x3, y3, z3, u3, v3, rgb, resolvedAlpha);
    }

    public static final class SpriteQuadBatch {
        private final PoseStack poseStack;
        private final MultiBufferSource buffer;
        private final LegacyTexturedRenderMode renderMode;
        private final int alpha;
        private VertexConsumer consumer;
        private PoseStack.Pose pose;

        private SpriteQuadBatch(PoseStack poseStack, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, int alpha) {
            this.poseStack = poseStack;
            this.buffer = buffer;
            this.renderMode = renderMode;
            this.alpha = alpha;
        }

        private PoseStack poseStack() {
            return poseStack;
        }

        private MultiBufferSource buffer() {
            return buffer;
        }

        private LegacyTexturedRenderMode renderMode() {
            return renderMode;
        }

        private int alpha() {
            return alpha;
        }

        private VertexConsumer consumer() {
            if (consumer == null) {
                consumer = buffer.getBuffer(renderMode.withAlpha(alpha).renderType(InventoryMenu.BLOCK_ATLAS));
            }
            return consumer;
        }

        private PoseStack.Pose pose() {
            if (pose == null) {
                pose = poseStack.last();
            }
            return pose;
        }
    }

    public static final class TexturedQuadBatch {
        private final ResourceLocation texture;
        private final PoseStack poseStack;
        private final MultiBufferSource buffer;
        private final LegacyTexturedRenderMode renderMode;
        private final int alpha;
        private VertexConsumer consumer;
        private PoseStack.Pose pose;

        private TexturedQuadBatch(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, int alpha) {
            this.texture = texture;
            this.poseStack = poseStack;
            this.buffer = buffer;
            this.renderMode = renderMode;
            this.alpha = alpha;
        }

        private ResourceLocation texture() {
            return texture;
        }

        private PoseStack poseStack() {
            return poseStack;
        }

        private MultiBufferSource buffer() {
            return buffer;
        }

        private LegacyTexturedRenderMode renderMode() {
            return renderMode;
        }

        private int alpha() {
            return alpha;
        }

        private VertexConsumer consumer() {
            if (consumer == null) {
                consumer = buffer.getBuffer(renderMode.withAlpha(alpha).renderType(texture));
            }
            return consumer;
        }

        private PoseStack.Pose pose() {
            if (pose == null) {
                pose = poseStack.last();
            }
            return pose;
        }
    }

    public interface SpritePixelQuadSink {
        void add(TextureAtlasSprite sprite, int packedLight, int packedOverlay,
                float normalX, float normalY, float normalZ,
                double x0, double y0, double z0, double pixelU0, double pixelV0,
                double x1, double y1, double z1, double pixelU1, double pixelV1,
                double x2, double y2, double z2, double pixelU2, double pixelV2,
                double x3, double y3, double z3, double pixelU3, double pixelV3,
                int color, int alpha);
    }

    private static void emitQuad(VertexConsumer consumer, PoseStack.Pose pose,
            float normalX, float normalY, float normalZ, int packedLight, int packedOverlay, Vertex[] vertices) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        for (Vertex vertex : vertices) {
            emitVertex(consumer, position, normal, normalX, normalY, normalZ, packedLight, packedOverlay, vertex);
        }
    }

    private static void emitSpriteQuad(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
            float normalX, float normalY, float normalZ, boolean unitUv, int packedLight, int packedOverlay,
            Vertex[] vertices) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        for (Vertex vertex : vertices) {
            emitSpriteVertex(consumer, position, normal, sprite, normalX, normalY, normalZ, unitUv,
                    packedLight, packedOverlay, vertex);
        }
    }

    public static void emitPositionColorTexLightmapQuadIdentity(VertexConsumer consumer, int packedLight,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, v0);
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, v1);
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, v2);
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, v3);
    }

    public static void emitPositionColorTexLightmapQuadIdentityDirect(VertexConsumer consumer, int packedLight,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        int rgb = color & 0xFFFFFF;
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, x0, y0, z0, u0, v0, rgb, alpha);
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, x1, y1, z1, u1, v1, rgb, alpha);
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, x2, y2, z2, u2, v2, rgb, alpha);
        emitPositionColorTexLightmapVertexIdentity(consumer, packedLight, x3, y3, z3, u3, v3, rgb, alpha);
    }

    public static void emitParticleQuadIdentity(VertexConsumer consumer, int packedLight,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        emitParticleVertexIdentity(consumer, packedLight, v0);
        emitParticleVertexIdentity(consumer, packedLight, v1);
        emitParticleVertexIdentity(consumer, packedLight, v2);
        emitParticleVertexIdentity(consumer, packedLight, v3);
    }

    private static void emitPositionColorTexLightmapVertexIdentity(VertexConsumer consumer, int packedLight,
            Vertex vertex) {
        consumer.vertex(vertex.x(), vertex.y(), vertex.z())
                .color(vertex.color() >> 16 & 255, vertex.color() >> 8 & 255, vertex.color() & 255,
                        clampAlpha(vertex.alpha()))
                .uv(vertex.u(), vertex.v())
                .uv2(packedLight(packedLight, vertex))
                .endVertex();
    }

    private static void emitPositionColorTexLightmapVertexIdentity(VertexConsumer consumer, int packedLight,
            double x, double y, double z, double u, double v, int color, int alpha) {
        consumer.vertex(x, y, z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, clampAlpha(alpha))
                .uv((float) u, (float) v)
                .uv2(packedLight)
                .endVertex();
    }

    private static void emitParticleVertexIdentity(VertexConsumer consumer, int packedLight, Vertex vertex) {
        consumer.vertex(vertex.x(), vertex.y(), vertex.z())
                .uv(vertex.u(), vertex.v())
                .color(vertex.color() >> 16 & 255, vertex.color() >> 8 & 255, vertex.color() & 255,
                        clampAlpha(vertex.alpha()))
                .uv2(packedLight(packedLight, vertex))
                .endVertex();
    }

    private static void emitVertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            float normalX, float normalY, float normalZ, int packedLight, int packedOverlay, Vertex vertex) {
        consumer.vertex(position, (float) vertex.x(), (float) vertex.y(), (float) vertex.z())
                .color(vertex.color() >> 16 & 255, vertex.color() >> 8 & 255, vertex.color() & 255,
                        clampAlpha(vertex.alpha()))
                .uv(vertex.u(), vertex.v())
                .overlayCoords(packedOverlay(packedOverlay, vertex))
                .uv2(packedLight(packedLight, vertex))
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    private static void emitPrimitiveVertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            float normalX, float normalY, float normalZ, int packedLight, int packedOverlay,
            double x, double y, double z, double u, double v, int color, int alpha) {
        consumer.vertex(position, (float) x, (float) y, (float) z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, clampAlpha(alpha))
                .uv((float) u, (float) v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    private static void emitSpriteVertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            TextureAtlasSprite sprite, float normalX, float normalY, float normalZ, boolean unitUv, int packedLight,
            int packedOverlay, Vertex vertex) {
        float pixelScale = unitUv ? 16.0F : 1.0F;
        consumer.vertex(position, (float) vertex.x(), (float) vertex.y(), (float) vertex.z())
                .color(vertex.color() >> 16 & 255, vertex.color() >> 8 & 255, vertex.color() & 255,
                        clampAlpha(vertex.alpha()))
                .uv(sprite.getU(vertex.u() * pixelScale), sprite.getV(vertex.v() * pixelScale))
                .overlayCoords(packedOverlay(packedOverlay, vertex))
                .uv2(packedLight(packedLight, vertex))
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    private static void emitPrimitiveSpriteVertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal,
            TextureAtlasSprite sprite, float normalX, float normalY, float normalZ, float pixelScale, int packedLight,
            int packedOverlay, double x, double y, double z, double u, double v, int color, int alpha) {
        consumer.vertex(position, (float) x, (float) y, (float) z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, alpha)
                .uv(sprite.getU((float) u * pixelScale), sprite.getV((float) v * pixelScale))
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    private static boolean tryTransientQuad(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, Vertex[] vertices) {
        Vertex first = vertices[0];
        int color = first.color() & 0xFFFFFF;
        int alpha = clampAlpha(first.alpha());
        int resolvedLight = packedLight(packedLight, first);
        int resolvedOverlay = packedOverlay(packedOverlay, first);
        for (int i = 1; i < vertices.length; i++) {
            Vertex vertex = vertices[i];
            if ((vertex.color() & 0xFFFFFF) != color || clampAlpha(vertex.alpha()) != alpha
                    || packedLight(packedLight, vertex) != resolvedLight
                    || packedOverlay(packedOverlay, vertex) != resolvedOverlay) {
                return false;
            }
        }
        return LegacyWavefrontModel.renderTexturedTransientQuad(texture, poseStack, buffer, resolvedLight,
                resolvedOverlay, renderMode, normalX, normalY, normalZ,
                first.x(), first.y(), first.z(), first.u(), first.v(),
                vertices[1].x(), vertices[1].y(), vertices[1].z(), vertices[1].u(), vertices[1].v(),
                vertices[2].x(), vertices[2].y(), vertices[2].z(), vertices[2].u(), vertices[2].v(),
                vertices[3].x(), vertices[3].y(), vertices[3].z(), vertices[3].u(), vertices[3].v(),
                color, alpha);
    }

    private static boolean tryTransientSpriteQuad(TextureAtlasSprite sprite, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, boolean unitUv, Vertex[] vertices) {
        Vertex first = vertices[0];
        int color = first.color() & 0xFFFFFF;
        int alpha = clampAlpha(first.alpha());
        int resolvedLight = packedLight(packedLight, first);
        int resolvedOverlay = packedOverlay(packedOverlay, first);
        for (int i = 1; i < vertices.length; i++) {
            Vertex vertex = vertices[i];
            if ((vertex.color() & 0xFFFFFF) != color || clampAlpha(vertex.alpha()) != alpha
                    || packedLight(packedLight, vertex) != resolvedLight
                    || packedOverlay(packedOverlay, vertex) != resolvedOverlay) {
                return false;
            }
        }
        return LegacyWavefrontModel.renderSpriteTransientQuad(sprite, poseStack, buffer, resolvedLight,
                resolvedOverlay, renderMode, normalX, normalY, normalZ, unitUv,
                first.x(), first.y(), first.z(), first.u(), first.v(),
                vertices[1].x(), vertices[1].y(), vertices[1].z(), vertices[1].u(), vertices[1].v(),
                vertices[2].x(), vertices[2].y(), vertices[2].z(), vertices[2].u(), vertices[2].v(),
                vertices[3].x(), vertices[3].y(), vertices[3].z(), vertices[3].u(), vertices[3].v(),
                color, alpha);
    }

    private static LegacyTexturedRenderMode renderMode(LegacyTexturedRenderMode renderMode, Vertex[] vertices) {
        int alpha = 255;
        for (Vertex vertex : vertices) {
            alpha = Math.min(alpha, clampAlpha(vertex.alpha()));
        }
        return renderMode.withAlpha(alpha);
    }

    private static int packedLight(int packedLight, Vertex vertex) {
        return vertex.packedLight() == INHERIT_LIGHT ? packedLight : vertex.packedLight();
    }

    private static int packedOverlay(int packedOverlay, Vertex vertex) {
        int overlay = vertex.packedOverlay() == INHERIT_OVERLAY ? packedOverlay : vertex.packedOverlay();
        return resolvedOverlay(overlay);
    }

    private static int resolvedOverlay(int packedOverlay) {
        return packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay;
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
        return clampLightmapCoord(Math.round(lightmapY)) << 16 | clampLightmapCoord(Math.round(lightmapX));
    }

    private static int clampLightmapCoord(int value) {
        return Math.max(0, Math.min(65535, value));
    }

    public static Vector3f normal(float x, float y, float z) {
        float lengthSquared = x * x + y * y + z * z;
        if (lengthSquared <= 1.0E-6F) {
            return new Vector3f(0.0F, 1.0F, 0.0F);
        }
        float invLength = (float) (1.0D / Math.sqrt(lengthSquared));
        return new Vector3f(x * invLength, y * invLength, z * invLength);
    }

    public static Vector3f computedNormal(Vertex v0, Vertex v1, Vertex v2) {
        float[] normal = computedNormalScratch(v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z(),
                v2.x(), v2.y(), v2.z());
        return new Vector3f(normal[0], normal[1], normal[2]);
    }

    private static float[] computedNormalScratch(double x0, double y0, double z0,
            double x1, double y1, double z1, double x2, double y2, double z2) {
        float edgeAX = (float) (x1 - x0);
        float edgeAY = (float) (y1 - y0);
        float edgeAZ = (float) (z1 - z0);
        float edgeBX = (float) (x2 - x0);
        float edgeBY = (float) (y2 - y0);
        float edgeBZ = (float) (z2 - z0);
        float normalX = edgeAY * edgeBZ - edgeAZ * edgeBY;
        float normalY = edgeAZ * edgeBX - edgeAX * edgeBZ;
        float normalZ = edgeAX * edgeBY - edgeAY * edgeBX;
        float lengthSquared = normalX * normalX + normalY * normalY + normalZ * normalZ;
        float[] normal = NORMAL_SCRATCH.get();
        if (lengthSquared <= 1.0E-6F) {
            normal[0] = 0.0F;
            normal[1] = 1.0F;
            normal[2] = 0.0F;
        } else {
            float invLength = (float) (1.0D / Math.sqrt(lengthSquared));
            normal[0] = normalX * invLength;
            normal[1] = normalY * invLength;
            normal[2] = normalZ * invLength;
        }
        return normal;
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

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

    public static void clearSpriteCache() {
        BLOCK_SPRITE_CACHE.clear();
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

    public static void quadWithComputedNormal(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vector3f normal = computedNormal(v0, v1, v2);
        quad(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normal.x(), normal.y(), normal.z(), v0, v1, v2, v3);
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

    public static void spriteQuad(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        spriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, normalY, normalZ, false, v0, v1, v2, v3);
    }

    private static void spriteQuad(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            float normalX, float normalY, float normalZ, boolean unitUv, Vertex v0, Vertex v1, Vertex v2,
            Vertex v3) {
        Vertex[] vertices = {v0, v1, v2, v3};
        if (tryTransientSpriteQuad(sprite, poseStack, buffer, packedLight, packedOverlay, renderMode, normalX,
                normalY, normalZ, unitUv, vertices)) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(renderMode(renderMode, vertices).renderType(InventoryMenu.BLOCK_ATLAS));
        emitSpriteQuad(consumer, poseStack.last(), sprite, normalX, normalY, normalZ, unitUv, packedLight,
                packedOverlay, vertices);
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
        return overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay;
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

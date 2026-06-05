package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class LegacyAtlasCuboidRenderer {
    private static final double SMALL_BLOCK_PIXEL = 1.0D / 16.0D;
    private static final double SMALL_BLOCK_MIN = 11.0D * SMALL_BLOCK_PIXEL / 2.0D;
    private static final double SMALL_BLOCK_MAX = 1.0D - SMALL_BLOCK_MIN;

    public static void smallBlock(TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            ObjRenderContext context, double x, double y, double z) {
        cuboid(top, bottom, north, south, east, west, context,
                x + SMALL_BLOCK_MIN, y + SMALL_BLOCK_MIN, z + SMALL_BLOCK_MIN,
                x + SMALL_BLOCK_MAX, y + SMALL_BLOCK_MAX, z + SMALL_BLOCK_MAX);
    }

    public static void cuboid(TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            ObjRenderContext context, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        southFace(south, context, minX, minY, maxZ, maxX, maxY);
        eastFace(east, context, maxX, minY, minZ, maxY, maxZ);
        northFace(north, context, minX, minY, minZ, maxX, maxY);
        westFace(west, context, minX, minY, minZ, maxY, maxZ);
        topFace(top, context, minX, minZ, maxX, maxY, maxZ);
        bottomFace(bottom, context, minX, minY, minZ, maxX, maxZ);
    }

    public static void cross(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyTexturedQuadRenderer.doubleSidedSpriteQuad(sprite, context, 0.0F, 0.0F, 1.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 0.0D));
        LegacyTexturedQuadRenderer.doubleSidedSpriteQuad(sprite, context, 1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, maxZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, maxZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, minZ, 16.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, minZ, 16.0D, 0.0D));
    }

    public static void wallQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyTexturedQuadRenderer.doubleSidedSpriteQuad(sprite, context, 0.0F, 1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 0.0D));
    }

    private static void southFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double z, double maxX, double maxY) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 0.0F, 1.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, z, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, z, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, z, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, z, 16.0D, 16.0D));
    }

    private static void northFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double z, double maxX, double maxY) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 0.0F, -1.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, z, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, z, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, z, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, z, 16.0D, 16.0D));
    }

    private static void eastFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double x, double minY, double minZ, double maxY, double maxZ) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, maxZ, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, minZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, minZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, maxZ, 16.0D, 16.0D));
    }

    private static void westFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double x, double minY, double minZ, double maxY, double maxZ) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, -1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, minZ, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, maxZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, maxZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, minZ, 16.0D, 16.0D));
    }

    private static void topFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minZ, double maxX, double y, double maxZ) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, maxZ, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, maxZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, minZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, minZ, 16.0D, 16.0D));
    }

    private static void bottomFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double y, double minZ, double maxX, double maxZ) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, -1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, maxZ, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, maxZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, minZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, minZ, 16.0D, 16.0D));
    }

    private LegacyAtlasCuboidRenderer() {
    }
}

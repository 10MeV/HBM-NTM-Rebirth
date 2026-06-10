package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.List;

public final class LegacyAtlasCuboidRenderer {
    public static final double SMALL_BLOCK_PIXEL = 1.0D / 16.0D;
    public static final double SMALL_BLOCK_MIN = 11.0D * SMALL_BLOCK_PIXEL / 2.0D;
    public static final double SMALL_BLOCK_MAX = 1.0D - SMALL_BLOCK_MIN;
    public static final int SMALL_BLOCK_GHOST_ALPHA = 191;
    public static final SmallBlockStatePlan SMALL_BLOCK_GHOST_STATE =
            new SmallBlockStatePlan(true, true, false, false, false, SMALL_BLOCK_GHOST_ALPHA);

    public static void smallBlock(TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            ObjRenderContext context, double x, double y, double z) {
        CuboidBounds bounds = smallBlockBounds(x, y, z);
        cuboid(top, bottom, north, south, east, west, context, bounds);
    }

    public static void smallBlockGhost(TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            ObjRenderContext context, double x, double y, double z) {
        smallBlock(top, bottom, north, south, east, west,
                smallBlockGhostContext(context), x, y, z);
    }

    public static ObjRenderContext smallBlockGhostContext(ObjRenderContext context) {
        return context.withTranslucencyNoDepthWrite().withAlpha(SMALL_BLOCK_GHOST_ALPHA);
    }

    public static CuboidBounds smallBlockBounds(double x, double y, double z) {
        return new CuboidBounds(
                x + SMALL_BLOCK_MIN, y + SMALL_BLOCK_MIN, z + SMALL_BLOCK_MIN,
                x + SMALL_BLOCK_MAX, y + SMALL_BLOCK_MAX, z + SMALL_BLOCK_MAX);
    }

    public static SmallBlockPlan smallBlockPlan(double x, double y, double z) {
        return smallBlockPlan(smallBlockBounds(x, y, z));
    }

    public static SmallBlockPlan smallBlockPlan(CuboidBounds bounds) {
        return new SmallBlockPlan(bounds, List.of(
                smallFace(Direction.SOUTH, "south", 0.0F, 1.0F, 0.0F,
                        vertex(bounds.minX(), bounds.minY(), bounds.maxZ(), 1.0D, 0.0D),
                        vertex(bounds.maxX(), bounds.minY(), bounds.maxZ(), 0.0D, 0.0D),
                        vertex(bounds.maxX(), bounds.maxY(), bounds.maxZ(), 0.0D, 1.0D),
                        vertex(bounds.minX(), bounds.maxY(), bounds.maxZ(), 1.0D, 1.0D)),
                smallFace(Direction.EAST, "east", 0.0F, 1.0F, 0.0F,
                        vertex(bounds.minX(), bounds.minY(), bounds.maxZ(), 1.0D, 0.0D),
                        vertex(bounds.minX(), bounds.minY(), bounds.minZ(), 0.0D, 0.0D),
                        vertex(bounds.minX(), bounds.maxY(), bounds.minZ(), 0.0D, 1.0D),
                        vertex(bounds.minX(), bounds.maxY(), bounds.maxZ(), 1.0D, 1.0D)),
                smallFace(Direction.NORTH, "north", 0.0F, 1.0F, 0.0F,
                        vertex(bounds.maxX(), bounds.minY(), bounds.maxZ(), 1.0D, 0.0D),
                        vertex(bounds.minX(), bounds.minY(), bounds.maxZ(), 0.0D, 0.0D),
                        vertex(bounds.minX(), bounds.maxY(), bounds.maxZ(), 0.0D, 1.0D),
                        vertex(bounds.maxX(), bounds.maxY(), bounds.maxZ(), 1.0D, 1.0D)),
                smallFace(Direction.WEST, "west", 0.0F, 1.0F, 0.0F,
                        vertex(bounds.maxX(), bounds.minY(), bounds.minZ(), 1.0D, 0.0D),
                        vertex(bounds.maxX(), bounds.minY(), bounds.maxZ(), 0.0D, 0.0D),
                        vertex(bounds.maxX(), bounds.maxY(), bounds.maxZ(), 0.0D, 1.0D),
                        vertex(bounds.maxX(), bounds.maxY(), bounds.minZ(), 1.0D, 1.0D)),
                smallFace(Direction.UP, "top", 0.0F, 1.0F, 0.0F,
                        vertex(bounds.minX(), bounds.minY(), bounds.maxZ(), 1.0D, 0.0D),
                        vertex(bounds.maxX(), bounds.minY(), bounds.maxZ(), 0.0D, 0.0D),
                        vertex(bounds.maxX(), bounds.minY(), bounds.minZ(), 0.0D, 1.0D),
                        vertex(bounds.minX(), bounds.minY(), bounds.minZ(), 1.0D, 1.0D)),
                smallFace(Direction.DOWN, "bottom", 0.0F, 1.0F, 0.0F,
                        vertex(bounds.maxX(), bounds.maxY(), bounds.maxZ(), 1.0D, 0.0D),
                        vertex(bounds.minX(), bounds.maxY(), bounds.maxZ(), 0.0D, 0.0D),
                        vertex(bounds.minX(), bounds.maxY(), bounds.minZ(), 0.0D, 1.0D),
                        vertex(bounds.maxX(), bounds.maxY(), bounds.minZ(), 1.0D, 1.0D))));
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

    public static void cuboid(TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            ObjRenderContext context, CuboidBounds bounds) {
        if (bounds == null) {
            return;
        }
        cuboid(top, bottom, north, south, east, west, context,
                bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
    }

    public static void cuboid(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        cuboid(sprite, sprite, sprite, sprite, sprite, sprite, context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static CuboidPlan cuboidPlan(CuboidBounds bounds) {
        return cuboidPlan(bounds, false);
    }

    public static void croppedCuboid(TextureAtlasSprite top, TextureAtlasSprite bottom,
            TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west,
            ObjRenderContext context, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        croppedSouthFace(south, context, minX, minY, maxZ, maxX, maxY);
        croppedEastFace(east, context, maxX, minY, minZ, maxY, maxZ);
        croppedNorthFace(north, context, minX, minY, minZ, maxX, maxY);
        croppedWestFace(west, context, minX, minY, minZ, maxY, maxZ);
        croppedTopFace(top, context, minX, minZ, maxX, maxY, maxZ);
        croppedBottomFace(bottom, context, minX, minY, minZ, maxX, maxZ);
    }

    public static void croppedCuboid(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        croppedCuboid(sprite, sprite, sprite, sprite, sprite, sprite, context, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static void croppedCuboid(TextureAtlasSprite sprite, ObjRenderContext context, CuboidBounds bounds) {
        if (bounds == null) {
            return;
        }
        croppedCuboid(sprite, context, bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
    }

    public static CuboidPlan croppedCuboidPlan(CuboidBounds bounds) {
        return cuboidPlan(bounds, true);
    }

    public static void centeredCube(TextureAtlasSprite sprite, ObjRenderContext context, double radius) {
        croppedCuboid(sprite, context, centeredCubeBounds(radius));
    }

    public static CuboidBounds centeredCubeBounds(double radius) {
        return new CuboidBounds(
                0.5D - radius, 0.5D - radius, 0.5D - radius,
                0.5D + radius, 0.5D + radius, 0.5D + radius);
    }

    public static CuboidPlan centeredCubePlan(double radius) {
        return croppedCuboidPlan(centeredCubeBounds(radius));
    }

    public static void directionalSlab(TextureAtlasSprite sprite, ObjRenderContext context,
            Direction direction, double thickness) {
        croppedCuboid(sprite, context, directionalSlabBounds(direction, thickness));
    }

    public static CuboidBounds directionalSlabBounds(Direction direction, double thickness) {
        double minX = 0.0D;
        double minY = 0.0D;
        double minZ = 0.0D;
        double maxX = 1.0D;
        double maxY = 1.0D;
        double maxZ = 1.0D;

        switch (direction) {
            case DOWN -> maxY = thickness;
            case UP -> minY = 1.0D - thickness;
            case NORTH -> maxZ = thickness;
            case SOUTH -> minZ = 1.0D - thickness;
            case WEST -> maxX = thickness;
            case EAST -> minX = 1.0D - thickness;
        }

        return new CuboidBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static CuboidPlan directionalSlabPlan(Direction direction, double thickness) {
        return croppedCuboidPlan(directionalSlabBounds(direction, thickness));
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

    public static List<PlaneQuadPlan> crossPlan(double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ) {
        return List.of(
                new PlaneQuadPlan("cross_z", true, 0.0F, 0.0F, 1.0F, List.of(
                        cuboidVertex(minX, maxY, minZ, 0.0D, 0.0D),
                        cuboidVertex(minX, minY, minZ, 0.0D, 16.0D),
                        cuboidVertex(maxX, minY, maxZ, 16.0D, 16.0D),
                        cuboidVertex(maxX, maxY, maxZ, 16.0D, 0.0D))),
                new PlaneQuadPlan("cross_x", true, 1.0F, 0.0F, 0.0F, List.of(
                        cuboidVertex(minX, maxY, maxZ, 0.0D, 0.0D),
                        cuboidVertex(minX, minY, maxZ, 0.0D, 16.0D),
                        cuboidVertex(maxX, minY, minZ, 16.0D, 16.0D),
                        cuboidVertex(maxX, maxY, minZ, 16.0D, 0.0D))));
    }

    public static void wallQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        wallQuad(sprite, context, 0.0F, 1.0F, 0.0F, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static void wallQuad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        LegacyTexturedQuadRenderer.doubleSidedSpriteQuad(sprite, context, normalX, normalY, normalZ,
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, minZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, minZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, maxZ, 16.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, maxZ, 16.0D, 0.0D));
    }

    public static PlaneQuadPlan wallQuadPlan(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return wallQuadPlan(0.0F, 1.0F, 0.0F, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static PlaneQuadPlan wallQuadPlan(float normalX, float normalY, float normalZ,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new PlaneQuadPlan("wall", true, normalX, normalY, normalZ, List.of(
                cuboidVertex(minX, maxY, minZ, 0.0D, 0.0D),
                cuboidVertex(minX, minY, minZ, 0.0D, 16.0D),
                cuboidVertex(maxX, minY, maxZ, 16.0D, 16.0D),
                cuboidVertex(maxX, maxY, maxZ, 16.0D, 0.0D)));
    }

    private static CuboidPlan cuboidPlan(CuboidBounds bounds, boolean cropped) {
        return new CuboidPlan(bounds, cropped, List.of(
                cuboidFace(Direction.SOUTH, "south", cropped, 0.0F, 0.0F, 1.0F,
                        cuboidVertex(bounds.maxX(), bounds.maxY(), bounds.maxZ(), cropped ? forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D) : 16.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.maxY(), bounds.maxZ(), cropped ? forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D) : 0.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.minY(), bounds.maxZ(), cropped ? forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D) : 0.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D),
                        cuboidVertex(bounds.maxX(), bounds.minY(), bounds.maxZ(), cropped ? forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D) : 16.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D)),
                cuboidFace(Direction.EAST, "east", cropped, 1.0F, 0.0F, 0.0F,
                        cuboidVertex(bounds.maxX(), bounds.maxY(), bounds.maxZ(), cropped ? reversePixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 16.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.maxX(), bounds.maxY(), bounds.minZ(), cropped ? reversePixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 0.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.maxX(), bounds.minY(), bounds.minZ(), cropped ? reversePixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 0.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D),
                        cuboidVertex(bounds.maxX(), bounds.minY(), bounds.maxZ(), cropped ? reversePixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 16.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D)),
                cuboidFace(Direction.NORTH, "north", cropped, 0.0F, 0.0F, -1.0F,
                        cuboidVertex(bounds.maxX(), bounds.maxY(), bounds.minZ(), cropped ? reversePixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 0.0D) : 16.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.maxY(), bounds.minZ(), cropped ? reversePixel(bounds.minX(), bounds.minX(), bounds.maxX(), 16.0D) : 0.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.minY(), bounds.minZ(), cropped ? reversePixel(bounds.minX(), bounds.minX(), bounds.maxX(), 16.0D) : 0.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D),
                        cuboidVertex(bounds.maxX(), bounds.minY(), bounds.minZ(), cropped ? reversePixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 0.0D) : 16.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D)),
                cuboidFace(Direction.WEST, "west", cropped, -1.0F, 0.0F, 0.0F,
                        cuboidVertex(bounds.minX(), bounds.maxY(), bounds.minZ(), cropped ? forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 16.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.maxY(), bounds.maxZ(), cropped ? forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 0.0D, cropped ? reversePixel(bounds.maxY(), bounds.minY(), bounds.maxY(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.minY(), bounds.maxZ(), cropped ? forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 0.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D),
                        cuboidVertex(bounds.minX(), bounds.minY(), bounds.minZ(), cropped ? forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 16.0D, cropped ? reversePixel(bounds.minY(), bounds.minY(), bounds.maxY(), 16.0D) : 16.0D)),
                cuboidFace(Direction.UP, "top", cropped, 0.0F, 1.0F, 0.0F,
                        cuboidVertex(bounds.maxX(), bounds.maxY(), bounds.minZ(), cropped ? forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D) : 16.0D, cropped ? forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.maxY(), bounds.minZ(), cropped ? forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D) : 0.0D, cropped ? forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.maxY(), bounds.maxZ(), cropped ? forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D) : 0.0D, cropped ? forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 16.0D),
                        cuboidVertex(bounds.maxX(), bounds.maxY(), bounds.maxZ(), cropped ? forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D) : 16.0D, cropped ? forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 16.0D)),
                cuboidFace(Direction.DOWN, "bottom", cropped, 0.0F, -1.0F, 0.0F,
                        cuboidVertex(bounds.maxX(), bounds.minY(), bounds.minZ(), cropped ? forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D) : 16.0D, cropped ? forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.minY(), bounds.minZ(), cropped ? forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D) : 0.0D, cropped ? forwardPixel(bounds.minZ(), bounds.minZ(), bounds.maxZ(), 0.0D) : 0.0D),
                        cuboidVertex(bounds.minX(), bounds.minY(), bounds.maxZ(), cropped ? forwardPixel(bounds.minX(), bounds.minX(), bounds.maxX(), 0.0D) : 0.0D, cropped ? forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 16.0D),
                        cuboidVertex(bounds.maxX(), bounds.minY(), bounds.maxZ(), cropped ? forwardPixel(bounds.maxX(), bounds.minX(), bounds.maxX(), 16.0D) : 16.0D, cropped ? forwardPixel(bounds.maxZ(), bounds.minZ(), bounds.maxZ(), 16.0D) : 16.0D))));
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
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, minZ, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, minZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, maxZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, maxZ, 16.0D, 16.0D));
    }

    private static void bottomFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double y, double minZ, double maxX, double maxZ) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, -1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, minZ, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, minZ, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, maxZ, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, maxZ, 16.0D, 16.0D));
    }

    private static void croppedSouthFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double z, double maxX, double maxY) {
        double u0 = forwardPixel(maxX, minX, maxX, 16.0D);
        double u1 = forwardPixel(minX, minX, maxX, 0.0D);
        double v0 = reversePixel(maxY, minY, maxY, 0.0D);
        double v1 = reversePixel(minY, minY, maxY, 16.0D);
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 0.0F, 1.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, z, u0, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, z, u1, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, z, u1, v1),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, z, u0, v1));
    }

    private static void croppedNorthFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minY, double z, double maxX, double maxY) {
        double u0 = reversePixel(maxX, minX, maxX, 0.0D);
        double u1 = reversePixel(minX, minX, maxX, 16.0D);
        double v0 = reversePixel(maxY, minY, maxY, 0.0D);
        double v1 = reversePixel(minY, minY, maxY, 16.0D);
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 0.0F, -1.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, maxY, z, u0, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, maxY, z, u1, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, minY, z, u1, v1),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, minY, z, u0, v1));
    }

    private static void croppedEastFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double x, double minY, double minZ, double maxY, double maxZ) {
        double u0 = reversePixel(maxZ, minZ, maxZ, 0.0D);
        double u1 = reversePixel(minZ, minZ, maxZ, 16.0D);
        double v0 = reversePixel(maxY, minY, maxY, 0.0D);
        double v1 = reversePixel(minY, minY, maxY, 16.0D);
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, maxZ, u0, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, minZ, u1, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, minZ, u1, v1),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, maxZ, u0, v1));
    }

    private static void croppedWestFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double x, double minY, double minZ, double maxY, double maxZ) {
        double u0 = forwardPixel(maxZ, minZ, maxZ, 16.0D);
        double u1 = forwardPixel(minZ, minZ, maxZ, 0.0D);
        double v0 = reversePixel(maxY, minY, maxY, 0.0D);
        double v1 = reversePixel(minY, minY, maxY, 16.0D);
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, -1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, minZ, u1, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, maxY, maxZ, u0, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, maxZ, u0, v1),
                LegacyTexturedQuadRenderer.spritePixelVertex(x, minY, minZ, u1, v1));
    }

    private static void croppedTopFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double minZ, double maxX, double y, double maxZ) {
        double u0 = forwardPixel(maxX, minX, maxX, 16.0D);
        double u1 = forwardPixel(minX, minX, maxX, 0.0D);
        double v0 = forwardPixel(minZ, minZ, maxZ, 0.0D);
        double v1 = forwardPixel(maxZ, minZ, maxZ, 16.0D);
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, 1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, minZ, u0, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, minZ, u1, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, maxZ, u1, v1),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, maxZ, u0, v1));
    }

    private static void croppedBottomFace(TextureAtlasSprite sprite, ObjRenderContext context,
            double minX, double y, double minZ, double maxX, double maxZ) {
        double u0 = forwardPixel(maxX, minX, maxX, 16.0D);
        double u1 = forwardPixel(minX, minX, maxX, 0.0D);
        double v0 = forwardPixel(minZ, minZ, maxZ, 0.0D);
        double v1 = forwardPixel(maxZ, minZ, maxZ, 16.0D);
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, 0.0F, -1.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, minZ, u0, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, minZ, u1, v0),
                LegacyTexturedQuadRenderer.spritePixelVertex(minX, y, maxZ, u1, v1),
                LegacyTexturedQuadRenderer.spritePixelVertex(maxX, y, maxZ, u0, v1));
    }

    private static double forwardPixel(double value, double min, double max, double fallback) {
        if (min < 0.0D || max > 1.0D) {
            return fallback;
        }
        return value * 16.0D;
    }

    private static double reversePixel(double value, double min, double max, double fallback) {
        if (min < 0.0D || max > 1.0D) {
            return fallback;
        }
        return (1.0D - value) * 16.0D;
    }

    public record CuboidBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    }

    public record CuboidPlan(CuboidBounds bounds, boolean cropped, List<CuboidFacePlan> faces) {
    }

    public record CuboidFacePlan(Direction direction, String legacyIconRole, boolean cropped,
                                 float normalX, float normalY, float normalZ, List<CuboidVertex> vertices) {
    }

    public record PlaneQuadPlan(String legacyRole, boolean doubleSided,
                                float normalX, float normalY, float normalZ, List<CuboidVertex> vertices) {
    }

    public record CuboidVertex(double x, double y, double z, double u, double v) {
    }

    public record SmallBlockPlan(CuboidBounds bounds, List<SmallBlockFacePlan> faces) {
    }

    public record SmallBlockFacePlan(Direction direction, String legacyIconRole, float normalX, float normalY,
                                     float normalZ, List<SmallBlockVertex> vertices) {
    }

    public record SmallBlockVertex(double x, double y, double z, double u, double v) {
    }

    public record SmallBlockStatePlan(
            boolean blendEnabled,
            boolean cullEnabled,
            boolean alphaTestEnabled,
            boolean depthWriteEnabled,
            boolean lightingEnabledAfterDraw,
            int alpha) {
    }

    private static SmallBlockFacePlan smallFace(Direction direction, String legacyIconRole,
            float normalX, float normalY, float normalZ, SmallBlockVertex... vertices) {
        return new SmallBlockFacePlan(direction, legacyIconRole, normalX, normalY, normalZ, List.of(vertices));
    }

    private static CuboidFacePlan cuboidFace(Direction direction, String legacyIconRole, boolean cropped,
            float normalX, float normalY, float normalZ, CuboidVertex... vertices) {
        return new CuboidFacePlan(direction, legacyIconRole, cropped, normalX, normalY, normalZ, List.of(vertices));
    }

    private static CuboidVertex cuboidVertex(double x, double y, double z, double u, double v) {
        return new CuboidVertex(x, y, z, u, v);
    }

    private static SmallBlockVertex vertex(double x, double y, double z, double u, double v) {
        return new SmallBlockVertex(x, y, z, u, v);
    }

    private LegacyAtlasCuboidRenderer() {
    }
}

package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class LegacyConnectedCuboidRenderer {
    public static final double CORE_MIN = 0.34375D;
    public static final double CORE_MAX = 0.65625D;
    public static final double MIN = 0.0D;
    public static final double MAX = 1.0D;

    private static final double CORE_U0 = 0.0D;
    private static final double CORE_U1 = 5.0D;
    private static final double CORE_V0 = 0.0D;
    private static final double CORE_V1 = 5.0D;
    private static final double SIDE_U0 = 5.0D;
    private static final double SIDE_U1 = 10.0D;
    private static final double SIDE_V0 = 0.0D;
    private static final double SIDE_V1 = 5.0D;

    private static final int TOP = shade(1.0F);
    private static final int BRIGHT = shade(0.8F);
    private static final int DARK = shade(0.6F);
    private static final int BOTTOM = shade(0.5F);

    public static void cableClassic(TextureAtlasSprite sprite, ObjRenderContext context,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        if (!posY) {
            coreTop(sprite, context);
        } else {
            armPosY(sprite, context);
        }

        if (!negY) {
            coreBottom(sprite, context);
        } else {
            armNegY(sprite, context);
        }

        if (!posX) {
            coreEast(sprite, context);
        } else {
            armPosX(sprite, context);
        }

        if (!negX) {
            coreWest(sprite, context);
        } else {
            armNegX(sprite, context);
        }

        if (!posZ) {
            coreSouth(sprite, context);
        } else {
            armPosZ(sprite, context);
        }

        if (!negZ) {
            coreNorth(sprite, context);
        } else {
            armNegZ(sprite, context);
        }
    }

    private static void coreTop(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 1.0F, 0.0F,
                v(CORE_MAX, CORE_MAX, CORE_MIN, CORE_U1, CORE_V0, TOP),
                v(CORE_MIN, CORE_MAX, CORE_MIN, CORE_U0, CORE_V0, TOP),
                v(CORE_MIN, CORE_MAX, CORE_MAX, CORE_U0, CORE_V1, TOP),
                v(CORE_MAX, CORE_MAX, CORE_MAX, CORE_U1, CORE_V1, TOP));
    }

    private static void coreBottom(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, -1.0F, 0.0F,
                v(CORE_MIN, CORE_MIN, CORE_MIN, CORE_U0, CORE_V0, BOTTOM),
                v(CORE_MAX, CORE_MIN, CORE_MIN, CORE_U1, CORE_V0, BOTTOM),
                v(CORE_MAX, CORE_MIN, CORE_MAX, CORE_U1, CORE_V1, BOTTOM),
                v(CORE_MIN, CORE_MIN, CORE_MAX, CORE_U0, CORE_V1, BOTTOM));
    }

    private static void coreEast(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 1.0F, 0.0F, 0.0F,
                v(CORE_MAX, CORE_MAX, CORE_MIN, CORE_U1, CORE_V0, DARK),
                v(CORE_MAX, CORE_MAX, CORE_MAX, CORE_U0, CORE_V0, DARK),
                v(CORE_MAX, CORE_MIN, CORE_MAX, CORE_U0, CORE_V1, DARK),
                v(CORE_MAX, CORE_MIN, CORE_MIN, CORE_U1, CORE_V1, DARK));
    }

    private static void coreWest(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, -1.0F, 0.0F, 0.0F,
                v(CORE_MIN, CORE_MAX, CORE_MAX, CORE_U1, CORE_V0, DARK),
                v(CORE_MIN, CORE_MAX, CORE_MIN, CORE_U0, CORE_V0, DARK),
                v(CORE_MIN, CORE_MIN, CORE_MIN, CORE_U0, CORE_V1, DARK),
                v(CORE_MIN, CORE_MIN, CORE_MAX, CORE_U1, CORE_V1, DARK));
    }

    private static void coreSouth(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 0.0F, 1.0F,
                v(CORE_MAX, CORE_MAX, CORE_MAX, CORE_U1, CORE_V0, BRIGHT),
                v(CORE_MIN, CORE_MAX, CORE_MAX, CORE_U0, CORE_V0, BRIGHT),
                v(CORE_MIN, CORE_MIN, CORE_MAX, CORE_U0, CORE_V1, BRIGHT),
                v(CORE_MAX, CORE_MIN, CORE_MAX, CORE_U1, CORE_V1, BRIGHT));
    }

    private static void coreNorth(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 0.0F, -1.0F,
                v(CORE_MIN, CORE_MAX, CORE_MIN, CORE_U1, CORE_V0, BRIGHT),
                v(CORE_MAX, CORE_MAX, CORE_MIN, CORE_U0, CORE_V0, BRIGHT),
                v(CORE_MAX, CORE_MIN, CORE_MIN, CORE_U0, CORE_V1, BRIGHT),
                v(CORE_MIN, CORE_MIN, CORE_MIN, CORE_U1, CORE_V1, BRIGHT));
    }

    private static void armPosY(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 0.0F, -1.0F,
                v(CORE_MAX, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MIN, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V1, BRIGHT),
                v(CORE_MIN, MAX, CORE_MIN, SIDE_U1, SIDE_V1, BRIGHT),
                v(CORE_MAX, MAX, CORE_MIN, SIDE_U1, SIDE_V0, BRIGHT));
        quad(sprite, context, 1.0F, 0.0F, 0.0F,
                v(CORE_MAX, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MAX, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MAX, MAX, CORE_MIN, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MAX, MAX, CORE_MAX, SIDE_U1, SIDE_V0, DARK));
        quad(sprite, context, 0.0F, 0.0F, 1.0F,
                v(CORE_MIN, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MAX, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V1, BRIGHT),
                v(CORE_MAX, MAX, CORE_MAX, SIDE_U1, SIDE_V1, BRIGHT),
                v(CORE_MIN, MAX, CORE_MAX, SIDE_U1, SIDE_V0, BRIGHT));
        quad(sprite, context, -1.0F, 0.0F, 0.0F,
                v(CORE_MIN, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MIN, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MIN, MAX, CORE_MAX, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MIN, MAX, CORE_MIN, SIDE_U1, SIDE_V0, DARK));
    }

    private static void armNegY(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 0.0F, -1.0F,
                v(CORE_MIN, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MAX, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V1, BRIGHT),
                v(CORE_MAX, MIN, CORE_MIN, SIDE_U1, SIDE_V1, BRIGHT),
                v(CORE_MIN, MIN, CORE_MIN, SIDE_U1, SIDE_V0, BRIGHT));
        quad(sprite, context, 1.0F, 0.0F, 0.0F,
                v(CORE_MAX, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MAX, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MAX, MIN, CORE_MAX, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MAX, MIN, CORE_MIN, SIDE_U1, SIDE_V0, DARK));
        quad(sprite, context, 0.0F, 0.0F, 1.0F,
                v(CORE_MAX, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MIN, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V1, BRIGHT),
                v(CORE_MIN, MIN, CORE_MAX, SIDE_U1, SIDE_V1, BRIGHT),
                v(CORE_MAX, MIN, CORE_MAX, SIDE_U1, SIDE_V0, BRIGHT));
        quad(sprite, context, -1.0F, 0.0F, 0.0F,
                v(CORE_MIN, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MIN, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MIN, MIN, CORE_MIN, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MIN, MIN, CORE_MAX, SIDE_U1, SIDE_V0, DARK));
    }

    private static void armPosX(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 1.0F, 0.0F,
                v(CORE_MAX, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V0, TOP),
                v(CORE_MAX, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V1, TOP),
                v(MAX, CORE_MAX, CORE_MAX, SIDE_U1, SIDE_V1, TOP),
                v(MAX, CORE_MAX, CORE_MIN, SIDE_U1, SIDE_V0, TOP));
        quad(sprite, context, 0.0F, 0.0F, -1.0F,
                v(CORE_MAX, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MAX, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V1, BRIGHT),
                v(MAX, CORE_MAX, CORE_MIN, SIDE_U1, SIDE_V1, BRIGHT),
                v(MAX, CORE_MIN, CORE_MIN, SIDE_U1, SIDE_V0, BRIGHT));
        quad(sprite, context, 0.0F, -1.0F, 0.0F,
                v(CORE_MAX, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V0, BOTTOM),
                v(CORE_MAX, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V1, BOTTOM),
                v(MAX, CORE_MIN, CORE_MIN, SIDE_U1, SIDE_V1, BOTTOM),
                v(MAX, CORE_MIN, CORE_MAX, SIDE_U1, SIDE_V0, BOTTOM));
        quad(sprite, context, 0.0F, 0.0F, 1.0F,
                v(CORE_MAX, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MAX, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V1, BRIGHT),
                v(MAX, CORE_MIN, CORE_MAX, SIDE_U1, SIDE_V1, BRIGHT),
                v(MAX, CORE_MAX, CORE_MAX, SIDE_U1, SIDE_V0, BRIGHT));
    }

    private static void armNegX(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 1.0F, 0.0F,
                v(CORE_MIN, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V0, TOP),
                v(CORE_MIN, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V1, TOP),
                v(MIN, CORE_MAX, CORE_MIN, SIDE_U1, SIDE_V1, TOP),
                v(MIN, CORE_MAX, CORE_MAX, SIDE_U1, SIDE_V0, TOP));
        quad(sprite, context, 0.0F, 0.0F, -1.0F,
                v(CORE_MIN, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MIN, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V1, BRIGHT),
                v(MIN, CORE_MIN, CORE_MIN, SIDE_U1, SIDE_V1, BRIGHT),
                v(MIN, CORE_MAX, CORE_MIN, SIDE_U1, SIDE_V0, BRIGHT));
        quad(sprite, context, 0.0F, -1.0F, 0.0F,
                v(CORE_MIN, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V0, BOTTOM),
                v(CORE_MIN, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V1, BOTTOM),
                v(MIN, CORE_MIN, CORE_MAX, SIDE_U1, SIDE_V1, BOTTOM),
                v(MIN, CORE_MIN, CORE_MIN, SIDE_U1, SIDE_V0, BOTTOM));
        quad(sprite, context, 0.0F, 0.0F, 1.0F,
                v(CORE_MIN, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V0, BRIGHT),
                v(CORE_MIN, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V1, BRIGHT),
                v(MIN, CORE_MAX, CORE_MAX, SIDE_U1, SIDE_V1, BRIGHT),
                v(MIN, CORE_MIN, CORE_MAX, SIDE_U1, SIDE_V0, BRIGHT));
    }

    private static void armPosZ(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 1.0F, 0.0F,
                v(CORE_MAX, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V0, TOP),
                v(CORE_MIN, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V1, TOP),
                v(CORE_MIN, CORE_MAX, MAX, SIDE_U1, SIDE_V1, TOP),
                v(CORE_MAX, CORE_MAX, MAX, SIDE_U1, SIDE_V0, TOP));
        quad(sprite, context, -1.0F, 0.0F, 0.0F,
                v(CORE_MIN, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MIN, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MIN, CORE_MIN, MAX, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MIN, CORE_MAX, MAX, SIDE_U1, SIDE_V0, DARK));
        quad(sprite, context, 0.0F, -1.0F, 0.0F,
                v(CORE_MIN, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V0, BOTTOM),
                v(CORE_MAX, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V1, BOTTOM),
                v(CORE_MAX, CORE_MIN, MAX, SIDE_U1, SIDE_V1, BOTTOM),
                v(CORE_MIN, CORE_MIN, MAX, SIDE_U1, SIDE_V0, BOTTOM));
        quad(sprite, context, 1.0F, 0.0F, 0.0F,
                v(CORE_MAX, CORE_MIN, CORE_MAX, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MAX, CORE_MAX, CORE_MAX, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MAX, CORE_MAX, MAX, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MAX, CORE_MIN, MAX, SIDE_U1, SIDE_V0, DARK));
    }

    private static void armNegZ(TextureAtlasSprite sprite, ObjRenderContext context) {
        quad(sprite, context, 0.0F, 1.0F, 0.0F,
                v(CORE_MIN, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V0, TOP),
                v(CORE_MAX, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V1, TOP),
                v(CORE_MAX, CORE_MAX, MIN, SIDE_U1, SIDE_V1, TOP),
                v(CORE_MIN, CORE_MAX, MIN, SIDE_U1, SIDE_V0, TOP));
        quad(sprite, context, -1.0F, 0.0F, 0.0F,
                v(CORE_MIN, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MIN, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MIN, CORE_MAX, MIN, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MIN, CORE_MIN, MIN, SIDE_U1, SIDE_V0, DARK));
        quad(sprite, context, 0.0F, -1.0F, 0.0F,
                v(CORE_MAX, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V0, BOTTOM),
                v(CORE_MIN, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V1, BOTTOM),
                v(CORE_MIN, CORE_MIN, MIN, SIDE_U1, SIDE_V1, BOTTOM),
                v(CORE_MAX, CORE_MIN, MIN, SIDE_U1, SIDE_V0, BOTTOM));
        quad(sprite, context, 1.0F, 0.0F, 0.0F,
                v(CORE_MAX, CORE_MAX, CORE_MIN, SIDE_U0, SIDE_V0, DARK),
                v(CORE_MAX, CORE_MIN, CORE_MIN, SIDE_U0, SIDE_V1, DARK),
                v(CORE_MAX, CORE_MIN, MIN, SIDE_U1, SIDE_V1, DARK),
                v(CORE_MAX, CORE_MAX, MIN, SIDE_U1, SIDE_V0, DARK));
    }

    private static LegacyTexturedQuadRenderer.Vertex v(double x, double y, double z, double u, double v, int color) {
        return LegacyTexturedQuadRenderer.spritePixelVertex(x, y, z, u, v, color, 255);
    }

    private static void quad(TextureAtlasSprite sprite, ObjRenderContext context,
            float normalX, float normalY, float normalZ,
            LegacyTexturedQuadRenderer.Vertex v0, LegacyTexturedQuadRenderer.Vertex v1,
            LegacyTexturedQuadRenderer.Vertex v2, LegacyTexturedQuadRenderer.Vertex v3) {
        LegacyTexturedQuadRenderer.spriteQuad(sprite, context, normalX, normalY, normalZ, v0, v1, v2, v3);
    }

    private static int shade(float value) {
        int channel = Math.max(0, Math.min(255, Math.round(value * 255.0F)));
        return channel << 16 | channel << 8 | channel;
    }

    private LegacyConnectedCuboidRenderer() {
    }
}

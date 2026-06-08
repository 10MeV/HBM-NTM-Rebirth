package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class LegacyDangerDiamondRenderer {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/misc/danger_diamond.png");

    private static final double TEXTURE_SIZE = 256.0D;
    private static final double DIAMOND_SIZE = 0.5D;
    private static final double NUMBER_WIDTH = 10.0D / 139.0D;
    private static final double NUMBER_HEIGHT = 14.0D / 139.0D;
    private static final double SYMBOL_SIZE = 59.0D / 2.0D / 139.0D;
    private static final double NUMBER_OFFSET = 33.0D / 139.0D;
    private static final double LAYER_OFFSET_X = 0.01D;

    public static void render(ObjRenderContext context, int poison, int flammability, int reactivity, Symbol symbol) {
        quad(context, 144, 45, 5, 184, 0.0D, 0.0D, 0.0D, DIAMOND_SIZE, DIAMOND_SIZE);
        number(context, poison, 0.0D, NUMBER_OFFSET);
        number(context, flammability, NUMBER_OFFSET, 0.0D);
        number(context, reactivity, 0.0D, -NUMBER_OFFSET);
        symbol(context, symbol, -NUMBER_OFFSET, 0.0D);
    }

    public static void number(ObjRenderContext context, int value, double yOffset, double zOffset) {
        if (value < 0 || value >= 6) {
            return;
        }
        int x = value == 0 ? 125 : 5 + (value - 1) * 24;
        quad(context, x + 20, 5, x, 33, LAYER_OFFSET_X, yOffset, zOffset, NUMBER_WIDTH, NUMBER_HEIGHT);
    }

    public static void symbol(ObjRenderContext context, Symbol symbol, double yOffset, double zOffset) {
        if (symbol == null || symbol == Symbol.NONE) {
            return;
        }
        quad(context, symbol.x() + 59, symbol.y(), symbol.x(), symbol.y() + 59,
                LAYER_OFFSET_X, yOffset, zOffset, SYMBOL_SIZE, SYMBOL_SIZE);
    }

    public static void quad(ObjRenderContext context, int u0, int v0, int u1, int v1,
            double x, double y, double z, double width, double height) {
        LegacyTexturedQuadRenderer.pixelQuad(TEXTURE, context, 1.0F, 0.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE,
                x, y + height, z - width, u0, v0,
                x, y + height, z + width, u1, v0,
                x, y - height, z + width, u1, v1,
                x, y - height, z - width, u0, v1,
                0xFFFFFF, 255);
    }

    public enum Symbol {
        NONE(0, 0),
        RADIATION(195, 2),
        NOWATER(195, 63),
        ACID(195, 124),
        ASPHYXIANT(195, 185),
        CRYOGENIC(134, 185),
        ANTIMATTER(73, 185),
        OXIDIZER(12, 185);

        private final int x;
        private final int y;

        Symbol(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }
    }

    private LegacyDangerDiamondRenderer() {
    }
}

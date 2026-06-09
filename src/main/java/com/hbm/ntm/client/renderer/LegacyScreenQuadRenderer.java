package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class LegacyScreenQuadRenderer {
    public static final ResourceLocation VANILLA_ITEM_GLINT =
            new ResourceLocation("minecraft", "textures/misc/enchanted_item_glint.png");

    public static void blit(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height) {
        graphics.blit(texture, x, y, u, v, width, height);
    }

    public static void blit(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
    }

    public static void blitColored(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight,
            float red, float green, float blue, float alpha) {
        graphics.setColor(red, green, blue, alpha);
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void pixelQuad(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, double u, double v, double width, double height,
            double textureWidth, double textureHeight, int color, int alpha, BlendMode blendMode) {
        if (textureWidth == 0.0D || textureHeight == 0.0D) {
            return;
        }
        unitQuad(texture, graphics, x, y, width, height,
                u / textureWidth, (v + height) / textureHeight,
                (u + width) / textureWidth, (v + height) / textureHeight,
                (u + width) / textureWidth, v / textureHeight,
                u / textureWidth, v / textureHeight,
                color, alpha, blendMode);
    }

    public static int scaled(double current, double max, int scale) {
        if (max <= 0.0D || scale <= 0) {
            return 0;
        }
        return Math.max(0, (int) Math.min(current / max * scale, scale));
    }

    public static void blitHorizontalProgress(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, int progressWidth) {
        int clampedWidth = Math.max(0, Math.min(width, progressWidth));
        if (clampedWidth <= 0 || height <= 0) {
            return;
        }
        graphics.blit(texture, x, y, u, v, clampedWidth, height);
    }

    public static int blitHorizontalScaled(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int u, int v, int width, int height, double current, double max) {
        int progress = scaled(current, max, width);
        blitHorizontalProgress(texture, graphics, x, y, u, v, width, height, progress);
        return progress;
    }

    public static void scope16x9(ResourceLocation texture, GuiGraphics graphics, int screenWidth, int screenHeight) {
        double width = screenWidth;
        double height = screenHeight;
        double smallest = Math.min(width, height);
        double divisor = smallest / (9.0D / 16.0D);
        smallest = 9.0D / 16.0D;
        double largest = Math.max(width, height) / divisor;

        double vMin = height < width ? 0.5D - smallest / 2.0D : 0.5D - largest / 2.0D;
        double vMax = height < width ? 0.5D + smallest / 2.0D : 0.5D + largest / 2.0D;
        double uMin = width < height ? 0.5D - smallest / 2.0D : 0.5D - largest / 2.0D;
        double uMax = width < height ? 0.5D + smallest / 2.0D : 0.5D + largest / 2.0D;

        unitQuad(texture, graphics, 0, 0, screenWidth, screenHeight, uMin, vMin, uMax, vMax,
                0xFFFFFF, 255, BlendMode.NORMAL);
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y) {
        itemGlint(graphics, x, y, 16, 16);
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y, int width, int height) {
        itemGlint(graphics, x, y, width, height, 0x8040CC);
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        itemGlint(graphics, x, y, width, height, color, System.currentTimeMillis());
    }

    public static void itemGlint(GuiGraphics graphics, int x, int y, int width, int height, int color, long currentMillis) {
        for (int i = 0; i < 2; i++) {
            LegacyUvAnimation.UnitQuadUv uv = LegacyUvAnimation.flatItemGlintUv(currentMillis, i, width, height);
            unitQuad(VANILLA_ITEM_GLINT, graphics, x, y, width, height,
                    uv.bottomLeftU(), uv.bottomLeftV(),
                    uv.bottomRightU(), uv.bottomRightV(),
                    uv.topRightU(), uv.topRightV(),
                    uv.topLeftU(), uv.topLeftV(),
                    color, 255, BlendMode.GLINT);
        }
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int width, int height, double uMin, double vMin, double uMax, double vMax,
            int color, int alpha, BlendMode blendMode) {
        unitQuad(texture, graphics, (double) x, (double) y, (double) width, (double) height,
                uMin, vMax, uMax, vMax, uMax, vMin, uMin, vMin, color, alpha, blendMode);
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, double width, double height, double uMin, double vMin, double uMax, double vMax,
            int color, int alpha, BlendMode blendMode) {
        unitQuad(texture, graphics, x, y, width, height,
                uMin, vMax, uMax, vMax, uMax, vMin, uMin, vMin, color, alpha, blendMode);
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            double x, double y, double width, double height,
            double bottomLeftU, double bottomLeftV,
            double bottomRightU, double bottomRightV,
            double topRightU, double topRightV,
            double topLeftU, double topLeftV,
            int color, int alpha, BlendMode blendMode) {
        if (width <= 0 || height <= 0) {
            return;
        }

        blendMode.apply();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        int clampedAlpha = clamp(alpha);
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        float z = 0.0F;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, (float) x, (float) (y + height), z).uv((float) bottomLeftU, (float) bottomLeftV)
                .color(red, green, blue, clampedAlpha).endVertex();
        buffer.vertex(matrix, (float) (x + width), (float) (y + height), z).uv((float) bottomRightU, (float) bottomRightV)
                .color(red, green, blue, clampedAlpha).endVertex();
        buffer.vertex(matrix, (float) (x + width), (float) y, z).uv((float) topRightU, (float) topRightV)
                .color(red, green, blue, clampedAlpha).endVertex();
        buffer.vertex(matrix, (float) x, (float) y, z).uv((float) topLeftU, (float) topLeftV)
                .color(red, green, blue, clampedAlpha).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        blendMode.clear();
    }

    public static void unitQuad(ResourceLocation texture, GuiGraphics graphics,
            int x, int y, int width, int height,
            double bottomLeftU, double bottomLeftV,
            double bottomRightU, double bottomRightV,
            double topRightU, double topRightV,
            double topLeftU, double topLeftV,
            int color, int alpha, BlendMode blendMode) {
        unitQuad(texture, graphics, (double) x, (double) y, (double) width, (double) height,
                bottomLeftU, bottomLeftV, bottomRightU, bottomRightV, topRightU, topRightV, topLeftU, topLeftV,
                color, alpha, blendMode);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public enum BlendMode {
        NONE {
            @Override
            void apply() {
                RenderSystem.disableBlend();
            }
        },
        NORMAL {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }
        },
        ADDITIVE {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            }
        },
        GLINT {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            }
        },
        INVERT_CROSSHAIR {
            @Override
            void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
            }
        };

        abstract void apply();

        void clear() {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private LegacyScreenQuadRenderer() {
    }
}

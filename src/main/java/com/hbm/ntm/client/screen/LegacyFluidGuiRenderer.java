package com.hbm.ntm.client.screen;

import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class LegacyFluidGuiRenderer {
    private static final int TILE_SIZE = 16;

    private LegacyFluidGuiRenderer() {
    }

    static void renderVerticalTank(GuiGraphics graphics, int left, int bottom, int width, int height,
            HbmFluidGuiHelper.TankData tank) {
        if (tank == null || tank.isEmpty()) {
            return;
        }
        int fill = tank.scaledFill(height);
        if (fill <= 0) {
            return;
        }
        renderTank(graphics, tank, left, bottom - fill, width, fill, true);
    }

    static void renderHorizontalTank(GuiGraphics graphics, int left, int bottom, int width, int height,
            HbmFluidGuiHelper.TankData tank) {
        if (tank == null || tank.isEmpty()) {
            return;
        }
        int fill = tank.scaledFill(width);
        if (fill <= 0) {
            return;
        }
        renderTank(graphics, tank, left, bottom - height, fill, height, false);
    }

    private static void renderTank(GuiGraphics graphics, HbmFluidGuiHelper.TankData tank, int x, int y,
            int width, int height, boolean bottomAligned) {
        ResourceLocation texture = tank.type().getTexture();
        int tint = tank.guiTint();
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        for (int dx = 0; dx < width; dx += TILE_SIZE) {
            int tileWidth = Math.min(TILE_SIZE, width - dx);
            for (int dy = 0; dy < height; dy += TILE_SIZE) {
                int tileHeight = Math.min(TILE_SIZE, height - dy);
                int drawY = bottomAligned ? y + height - dy - tileHeight : y + dy;
                int v = bottomAligned ? TILE_SIZE - tileHeight : 0;
                graphics.blit(texture, x + dx, drawY, 0, v, tileWidth, tileHeight, TILE_SIZE, TILE_SIZE);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}

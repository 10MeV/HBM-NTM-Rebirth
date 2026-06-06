package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.BasicMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BasicMachineScreen extends AbstractContainerScreen<BasicMachineMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_press.png");

    public BasicMachineScreen(BasicMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 202;
        inventoryLabelY = 108;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getStoredOperations() > 0) {
            graphics.blit(TEXTURE, leftPos + 27, topPos + 36, 0, 202, 14, 14);
        }
        int pressHeight = menu.getPressHeight(16);
        graphics.blit(TEXTURE, leftPos + 79, topPos + 35, 14, 202, 18, pressHeight);
        drawSmoothGauge(graphics, leftPos + 34, topPos + 25, menu.getSpeedPercent() / 100.0D);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(25, 16, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getSpeedPercent() + "%"), mouseX, mouseY);
        } else if (isHovering(25, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getStoredOperations() + " operations left"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static void drawSmoothGauge(GuiGraphics graphics, int x, int y, double progress) {
        progress = Math.max(0.0D, Math.min(1.0D, progress));
        double angle = Math.toRadians(135.0D - progress * 270.0D);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        int tipX = (int) Math.round(x - 5.0D * sin);
        int tipY = (int) Math.round(y - 5.0D * cos);
        drawNeedleLine(graphics, x, y, tipX, tipY, 0xFF7F0000);
        graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xFF300000);
    }

    private static void drawNeedleLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int error = dx - dy;

        while (true) {
            graphics.fill(x0, y0, x0 + 2, y0 + 2, color);
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = error * 2;
            if (e2 > -dy) {
                error -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                error += dx;
                y0 += sy;
            }
        }
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.BlastFurnaceMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BlastFurnaceScreen extends AbstractContainerScreen<BlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_blast_furnace.png");

    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        titleLabelX = 88;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int fuel = menu.getFuelPixels();
        int progress = (int) Math.round(menu.getProgressRatio() * (88.0D - fuel));
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 106 - progress - fuel,
                    176, 102 - progress - fuel, 56, progress);
        }
        if (fuel > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 106 - fuel,
                    176, 128 - fuel, 56, fuel);
        }
        if (menu.isProgressing()) {
            graphics.blit(TEXTURE, leftPos + 81, topPos + 64, 176, 0, 14, 14);
        }
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 80,
                tankRatio(menu.getAirblastTank()), 5, 2, 1, 0x800000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 26,
                tankRatio(menu.getFlueTank()), 5, 2, 1, 0x800000);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(79, 62, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal("Speed: " + menu.getSpeedPercent() + "%").getVisualOrderText()),
                    mouseX, mouseY);
        } else if (isHovering(25, 71, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getAirblastTank(),
                    menu.getAirblastTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(25, 17, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getFlueTank(),
                    menu.getFlueTooltip(hasShiftDown()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static double tankRatio(com.hbm.ntm.fluid.HbmFluidGuiHelper.TankData tank) {
        return tank.capacity() <= 0 ? 0.0D : (double) tank.fill() / (double) tank.capacity();
    }
}

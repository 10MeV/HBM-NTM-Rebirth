package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.BasicMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BasicMachineScreen extends AbstractContainerScreen<BasicMachineMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_press.png");

    public BasicMachineScreen(BasicMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 214;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getStoredOperations() > 0) {
            graphics.blit(TEXTURE, leftPos + 26, topPos + 36, 0, 214, 14, 14);
        }
        int pressHeight = menu.getPressHeight(16);
        graphics.blit(TEXTURE, leftPos + 79, topPos + 35, 15, 214, 18, pressHeight);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 25, menu.getSpeedPercent() / 100.0D,
                5.0D, 2.0D, 1.0D, 0x7F0000);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 5, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
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
}

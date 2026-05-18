package com.hbm.ntm.client.screen;

import com.hbm.ntm.menu.BasicMachineMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicMachineScreen extends AbstractContainerScreen<BasicMachineMenu> {
    public BasicMachineScreen(BasicMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF2B2B2B);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF3A3A3A);

        graphics.fill(leftPos + 25, topPos + 34, leftPos + 44, topPos + 53, 0xFF151515);
        graphics.fill(leftPos + 61, topPos + 34, leftPos + 80, topPos + 53, 0xFF151515);
        graphics.fill(leftPos + 97, topPos + 34, leftPos + 116, topPos + 53, 0xFF151515);
        graphics.fill(leftPos + 133, topPos + 34, leftPos + 152, topPos + 53, 0xFF151515);

        int progressWidth = menu.getProgressWidth(24);
        graphics.fill(leftPos + 76, topPos + 59, leftPos + 100, topPos + 63, 0xFF151515);
        graphics.fill(leftPos + 76, topPos + 59, leftPos + 76 + progressWidth, topPos + 63, 0xFFB77932);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

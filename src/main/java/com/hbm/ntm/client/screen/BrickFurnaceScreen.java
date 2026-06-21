package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.BrickFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BrickFurnaceScreen extends AbstractContainerScreen<BrickFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_furnace_brick.png");

    public BrickFurnaceScreen(BrickFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getProgressWidth(70);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 53, topPos + 36, 176, 18, progress, 5);
        }
        int burn = menu.getBurnWidth(70);
        if (burn > 0) {
            graphics.blit(TEXTURE, leftPos + 53, topPos + 45, 176, 23, burn, 5);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

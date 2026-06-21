package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.RBMKHeaterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RBMKHeaterScreen extends AbstractContainerScreen<RBMKHeaterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_rbmk_heater.png");

    public RBMKHeaterScreen(RBMKHeaterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 68, topPos + 82, 14, 58,
                menu.getFeedTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 126, topPos + 82, 14, 58,
                menu.getOutputTank());
        graphics.blit(TEXTURE, leftPos + 72, topPos + 72, 176, 0, 10, 10);
        graphics.blit(TEXTURE, leftPos + 130, topPos + 72, 186, 0, 10, 10);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(68, 24, 16, 58, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getFeedTank(),
                    menu.getFeedTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
        if (isHovering(126, 24, 16, 58, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOutputTank(),
                    menu.getOutputTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.BreedingReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BreedingReactorScreen extends AbstractContainerScreen<BreedingReactorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_breeder.png");

    public BreedingReactorScreen(BreedingReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int width = menu.getProgressWidth(70);
        if (width > 0) {
            graphics.blit(TEXTURE, leftPos + 53, topPos + 32, 176, 0, width, 20);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        String flux = Integer.toString(menu.getFlux());
        graphics.drawString(font, flux, 88 - font.width(flux) / 2, 21, 0x08FF00, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(-16, 16, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.literal("Adjacent research reactors provide neutron flux."), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

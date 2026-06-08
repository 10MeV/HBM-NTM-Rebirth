package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.LiquefactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LiquefactorScreen extends AbstractContainerScreen<LiquefactorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_liquefactor.png");

    public LiquefactorScreen(LiquefactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 134, topPos + 70 - power, 176, 52 - power, 16, power);
            graphics.blit(TEXTURE, leftPos + 138, topPos + 4, 176, 52, 9, 12);
        }
        int progress = menu.getProgressWidth(42);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 42, topPos + 17, 192, 0, progress, 35);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 71, topPos + 88,
                16, 52, menu.getTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), titleLabelX, titleLabelY, 124, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(71, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(134, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 134, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(98, 36, 18, 36, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getUsage() + " HE/t"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

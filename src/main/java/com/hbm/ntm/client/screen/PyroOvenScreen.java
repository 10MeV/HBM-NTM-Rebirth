package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.PyroOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PyroOvenScreen extends AbstractContainerScreen<PyroOvenMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_pyrooven.png");

    public PyroOvenScreen(PyroOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelX = 52;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 70 - power, 176, 64 - power, 16, power);
        }
        int progress = menu.getProgressWidth(27);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 57, topPos + 47, 176, 0, progress, 12);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8, topPos + 70, 16, 52, menu.getInputTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 116, topPos + 70, 16, 52, menu.getOutputTank());
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
        if (isHovering(8, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTank(),
                    menu.getInputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(116, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOutputTank(),
                    menu.getOutputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"),
                    mouseX, mouseY);
        } else if (isHovering(108, 76, 8, 8, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getUsage() + " HE/t"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.RotaryFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RotaryFurnaceScreen extends AbstractContainerScreen<RotaryFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_rotary_furnace.png");

    public RotaryFurnaceScreen(RotaryFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        titleLabelX = 61;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getProgressPixels();
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 63, topPos + 30, 176, 0, progress, 10);
        }
        int burn = menu.getBurnPixels();
        if (burn > 0) {
            graphics.blit(TEXTURE, leftPos + 26, topPos + 69 - burn, 176, 24 - burn, 14, burn);
        }
        int output = menu.getOutputPixels();
        if (output > 0) {
            int color = menu.getOutputColor();
            graphics.setColor(((color >> 16) & 255) / 255.0F,
                    ((color >> 8) & 255) / 255.0F,
                    (color & 255) / 255.0F, 1.0F);
            graphics.blit(TEXTURE, leftPos + 98, topPos + 70 - output, 176, 76 - output, 16, output);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        LegacyFluidGuiRenderer.renderHorizontalTank(graphics, leftPos + 8, topPos + 52, 52, 16,
                menu.getInputTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 134, topPos + 70, 16, 52,
                menu.getSteamTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 152, topPos + 70, 16, 52,
                menu.getSpentSteamTank());
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
        if (isHovering(8, 36, 52, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTank(),
                    menu.getInputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(134, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSteamTank(),
                    menu.getSteamTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSpentSteamTank(),
                    menu.getSpentSteamTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(98, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getOutputText(hasShiftDown())), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

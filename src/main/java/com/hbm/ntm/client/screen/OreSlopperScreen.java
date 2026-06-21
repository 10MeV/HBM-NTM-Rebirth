package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.OreSlopperMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OreSlopperScreen extends AbstractContainerScreen<OreSlopperMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_ore_slopper.png");

    public OreSlopperScreen(OreSlopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int progress = menu.getProgressBarHeight(35);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 52 - progress,
                    176, 34 - progress, 34, progress);
        }

        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 70 - power,
                    176, 86 - power, 16, power);
        }

        if (menu.getPower() >= menu.getConsumption()) {
            graphics.blit(TEXTURE, leftPos + 12, topPos + 4, 202, 34, 9, 12);
        }

        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 26, topPos + 70, 16, 52,
                menu.getWaterTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 116, topPos + 70, 16, 52,
                menu.getSlopTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, imageWidth / 2 - 9, titleLabelY,
                130, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(26, 18, 34, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getWaterTank(),
                    menu.waterTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(116, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSlopTank(),
                    menu.slopTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(8, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(62, 72, 36, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderUpgradeInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 62, topPos + 72, 36, 16, menu.getBlockEntity());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

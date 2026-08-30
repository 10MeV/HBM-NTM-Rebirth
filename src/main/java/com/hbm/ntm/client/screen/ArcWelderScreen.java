package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ArcWelderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ArcWelderScreen extends AbstractContainerScreen<ArcWelderMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_arc_welder.png");

    public ArcWelderScreen(ArcWelderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        // Legacy centers the title at xSize / 2 - 18, leaving room for the right-side controls.
        titleLabelX = imageWidth / 2 - 18;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 70 - power, 176, 52 - power, 16, power);
        }
        int progress = menu.getProgressWidth(33);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 72, topPos + 37, 192, 0, progress, 14);
        }
        if (menu.getPower() >= menu.getConsumption()) {
            graphics.blit(TEXTURE, leftPos + 156, topPos + 4, 176, 52, 9, 12);
        }
        LegacyFluidGuiRenderer.renderHorizontalTank(graphics, leftPos + 35, topPos + 79,
                34, 16, menu.getInputTankData());
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 78, topPos + 67, 8);
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
        if (isLegacyHovering(152, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isLegacyHovering(35, 63, 34, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTankData(),
                    menu.getInputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 78, topPos + 67, 8, 8, leftPos + 78, topPos + 67,
                    LegacyGuiElements.getUpgradeInfo(menu));
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }
}

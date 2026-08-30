package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.OilDrillMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OilDrillScreen extends AbstractContainerScreen<OilDrillMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_well.png");

    public OilDrillScreen(OilDrillMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 184;
        imageHeight = 190;
        titleLabelY = 10;
        inventoryLabelX = 12;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderEnergy(graphics);
        renderIndicator(graphics);
        if (!menu.hasFrackingTank()) {
            graphics.blit(TEXTURE, leftPos + 48, topPos + 44, 200, 0, 18, 34);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 76, topPos + 74, 16, 52, menu.getTank(0));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 112, topPos + 74, 16, 52, menu.getTank(1));
        if (menu.hasFrackingTank()) {
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 54, topPos + 77, 6, 32, menu.getTank(2));
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 160, topPos + 21, 8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String machineName = title.getString();
        graphics.drawString(font, machineName, 126 - font.width(machineName) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, 0, 76, 22, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 1, 112, 22, 16, 52);
        if (menu.hasFrackingTank()) {
            renderTankTooltip(graphics, mouseX, mouseY, 2, 54, 45, 6, 32);
        }
        if (isLegacyHovering(8, 22, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 22, 16, 34, menu.getPower(), menu.getMaxPower());
        } else if (isLegacyHovering(160, 21, 8, 8, mouseX, mouseY)) {
            LegacyGuiElements.renderUpgradeInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 160, topPos + 21, 8, 8, menu.getBlockEntity());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderEnergy(GuiGraphics graphics) {
        int height = menu.getPowerBarHeight(34);
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 56 - height, 184, 34 - height, 16, height);
        }
    }

    private void renderIndicator(GuiGraphics graphics) {
        int indicator = menu.getIndicator();
        if (indicator > 0) {
            graphics.blit(TEXTURE, leftPos + 50, topPos + 19, 184 + (indicator - 1) * 14, 34, 14, 14);
        }
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, int index, int x, int y,
            int width, int height) {
        if (!isLegacyHovering(x, y, width, height, mouseX, mouseY)) {
            return;
        }
        List<Component> tooltip = menu.getTankTooltip(index, hasShiftDown());
        LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(index), tooltip, mouseX, mouseY);
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }
}

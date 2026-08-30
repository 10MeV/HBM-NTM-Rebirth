package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ElectricFurnaceMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_electric_furnace.png");

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int powerHeight = menu.getPowerBarHeight(34);
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 52 - powerHeight, 176,
                    64 - powerHeight, 16, powerHeight);
        }
        if (menu.isActive()) {
            graphics.blit(TEXTURE, leftPos + 45, topPos + 20, 192, 12, 18, 16);
            graphics.blit(TEXTURE, leftPos + 46, topPos + 47, 192, 28, 18, 16);
        }
        int progress = menu.getProgressWidth(28);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 43, topPos + 36, 176, 0, progress, 12);
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 115, topPos + 19, 8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 70 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(152, 18, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 18, 16, 34, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(115, 19, 8, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatableWithFallback("desc.gui.upgrade", "Upgrade"),
                    Component.translatableWithFallback("desc.gui.upgrade.speed", "Speed"),
                    Component.translatableWithFallback("desc.gui.upgrade.power", "Power")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

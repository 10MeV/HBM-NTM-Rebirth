package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.SatelliteLinkerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SatelliteLinkerScreen extends AbstractContainerScreen<SatelliteLinkerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/machine/gui_sat_linker.png");

    public SatelliteLinkerScreen(SatelliteLinkerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 12, topPos + 28, 2);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 12, topPos + 44, 3);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        LegacyGuiElements.renderCustomInfoStat(graphics, font, mouseX, mouseY,
                leftPos + 12, topPos + 28, 16, 16, leftPos + 20, topPos + 44,
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.copy.0"),
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.copy.1"));
        LegacyGuiElements.renderCustomInfoStat(graphics, font, mouseX, mouseY,
                leftPos + 12, topPos + 44, 16, 16, leftPos + 20, topPos + 60,
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.randomize.0"),
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.randomize.1"));
        renderTooltip(graphics, mouseX, mouseY);
    }
}

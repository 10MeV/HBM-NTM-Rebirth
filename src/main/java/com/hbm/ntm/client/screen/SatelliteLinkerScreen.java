package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.SatelliteLinkerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SatelliteLinkerScreen extends AbstractContainerScreen<SatelliteLinkerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_linker.png");

    public SatelliteLinkerScreen(SatelliteLinkerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 36, 2);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 52, 3);
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
        // GUIMachineSatLinker used GuiInfoContainer#drawCustomInfoStat for
        // both icons: the legacy panel and its tooltip are deliberately
        // anchored at guiLeft - 8, guiTop + 52 rather than at the cursor.
        LegacyGuiElements.renderCustomInfoStat(graphics, font, mouseX, mouseY,
                leftPos - 16, topPos + 36, 16, 16, leftPos - 8, topPos + 52,
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.copy.0"),
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.copy.1"));
        LegacyGuiElements.renderCustomInfoStat(graphics, font, mouseX, mouseY,
                leftPos - 16, topPos + 52, 16, 16, leftPos - 8, topPos + 52,
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.randomize.0"),
                Component.translatable("container.hbm_ntm_rebirth.sat_linker.randomize.1"));
        renderTooltip(graphics, mouseX, mouseY);
    }
}

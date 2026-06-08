package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.SatelliteDockMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SatelliteDockScreen extends AbstractContainerScreen<SatelliteDockMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_dock.png");

    public SatelliteDockScreen(SatelliteDockMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 36, 2);
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
                leftPos - 16, topPos + 36, 16, 16, leftPos - 8, topPos + 52,
                Component.translatable("container.hbm_ntm_rebirth.sat_dock.info.0"),
                Component.translatable("container.hbm_ntm_rebirth.sat_dock.info.1"),
                Component.translatable("container.hbm_ntm_rebirth.sat_dock.info.2"));
        renderTooltip(graphics, mouseX, mouseY);
    }
}

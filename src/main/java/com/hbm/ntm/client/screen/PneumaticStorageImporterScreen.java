package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.PneumaticStorageImporterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

public class PneumaticStorageImporterScreen extends AbstractContainerScreen<PneumaticStorageImporterMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_pneumatic_importer.png");
    public PneumaticStorageImporterScreen(PneumaticStorageImporterMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 185; inventoryLabelY = 91; }
    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) { graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight); }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 5, 0x404040, false); graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) { renderBackground(graphics); super.render(graphics, mouseX, mouseY, partial); renderTooltip(graphics, mouseX, mouseY); }
}

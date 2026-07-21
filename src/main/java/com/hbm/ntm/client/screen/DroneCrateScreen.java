package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.DroneCrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** gui_crate_drone's two legacy toggle cells use normal menu button packets. */
public class DroneCrateScreen extends AbstractContainerScreen<DroneCrateMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_crate_drone.png");
    public DroneCrateScreen(DroneCrateMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 185; inventoryLabelY = 91; }
    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // GUIDroneCrate: the two texture cells show item/fluid and send/receive state.
        graphics.blit(TEXTURE, leftPos + 151, topPos + 16, 194, menu.getBlockEntity().itemType() ? 0 : 18, 18, 18);
        graphics.blit(TEXTURE, leftPos + 151, topPos + 52, 176, menu.getBlockEntity().sendingMode() ? 18 : 0, 18, 18);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 125, topPos + 51, 16, 34, menu.getTankData());
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false); graphics.drawString(font, playerInventoryTitle, 8, 91, 0x404040, false); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        if (isHovering(125, 17, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null) {
            if (isHovering(151, 16, 18, 18, mouseX, mouseY)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                LegacyGuiElements.playClickSound();
                return true;
            }
            if (isHovering(151, 52, 18, 18, mouseX, mouseY)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                LegacyGuiElements.playClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.StorageDrumMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StorageDrumScreen extends AbstractContainerScreen<StorageDrumMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_drum.png");

    public StorageDrumScreen(StorageDrumMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 234;
        inventoryLabelY = 141;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 17, topPos + 130,
                7, 106, menu.getLiquidTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 152, topPos + 130,
                7, 106, menu.getGasTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(17, 24, 7, 106, mouseX, mouseY)) {
            renderTankTooltip(graphics, menu.getLiquidTank(), mouseX, mouseY);
        } else if (isHovering(152, 24, 7, 106, mouseX, mouseY)) {
            renderTankTooltip(graphics, menu.getGasTank(), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderTankTooltip(GuiGraphics graphics, HbmFluidGuiHelper.TankData tank, int mouseX, int mouseY) {
        LegacyGuiElements.renderFluidTooltip(graphics, font, tank,
                menu.getTankTooltip(tank, hasShiftDown()), mouseX, mouseY);
    }
}

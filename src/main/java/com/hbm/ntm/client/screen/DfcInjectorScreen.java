package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.DfcInjectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DfcInjectorScreen extends AbstractContainerScreen<DfcInjectorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/dfc/gui_injector.png");

    public DfcInjectorScreen(DfcInjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 44, topPos + 17, 16, 52, menu.getFuel1());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 116, topPos + 17, 16, 52, menu.getFuel2());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, 0, 6, imageWidth, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(44, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getFuel1().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        } else if (isHovering(116, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getFuel2().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

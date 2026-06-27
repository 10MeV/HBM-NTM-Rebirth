package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.DfcReceiverMenu;
import com.hbm.ntm.util.BobMathUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DfcReceiverScreen extends AbstractContainerScreen<DfcReceiverMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/dfc/gui_receiver.png");

    public DfcReceiverScreen(DfcReceiverMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8, topPos + 17, 16, 52, menu.getCryogel());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, 0, 6, imageWidth, 0x404040);
        graphics.drawString(font, "Input:", 40, 25, 0xFF7F7F, false);
        graphics.drawString(font, BobMathUtil.getShortNumber(menu.getJoules()) + "Spk", 50, 35, 0xFF7F7F, false);
        graphics.drawString(font, "Output:", 40, 45, 0xFF7F7F, false);
        graphics.drawString(font, BobMathUtil.getShortNumber(menu.getOutputPower()) + "HE", 50, 55, 0xFF7F7F, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getCryogel().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.RBMKOutgasserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RBMKOutgasserScreen extends AbstractContainerScreen<RBMKOutgasserMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_rbmk_outgasser.png");

    public RBMKOutgasserScreen(RBMKOutgasserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = (int) (menu.getProgress() * 13.0D / RBMKColumnBlockEntity.OUTGASSER_DURATION);
        graphics.blit(TEXTURE, leftPos + 82, topPos + 50, 176, 0, progress, 6);
        HbmFluidGuiHelper.TankData gas = menu.getGasTank();
        int gasHeight = gas == null ? 0 : gas.scaledFill(42);
        graphics.blit(TEXTURE, leftPos + 115, topPos + 66 - gasHeight, 188,
                42 - gasHeight, 10, gasHeight);
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
        if (isHovering(112, 21, 16, 48, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getGasTank(),
                    menu.getGasTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

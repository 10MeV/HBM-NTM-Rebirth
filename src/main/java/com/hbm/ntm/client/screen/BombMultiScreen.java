package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.BombMultiMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BombMultiScreen extends AbstractContainerScreen<BombMultiMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/weapon/bombgeneric.png");

    public BombMultiScreen(BombMultiMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = BombMultiMenu.IMAGE_WIDTH;
        imageHeight = BombMultiMenu.IMAGE_HEIGHT;
        inventoryLabelX = BombMultiMenu.PLAYER_INV_X;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int type = menu.modifierOverlayType();
        if (type > 0) {
            int v = type == 7 ? 7 * 18 : (type - 1) * 18;
            graphics.blit(TEXTURE, leftPos + 124, topPos + 34, 176, v, 18, 18, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

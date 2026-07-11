package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.CartDestroyerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CartDestroyerScreen extends AbstractContainerScreen<CartDestroyerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/cart/gui_destroyer.png");

    public CartDestroyerScreen(CartDestroyerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 96 + 4;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int index = (int) (System.currentTimeMillis() % 1000L) / 128;
        if (index == 1 || index == 7) {
            graphics.blit(TEXTURE, leftPos + 66, topPos + 35, 0, 166, 44, 16);
        }
        if (index == 2 || index == 6) {
            graphics.blit(TEXTURE, leftPos + 66, topPos + 35, 0, 182, 44, 16);
        }
        if (index == 3 || index == 5) {
            graphics.blit(TEXTURE, leftPos + 66, topPos + 35, 0, 198, 44, 16);
        }
        if (index == 4) {
            graphics.blit(TEXTURE, leftPos + 66, topPos + 35, 0, 214, 44, 16);
        }
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
        renderTooltip(graphics, mouseX, mouseY);
    }
}

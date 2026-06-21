package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.CrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrateScreen extends AbstractContainerScreen<CrateMenu> {
    private final ResourceLocation texture;

    public CrateScreen(CrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = menu.getBlockEntity().kind().imageWidth();
        this.imageHeight = menu.getBlockEntity().kind().imageHeight();
        this.inventoryLabelY = imageHeight - 94;
        this.texture = texture(menu);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture(menu), leftPos, topPos, 0, 0, imageWidth, imageHeight);
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

    private ResourceLocation texture(CrateMenu menu) {
        return new ResourceLocation(HbmNtm.MOD_ID,
                "textures/gui/storage/"
                        + menu.getBlockEntity().kind().textureName(menu.getBlockEntity().isHot()) + ".png");
    }
}

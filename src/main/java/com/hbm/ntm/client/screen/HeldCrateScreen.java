package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.StorageCrateBlockEntity;
import com.hbm.ntm.menu.HeldCrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** The old ItemBlockStorageCrate uses the same five GUI sheets as placed crates. */
public class HeldCrateScreen extends AbstractContainerScreen<HeldCrateMenu> {
    private final ResourceLocation texture;
    private final int labelColor;

    public HeldCrateScreen(HeldCrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = menu.kind().imageWidth();
        this.imageHeight = menu.kind().imageHeight();
        this.inventoryLabelX = menu.kind().playerInventoryX();
        this.inventoryLabelY = imageHeight - 94
                + (menu.kind() == StorageCrateBlockEntity.Kind.DESH ? 1 : 0);
        this.labelColor = menu.kind() == StorageCrateBlockEntity.Kind.TUNGSTEN ? 0xFFFFFF : 0x404040;
        this.texture = new ResourceLocation(HbmNtm.MOD_ID,
                "textures/gui/storage/" + menu.kind().textureName(menu.isHot()) + ".png");
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, labelColor, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, labelColor, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

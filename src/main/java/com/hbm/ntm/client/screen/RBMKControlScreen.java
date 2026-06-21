package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.RBMKControlMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RBMKControlScreen extends AbstractContainerScreen<RBMKControlMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_rbmk_control.png");

    public RBMKControlScreen(RBMKControlMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int height = (int) (56.0D * (1.0D - menu.getLevelPercent() / 100.0D));
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 75, topPos + 29, 176, 56 - height, 8, height);
        }
        int color = menu.getColor();
        if (color >= 0 && color < 5) {
            graphics.blit(TEXTURE, leftPos + 28, topPos + 26 + color * 11, 184, color * 10, 12, 10);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        for (int k = 0; k < 5; k++) {
            if (isHovering(118, 26 + k * 11, 30, 10, mouseX, mouseY)) {
                CompoundTag tag = new CompoundTag();
                tag.putDouble(RBMKColumnKeys.LEVEL, 1.0D - k * 0.25D);
                send(tag);
                return true;
            }
            if (isHovering(28, 26 + k * 11, 12, 10, mouseX, mouseY)) {
                CompoundTag tag = new CompoundTag();
                tag.putInt(RBMKColumnKeys.COLOR, k);
                send(tag);
                return true;
            }
        }
        return handled;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(71, 29, 16, 56, mouseX, mouseY)) {
            LegacyGuiElements.renderTextTooltip(graphics, font, mouseX, mouseY, menu.getLevelPercent() + "%");
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void send(CompoundTag tag) {
        LegacyGuiElements.playClickSound();
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }
}

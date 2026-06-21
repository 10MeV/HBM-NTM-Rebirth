package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.SirenCassetteItem;
import com.hbm.ntm.menu.SirenMenu;
import com.hbm.ntm.sound.LegacySirenTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SirenScreen extends AbstractContainerScreen<SirenMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_siren.png");

    public SirenScreen(SirenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        LegacySirenTrack track = SirenCassetteItem.track(menu.getCassette());
        if (track != LegacySirenTrack.NULL) {
            int color = track.color();
            graphics.drawString(font, track.title(), 46, 28, color, false);
            graphics.drawString(font, "Type: " + track.type().name(), 46, 40, color, false);
            graphics.drawString(font, "Volume: " + track.volume(), 46, 52, color, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

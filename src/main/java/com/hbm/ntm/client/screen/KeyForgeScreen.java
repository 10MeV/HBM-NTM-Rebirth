package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.KeyForgeMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class KeyForgeScreen extends AbstractContainerScreen<KeyForgeMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/gui_keyforge.png");

    public KeyForgeScreen(KeyForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(TEXTURE, leftPos - 16, topPos + 36, 176, 0, 16, 16);
        graphics.blit(TEXTURE, leftPos - 16, topPos + 52, 176, 16, 16, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos - 16 && mouseX < leftPos && mouseY >= topPos + 36 && mouseY < topPos + 52) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("The first slot will copy the key/lock's"),
                    Component.literal("pin configuration and paste it to the second slot.")), mouseX, mouseY);
        } else if (mouseX >= leftPos - 16 && mouseX < leftPos && mouseY >= topPos + 52 && mouseY < topPos + 68) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("The third slot will randomize the"),
                    Component.literal("key/lock's pin configuration.")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

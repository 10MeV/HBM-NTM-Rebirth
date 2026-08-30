package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.KeyForgeMenu;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class KeyForgeScreen extends AbstractContainerScreen<KeyForgeMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/machine/gui_keyforge.png");

    public KeyForgeScreen(KeyForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 12, topPos + 28, 2);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 12, topPos + 44, 3);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 12, topPos + 28, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 12, topPos + 28, 16, 16, leftPos - 8, topPos + 52,
                    splitLegacyInfo(Component.translatableWithFallback("desc.gui.keyforge.key",
                            "The first slot will copy the key/lock's$pin configuration and paste it to the second slot.")));
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 12, topPos + 44, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 12, topPos + 44, 16, 16, leftPos - 8, topPos + 52,
                    splitLegacyInfo(Component.translatableWithFallback("desc.gui.keyforge.random",
                            "The third slot will randomize the$key/lock's pin configuration.")));
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static List<Component> splitLegacyInfo(Component text) {
        return Arrays.stream(text.getString().split("\\$"))
                .map(Component::literal)
                .map(Component.class::cast)
                .toList();
    }
}

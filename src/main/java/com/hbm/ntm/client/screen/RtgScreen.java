package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RtgBlockEntity;
import com.hbm.ntm.menu.RtgMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RtgScreen extends AbstractContainerScreen<RtgMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_rtg.png");

    public RtgScreen(RtgMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 188;
        titleLabelX = 60;
        titleLabelY = 7;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int heat = menu.getHeatBarHeight(51);
        if (heat > 0) {
            graphics.blit(TEXTURE, leftPos + 124, topPos + 61 - heat, 176, 10 + (51 - heat), 16, heat);
        }
        int power = menu.getPowerBarHeight(51);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 146, topPos + 61 - power, 192, 10 + (51 - power), 16, power);
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 12, topPos + 25, 2);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX - font.width(title) / 2, titleLabelY, 0xA6B5AE, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 146, 9, 16, 51)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 146, topPos + 9, 16, 51, menu.getPower(), menu.getMaxPower());
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 124, 9, 16, 51)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(
                    Component.translatableWithFallback("desc.gui.rtg.heat",
                            "\u00a7eCurrent heat level: %s", menu.getHeat())), mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 12, topPos + 25, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 12, topPos + 25, 16, 16, leftPos - 8, topPos + 52,
                    RtgBlockEntity.acceptedPelletTooltip());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

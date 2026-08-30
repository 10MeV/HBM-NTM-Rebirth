package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.RadGenMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RadGenScreen extends AbstractContainerScreen<RadGenMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_radgen.png");

    public RadGenScreen(RadGenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 184;
        titleLabelX = 0;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarWidth(48);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 64, topPos + 83, 176, 3, power, 4);
        }
        for (int lane = 0; lane < 12; lane++) {
            int width = menu.getProgressWidth(lane, 44);
            if (width > 0) {
                graphics.blit(TEXTURE, leftPos + 66, topPos + 19 + lane * 5,
                        176, 0, width, 3);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = imageWidth / 2 - font.width(title) / 2;
        graphics.drawString(font, title, titleX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 64, topPos + 83, 48, 4)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 64, topPos + 83, 48, 4, menu.getPower(), menu.getMaxPower());
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 65, topPos + 18, 46, 60)) {
            int lane = (mouseY - topPos - 19) / 5;
            if (lane >= 0 && lane < 12 && menu.hasLaneProgress(lane)) {
                graphics.renderTooltip(font, List.of(
                        Component.literal("Slot " + (lane + 1) + ":").getVisualOrderText(),
                        Component.literal(menu.getProduction(lane) + "HE/t for").getVisualOrderText(),
                        Component.literal(menu.getRemainingTicks(lane) + " ticks ("
                                + menu.getRemainingPercent(lane) + "%)").getVisualOrderText()), mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

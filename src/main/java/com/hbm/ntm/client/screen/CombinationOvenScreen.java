package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CombinationOvenBlockEntity;
import com.hbm.ntm.menu.CombinationOvenMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CombinationOvenScreen extends AbstractContainerScreen<CombinationOvenMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_furnace_combination.png");

    public CombinationOvenScreen(CombinationOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getProgressPixels(38);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 45, topPos + 37, 176, 0, progress, 5);
        }
        int heat = menu.getHeatPixels(37);
        if (heat > 0) {
            graphics.blit(TEXTURE, leftPos + 45, topPos + 46, 176, 5, heat, 5);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 118, topPos + 70, 16, 52,
                menu.getTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX - font.width(title) / 2, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(118, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(45, 37, 38, 5, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal(menu.getProgress() + " / " + CombinationOvenBlockEntity.PROCESS_TIME)
                            .getVisualOrderText()), mouseX, mouseY);
        } else if (isHovering(45, 46, 37, 5, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal(menu.getHeat() + " / " + CombinationOvenBlockEntity.MAX_HEAT + " TU")
                            .getVisualOrderText()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

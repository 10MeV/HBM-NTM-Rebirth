package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.LegacyFurnaceBlockEntity;
import com.hbm.ntm.menu.LegacyFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LegacyFurnaceScreen extends AbstractContainerScreen<LegacyFurnaceMenu> {
    private static final ResourceLocation IRON =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_furnace_iron.png");
    private static final ResourceLocation STEEL =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_furnace_steel.png");

    public LegacyFurnaceScreen(LegacyFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (menu.getBlockEntity().kind() == LegacyFurnaceBlockEntity.Kind.STEEL) {
            renderSteel(graphics);
        } else {
            renderIron(graphics);
        }
    }

    private void renderIron(GuiGraphics graphics) {
        graphics.blit(IRON, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getProgressWidth(70);
        if (progress > 0) {
            graphics.blit(IRON, leftPos + 53, topPos + 36, 176, 18, progress, 5);
        }
        int burn = menu.getBurnWidth(70);
        if (burn > 0) {
            graphics.blit(IRON, leftPos + 53, topPos + 45, 176, 23, burn, 5);
        }
        if (menu.wasOn()) {
            graphics.blit(IRON, leftPos + 70, topPos + 16, 176, 0, 18, 18);
        }
    }

    private void renderSteel(GuiGraphics graphics) {
        graphics.blit(STEEL, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int heat = menu.getHeatBarHeight(48);
        if (heat > 0) {
            graphics.blit(STEEL, leftPos + 152, topPos + 67 - heat, 176, 76 - heat, 7, heat);
        }
        for (int i = 0; i < 3; i++) {
            int progress = menu.getSteelProgressWidth(i, 69);
            int bonus = menu.getSteelBonusWidth(i, 69);
            if (progress > 0) {
                graphics.blit(STEEL, leftPos + 54, topPos + 18 + i * 18, 176, 18, progress, 5);
            }
            if (bonus > 0) {
                graphics.blit(STEEL, leftPos + 54, topPos + 27 + i * 18, 176, 23, bonus, 5);
            }
            if (menu.wasOn()) {
                graphics.blit(STEEL, leftPos + 16, topPos + 16 + i * 18, 176, 0, 18, 18);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}

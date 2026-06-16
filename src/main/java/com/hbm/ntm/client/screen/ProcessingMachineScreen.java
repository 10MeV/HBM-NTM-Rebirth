package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.menu.ProcessingMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ProcessingMachineScreen extends AbstractContainerScreen<ProcessingMachineMenu> {
    private static final ResourceLocation CENTRIFUGE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_centrifuge.png");
    private static final ResourceLocation CRYSTALLIZER =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_crystallizer_alt.png");

    public ProcessingMachineScreen(ProcessingMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        boolean crystallizer = menu.getBlockEntity().kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER;
        imageWidth = 176;
        imageHeight = crystallizer ? 204 : 186;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (menu.getBlockEntity().kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER) {
            renderCrystallizer(graphics);
        } else {
            renderCentrifuge(graphics);
        }
    }

    private void renderCentrifuge(GuiGraphics graphics) {
        graphics.blit(CENTRIFUGE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(34);
        if (power > 0) {
            graphics.blit(CENTRIFUGE, leftPos + 9, topPos + 48 - power, 176, 35 - power, 16, power);
        }
        int progress = menu.getProgressHeight(145);
        for (int i = 0; i < 4; i++) {
            int height = Math.min(35, Math.max(0, progress - i * 36));
            if (height > 0) {
                graphics.blit(CENTRIFUGE, leftPos + 65 + i * 20, topPos + 50 - height,
                        176, 71 - height, 12, height);
            }
        }
    }

    private void renderCrystallizer(GuiGraphics graphics) {
        graphics.blit(CRYSTALLIZER, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(CRYSTALLIZER, leftPos + 152, topPos + 70 - power, 176, 64 - power, 16, power);
        }
        int progress = menu.getProgressWidth(28);
        if (progress > 0) {
            graphics.blit(CRYSTALLIZER, leftPos + 80, topPos + 47, 176, 0, progress, 12);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 35, topPos + 70,
                16, 52, menu.getTankData());
        graphics.blit(CRYSTALLIZER, leftPos + 117, topPos + 22, 176, 52, 8, 8);
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
        if (menu.getBlockEntity().kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER) {
            if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                        leftPos + 152, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
            } else if (isHovering(35, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                        menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
            }
        } else if (isHovering(9, 13, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 9, topPos + 13, 16, 34, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

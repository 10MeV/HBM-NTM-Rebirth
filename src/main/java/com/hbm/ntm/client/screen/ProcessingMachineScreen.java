package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.menu.ProcessingMachineMenu;
import java.util.List;
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
        imageWidth = crystallizer ? 176 : 182;
        imageHeight = crystallizer ? 204 : 189;
        titleLabelX = crystallizer ? 8 : 11;
        titleLabelY = 6;
        inventoryLabelX = crystallizer ? 8 : 11;
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
        int power = menu.getPowerBarHeight(37);
        if (power > 0) {
            graphics.blit(CENTRIFUGE, leftPos + 8, topPos + 55 - power, 182, 37 - power, 16, power);
        }
        int progress = menu.getProgressHeight(145);
        for (int i = 0; i < 4; i++) {
            int height = Math.min(36, Math.max(0, progress - i * 36));
            if (height > 0) {
                graphics.blit(CENTRIFUGE, leftPos + 72 + i * 20, topPos + 57 - height,
                        182, 73 - height, 12, height);
            }
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 160, topPos + 16, 8);
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
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 117, topPos + 22, 8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = menu.getBlockEntity().kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER
                ? 70 - font.width(title) / 2 : imageWidth / 2 + 18 - font.width(title) / 2;
        int titleColor = menu.getBlockEntity().kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER
                ? 0x404040 : 0xFFFFFF;
        graphics.drawString(font, title, titleX, titleLabelY, titleColor, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (menu.getBlockEntity().kind() == ProcessingMachineBlockEntity.Kind.CRYSTALLIZER) {
            if (isLegacyHovering(152, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                        leftPos + 152, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
            } else if (isLegacyHovering(35, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                        menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
            } else if (isLegacyHovering(117, 22, 8, 8, mouseX, mouseY)) {
                LegacyGuiElements.renderTooltip(graphics, font, List.of(
                        Component.translatableWithFallback("desc.gui.upgrade", "Acceptable Upgrades:"),
                        Component.translatableWithFallback("desc.gui.upgrade.speed", "Speed"),
                        Component.translatableWithFallback("desc.gui.upgrade.effectiveness", "Effectiveness"),
                        Component.translatableWithFallback("desc.gui.upgrade.overdrive", "Overdrive")),
                        leftPos + 200, topPos + 45);
            }
        } else if (isLegacyHovering(8, 18, 16, 37, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 18, 16, 37, menu.getPower(), menu.getMaxPower());
        } else if (isLegacyHovering(160, 16, 8, 8, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(
                    Component.translatableWithFallback("desc.gui.upgrade", "Acceptable Upgrades:"),
                    Component.translatableWithFallback("desc.gui.upgrade.speed", "Speed"),
                    Component.translatableWithFallback("desc.gui.upgrade.power", "Power"),
                    Component.translatableWithFallback("desc.gui.upgrade.overdrive", "Overdrive")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }
}

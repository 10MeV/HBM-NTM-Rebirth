package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.menu.CompressorMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompressorScreen extends AbstractContainerScreen<CompressorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_compressor.png");

    public CompressorScreen(CompressorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getPower() >= menu.getPowerRequirement()) {
            graphics.blit(TEXTURE, leftPos + 156, topPos + 4, 176, 52, 9, 12);
        }
        graphics.blit(TEXTURE, leftPos + 43 + menu.getInputPressure() * 11, topPos + 46, 193, 18, 8, 14);

        int progress = menu.getProgressWidth(55);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 42, topPos + 26, 192, 0, progress, 17);
        }
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 70 - power, 176, 52 - power, 16, power);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 17, topPos + 70,
                16, 52, menu.getInputTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 107, topPos + 70,
                16, 52, menu.getOutputTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), titleLabelX, titleLabelY, 124, 0xC7C1A3);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(17, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTankData(),
                    menu.getInputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(107, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOutputTankData(),
                    menu.getOutputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        } else {
            for (int pressure = 0; pressure < 5; pressure++) {
                if (isHovering(43 + pressure * 11, 46, 8, 14, mouseX, mouseY)) {
                    graphics.renderTooltip(font,
                            Component.literal(pressure + " PU -> " + (pressure + 1) + " PU"),
                            mouseX, mouseY);
                    break;
                }
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int pressure = 0; pressure < 5; pressure++) {
            if (isHovering(43 + pressure * 11, 46, 8, 14, mouseX, mouseY)) {
                ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), pressure,
                        CompressorBlockEntity.CONTROL_INPUT_PRESSURE);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}

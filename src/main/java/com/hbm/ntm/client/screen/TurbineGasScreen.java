package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.TurbineGasBlockEntity;
import com.hbm.ntm.menu.TurbineGasMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class TurbineGasScreen extends AbstractContainerScreen<TurbineGasMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/generators/gui_turbinegas.png");
    private static final ResourceLocation RPM_GAUGE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gauges/button_big.png");

    private boolean draggingSlider;
    private int dragStartY;
    private int dragStartSlider;
    private int localSlider;
    private int startupNumber;
    private int startupDigit;
    private int startupExponent;

    public TurbineGasScreen(TurbineGasMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 223;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
        localSlider = menu.getSliderPos();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        localSlider = draggingSlider ? localSlider : menu.getSliderPos();

        graphics.blit(TEXTURE, leftPos + 74, topPos + 86, 194, menu.isAutoMode() ? 11 : 24, 29, 13);
        switch (menu.getState()) {
            case 0 -> graphics.blit(TEXTURE, leftPos + 80, topPos + 32, 178, 38, 16, 16);
            case -1 -> {
                graphics.blit(TEXTURE, leftPos + 80, topPos + 32, 194, 38, 16, 16);
                renderStartupDisplay(graphics);
            }
            case 1 -> {
                graphics.blit(TEXTURE, leftPos + 80, topPos + 32, 210, 38, 16, 16);
                renderPowerMeterDisplay(graphics, menu.getInstantPowerOutput() * 20);
            }
            default -> {
            }
        }

        graphics.blit(TEXTURE, leftPos + 36, topPos + 97 - localSlider, 178, 0, 16, 6);
        int power = menu.getPowerBarWidth(142);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 26, topPos + 109, 0, 223, power, 16);
        }
        renderRpmGauge(graphics, menu.getRpm());
        renderThermometer(graphics, menu.getTemperature());

        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 34, 3);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 50, 2);
        if (menu.hasNoFuelOrLube()) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 66, 6);
        } else if (menu.hasLowFuelOrLube()) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 66, 7);
        }

        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8, topPos + 65, 16, 48,
                menu.getFuelTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8, topPos + 103, 16, 32,
                menu.getLubricantTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 147, topPos + 98, 16, 36,
                menu.getWaterTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 147, topPos + 58, 16, 36,
                menu.getSteamTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(26, 108, 142, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 26, topPos + 108, 142, 16, menu.getPower(), menu.getMaxPower());
        } else if (draggingSlider || isHovering(36, 36, 16, 66, mouseX, mouseY)) {
            renderSliderTooltip(graphics, mouseX, mouseY);
        } else if (isHovering(133, 23, 8, 72, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(Component.literal("Temperature: "
                    + Math.max(20, menu.getTemperature()) + " C").getVisualOrderText()), mouseX, mouseY);
        } else if (isHovering(8, 16, 16, 48, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getFuelTank(),
                    menu.getFuelTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(8, 70, 16, 32, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getLubricantTank(),
                    menu.getLubricantTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(147, 61, 16, 36, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getWaterTank(),
                    menu.getWaterTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(147, 21, 16, 36, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSteamTank(),
                    menu.getSteamTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(-16, 34, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.translatableWithFallback("desc.gui.turbinegas.automode",
                            "Automatic throttle follows stored power and fuel."))), mouseX, mouseY);
        } else if (isHovering(-16, 50, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.translatableWithFallback("desc.gui.turbinegas.fuels",
                            "Accepts combustible gas-grade fuel identifiers."))), mouseX, mouseY);
        } else if ((menu.hasLowFuelOrLube() || menu.hasNoFuelOrLube())
                && isHovering(-16, 66, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.translatableWithFallback("desc.gui.turbinegas.warning",
                            "Low fuel or lubricant."))), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double buttonDx = mouseX - leftPos - 88.0D;
        double buttonDy = mouseY - topPos - 40.0D;
        if (Math.sqrt(buttonDx * buttonDx + buttonDy * buttonDy) <= 8.0D) {
            if (menu.getCounter() == 0 || menu.getCounter() == 579) {
                int value = menu.getState() - 1;
                ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), value,
                        TurbineGasBlockEntity.CONTROL_STATE);
                LegacyGuiElements.playClickSound();
            }
            return true;
        }
        if (menu.getState() == 1 && isHovering(74, 86, 29, 13, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), menu.isAutoMode() ? 0 : 1,
                    TurbineGasBlockEntity.CONTROL_AUTO);
            LegacyGuiElements.playClickSound();
            return true;
        }
        if (menu.getState() == 1 && isHovering(36, 97 - menu.getSliderPos(), 16, 6, mouseX, mouseY)) {
            draggingSlider = true;
            dragStartY = (int) mouseY;
            dragStartSlider = menu.getSliderPos();
            localSlider = dragStartSlider;
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0,
                    TurbineGasBlockEntity.CONTROL_AUTO);
            LegacyGuiElements.playClickSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingSlider) {
            updateSlider(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSlider) {
            draggingSlider = false;
            updateSlider(mouseY);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSlider(double mouseY) {
        int next = Mth.clamp(dragStartSlider + dragStartY - (int) mouseY, 0, 60);
        if (next != localSlider) {
            localSlider = next;
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), next,
                    TurbineGasBlockEntity.CONTROL_SLIDER);
        }
    }

    private void renderSliderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getState() == 1) {
            graphics.renderTooltip(font, split(List.of(Component.literal(String.format(Locale.US,
                    "Fuel consumption: %.1f mB/s", menu.fuelConsumptionPerSecond())))),
                    mouseX, mouseY);
        } else {
            graphics.renderTooltip(font, split(List.of(Component.literal("Generator offline"))), mouseX, mouseY);
        }
    }

    private void renderStartupDisplay(GuiGraphics graphics) {
        if (startupNumber < 8_888_888 && menu.getCounter() < 60) {
            startupDigit++;
            if (startupDigit == 9) {
                startupDigit = 1;
                startupExponent++;
            }
            startupNumber += (int) Math.pow(10.0D, startupExponent);
        }
        if (menu.getCounter() > 50) {
            startupNumber = 0;
        }
        renderPowerMeterDisplay(graphics, startupNumber);
    }

    private void renderPowerMeterDisplay(GuiGraphics graphics, int number) {
        int firstDigitX = 65;
        int firstDigitY = 71;
        int[] digits = new int[7];
        int safeNumber = Math.max(0, number);
        for (int i = 6; i >= 0; i--) {
            digits[i] = safeNumber % 10;
            safeNumber /= 10;
            graphics.blit(TEXTURE, leftPos + firstDigitX + i * 7, topPos + firstDigitY,
                    194 + digits[i] * 5, 0, 5, 11);
        }
        int leadingZeros = 0;
        for (int i = 0; i < 6; i++) {
            if (digits[i] == 0) {
                leadingZeros++;
            } else {
                break;
            }
        }
        for (int i = 0; i < leadingZeros; i++) {
            graphics.blit(TEXTURE, leftPos + firstDigitX + i * 7, topPos + firstDigitY, 244, 0, 5, 11);
        }
    }

    private void renderThermometer(GuiGraphics graphics, int temperature) {
        int clamped = Mth.clamp(temperature, 0, 800);
        int height = 64 * clamped / 800;
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 136, topPos + 28 + 64 - height, 176, 64 - height,
                    2, height);
        }
    }

    private void renderRpmGauge(GuiGraphics graphics, int rpm) {
        int boundedRpm = Mth.clamp(rpm, 0, 100);
        graphics.blit(RPM_GAUGE, leftPos + 64, topPos + 16, boundedRpm * 48, 0,
                48, 48, 4848, 48);
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}

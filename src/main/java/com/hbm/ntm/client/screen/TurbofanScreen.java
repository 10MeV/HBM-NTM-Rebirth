package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.TurbofanMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TurbofanScreen extends AbstractContainerScreen<TurbofanMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/generators/gui_turbofan.png");

    public TurbofanScreen(TurbofanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 203;
        titleLabelX = 43;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 143, topPos + 69 - power, 192, 52 - power, 16, power);
        }

        if (menu.getAfterburner() > 0) {
            int afterburnerFrame = Math.min(menu.getAfterburner(), 6);
            graphics.blit(TEXTURE, leftPos + 98, topPos + 44, 176, (afterburnerFrame - 1) * 16, 16, 16);
        }

        if (menu.showsBlood()) {
            double bloodFill = menu.getBloodTank().capacity() <= 0
                    ? 0.0D
                    : (double) menu.getBloodTank().fill() / (double) menu.getBloodTank().capacity();
            LegacyGuiElements.renderRoundSmallGauge(graphics, leftPos + 97, topPos + 16, bloodFill);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 35, topPos + 69, 34, 52,
                menu.getFuelTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(35, 17, 34, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getFuelTank(),
                    menu.fuelTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (menu.showsBlood() && isHovering(98, 17, 16, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getBloodTank(),
                    menu.bloodTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(143, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 143, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(98, 44, 16, 16, mouseX, mouseY) && menu.getAfterburner() > 0) {
            graphics.renderTooltip(font, List.of(
                    Component.literal("Afterburner: " + menu.getAfterburner()).getVisualOrderText()),
                    mouseX, mouseY);
        } else if (isHovering(116, 44, 52, 18, mouseX, mouseY)
                && (menu.getOutput() > 0 || menu.getConsumption() > 0)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal(menu.getOutput() + " HE/t").getVisualOrderText(),
                    Component.literal(menu.getConsumption() + " mB/t").getVisualOrderText()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

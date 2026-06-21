package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.DieselGeneratorMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DieselGeneratorScreen extends AbstractContainerScreen<DieselGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/guidiesel.png");

    public DieselGeneratorScreen(DieselGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 69 - power, 176, 52 - power, 16, power);
        }
        if (menu.wasOn()) {
            graphics.blit(TEXTURE, leftPos + 115, topPos + 34, 208, 0, 18, 18);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 80, topPos + 69, 16, 52,
                menu.getTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(80, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(152, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(-16, 36, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal("Fuel consumption rate:"),
                    Component.literal("  1 mB/t"),
                    Component.literal("  20 mB/s"),
                    Component.literal("(Consumption rate is constant)"))), mouseX, mouseY);
        } else if (!menu.hasAcceptableFuel() && isHovering(-16, 68, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal("Error: The currently set fuel type"),
                    Component.literal("is not supported by this engine!"))), mouseX, mouseY);
        } else if (isHovering(115, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(menu.getOutput() + " HE/t"))), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}

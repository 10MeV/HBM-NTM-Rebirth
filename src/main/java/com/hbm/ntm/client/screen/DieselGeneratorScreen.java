package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.DieselGeneratorBlockEntity;
import com.hbm.ntm.menu.DieselGeneratorMenu;
import com.hbm.ntm.network.ModMessages;
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
        imageHeight = 203;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 141, topPos + 69 - power, 176, 52 - power, 16, power);
        }
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 61, 192, 16, 35, 14);
        }
        if (menu.wasOn()) {
            graphics.blit(TEXTURE, leftPos + 89, topPos + 42, 192, 0, 16, 16);
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 8, topPos + 36, 2);
        if (!menu.hasAcceptableFuel()) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 8, topPos + 68, 6);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 35, topPos + 69, 16, 52,
                menu.getTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isLegacyHovering(35, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isLegacyHovering(141, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 141, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isLegacyHovering(-8, 36, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal("Fuel consumption rate:"),
                    Component.literal("  1 mB/t"),
                    Component.literal("  20 mB/s"),
                    Component.literal("(Consumption rate is constant)"))), mouseX, mouseY);
        } else if (!menu.hasAcceptableFuel() && isLegacyHovering(-8, 68, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal("Error: The currently set fuel type"),
                    Component.literal("is not supported by this engine!"))), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isLegacyHovering(89, 61, 16, 14, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0,
                    DieselGeneratorBlockEntity.CONTROL_TOGGLE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}

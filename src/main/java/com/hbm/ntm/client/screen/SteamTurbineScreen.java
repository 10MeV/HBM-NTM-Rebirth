package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.menu.SteamTurbineMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SteamTurbineScreen extends AbstractContainerScreen<SteamTurbineMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_turbine.png");

    public SteamTurbineScreen(SteamTurbineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderSteamTypeIcon(graphics);
        renderEnergy(graphics);
        if (menu.getOutputTank().type() == HbmFluids.NONE) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 68, 6);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 62, topPos + 69,
                16, 52, menu.getInputTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 134, topPos + 69,
                16, 52, menu.getOutputTank());
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
        if (isHovering(62, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTank(),
                    menu.getInputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(134, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOutputTank(),
                    menu.getOutputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(123, 35, 7, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 123, topPos + 35, 7, 34, menu.getPower(), menu.getMaxPower());
        } else if (menu.getOutputTank().type() == HbmFluids.NONE
                && isHovering(-16, 68, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(Component.literal("Error: Invalid fluid!"))),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderSteamTypeIcon(GuiGraphics graphics) {
        int v = -1;
        if (menu.getInputTank().type() == HbmFluids.STEAM) {
            v = 0;
        } else if (menu.getInputTank().type() == HbmFluids.HOTSTEAM) {
            v = 14;
        } else if (menu.getInputTank().type() == HbmFluids.SUPERHOTSTEAM) {
            v = 28;
        } else if (menu.getInputTank().type() == HbmFluids.ULTRAHOTSTEAM) {
            v = 42;
        }
        if (v >= 0) {
            graphics.blit(TEXTURE, leftPos + 99, topPos + 18, 183, v, 14, 14);
        }
    }

    private void renderEnergy(GuiGraphics graphics) {
        int height = menu.getPowerBarHeight(34);
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 123, topPos + 69 - height, 176, 34 - height, 7, height);
        }
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}

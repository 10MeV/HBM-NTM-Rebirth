package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.OilDrillMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OilDrillScreen extends AbstractContainerScreen<OilDrillMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_well.png");

    public OilDrillScreen(OilDrillMenu menu, Inventory inventory, Component title) {
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
        renderEnergy(graphics);
        renderIndicator(graphics);
        if (!menu.hasFrackingTank()) {
            graphics.blit(TEXTURE, leftPos + 34, topPos + 36, 192, 0, 18, 34);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 62, topPos + 69, 16, 52, menu.getTank(0));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 107, topPos + 69, 16, 52, menu.getTank(1));
        if (menu.hasFrackingTank()) {
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 40, topPos + 69, 6, 32, menu.getTank(2));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), 0, titleLabelY, imageWidth, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, 0, 62, 17, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 1, 107, 17, 16, 52);
        if (menu.hasFrackingTank()) {
            renderTankTooltip(graphics, mouseX, mouseY, 2, 40, 37, 6, 32);
        }
        if (isHovering(8, 17, 16, 34, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"),
                    mouseX, mouseY);
        } else if (isHovering(156, 3, 8, 8, mouseX, mouseY)) {
            List<Component> tooltip = List.of(
                    Component.literal("Upgrades"),
                    Component.literal("Speed"),
                    Component.literal("Power Saving"),
                    Component.literal("Overdrive"));
            graphics.renderTooltip(font, tooltip.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderEnergy(GuiGraphics graphics) {
        int height = menu.getPowerBarHeight(34);
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 51 - height, 176, 34 - height, 16, height);
        }
    }

    private void renderIndicator(GuiGraphics graphics) {
        int indicator = menu.getIndicator();
        if (indicator > 0) {
            graphics.blit(TEXTURE, leftPos + 35, topPos + 17, 176 + (indicator - 1) * 16, 52, 16, 16);
        }
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, int index, int x, int y,
            int width, int height) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) {
            return;
        }
        List<Component> tooltip = menu.getTankTooltip(index, hasShiftDown());
        LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(index), tooltip, mouseX, mouseY);
    }
}

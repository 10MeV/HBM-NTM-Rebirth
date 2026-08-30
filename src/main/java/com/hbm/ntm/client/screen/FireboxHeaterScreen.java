package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FireboxHeaterBlockEntity;
import com.hbm.ntm.menu.FireboxHeaterMenu;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FireboxHeaterScreen extends AbstractContainerScreen<FireboxHeaterMenu> {
    private static final ResourceLocation FIREBOX_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_firebox.png");
    private static final ResourceLocation OVEN_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_heating_oven.png");

    public FireboxHeaterScreen(FireboxHeaterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = menu.isOven() ? OVEN_TEXTURE : FIREBOX_TEXTURE;
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(texture, leftPos + 81, topPos + 28, 176, 0, menu.heatBarWidth(), 5);
        graphics.blit(texture, leftPos + 81, topPos + 37, 176, 5, menu.burnBarWidth(), 5);
        if (menu.wasOn()) {
            graphics.blit(texture, leftPos + 25, topPos + 26, 176, 10, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleColor = menu.isOven() ? 0xFFFFFF : 0x404040;
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, titleColor, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (menu.getCarried().isEmpty() && renderEmptyFuelSlotTooltip(graphics, mouseX, mouseY)) {
            // The legacy GUI reserves empty fuel-slot hover for the burn-module description.
        } else if (isLegacyHovering(80, 27, 71, 7, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal(String.format(Locale.US, "%,d / %,dTU",
                    menu.getHeatEnergy(), menu.getMaxHeat()))), mouseX, mouseY);
        } else if (isLegacyHovering(80, 36, 71, 7, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal(menu.getBurnHeat() + "TU/t"),
                    Component.literal((menu.getBurnTime() / 20) + "s")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean renderEmptyFuelSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int slot = 0; slot < 2; slot++) {
            if (!isHovering(44 + slot * 18, 27, 16, 16, mouseX, mouseY) || menu.getSlot(slot).hasItem()) {
                continue;
            }
            List<Component> description = FireboxHeaterBlockEntity.burnModule().getDescription().stream()
                    .map(text -> (Component) Component.literal(text)).toList();
            if (!description.isEmpty()) {
                graphics.renderComponentTooltip(font, description, mouseX, mouseY);
            }
            return true;
        }
        return false;
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }
}

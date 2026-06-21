package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.CyclotronMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CyclotronScreen extends AbstractContainerScreen<CyclotronMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_cyclotron.png");

    public CyclotronScreen(CyclotronMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 190;
        imageHeight = 215;
        inventoryLabelX = 15;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerHeight(63);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 168, topPos + 80 - power, 190, 62 - power, 16, power);
        }
        int progress = menu.getProgressWidth(34);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 48, topPos + 27, 206, 0, progress, 34);
            graphics.blit(TEXTURE, leftPos + 172, topPos + 4, 190, 63, 9, 12);
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 49, topPos + 85, 8);
        LegacyFluidGuiRenderer.renderHorizontalTank(graphics, leftPos + 11, topPos + 88, 34, 7,
                menu.getWaterTank());
        LegacyFluidGuiRenderer.renderHorizontalTank(graphics, leftPos + 11, topPos + 97, 34, 7,
                menu.getSpentSteamTank());
        LegacyFluidGuiRenderer.renderHorizontalTank(graphics, leftPos + 107, topPos + 97, 34, 16,
                menu.getAmatTank());
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
        if (isHovering(168, 18, 16, 63, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 168, topPos + 18, 16, 63, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(11, 81, 34, 7, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getWaterTank(),
                    menu.getWaterTank().tooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(11, 90, 34, 7, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSpentSteamTank(),
                    menu.getSpentSteamTank().tooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(107, 81, 34, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getAmatTank(),
                    menu.getAmatTank().tooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(49, 85, 8, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatableWithFallback("desc.gui.upgrade", "Upgrades"),
                    Component.translatableWithFallback("desc.gui.upgrade.speed", "Speed"),
                    Component.translatableWithFallback("desc.gui.upgrade.effectiveness", "Effectiveness"),
                    Component.translatableWithFallback("desc.gui.upgrade.power", "Power-Saving")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

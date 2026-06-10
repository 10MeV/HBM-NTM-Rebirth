package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.hbm.ntm.menu.LaunchPadMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class LaunchPadScreen extends AbstractContainerScreen<LaunchPadMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/weapon/gui_launch_pad_large.png");

    public LaunchPadScreen(LaunchPadMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 236;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int fuel = menu.getFuelState();
        int oxidizer = menu.getOxidizerState();
        if (fuel == 1) {
            graphics.blit(TEXTURE, leftPos + 130, topPos + 23, 192, 0, 6, 8);
        } else if (fuel == -1) {
            graphics.blit(TEXTURE, leftPos + 130, topPos + 23, 198, 0, 6, 8);
        }
        if (oxidizer == 1) {
            graphics.blit(TEXTURE, leftPos + 148, topPos + 23, 192, 0, 6, 8);
        } else if (oxidizer == -1) {
            graphics.blit(TEXTURE, leftPos + 148, topPos + 23, 198, 0, 6, 8);
        }
        if (!menu.getMissileStack().isEmpty()) {
            graphics.blit(TEXTURE, leftPos + 112, topPos + 23, menu.getPower() >= 75_000L ? 192 : 198, 0, 6, 8);
        }

        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 107, topPos + 88 - power, 176, 52 - power, 16, power);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 125, topPos + 36,
                16, 52, menu.getFuelTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 143, topPos + 36,
                16, 52, menu.getOxidizerTankData());

        ItemStack missile = menu.getMissileStack();
        if (!missile.isEmpty()) {
            graphics.renderItem(missile, leftPos + 62, topPos + 105);
        }
        drawStateLabel(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 4, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(107, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 107, topPos + 36, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(125, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getFuelTankData(),
                    menu.getFuelTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(143, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOxidizerTankData(),
                    menu.getOxidizerTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawStateLabel(GuiGraphics graphics) {
        Component text = switch (menu.getState()) {
            case LaunchPadBlockEntity.STATE_LOADING -> Component.translatableWithFallback(
                    "gui.hbm_ntm_rebirth.launch_pad.loading", "Loading...");
            case LaunchPadBlockEntity.STATE_READY -> Component.translatableWithFallback(
                    "gui.hbm_ntm_rebirth.launch_pad.ready", "Ready");
            default -> Component.translatableWithFallback(
                    "gui.hbm_ntm_rebirth.launch_pad.not_ready", "Not ready");
        };
        int color = switch (menu.getState()) {
            case LaunchPadBlockEntity.STATE_LOADING -> 0xFF8000;
            case LaunchPadBlockEntity.STATE_READY -> 0x00FF00;
            default -> 0xFF0000;
        };
        LegacyGuiElements.drawCenteredLabel(graphics, font, text, leftPos + 34, topPos + 103, 56, color);
    }
}

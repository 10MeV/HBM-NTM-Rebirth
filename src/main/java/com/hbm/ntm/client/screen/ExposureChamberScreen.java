package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ExposureChamberBlockEntity;
import com.hbm.ntm.menu.ExposureChamberMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExposureChamberScreen extends AbstractContainerScreen<ExposureChamberMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_exposure_chamber.png");

    public ExposureChamberScreen(ExposureChamberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int progress = menu.getProgressWidth(42);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 36, topPos + 39, 192, 0, progress, 10);
        }

        int particles = menu.getParticleHeight(16);
        if (particles > 0) {
            graphics.blit(TEXTURE, leftPos + 26, topPos + 52 - particles, 192, 26 - particles, 9, particles);
        }

        int power = menu.getPowerBarHeight(34);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 52 - power, 176, 34 - power, 16, power);
        }

        if (menu.hasEnoughPower()) {
            graphics.blit(TEXTURE, leftPos + 156, topPos + 4, 176, 34, 9, 12);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = 70 - font.width(title) / 2;
        graphics.drawString(font, title, titleX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(152, 18, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 18, 16, 34, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(26, 36, 9, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal(menu.getSavedParticles()
                    + " / " + ExposureChamberBlockEntity.MAX_PARTICLES)), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

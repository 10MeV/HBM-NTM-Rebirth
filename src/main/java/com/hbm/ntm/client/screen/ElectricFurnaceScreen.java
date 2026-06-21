package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ElectricFurnaceMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/guielectricfurnace.png");

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int powerHeight = menu.getPowerBarHeight(52);
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 20, topPos + 69 - powerHeight, 200,
                    52 - powerHeight, 16, powerHeight);
        }
        if (menu.isActive()) {
            graphics.blit(TEXTURE, leftPos + 56, topPos + 35, 176, 0, 16, 16);
        }
        graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 17,
                menu.getProgressWidth(24) + 1, 17);
        graphics.blit(TEXTURE, leftPos + 151, topPos + 19, 176, 72, 8, 8);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(20, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font,
                    List.of(Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE")),
                    mouseX, mouseY);
        } else if (isHovering(151, 19, 8, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatableWithFallback("desc.gui.upgrade", "Upgrade"),
                    Component.translatableWithFallback("desc.gui.upgrade.speed", "Speed"),
                    Component.translatableWithFallback("desc.gui.upgrade.power", "Power"),
                    Component.literal("Consumption: " + menu.getConsumption() + " HE/t")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

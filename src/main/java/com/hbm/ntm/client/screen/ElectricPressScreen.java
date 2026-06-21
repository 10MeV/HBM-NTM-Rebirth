package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ElectricPressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectricPressScreen extends AbstractContainerScreen<ElectricPressMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_epress.png");

    public ElectricPressScreen(ElectricPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 17, topPos + 69 - power, 176, 52 - power, 16, power);
        }

        int press = menu.getPressHeight(16);
        if (press > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 35, 192, 0, 18, press);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                leftPos + 17, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        if (isHovering(44, 21, 16, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderUpgradeInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 44, topPos + 21, 16, 16, menu);
        }
        if (isHovering(17, 69, 16, 8, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getConsumption() + " HE/t"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

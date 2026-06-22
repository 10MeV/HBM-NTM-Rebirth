package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.MicrowaveBlockEntity;
import com.hbm.ntm.menu.MicrowaveMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MicrowaveScreen extends AbstractContainerScreen<MicrowaveMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_microwave.png");

    public MicrowaveScreen(MicrowaveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(34);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 51 - power, 176, 34 - power, 16, power);
        }
        int progress = Math.min(menu.getProgressWidth(23), 22);
        graphics.blit(TEXTURE, leftPos + 104, topPos + 34, 192, 0, progress, 16);
        int speed = menu.getSpeed() * 34 / 5;
        graphics.blit(TEXTURE, leftPos + 62, topPos + 60 - speed, 214, 34 - speed, 4, speed);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 17, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 17, 16, 34, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(43, 25, 18, 18, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity(), 0, MicrowaveBlockEntity.CONTROL_SPEED_UP);
            return true;
        }
        if (isHovering(43, 43, 18, 18, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity(), 0, MicrowaveBlockEntity.CONTROL_SPEED_DOWN);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

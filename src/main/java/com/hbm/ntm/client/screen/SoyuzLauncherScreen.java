package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.menu.SoyuzLauncherMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SoyuzLauncherScreen extends AbstractContainerScreen<SoyuzLauncherMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/gui_soyuz.png");

    public SoyuzLauncherScreen(SoyuzLauncherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int power = menu.getPowerBarHeight(34);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 49, topPos + 106 - power, 194, 52 - power, 6, power);
        }
        graphics.blit(TEXTURE, leftPos + 61, topPos + 17,
                176 + (menu.getRocketStatus() > 0 ? 18 : 0), 0, 18, 18);
        if (menu.getDesignatorStatus() > 0) {
            graphics.blit(TEXTURE, leftPos + 61, topPos + 35,
                    176 + (menu.getDesignatorStatus() - 1) * 18, 0, 18, 18);
        }
        graphics.blit(TEXTURE, leftPos + 88, topPos + 17 + menu.getMode() * 18,
                176, 18 + menu.getMode() * 18, 18, 18);
        if (menu.getOrbitalStatus() > 0) {
            graphics.blit(TEXTURE, leftPos + 115, topPos + 35,
                    176 + (menu.getOrbitalStatus() - 1) * 18, 0, 18, 18);
        }
        if (menu.getSatelliteStatus() > 0) {
            graphics.blit(TEXTURE, leftPos + 115, topPos + 17,
                    176 + (menu.getSatelliteStatus() - 1) * 18, 0, 18, 18);
        }
        if (menu.isStarting()) {
            graphics.blit(TEXTURE, leftPos + 151, topPos + 17, 176, 54, 18, 18);
        }

        graphics.blit(TEXTURE, leftPos + 13, topPos + 23, menu.hasFuel() ? 212 : 218, 0, 6, 8);
        graphics.blit(TEXTURE, leftPos + 31, topPos + 23, menu.hasOxygen() ? 212 : 218, 0, 6, 8);
        graphics.blit(TEXTURE, leftPos + 49, topPos + 59, menu.hasPower() ? 212 : 218, 0, 6, 8);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8, topPos + 88,
                16, 52, menu.getKeroseneTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 26, topPos + 88,
                16, 52, menu.getOxygenTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        int secs = menu.getCountdown() / 20;
        int cents = (menu.getCountdown() % 20) * 5;
        String timer = String.format("%02d:%02d", secs, cents);
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, timer, 307, 75, 0xFF0000, false);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getKeroseneTankData(),
                    menu.getKeroseneTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(26, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOxygenTankData(),
                    menu.getOxygenTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(49, 72, 6, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 49, topPos + 72, 6, 34, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(88, 17, 18, 18, mouseX, mouseY)) {
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(),
                    SoyuzLauncherBlockEntity.MODE_SATELLITE, SoyuzLauncherBlockEntity.CONTROL_MODE);
            return true;
        }
        if (isHovering(88, 35, 18, 18, mouseX, mouseY)) {
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(),
                    SoyuzLauncherBlockEntity.MODE_CARGO, SoyuzLauncherBlockEntity.CONTROL_MODE);
            return true;
        }
        if (isHovering(151, 17, 18, 18, mouseX, mouseY)) {
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(), 0,
                    SoyuzLauncherBlockEntity.CONTROL_START);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

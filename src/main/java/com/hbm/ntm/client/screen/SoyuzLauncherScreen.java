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

        // GUISoyuzLauncher did not use a conventional zero-padded fractional
        // formatter: its single-character fraction has a zero *appended*.
        // Keep that visible source quirk (e.g. countdown 1 -> "00:50").
        String secs = "" + menu.getCountdown() / 20;
        String cents = "" + (menu.getCountdown() % 20) * 5;
        if (secs.length() == 1) {
            secs = "0" + secs;
        }
        if (cents.length() == 1) {
            cents += "0";
        }
        String timer = secs + ":" + cents;
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, timer, 307, 75, 0xFF0000, false);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        // GUISoyuzLauncher uses GuiInfoContainer#drawCustomInfoStat for these
        // six source-defined hints.  Unlike the linker side panels, each old
        // call deliberately anchors its tooltip to the live cursor.
        LegacyGuiElements.renderCustomInfoTextStat(graphics, font, mouseX, mouseY,
                leftPos + 43, topPos + 17, 18, 18, mouseX, mouseY, "The Soyuz goes here");
        LegacyGuiElements.renderCustomInfoTextStat(graphics, font, mouseX, mouseY,
                leftPos + 43, topPos + 35, 18, 18, mouseX, mouseY, "Designator only for CARGO MODE");
        LegacyGuiElements.renderCustomInfoTextStat(graphics, font, mouseX, mouseY,
                leftPos + 133, topPos + 17, 18, 18, mouseX, mouseY, "The payload for SATELLITE MODE");
        LegacyGuiElements.renderCustomInfoTextStat(graphics, font, mouseX, mouseY,
                leftPos + 133, topPos + 35, 18, 18, mouseX, mouseY, "The orbital module for special payloads");
        LegacyGuiElements.renderCustomInfoTextStat(graphics, font, mouseX, mouseY,
                leftPos + 88, topPos + 17, 18, 18, mouseX, mouseY, "SATELLITE MODE");
        LegacyGuiElements.renderCustomInfoTextStat(graphics, font, mouseX, mouseY,
                leftPos + 88, topPos + 35, 18, 18, mouseX, mouseY, "CARGO MODE");
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 8, topPos + 36, 16, 52)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getKeroseneTankData(),
                    menu.getKeroseneTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 26, topPos + 36, 16, 52)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOxygenTankData(),
                    menu.getOxygenTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 49, topPos + 72, 6, 34)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 49, topPos + 72, 6, 34, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 88, 17, 18, 18)) {
            LegacyGuiElements.playClickSound();
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(),
                    SoyuzLauncherBlockEntity.MODE_SATELLITE, SoyuzLauncherBlockEntity.CONTROL_MODE);
            return true;
        }
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 88, 35, 18, 18)) {
            LegacyGuiElements.playClickSound();
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(),
                    SoyuzLauncherBlockEntity.MODE_CARGO, SoyuzLauncherBlockEntity.CONTROL_MODE);
            return true;
        }
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 151, 17, 18, 18)) {
            LegacyGuiElements.playClickSound();
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(), 0,
                    SoyuzLauncherBlockEntity.CONTROL_START);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

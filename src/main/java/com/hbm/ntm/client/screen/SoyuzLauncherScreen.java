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
        imageWidth = 194;
        imageHeight = 244;
        inventoryLabelX = 17;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 134, topPos + 96 - power, 194, 52 - power, 16, power);
        }
        graphics.blit(TEXTURE, leftPos + 97, topPos + 79,
                210 + (menu.getRocketStatus() > 0 ? 18 : 0), 8, 18, 18);
        if (menu.getDesignatorStatus() > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 79,
                    210 + (menu.getDesignatorStatus() - 1) * 18, 8, 18, 18);
        }
        graphics.blit(TEXTURE, leftPos + 97 - menu.getMode() * 18, topPos + 52,
                228 - menu.getMode() * 18, 26, 18, 18);
        if (menu.getOrbitalStatus() > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 25,
                    210 + (menu.getOrbitalStatus() - 1) * 18, 8, 18, 18);
        }
        if (menu.getSatelliteStatus() > 0) {
            graphics.blit(TEXTURE, leftPos + 97, topPos + 25,
                    210 + (menu.getSatelliteStatus() - 1) * 18, 8, 18, 18);
        }
        if (menu.isStarting()) {
            graphics.blit(TEXTURE, leftPos + 88, topPos + 97, 210, 44, 18, 18);
        }

        graphics.blit(TEXTURE, leftPos + 157, topPos + 31, menu.hasFuel() ? 210 : 216, 0, 6, 8);
        graphics.blit(TEXTURE, leftPos + 175, topPos + 31, menu.hasOxygen() ? 210 : 216, 0, 6, 8);
        graphics.blit(TEXTURE, leftPos + 139, topPos + 31, menu.hasPower() ? 210 : 216, 0, 6, 8);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 152, topPos + 96,
                16, 52, menu.getKeroseneTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 170, topPos + 96,
                16, 52, menu.getOxygenTankData());
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 53, 2);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 4, 0xFFFFFF, false);
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
        graphics.drawString(font, timer, 85, 121, 0xFF0000, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        LegacyGuiElements.renderCustomInfoStat(graphics, font, mouseX, mouseY,
                leftPos - 16, topPos + 53, 16, 16, leftPos - 8, topPos + 69,
                Component.translatable("container.hbm_ntm_rebirth.soyuz_launcher.desc.0"),
                Component.translatable("container.hbm_ntm_rebirth.soyuz_launcher.desc.1"));
        LegacyGuiElements.renderCustomInfoStat(graphics, font, mouseX, mouseY,
                leftPos + 79, topPos + 52, 18, 18, mouseX, mouseY,
                Component.translatable("container.hbm_ntm_rebirth.soyuz_launcher.cargo"));
        LegacyGuiElements.renderCustomInfoStat(graphics, font, mouseX, mouseY,
                leftPos + 97, topPos + 52, 18, 18, mouseX, mouseY,
                Component.translatable("container.hbm_ntm_rebirth.soyuz_launcher.satellite"));
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 152, topPos + 44, 16, 52)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getKeroseneTankData(),
                    menu.getKeroseneTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 170, topPos + 44, 16, 52)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOxygenTankData(),
                    menu.getOxygenTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 134, topPos + 44, 16, 52)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 134, topPos + 44, 16, 52, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 97, 52, 18, 18)) {
            LegacyGuiElements.playClickSound();
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(),
                    SoyuzLauncherBlockEntity.MODE_SATELLITE, SoyuzLauncherBlockEntity.CONTROL_MODE);
            return true;
        }
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 79, 52, 18, 18)) {
            LegacyGuiElements.playClickSound();
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(),
                    SoyuzLauncherBlockEntity.MODE_CARGO, SoyuzLauncherBlockEntity.CONTROL_MODE);
            return true;
        }
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 88, 97, 18, 18)) {
            LegacyGuiElements.playClickSound();
            ModMessages.sendAuxButton(menu.getBlockEntity().getBlockPos(), 0,
                    SoyuzLauncherBlockEntity.CONTROL_START);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

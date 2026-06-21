package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FelBlockEntity;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.menu.FelMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class FelScreen extends AbstractContainerScreen<FelMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_fel.png");

    public FelScreen(FelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 203;
        imageHeight = 169;
        titleLabelX = 90;
        titleLabelY = 7;
        inventoryLabelY = imageHeight - 98;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 142, topPos + 41, 203, 0, 29, 17);
        }
        int power = menu.getPowerHeight(114);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 182, topPos + 27 + 113 - power, 203, 17 + 113 - power, 16, power);
        }
        if (menu.isBeamActive()) {
            int color = guiColor(LaserWavelength.byOrdinal(menu.getModeOrdinal()));
            graphics.fill(leftPos + 113, topPos + 30, leftPos + 136, topPos + 33, 0xFF000000 | color);
            graphics.fill(0, topPos + 30, leftPos + 5, topPos + 33, 0xFF000000 | color);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = 90 + imageWidth / 2 - font.width(title) / 2;
        graphics.drawString(font, title, titleX, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        if (menu.isOn()) {
            Component state = menu.isMissingValidSilex()
                    ? Component.literal("ERR.").withStyle(ChatFormatting.RED)
                    : Component.literal("LIVE").withStyle(ChatFormatting.GREEN);
            graphics.drawString(font, state, 54 + imageWidth / 2 - font.width(title) / 2, 9, 0xFFFFFF, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(182, 27, 16, 113, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 182, topPos + 27, 16, 113, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(142, 41, 29, 17, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity(), 0, FelBlockEntity.CONTROL_POWER);
            LegacyGuiElements.playClickSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static int guiColor(LaserWavelength wavelength) {
        if (wavelength == LaserWavelength.VISIBLE) {
            long gameTime = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
            return Mth.hsvToRgb(Mth.frac(gameTime / 50.0F), 0.5F, 1.0F);
        }
        return wavelength.guiColor();
    }
}

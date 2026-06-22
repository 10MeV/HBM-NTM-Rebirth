package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.ZirnoxReactorMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.registry.ModSounds;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ZirnoxReactorScreen extends AbstractContainerScreen<ZirnoxReactorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_zirnox.png");

    public ZirnoxReactorScreen(ZirnoxReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 203;
        imageHeight = 256;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawVerticalGauge(graphics, 160, 108, 18, 12, menu.getSteamTank().scaledFill(6), 238, 0);
        drawVerticalGauge(graphics, 142, 108, 18, 12, menu.getCarbonDioxideTank().scaledFill(6), 238, 0);
        drawVerticalGauge(graphics, 178, 108, 18, 12, menu.getWaterTank().scaledFill(6), 238, 0);
        drawVerticalGauge(graphics, 160, 33, 18, 17, menu.getHeatScaled(12), 220, 0, 18);
        drawVerticalGauge(graphics, 178, 33, 18, 17, menu.getPressureScaled(12), 220, 0, 18);
        if (menu.isOn()) {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    graphics.blit(TEXTURE, leftPos + 7 + 36 * x, topPos + 15 + 36 * y, 238, 238, 18, 18);
                }
            }
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    graphics.blit(TEXTURE, leftPos + 25 + 36 * x, topPos + 33 + 36 * y, 238, 238, 18, 18);
                }
            }
            graphics.blit(TEXTURE, leftPos + 142, topPos + 15, 220, 238, 18, 18);
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 36, 2);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 52, 3);
        if (menu.getWaterTank().fill() <= 0) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 68, 6);
        }
        if (menu.getCarbonDioxideTank().fill() <= 4000) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 84, 6);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getSteamTank(), 160, 108, 18, 12);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getCarbonDioxideTank(), 142, 108, 18, 12);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getWaterTank(), 178, 108, 18, 12);
        if (isHovering(160, 33, 18, 17, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Temperature: " + menu.getTemperatureDisplay() + "\u00b0C"),
                    mouseX, mouseY);
        } else if (isHovering(178, 33, 18, 17, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Pressure: " + menu.getPressureDisplay() + " bar"),
                    mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 36, 16, 16)) {
            LegacyGuiElements.renderTooltip(graphics, font, splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.coolant",
                    "\u00a73Coolant\u00a7r$CO2 transfers heat from the core to the water."
                            + "$This will boil it into super dense steam."
                            + "$The efficiency of cooling and steam production$is based on pressure.")),
                    mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 52, 16, 16)) {
            LegacyGuiElements.renderTooltip(graphics, font, splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.pressure",
                    "\u00a76Pressure\u00a7r$Pressure can be reduced by venting CO2."
                            + "$However, too low a pressure, and cooling$efficiency and steam production will be reduced."
                            + "$Look out for meltdowns!")), mouseX, mouseY);
        } else if (menu.getWaterTank().fill() <= 0
                && LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 68, 16, 16)) {
            LegacyGuiElements.renderTooltip(graphics, font, splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.warning1",
                    "\u00a7cError:\u00a7r Water is required for$the reactor to function properly!")),
                    mouseX, mouseY);
        } else if (menu.getCarbonDioxideTank().fill() < 4000
                && LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 84, 16, 16)) {
            LegacyGuiElements.renderTooltip(graphics, font, splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.warning2",
                    "\u00a7cError:\u00a7r CO2 is required for$the reactor to function properly!")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (leftPos + 144 <= mouseX && mouseX < leftPos + 158 && topPos + 35 < mouseY && mouseY <= topPos + 49) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("control", true);
            ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
            playClick();
            return true;
        }
        if (leftPos + 151 <= mouseX && mouseX < leftPos + 187 && topPos + 51 < mouseY && mouseY <= topPos + 87) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("vent", true);
            ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
            playClick();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawVerticalGauge(GuiGraphics graphics, int x, int y, int width, int height, int scaled,
            int textureX, int textureY) {
        drawVerticalGauge(graphics, x, y, width, height, scaled, textureX, textureY, height);
    }

    private void drawVerticalGauge(GuiGraphics graphics, int x, int y, int width, int height, int scaled,
            int textureX, int textureY, int textureStep) {
        graphics.blit(TEXTURE, leftPos + x, topPos + y, textureX, textureY + textureStep * scaled, width, height);
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, HbmFluidGuiHelper.TankData tank,
            int x, int y, int width, int height) {
        if (isHovering(x, y, width, height, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, tank.tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
        }
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BLOCK_RBMK_AZ5_COVER.get(), 0.5F));
        }
    }

    private static List<Component> splitLegacyInfo(Component text) {
        return Arrays.stream(text.getString().split("\\$"))
                .map(line -> (Component) Component.literal(line))
                .toList();
    }
}

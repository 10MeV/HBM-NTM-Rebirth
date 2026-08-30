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
        inventoryLabelY = imageHeight - 96;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyGuiElements.drawSmoothLinearGauge(graphics, leftPos + 162, topPos + 114,
                menu.getSteamTank().fill() / (double) menu.getSteamTank().capacity(), 2.0D, 5.0D, 0.75D,
                14.0D, 0.0F, 0x7F0000);
        LegacyGuiElements.drawSmoothLinearGauge(graphics, leftPos + 144, topPos + 114,
                menu.getCarbonDioxideTank().fill() / (double) menu.getCarbonDioxideTank().capacity(),
                2.0D, 5.0D, 0.75D, 14.0D, 0.0F, 0x7F0000);
        LegacyGuiElements.drawSmoothLinearGauge(graphics, leftPos + 180, topPos + 114,
                menu.getWaterTank().fill() / (double) menu.getWaterTank().capacity(), 2.0D, 5.0D, 0.75D,
                14.0D, 0.0F, 0x7F0000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 169, topPos + 42,
                menu.getHeat() / 100_000.0D, 5.0D, 2.0D, 1.0D, 0x7F0000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 187, topPos + 42,
                menu.getPressure() / 100_000.0D, 5.0D, 2.0D, 1.0D, 0x7F0000);
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
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 160, 33, 18, 17)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(
                    Component.literal("Temperature:"),
                    Component.literal("   " + menu.getTemperatureDisplay() + "\u00b0C")), mouseX, mouseY);
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 178, 33, 18, 17)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(
                    Component.literal("Pressure:"),
                    Component.literal("   " + menu.getPressureDisplay() + " bar")), mouseX, mouseY);
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 36, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 16, topPos + 36, 16, 16, leftPos - 8, topPos + 52,
                    splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.coolant",
                    "\u00a73Coolant\u00a7r$CO2 transfers heat from the core to the water."
                            + "$This will boil it into super dense steam."
                            + "$The efficiency of cooling and steam production$is based on pressure.")));
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 52, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 16, topPos + 52, 16, 16, leftPos - 8, topPos + 68,
                    splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.pressure",
                    "\u00a76Pressure\u00a7r$Pressure can be reduced by venting CO2."
                            + "$However, too low a pressure, and cooling$efficiency and steam production will be reduced."
                            + "$Look out for meltdowns!")));
        } else if (menu.getWaterTank().fill() <= 0
                && LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 68, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 16, topPos + 68, 16, 16, leftPos - 8, topPos + 84,
                    splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.warning1",
                    "\u00a7cError:\u00a7r Water is required for$the reactor to function properly!")));
        } else if (menu.getCarbonDioxideTank().fill() < 4000
                && LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 84, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 16, topPos + 84, 16, 16, leftPos - 8, topPos + 100,
                    splitLegacyInfo(Component.translatableWithFallback(
                    "desc.gui.zirnox.warning2",
                    "\u00a7cError:\u00a7r CO2 is required for$the reactor to function properly!")));
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
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
        return handled;
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, HbmFluidGuiHelper.TankData tank,
            int x, int y, int width, int height) {
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, tank,
                    tank.tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
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

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.MiningLaserMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MiningLaserScreen extends AbstractContainerScreen<MiningLaserMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_laser_miner.png");

    public MiningLaserScreen(MiningLaserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        titleLabelY = 4;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 61, topPos + 17, 200, 0, 18, 18);
        }
        int power = menu.getPowerBarHeight(88);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 106 - power, 176, 88 - power, 16, power);
        }
        int progress = menu.getProgressBarHeight(34);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 66, topPos + 36, 192, 0, 8, progress);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 35, topPos + 124, 7, 52, menu.getOilTank());
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 87, topPos + 31, 8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        String width = Integer.toString(menu.getWidth());
        graphics.drawString(font, width, 43 - font.width(width) / 2, 26, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 18, 16, 88, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 18, 16, 88, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(35, 72, 7, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOilTank(),
                    menu.oilTooltip(false), mouseX, mouseY);
        } else if (isHovering(87, 31, 8, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("Acceptable upgrades:"),
                    Component.literal(" -Speed (stacks to level 12)"),
                    Component.literal(" -Effectiveness (stacks to level 12)"),
                    Component.literal(" -Overdrive (stacks to level 9)"),
                    Component.literal(" -Power (stacks to level 12)"),
                    Component.literal(" -Fortune (stacks to level 3)"),
                    Component.literal(" -Smelter"),
                    Component.literal(" -Nullifier"),
                    Component.literal(" -Screaming Scientist")), mouseX, mouseY);
        } else if (isHovering(61, 17, 18, 18, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal(menu.isOn() ? "On" : "Off"),
                    Component.literal("Consumption: " + menu.getConsumption() + " HE/t"),
                    Component.literal(menu.isRedstonePowered() ? "Redstone stopped" : "Redstone clear")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(61, 17, 18, 18, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

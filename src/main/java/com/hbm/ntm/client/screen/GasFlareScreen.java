package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.GasFlareBlockEntity;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.menu.GasFlareMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GasFlareScreen extends AbstractContainerScreen<GasFlareMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/generators/gui_flare_stack.png");

    public GasFlareScreen(GasFlareMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 203;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 143, topPos + 69 - power, 176, 94 - power, 16, power);
        }
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 15, 176, 0, 35, 10);
        }
        if (menu.doesBurn()) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 49, 176, 10, 35, 14);
        }
        if (menu.isOn() && menu.doesBurn() && menu.getTankData() != null
                && !menu.getTankData().isEmpty()
                && menu.getTankData().type().hasTrait(FlammableFluidTrait.class)) {
            graphics.blit(TEXTURE, leftPos + 88, topPos + 29, 176, 24, 18, 18);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 35, topPos + 69, 16, 52,
                menu.getTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(79, 16, 35, 10, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal("Valve"),
                    Component.literal(menu.isOn() ? "Open" : "Closed"))), mouseX, mouseY);
        } else if (isHovering(79, 50, 35, 14, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal("Ignition"),
                    Component.literal(menu.doesBurn() ? "Burn" : "Vent"))), mouseX, mouseY);
        } else if (isHovering(35, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(143, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 143, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(80, 71, 36, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(menu.getFluidUsed() + " mB/t"),
                    Component.literal(menu.getOutput() + " HE/t"))), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(89, 16, 16, 10, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, GasFlareBlockEntity.CONTROL_VALVE);
            return true;
        }
        if (isHovering(89, 50, 16, 14, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, GasFlareBlockEntity.CONTROL_BURN);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.OilburnerBlockEntity;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.menu.OilburnerMenu;
import com.hbm.ntm.network.ModMessages;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OilburnerScreen extends AbstractContainerScreen<OilburnerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_oilburner.png");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public OilburnerScreen(OilburnerMenu menu, Inventory inventory, Component title) {
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
        int heat = menu.heatBarHeight();
        if (heat > 0) {
            graphics.blit(TEXTURE, leftPos + 116, topPos + 69 - heat, 194, 52 - heat, 16, heat);
        }
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 70, topPos + 54, 210, 0, 35, 14);
            if (menu.getTankData() != null
                    && !menu.getTankData().isEmpty()
                    && menu.getTankData().type().hasTrait(FlammableFluidTrait.class)) {
                graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 0, 18, 18);
            }
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 44, topPos + 69, 16, 52,
                menu.getTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(116, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(Component.literal(NUMBER_FORMAT.format(menu.getHeatEnergy())
                    + " / " + NUMBER_FORMAT.format(OilburnerBlockEntity.MAX_HEAT) + " TU"))), mouseX, mouseY);
        } else if (isHovering(79, 34, 18, 18, mouseX, mouseY)
                && menu.getTankData() != null
                && menu.getTankData().type().hasTrait(FlammableFluidTrait.class)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(menu.getSetting() + " mB/t"),
                    Component.literal(NUMBER_FORMAT.format(menu.getCurrentHeatOutputPerTick()) + " TU/t"))),
                    mouseX, mouseY);
        } else if (isHovering(44, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(80, 54, 16, 14, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal("Burner"),
                    Component.literal(menu.isOn() ? "On" : "Off"))), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(80, 54, 16, 14, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, OilburnerBlockEntity.CONTROL_TOGGLE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}

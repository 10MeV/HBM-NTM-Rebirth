package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.WoodBurnerBlockEntity;
import com.hbm.ntm.menu.WoodBurnerMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.recipe.WoodBurnerRecipeRuntime;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WoodBurnerScreen extends AbstractContainerScreen<WoodBurnerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/generators/gui_wood_burner_alt.png");

    public WoodBurnerScreen(WoodBurnerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isLiquidBurn()) {
            graphics.blit(TEXTURE, leftPos + 16, topPos + 17, 176, 52, 60, 54);
            graphics.blit(TEXTURE, leftPos + 79, topPos + 17, 176, 106, 36, 54);
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 80, topPos + 70, 16, 52,
                    menu.getTankData());
        }
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 53, topPos + 17, 196, 0, 16, 15);
        }
        int power = menu.getPowerBarHeight(34);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 143, topPos + 52 - power, 176, 52 - power, 16, power);
        }
        int burn = menu.getBurnBarHeight(52);
        if (burn > 0) {
            graphics.blit(TEXTURE, leftPos + 17, topPos + 70 - burn, 192, 52 - burn, 4, burn);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX - font.width(title) / 2, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(143, 18, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 143, topPos + 18, 16, 34, menu.getPower(), menu.getMaxPower());
        } else if (menu.isLiquidBurn() && isHovering(80, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (!menu.isLiquidBurn() && isHovering(16, 17, 8, 54, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal((menu.getBurnTime() / 20) + "s").getVisualOrderText()), mouseX, mouseY);
        } else if (menu.getCarried().isEmpty()
                && menu.getBlockEntity().getItems().getStackInSlot(WoodBurnerBlockEntity.SLOT_FUEL).isEmpty()
                && isHovering(26, 18, 16, 16, mouseX, mouseY)) {
            List<Component> bonuses = WoodBurnerRecipeRuntime.burnModule().getDescription().stream()
                    .map(text -> (Component) Component.literal(text))
                    .toList();
            if (!bonuses.isEmpty()) {
                graphics.renderComponentTooltip(font, bonuses, mouseX, mouseY);
            }
        } else if (isHovering(53, 17, 16, 15, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal(menu.isOn() ? "ON" : "OFF").getVisualOrderText()), mouseX, mouseY);
        } else if (isHovering(46, 37, 30, 14, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal(menu.isLiquidBurn() ? "Liquid fuel" : "Solid fuel").getVisualOrderText()),
                    mouseX, mouseY);
        } else if (menu.getPowerGen() > 0 && isHovering(53, 52, 24, 12, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                    Component.literal(menu.getPowerGen() + " HE/t").getVisualOrderText(),
                    Component.literal((menu.getPowerGen() * 20L) + " HE/s").getVisualOrderText()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(53, 17, 16, 15, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, WoodBurnerBlockEntity.CONTROL_TOGGLE);
            return true;
        }
        if (isHovering(46, 37, 30, 14, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, WoodBurnerBlockEntity.CONTROL_SWITCH);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

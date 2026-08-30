package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.LegacyFurnaceBlockEntity;
import com.hbm.ntm.menu.LegacyFurnaceMenu;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LegacyFurnaceScreen extends AbstractContainerScreen<LegacyFurnaceMenu> {
    private static final ResourceLocation IRON =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_furnace_iron.png");
    private static final ResourceLocation STEEL =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_furnace_steel.png");

    public LegacyFurnaceScreen(LegacyFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (menu.getBlockEntity().kind() == LegacyFurnaceBlockEntity.Kind.STEEL) {
            renderSteel(graphics);
        } else {
            renderIron(graphics);
        }
    }

    private void renderIron(GuiGraphics graphics) {
        graphics.blit(IRON, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getProgressWidth(70);
        if (progress > 0) {
            graphics.blit(IRON, leftPos + 53, topPos + 36, 176, 18, progress, 5);
        }
        int burn = menu.getBurnWidth(70);
        if (burn > 0) {
            graphics.blit(IRON, leftPos + 53, topPos + 45, 176, 23, burn, 5);
        }
        if (menu.wasOn()) {
            graphics.blit(IRON, leftPos + 70, topPos + 16, 176, 0, 18, 18);
        }
    }

    private void renderSteel(GuiGraphics graphics) {
        graphics.blit(STEEL, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int heat = menu.getHeatBarHeight(48);
        if (heat > 0) {
            graphics.blit(STEEL, leftPos + 152, topPos + 67 - heat, 176, 76 - heat, 7, heat);
        }
        for (int i = 0; i < 3; i++) {
            int progress = menu.getSteelProgressWidth(i, 69);
            int bonus = menu.getSteelBonusWidth(i, 69);
            if (progress > 0) {
                graphics.blit(STEEL, leftPos + 54, topPos + 18 + i * 18, 176, 18, progress, 5);
            }
            if (bonus > 0) {
                graphics.blit(STEEL, leftPos + 54, topPos + 27 + i * 18, 176, 23, bonus, 5);
            }
            if (menu.wasOn()) {
                graphics.blit(STEEL, leftPos + 16, topPos + 16 + i * 18, 176, 0, 18, 18);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (menu.getBlockEntity().kind() == LegacyFurnaceBlockEntity.Kind.IRON) {
            renderIronTooltips(graphics, mouseX, mouseY);
        } else {
            renderSteelTooltips(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderIronTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && renderEmptyIronFuelSlotTooltip(graphics, mouseX, mouseY)) {
            return;
        }
        if (isLegacyHovering(52, 35, 71, 7, mouseX, mouseY)) {
            int percent = menu.getIronProgress() * 100 / Math.max(menu.getIronProcessingTime(), 1);
            graphics.renderComponentTooltip(font, List.of(Component.literal(percent + "%")), mouseX, mouseY);
        } else if (isLegacyHovering(52, 44, 71, 7, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal((menu.getBurnTime() / 20) + "s")),
                    mouseX, mouseY);
        }
    }

    private boolean renderEmptyIronFuelSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int slot = 1; slot < 3; slot++) {
            if (!isHovering(menu.getSlot(slot).x, menu.getSlot(slot).y, 16, 16, mouseX, mouseY)
                    || menu.getSlot(slot).hasItem()) {
                continue;
            }
            List<Component> description = menu.getBlockEntity().getBurnTimeDescription().stream()
                    .map(text -> (Component) Component.literal(text)).toList();
            if (!description.isEmpty()) {
                graphics.renderComponentTooltip(font, description, mouseX, mouseY);
            }
            return true;
        }
        return false;
    }

    private void renderSteelTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int lane = 0; lane < 3; lane++) {
            if (isLegacyHovering(53, 17 + 18 * lane, 70, 7, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, List.of(Component.literal(String.format(Locale.US, "%,d / %,dTU",
                        menu.getSteelProgress(lane), menu.getSteelProcessTime()))), mouseX, mouseY);
                return;
            }
            if (isLegacyHovering(53, 26 + 18 * lane, 70, 7, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, List.of(Component.literal("Bonus: " + menu.getSteelBonus(lane)
                        + "%")), mouseX, mouseY);
                return;
            }
        }
        if (isLegacyHovering(151, 18, 9, 50, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal(String.format(Locale.US, "%,d / %,dTU",
                    menu.getHeat(), menu.getSteelMaxHeat()))), mouseX, mouseY);
        }
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ElectrolyserBlockEntity;
import com.hbm.ntm.menu.ElectrolyserMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectrolyserScreen extends AbstractContainerScreen<ElectrolyserMenu> {
    private static final ResourceLocation FLUID_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_electrolyser_fluid.png");
    private static final ResourceLocation METAL_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_electrolyser_metal.png");

    public ElectrolyserScreen(ElectrolyserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 210;
        imageHeight = 204;
        titleLabelX = imageWidth / 2 - 16;
        titleLabelY = 7;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = menu.isMetalMode() ? METAL_TEXTURE : FLUID_TEXTURE;
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(89);
        if (power > 0) {
            graphics.blit(texture, leftPos + 186, topPos + 107 - power, 210, 89 - power, 16, power);
        }
        if (menu.isMetalMode()) {
            renderMaterial(graphics, 58, 18, menu.getLeftAmount(), menu.getLeftColor());
            renderMaterial(graphics, 96, 18, menu.getRightAmount(), menu.getRightColor());
            if (menu.getPower() >= menu.getUsageOre()) {
                graphics.blit(texture, leftPos + 190, topPos + 4, 226, 25, 9, 12);
            }
            int progress = menu.getOreProgressHeight(26);
            if (progress > 0) {
                graphics.blit(texture, leftPos + 7, topPos + 71 - progress, 226, 25 - progress, 22, progress);
            }
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 36, topPos + 70, 16, 52, menu.tank(3));
        } else {
            if (menu.getPower() >= menu.getUsageFluid()) {
                graphics.blit(texture, leftPos + 190, topPos + 4, 226, 40, 9, 12);
            }
            int progress = menu.getFluidProgressHeight(41);
            if (progress > 0) {
                graphics.blit(texture, leftPos + 62, topPos + 26, 226, 0, 12, progress);
            }
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 42, topPos + 70, 16, 52, menu.tank(0));
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 96, topPos + 70, 16, 52, menu.tank(1));
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 116, topPos + 70, 16, 52, menu.tank(2));
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
        if (menu.isMetalMode()) {
            if (isHovering(36, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.tank(3),
                        menu.tankTooltip(3, hasShiftDown()), mouseX, mouseY);
            } else if (isHovering(58, 18, 34, 42, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font,
                        List.of(menu.materialTooltip(menu.getLeftMaterialId(), menu.getLeftAmount(),
                                hasShiftDown()).withStyle(menu.getLeftAmount() > 0
                                        ? ChatFormatting.YELLOW : ChatFormatting.RED)),
                        mouseX, mouseY);
            } else if (isHovering(96, 18, 34, 42, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font,
                        List.of(menu.materialTooltip(menu.getRightMaterialId(), menu.getRightAmount(),
                                hasShiftDown()).withStyle(menu.getRightAmount() > 0
                                        ? ChatFormatting.YELLOW : ChatFormatting.RED)),
                        mouseX, mouseY);
            }
        } else {
            if (isHovering(42, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.tank(0),
                        menu.tankTooltip(0, hasShiftDown()), mouseX, mouseY);
            } else if (isHovering(96, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.tank(1),
                        menu.tankTooltip(1, hasShiftDown()), mouseX, mouseY);
            } else if (isHovering(116, 18, 16, 52, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.tank(2),
                        menu.tankTooltip(2, hasShiftDown()), mouseX, mouseY);
            }
        }
        if (isHovering(186, 18, 16, 89, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 186, topPos + 18, 16, 89, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(8, 82, 54, 12, mouseX, mouseY)) {
            int control = menu.isMetalMode()
                    ? ElectrolyserBlockEntity.CONTROL_FLUID_MODE
                    : ElectrolyserBlockEntity.CONTROL_METAL_MODE;
            ModMessages.sendLegacyButton(menu.getBlockEntity(), 0, control);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderMaterial(GuiGraphics graphics, int x, int y, int amount, int color) {
        int height = menu.getMaterialHeight(amount, 42);
        if (height <= 0) {
            return;
        }
        graphics.setColor(((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F, 1.0F);
        graphics.blit(METAL_TEXTURE, leftPos + x, topPos + y + 42 - height, 210, 131 - height, 34, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

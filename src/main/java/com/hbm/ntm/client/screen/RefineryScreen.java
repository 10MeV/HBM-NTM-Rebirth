package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.RefineryRecipe;
import com.hbm.ntm.menu.RefineryMenu;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_refinery.png");
    public RefineryScreen(RefineryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 182;
        imageHeight = 240;
        titleLabelY = 6;
        inventoryLabelX = 11;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderEnergy(graphics);
        renderInputTank(graphics);
        renderPipes(graphics);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 64, topPos + 88, 16, 52, menu.getTank(1));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 82, topPos + 88, 16, 52, menu.getTank(2));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 100, topPos + 88, 16, 52, menu.getTank(3));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 118, topPos + 88, 16, 52, menu.getTank(4));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 73 - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, 0, 12, 17, 16, 70);
        renderTankTooltip(graphics, mouseX, mouseY, 1, 64, 35, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 2, 82, 35, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 3, 100, 35, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 4, 118, 35, 16, 52);
        if (isLegacyHovering(158, 18, 16, 88, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 158, topPos + 18, 16, 88, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderEnergy(GuiGraphics graphics) {
        int height = menu.getPowerBarHeight(88);
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 158, topPos + 106 - height, 182, 88 - height, 16, height);
        }
    }

    private void renderInputTank(GuiGraphics graphics) {
        HbmFluidGuiHelper.TankData tank = menu.getTank(0);
        if (tank == null || tank.isEmpty()) {
            return;
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 12, topPos + 88, 16, 70, tank);
    }

    private void renderPipes(GuiGraphics graphics) {
        HbmFluidGuiHelper.TankData input = menu.getTank(0);
        RefineryRecipe recipe = input == null ? null
                : LegacyOilFluidRecipes.getRefinery(Minecraft.getInstance().level, input.type());
        if (recipe == null) {
            graphics.blit(TEXTURE, leftPos + 30, topPos + 30, 0, 248, 43, 4);
            graphics.blit(TEXTURE, leftPos + 30, topPos + 26, 0, 240, 61, 8);
            graphics.blit(TEXTURE, leftPos + 30, topPos + 22, 61, 240, 79, 12);
            graphics.blit(TEXTURE, leftPos + 30, topPos + 18, 140, 240, 97, 16);
            return;
        }
        HbmFluidStack[] outputs = recipe.outputs();
        blitTinted(graphics, outputs[0], leftPos + 30, topPos + 30, 0, 248, 43, 4);
        blitTinted(graphics, outputs[1], leftPos + 30, topPos + 26, 0, 240, 61, 8);
        blitTinted(graphics, outputs[2], leftPos + 30, topPos + 22, 61, 240, 79, 12);
        blitTinted(graphics, outputs[3], leftPos + 30, topPos + 18, 140, 240, 97, 16);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }

    private void blitTinted(GuiGraphics graphics, HbmFluidStack fluid, int x, int y, int u, int v, int width,
            int height) {
        int color = fluid.type().getColor();
        graphics.setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, 1.0F);
        graphics.blit(TEXTURE, x, y, u, v, width, height);
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, int index, int x, int y,
            int width, int height) {
        if (!isLegacyHovering(x, y, width, height, mouseX, mouseY)) {
            return;
        }
        List<Component> tooltip = menu.getTankTooltip(index, hasShiftDown());
        LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(index), tooltip, mouseX, mouseY);
    }
}

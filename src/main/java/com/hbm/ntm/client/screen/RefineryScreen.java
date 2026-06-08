package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.RefineryRecipe;
import com.hbm.ntm.menu.RefineryMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_refinery.png");
    private static final int TEX_WIDTH = 350;
    private static final int TEX_HEIGHT = 256;

    public RefineryScreen(RefineryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 210;
        imageHeight = 231;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 4;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEX_WIDTH, TEX_HEIGHT);
        renderEnergy(graphics);
        renderInputTank(graphics);
        renderPipes(graphics);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 86, topPos + 95, 16, 52, menu.getTank(1));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 106, topPos + 95, 16, 52, menu.getTank(2));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 126, topPos + 95, 16, 52, menu.getTank(3));
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 146, topPos + 95, 16, 52, menu.getTank(4));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), 0, titleLabelY, imageWidth - 34, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, 0, 30, 27, 21, 104);
        renderTankTooltip(graphics, mouseX, mouseY, 1, 86, 42, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 2, 106, 42, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 3, 126, 42, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, 4, 146, 42, 16, 52);
        if (isHovering(186, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 186, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderEnergy(GuiGraphics graphics) {
        int height = menu.getPowerBarHeight(50);
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 186, topPos + 69 - height,
                    210, 52 - height, 16, height, TEX_WIDTH, TEX_HEIGHT);
        }
    }

    private void renderInputTank(GuiGraphics graphics) {
        HbmFluidGuiHelper.TankData tank = menu.getTank(0);
        if (tank == null || tank.isEmpty()) {
            return;
        }
        int fill = tank.scaledFill(101);
        if (fill > 0) {
            graphics.blit(TEXTURE, leftPos + 33, topPos + 130 - fill, 226,
                    101 - fill, 16, fill, TEX_WIDTH, TEX_HEIGHT);
        }
    }

    private void renderPipes(GuiGraphics graphics) {
        HbmFluidGuiHelper.TankData input = menu.getTank(0);
        RefineryRecipe recipe = input == null ? null : LegacyOilFluidRecipes.getRefinery(input.type());
        if (recipe == null) {
            graphics.blit(TEXTURE, leftPos + 52, topPos + 63, 247, 1, 33, 48, TEX_WIDTH, TEX_HEIGHT);
            graphics.blit(TEXTURE, leftPos + 52, topPos + 32, 247, 50, 66, 52, TEX_WIDTH, TEX_HEIGHT);
            graphics.blit(TEXTURE, leftPos + 52, topPos + 24, 247, 145, 86, 35, TEX_WIDTH, TEX_HEIGHT);
            graphics.blit(TEXTURE, leftPos + 36, topPos + 16, 211, 119, 122, 25, TEX_WIDTH, TEX_HEIGHT);
            return;
        }
        HbmFluidStack[] outputs = recipe.outputs();
        blitTinted(graphics, outputs[0], leftPos + 52, topPos + 63, 247, 1, 33, 48);
        blitTinted(graphics, outputs[1], leftPos + 52, topPos + 32, 247, 50, 66, 52);
        blitTinted(graphics, outputs[2], leftPos + 52, topPos + 24, 247, 145, 86, 35);
        blitTinted(graphics, outputs[3], leftPos + 36, topPos + 16, 211, 119, 122, 25);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void blitTinted(GuiGraphics graphics, HbmFluidStack fluid, int x, int y, int u, int v, int width,
            int height) {
        int color = fluid.type().getColor();
        graphics.setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, 1.0F);
        graphics.blit(TEXTURE, x, y, u, v, width, height, TEX_WIDTH, TEX_HEIGHT);
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, int index, int x, int y,
            int width, int height) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) {
            return;
        }
        List<Component> tooltip = menu.getTankTooltip(index, hasShiftDown());
        LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(index), tooltip, mouseX, mouseY);
    }
}

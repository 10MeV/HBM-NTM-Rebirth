package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FusionBreederBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.FusionBreederMenu;
import com.hbm.ntm.util.BobMathUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FusionBreederScreen extends AbstractContainerScreen<FusionBreederMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_fusion_breeder.png");

    public FusionBreederScreen(FusionBreederMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelY = 106;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int width = (int) Math.ceil(menu.getProgress() * 42.0D / FusionBreederBlockEntity.CAPACITY);
        if (width > 0) {
            graphics.blit(TEXTURE, leftPos + 67, topPos + 48, 176, 0, width, 10);
        }
        double gauge = 1.0D - Math.pow(Math.E, -menu.getNeutronEnergy() * 10.0D
                / FusionBreederBlockEntity.CAPACITY);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 88, topPos + 32, gauge, 5, 2, 1, 0xA00000);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 26, topPos + 70, 16, 52, menu.getInputTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 134, topPos + 70, 16, 52, menu.getOutputTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, imageWidth / 2, 6, 150, 0x404040);
        graphics.drawString(font, playerInventoryTitle, 35, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getInputTank(), 26, 18, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getOutputTank(), 134, 18, 16, 52);
        if (isHovering(79, 23, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal("-> ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal((int) Math.ceil(menu.getNeutronEnergy()) + " flux/t")
                            .withStyle(ChatFormatting.RESET))), mouseX, mouseY);
        } else if (isHovering(67, 46, 42, 14, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal(
                    BobMathUtil.format((int) Math.ceil(menu.getProgress())) + " / "
                            + BobMathUtil.format((int) Math.ceil(FusionBreederBlockEntity.CAPACITY))
                            + " flux")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, HbmFluidGuiHelper.TankData tank,
            int x, int y, int width, int height) {
        if (isHovering(x, y, width, height, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, tank.tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
        }
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.menu.GasCentMenu;
import com.hbm.ntm.menu.GasCentMenu.PseudoTankData;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GasCentScreen extends AbstractContainerScreen<GasCentMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_centrifuge_gas.png");

    public GasCentScreen(GasCentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 206;
        imageHeight = 204;
        titleLabelY = -1000;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 182, topPos + 69 - power, 206, 52 - power, 16, power);
        }
        int progress = menu.getProgressWidth(36);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 70, topPos + 35, 206, 52, progress, 13);
        }

        FluidType displayFluid = menu.getTank().type();
        if (displayFluid == HbmFluids.NONE) {
            displayFluid = HbmFluids.UF6;
        }
        renderPseudoTank(graphics, displayFluid, leftPos + 16, topPos + 16, 6, 52, menu.getInputTank());
        renderPseudoTank(graphics, displayFluid, leftPos + 32, topPos + 16, 6, 52, menu.getInputTank());
        renderPseudoTank(graphics, displayFluid, leftPos + 138, topPos + 16, 6, 52, menu.getOutputTank());
        renderPseudoTank(graphics, displayFluid, leftPos + 154, topPos + 16, 6, 52, menu.getOutputTank());

        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 12, topPos + 16, 3);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 12, topPos + 32, 2);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(15, 15, 24, 55, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(),
                    menu.pseudoTankTooltip(menu.getInputTank(), true, hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(137, 15, 25, 55, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(),
                    menu.pseudoTankTooltip(menu.getOutputTank(), false, hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(182, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 182, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(-12, 16, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font,
                    splitLegacyInfo(Component.translatableWithFallback("desc.gui.gasCent.enrichment",
                            "Enrichment$Uranium enrichment requires cascades.$Two-centrifuge cascades will give$uranium fuel, four-centrifuge cascades$will give total separation.")),
                    mouseX, mouseY);
        } else if (isHovering(-12, 32, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font,
                    splitLegacyInfo(Component.translatableWithFallback("desc.gui.gasCent.output",
                            "Fluid Transfer$Fluid can be transferred to another centrifuge$via the output port for further processing.")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static List<Component> splitLegacyInfo(Component text) {
        return Arrays.stream(text.getString().split("\\$"))
                .map(Component::literal)
                .map(Component.class::cast)
                .toList();
    }

    private static void renderPseudoTank(GuiGraphics graphics, FluidType fluid, int x, int y, int width, int height,
            PseudoTankData tank) {
        if (tank == null || tank.isEmpty()) {
            return;
        }
        int fill = tank.scaledFill(height);
        if (fill <= 0) {
            return;
        }
        int tint = fluid.getGuiTint();
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        graphics.blit(fluid.getTexture(), x, y + height - fill, 0, 16 - fill, width, fill, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}

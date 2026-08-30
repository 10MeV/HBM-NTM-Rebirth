package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.RadiolysisMenu;
import com.hbm.ntm.util.RtgPelletRuntime;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RadiolysisScreen extends AbstractContainerScreen<RadiolysisMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_radiolysis.png");

    public RadiolysisScreen(RadiolysisMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 166;
        titleLabelX = 88;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(34);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 51 - power, 240, 34 - power, 16, power);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 61, topPos + 69,
                8, 52, menu.getInputTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 87, topPos + 33,
                12, 16, menu.getOutputTank1());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 87, topPos + 69,
                12, 16, menu.getOutputTank2());
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 16, 10);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 34, 2);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 52, 3);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 61, 17, 8, 52)) {
            renderTankTooltip(graphics, menu.getInputTank(), mouseX, mouseY);
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 87, 17, 12, 16)) {
            renderTankTooltip(graphics, menu.getOutputTank1(), mouseX, mouseY);
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 87, 53, 12, 16)) {
            renderTankTooltip(graphics, menu.getOutputTank2(), mouseX, mouseY);
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 8, 17, 16, 34)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 17, 16, 34, menu.getPower(), menu.getMaxPower());
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 16, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 16, topPos + 16, 16, 16, leftPos - 8, topPos + 32,
                    splitLegacyInfo(Component.translatableWithFallback("desc.gui.radiolysis.desc",
                            "\u00a79Description\u00a7r$This RTG is more efficient then others, and$"
                                    + "comes equipped with a radiolysis chamber for$cracking and sterilization.")));
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 34, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 16, topPos + 34, 16, 16, leftPos - 8, topPos + 50,
                    Component.translatableWithFallback("desc.gui.rtg.heat",
                            "\u00a7eCurrent heat level: %s", menu.getHeat()));
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 52, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 16, topPos + 52, 16, 16, leftPos - 8, topPos + 68,
                    RtgPelletRuntime.acceptedPelletTooltip(10));
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static List<Component> splitLegacyInfo(Component text) {
        return Arrays.stream(text.getString().split("\\$"))
                .map(line -> (Component) Component.literal(line))
                .toList();
    }

    private void renderTankTooltip(GuiGraphics graphics, HbmFluidGuiHelper.TankData tank, int mouseX, int mouseY) {
        LegacyGuiElements.renderFluidTooltip(graphics, font, tank,
                menu.getTankTooltip(tank, hasShiftDown()), mouseX, mouseY);
    }
}

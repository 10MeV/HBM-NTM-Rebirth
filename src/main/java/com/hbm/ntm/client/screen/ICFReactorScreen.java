package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ICFReactorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.ICFReactorMenu;
import com.hbm.ntm.util.BobMathUtil;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ICFReactorScreen extends AbstractContainerScreen<ICFReactorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_icf.png");

    public ICFReactorScreen(ICFReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 248;
        imageHeight = 222;
        inventoryLabelX = 44;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, 114);
        graphics.blit(TEXTURE, leftPos + 36, topPos + 122, 36, 122, 176, 108);
        if (menu.getMaxLaser() > 0L) {
            int laser = (int) (menu.getLaser() * 70L / menu.getMaxLaser());
            if (laser > 0) {
                graphics.blit(TEXTURE, leftPos + 8, topPos + 88 - laser, 212, 192 - laser, 16, laser);
            }
        }
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 196, topPos + 98,
                menu.getHeat() / (double) ICFReactorBlockEntity.MAX_HEAT, 5, 2, 1, 0xFF00AF);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 44, topPos + 88, 16, 70,
                menu.getCoolantTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 188, topPos + 88, 16, 70,
                menu.getHotCoolantTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 224, topPos + 88, 16, 70,
                menu.getStellarFluxTank());
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
        if (!renderTankTooltip(graphics, mouseX, mouseY, menu.getCoolantTank(), 44, 18, 16, 70)
                && !renderTankTooltip(graphics, mouseX, mouseY, menu.getHotCoolantTank(), 188, 18, 16, 70)
                && !renderTankTooltip(graphics, mouseX, mouseY, menu.getStellarFluxTank(), 224, 18, 16, 70)) {
            if (isHovering(8, 18, 16, 70, mouseX, mouseY)) {
                String laser = menu.getMaxLaser() <= 0L ? "OFFLINE"
                        : shortNumber(menu.getLaser()) + "TU/t - "
                                + (menu.getLaser() * 1000L / menu.getMaxLaser()) / 10.0D + "%";
                graphics.renderComponentTooltip(font, List.of(Component.literal(laser)), mouseX, mouseY);
            } else if (isHovering(187, 89, 18, 18, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, List.of(Component.literal(shortNumber(menu.getHeat()) + " / "
                        + shortNumber(ICFReactorBlockEntity.MAX_HEAT) + "TU")), mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, HbmFluidGuiHelper.TankData tank,
            int x, int y, int width, int height) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) {
            return false;
        }
        graphics.renderComponentTooltip(font, tank.tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
        return true;
    }

    private static String shortNumber(long value) {
        return BobMathUtil.getShortNumber(value);
    }
}

package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.LegacyRemoteFluidMachineBlockEntity.LegacyGuiProfile;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.RemoteFluidMachineMenu;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RemoteFluidMachineScreen extends AbstractContainerScreen<RemoteFluidMachineMenu> {
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 52;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public RemoteFluidMachineScreen(RemoteFluidMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.getImageWidth();
        imageHeight = menu.getImageHeight();
        titleLabelY = 5;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture(menu.getProfile()), leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderEnergy(graphics);
        renderCokerBars(graphics);
        for (TankRect rect : tankRects(menu.getProfile())) {
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + rect.x(), topPos + rect.bottom(),
                    TANK_WIDTH, TANK_HEIGHT, menu.getTank(rect.index()));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int color = menu.getProfile() == LegacyGuiProfile.COKER ? 0xC7C1A3 : 0xFFFFFF;
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), 0, titleLabelY, imageWidth, color);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (TankRect rect : tankRects(menu.getProfile())) {
            if (isHovering(rect.x(), rect.bottom() - TANK_HEIGHT, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(rect.index()),
                        menu.getTankTooltip(rect.index(), hasShiftDown()), mouseX, mouseY);
                renderTooltip(graphics, mouseX, mouseY);
                return;
            }
        }
        EnergyRect energy = energyRect(menu.getProfile());
        if (energy != null && isHovering(energy.x(), energy.bottom() - energy.height(), energy.width(),
                energy.height(), mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + energy.x(), topPos + energy.bottom() - energy.height(),
                    energy.width(), energy.height(), menu.getPower(), menu.getMaxPower());
        }
        if (menu.getProfile() == LegacyGuiProfile.COKER) {
            if (isHovering(60, 45, 54, 7, mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.literal(NUMBER_FORMAT.format(menu.getCokerProgress()) + " / "
                        + NUMBER_FORMAT.format(menu.getCokerProcessTime()) + "TU"), mouseX, mouseY);
            } else if (isHovering(60, 54, 54, 7, mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.literal(NUMBER_FORMAT.format(menu.getCokerHeat()) + " / "
                        + NUMBER_FORMAT.format(menu.getCokerMaxHeat()) + "TU"), mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderEnergy(GuiGraphics graphics) {
        EnergyRect rect = energyRect(menu.getProfile());
        if (rect == null) {
            return;
        }
        int height = menu.getPowerBarHeight(rect.height());
        if (height > 0) {
            graphics.blit(texture(menu.getProfile()), leftPos + rect.x(), topPos + rect.bottom() - height,
                    176, rect.height() - height, rect.width(), height);
        }
    }

    private void renderCokerBars(GuiGraphics graphics) {
        if (menu.getProfile() != LegacyGuiProfile.COKER) {
            return;
        }
        int progressWidth = menu.getCokerProgressBarWidth(53);
        if (progressWidth > 0) {
            graphics.blit(texture(menu.getProfile()), leftPos + 61, topPos + 46, 176, 0, progressWidth, 5);
        }
        int heatWidth = menu.getCokerHeatBarWidth(52);
        if (heatWidth > 0) {
            graphics.blit(texture(menu.getProfile()), leftPos + 61, topPos + 55, 176, 5, heatWidth, 5);
        }
    }

    private static ResourceLocation texture(LegacyGuiProfile profile) {
        String path = switch (profile) {
            case COKER -> "textures/gui/processing/gui_coker.png";
            case HYDROTREATER -> "textures/gui/processing/gui_hydrotreater.png";
            case CATALYTIC_REFORMER -> "textures/gui/processing/gui_catalytic_reformer.png";
            case VACUUM_DISTILL -> "textures/gui/processing/gui_vacuum_distill.png";
            default -> "textures/gui/processing/gui_refinery.png";
        };
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static List<TankRect> tankRects(LegacyGuiProfile profile) {
        return switch (profile) {
            case COKER -> List.of(
                    new TankRect(0, 35, 70),
                    new TankRect(1, 125, 70));
            case HYDROTREATER -> List.of(
                    new TankRect(0, 35, 70),
                    new TankRect(1, 53, 70),
                    new TankRect(2, 125, 70),
                    new TankRect(3, 143, 70));
            case CATALYTIC_REFORMER -> List.of(
                    new TankRect(0, 35, 70),
                    new TankRect(1, 107, 70),
                    new TankRect(2, 125, 70),
                    new TankRect(3, 143, 70));
            case VACUUM_DISTILL -> List.of(
                    new TankRect(0, 44, 70),
                    new TankRect(1, 80, 70),
                    new TankRect(2, 98, 70),
                    new TankRect(3, 116, 70),
                    new TankRect(4, 134, 70));
            default -> List.of();
        };
    }

    private static EnergyRect energyRect(LegacyGuiProfile profile) {
        return switch (profile) {
            case HYDROTREATER, CATALYTIC_REFORMER -> new EnergyRect(17, 70, 16, 52);
            case VACUUM_DISTILL -> new EnergyRect(26, 70, 16, 52);
            default -> null;
        };
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private record TankRect(int index, int x, int bottom) {
    }

    private record EnergyRect(int x, int bottom, int width, int height) {
    }
}

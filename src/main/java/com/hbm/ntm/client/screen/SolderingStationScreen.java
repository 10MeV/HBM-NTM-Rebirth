package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.SolderingStationBlockEntity;
import com.hbm.ntm.menu.SolderingStationMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SolderingStationScreen extends AbstractContainerScreen<SolderingStationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_soldering_station.png");

    public SolderingStationScreen(SolderingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isCollisionPrevention()) {
            graphics.blit(TEXTURE, leftPos + 5, topPos + 66, 192, 14, 10, 10);
        }
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 70 - power, 176, 52 - power, 16, power);
        }
        int progress = menu.getProgressWidth(33);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 72, topPos + 28, 192, 0, progress, 14);
        }
        if (menu.getPower() >= menu.getConsumption()) {
            graphics.blit(TEXTURE, leftPos + 156, topPos + 4, 176, 52, 9, 12);
        }
        LegacyGuiElements.renderInfoPanel(graphics, leftPos + 78, topPos + 67, 8);
        LegacyFluidGuiRenderer.renderHorizontalTank(graphics, leftPos + 35, topPos + 79, 34, 16, menu.getTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = 70 - font.width(title) / 2;
        graphics.drawString(font, title, titleX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(35, 63, 34, 16, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(),
                    menu.tankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(5, 66, 10, 10, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("Recipe Collision Prevention: ")
                            .append(Component.literal(menu.isCollisionPrevention() ? "ON" : "OFF")
                                    .withStyle(menu.isCollisionPrevention()
                                            ? ChatFormatting.GREEN : ChatFormatting.RED)),
                    Component.literal("Prevents no-fluid recipes from being processed"),
                    Component.literal("when fluid is present.")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(5, 66, 10, 10, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0,
                    SolderingStationBlockEntity.CONTROL_COLLISION_PREVENTION);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

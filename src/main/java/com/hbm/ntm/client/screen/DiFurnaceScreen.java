package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.DiFurnaceBlockEntity;
import com.hbm.ntm.menu.DiFurnaceMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DiFurnaceScreen extends AbstractContainerScreen<DiFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/guidifurnace.png");

    public DiFurnaceScreen(DiFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int fuelHeight = menu.getFuelPixels(52);
        if (fuelHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 44, topPos + 70 - fuelHeight, 201,
                    53 - fuelHeight, 16, fuelHeight);
        }
        int progressWidth = menu.getProgressPixels(24);
        graphics.blit(TEXTURE, leftPos + 101, topPos + 35, 176, 14, progressWidth + 1, 17);
        if (menu.isProcessing() || progressWidth > 0) {
            graphics.blit(TEXTURE, leftPos + 63, topPos + 37, 176, 0, 14, 14);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(44, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.literal(menu.getFuel() + " / " + DiFurnaceBlockEntity.MAX_FUEL),
                    mouseX, mouseY);
        } else if (isHovering(101, 35, 24, 17, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.literal(menu.getProgress() + " / " + DiFurnaceBlockEntity.PROCESSING_SPEED),
                    mouseX, mouseY);
        } else {
            renderSideTooltip(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderSideTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int slot = hoveredMachineSlot(mouseX, mouseY);
        if (slot < 0 || !menu.getCarried().isEmpty()) {
            return;
        }
        int side = menu.getSideForSlot(slot);
        if (side < 0 || side >= Direction.values().length) {
            return;
        }
        graphics.renderComponentTooltip(font, List.of(Component.literal("Accepts items from: "
                + Direction.values()[side].getName()).withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
    }

    private int hoveredMachineSlot(int mouseX, int mouseY) {
        if (isHovering(80, 18, 16, 16, mouseX, mouseY)) {
            return DiFurnaceBlockEntity.SLOT_UPPER;
        }
        if (isHovering(80, 54, 16, 16, mouseX, mouseY)) {
            return DiFurnaceBlockEntity.SLOT_LOWER;
        }
        if (isHovering(8, 36, 16, 16, mouseX, mouseY)) {
            return DiFurnaceBlockEntity.SLOT_FUEL;
        }
        return -1;
    }
}

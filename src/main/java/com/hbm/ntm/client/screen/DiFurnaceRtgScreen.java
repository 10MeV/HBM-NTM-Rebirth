package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.DiFurnaceRtgBlockEntity;
import com.hbm.ntm.menu.DiFurnaceRtgMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DiFurnaceRtgScreen extends AbstractContainerScreen<DiFurnaceRtgMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_rtg_difurnace.png");

    public DiFurnaceRtgScreen(DiFurnaceRtgMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 185;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getProgressWidth(24);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 101, topPos + 35, 176, 14, progress + 1, 17);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(101, 35, 24, 17, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.literal(menu.getProgress() + " / " + DiFurnaceRtgBlockEntity.PROCESS_TIME),
                    mouseX, mouseY);
        } else if (isHovering(8, 72, 108, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getSpeed() + " heat/t"), mouseX, mouseY);
        } else {
            renderSideTooltip(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderSideTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int slot = hoveredInputSlot(mouseX, mouseY);
        if (slot < 0 || !menu.getCarried().isEmpty()) {
            return;
        }
        int side = menu.getSideForSlot(slot);
        if (side >= 0 && side < Direction.values().length) {
            graphics.renderComponentTooltip(font, List.of(Component.literal("Accepts items from: "
                    + Direction.values()[side].getName()).withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
        }
    }

    private int hoveredInputSlot(int mouseX, int mouseY) {
        if (isHovering(80, 18, 16, 16, mouseX, mouseY)) {
            return 0;
        }
        if (isHovering(80, 54, 16, 16, mouseX, mouseY)) {
            return 1;
        }
        return -1;
    }
}

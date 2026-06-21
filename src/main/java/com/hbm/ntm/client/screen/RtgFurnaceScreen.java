package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RtgFurnaceBlockEntity;
import com.hbm.ntm.menu.RtgFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RtgFurnaceScreen extends AbstractContainerScreen<RtgFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/rtgfurnace.png");

    public RtgFurnaceScreen(RtgFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
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
                    Component.literal(menu.getCookTime() + " / " + RtgFurnaceBlockEntity.PROCESS_TIME),
                    mouseX, mouseY);
        } else if (isHovering(17, 53, 54, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getHeat() + " heat/t"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

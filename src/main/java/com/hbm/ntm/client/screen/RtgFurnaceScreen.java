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
        if (menu.getHeat() > 0) {
            graphics.blit(TEXTURE, leftPos + 55, topPos + 35, 176, 0, 18, 16);
        }
        int progress = menu.getProgressWidth(24);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 16, progress + 1, 17);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(79, 34, 24, 17, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.literal(menu.getCookTime() + " / " + RtgFurnaceBlockEntity.PROCESS_TIME),
                    mouseX, mouseY);
        } else if (isHovering(55, 35, 18, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getHeat() + " heat/t"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

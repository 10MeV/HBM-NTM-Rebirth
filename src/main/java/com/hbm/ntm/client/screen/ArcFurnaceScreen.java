package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ArcFurnaceMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ArcFurnaceScreen extends AbstractContainerScreen<ArcFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_arc_furnace.png");

    public ArcFurnaceScreen(ArcFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 256;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isLiquidMode()) {
            graphics.blit(TEXTURE, leftPos + 151, topPos + 17, 190, 18, 18, 18);
        }
        if (menu.isProgressing()) {
            graphics.blit(TEXTURE, leftPos + 7, topPos + 17, 190, 0, 18, 18);
        }
        int power = menu.getPowerBarHeight(70);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 106 - power, 176, 70 - power, 7, power);
        }
        int progress = menu.getProgressBarHeight(70);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 17, topPos + 106 - progress, 183, 70 - progress, 7, progress);
        }
        // The legacy liquid stack is rendered at x=152 with the full 16px column,
        // regardless of the currently selected input mode.
        renderLiquid(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isLegacyHovering(8, 36, 7, 70, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 36, 7, 70, menu.getPower(), menu.getMaxPower());
        } else if (isLegacyHovering(152, 36, 16, 70, mouseX, mouseY)) {
            graphics.renderTooltip(font, menu.liquidTooltip(hasShiftDown()).withStyle(
                    menu.getLiquidAmount() > 0 ? ChatFormatting.YELLOW : ChatFormatting.RED), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isLegacyHovering(151, 17, 18, 18, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }

    private void renderLiquid(GuiGraphics graphics) {
        int height = menu.getLiquidHeight(70);
        if (height <= 0) {
            return;
        }
        int color = menu.getLiquidColor();
        graphics.setColor(((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F, 1.0F);
        graphics.blit(TEXTURE, leftPos + 152, topPos + 106 - height, 208, 70 - height, 16, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 0.3F);
        graphics.blit(TEXTURE, leftPos + 152, topPos + 106 - height, 208, 70 - height, 16, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

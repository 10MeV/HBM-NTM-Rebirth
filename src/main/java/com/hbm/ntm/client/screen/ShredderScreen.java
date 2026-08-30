package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ShredderMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ShredderScreen extends AbstractContainerScreen<ShredderMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_shredder.png");

    public ShredderScreen(ShredderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 233;
        inventoryLabelY = 139;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int powerHeight = menu.getPowerBarHeight(88);
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 106 - powerHeight, 176,
                    160 - powerHeight, 16, powerHeight);
        }
        graphics.blit(TEXTURE, leftPos + 63, topPos + 89, 176, 54,
                menu.getProgressWidth(34) + 1, 18);
        drawGear(graphics, menu.getLeftGear(), leftPos + 43, topPos + 71, 176);
        drawGear(graphics, menu.getRightGear(), leftPos + 79, topPos + 71, 194);
        if (menu.bladesBrokenOrMissing()) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 36, 6);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 106 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 8, topPos + 18, 16, 88)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 18, 16, 88, menu.getPower(), menu.getMaxPower());
        } else if (menu.bladesBrokenOrMissing()
                && LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 36, 16, 16)) {
            LegacyGuiElements.renderTooltip(graphics, font,
                    List.of(Component.literal("Error: Shredder blades are broken or missing!")),
                    leftPos - 8, topPos + 52);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawGear(GuiGraphics graphics, int state, int x, int y, int u) {
        if (state <= 0) {
            return;
        }
        int v = switch (state) {
            case 1 -> 0;
            case 2 -> 18;
            default -> 36;
        };
        graphics.blit(TEXTURE, x, y, u, v, 18, 18);
    }
}

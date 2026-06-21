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
            "textures/gui/gui_shredder.png");

    public ShredderScreen(ShredderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 233;
        inventoryLabelY = 137;
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
            graphics.blit(TEXTURE, leftPos - 16, topPos + 36, 176, 72, 16, 16);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 18, 16, 88, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font,
                    List.of(Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE")),
                    mouseX, mouseY);
        } else if (menu.bladesBrokenOrMissing()
                && mouseX >= leftPos - 16 && mouseX < leftPos && mouseY >= topPos + 36 && mouseY < topPos + 52) {
            LegacyGuiElements.renderTooltip(graphics, font,
                    List.of(Component.literal("Error: Shredder blades are broken or missing!")), mouseX, mouseY);
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

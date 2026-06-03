package com.hbm.ntm.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class LegacyGuiText {
    private LegacyGuiText() {
    }

    static void drawCenteredLabel(GuiGraphics graphics, Font font, String text, int centerX, int y, int maxWidth, int color) {
        int width = font.width(text);
        if (width <= maxWidth) {
            graphics.drawString(font, text, centerX - width / 2, y, color, false);
            return;
        }
        float scale = maxWidth / (float) width;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX - width * scale / 2.0F, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }
}

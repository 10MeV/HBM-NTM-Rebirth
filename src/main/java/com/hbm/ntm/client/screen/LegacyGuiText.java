package com.hbm.ntm.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class LegacyGuiText {
    private LegacyGuiText() {
    }

    static void drawCenteredLabel(GuiGraphics graphics, Font font, String text, int centerX, int y, int maxWidth, int color) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, text, centerX, y, maxWidth, color);
    }
}

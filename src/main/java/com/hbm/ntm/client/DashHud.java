package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class DashHud {
    private static final ResourceLocation OVERLAY_MISC = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/overlay_misc.png");
    private static final int BAR_WIDTH = 30;
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_GAP = 2;
    private static final int ROW_HEIGHT = 12;
    private static float fadeOut;

    public static void render(GuiGraphics graphics, int screenHeight) {
        int dashes = ClientHbmPlayerProperties.getDashCount();
        if (dashes <= 0) {
            fadeOut = 0.0F;
            return;
        }

        int stamina = Math.max(0, ClientHbmPlayerProperties.getStamina());
        int posX = 16;
        int posY = screenHeight - 40 - 2;
        int staminaDiv = stamina / BAR_WIDTH;
        int staminaMod = stamina % BAR_WIDTH;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(OVERLAY_MISC, posX - 10, posY, 107, 18, 7, BAR_HEIGHT);

        for (int barId = 0; barId < dashes; barId++) {
            int column = barId % 3;
            int row = barId / 3;
            int x = posX + (BAR_WIDTH + BAR_GAP) * column;
            int y = posY - ROW_HEIGHT * row;
            graphics.blit(OVERLAY_MISC, x, y, 76, 48, BAR_WIDTH, BAR_HEIGHT);

            int barStatus = 1;
            int barSize = BAR_WIDTH;
            if (staminaDiv < barId) {
                barStatus = 3;
            } else if (staminaDiv == barId) {
                barStatus = barId == 0 ? 0 : 2;
                barSize = (int) (staminaMod * (BAR_WIDTH / 30.0F));
            }
            if (barSize > 0) {
                graphics.blit(OVERLAY_MISC, x, y, 76, 18 + BAR_HEIGHT * barStatus, barSize, BAR_HEIGHT);
            }

            if (staminaDiv == barId && staminaMod >= 27) {
                fadeOut = 1.0F;
            }
            if (fadeOut > 0.0F && staminaDiv - 1 == barId) {
                renderFade(graphics, posX, posY, dashes, barId, staminaMod);
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderFade(GuiGraphics graphics, int posX, int posY, int dashes, int barId, int staminaMod) {
        int fadeBar = staminaMod >= 25 ? barId + 1 : barId;
        if (fadeBar >= dashes) {
            fadeOut = Math.max(0.0F, fadeOut - 0.04F);
            return;
        }
        int column = fadeBar % 3;
        int row = fadeBar / 3;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fadeOut);
        graphics.blit(OVERLAY_MISC, posX + (BAR_WIDTH + BAR_GAP) * column, posY - ROW_HEIGHT * row,
                76, 58, BAR_WIDTH, BAR_HEIGHT);
        fadeOut = Math.max(0.0F, fadeOut - 0.04F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private DashHud() {
    }
}

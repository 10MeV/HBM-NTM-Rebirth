package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.renderer.LegacyScreenQuadRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class DashHud {
    private static final ResourceLocation OVERLAY_MISC = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/overlay_misc.png");
    private static final int BAR_WIDTH = 30;
    private static final int BAR_HEIGHT = 10;
    private static final int FADE_TRIGGER_STAMINA = 27;
    private static final int FADE_ADVANCE_STAMINA = 25;
    private static float fadeOut;

    public static void render(GuiGraphics graphics, int screenHeight) {
        int dashes = ClientHbmPlayerProperties.getDashCount();
        if (dashes <= 0) {
            fadeOut = 0.0F;
            return;
        }

        int stamina = Math.max(0, ClientHbmPlayerProperties.getStamina());
        LegacyScreenQuadRenderer.DashLayout layout = LegacyScreenQuadRenderer.dashLayout(screenHeight);
        int staminaDiv = stamina / layout.barWidth();
        int staminaMod = stamina % layout.barWidth();

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(OVERLAY_MISC, layout.x() - 10, layout.y(), 107, 18, 7, BAR_HEIGHT);

        for (int barId = 0; barId < dashes; barId++) {
            int x = layout.x() + (layout.barWidth() + layout.gap()) * (barId % 3);
            int y = layout.y() - layout.rowHeight() * (barId / 3);
            int status = 1;
            int fillWidth = layout.barWidth();
            if (staminaDiv < barId) {
                status = 3;
            } else if (staminaDiv == barId) {
                status = barId == 0 ? 0 : 2;
                fillWidth = (int) (staminaMod * (layout.barWidth() / 30.0F));
            }

            graphics.blit(OVERLAY_MISC, x, y, 76, 48, BAR_WIDTH, BAR_HEIGHT);
            fillWidth = Math.max(0, Math.min(layout.barWidth(), fillWidth));
            if (fillWidth > 0) {
                graphics.blit(OVERLAY_MISC, x, y, 76, 18 + BAR_HEIGHT * status, fillWidth, BAR_HEIGHT);
            }

            if (staminaDiv == barId && staminaMod >= FADE_TRIGGER_STAMINA) {
                fadeOut = 1.0F;
            }
        }
        if (fadeOut > 0.0F) {
            renderFade(graphics, layout, staminaDiv, staminaMod, dashes);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderFade(GuiGraphics graphics, LegacyScreenQuadRenderer.DashLayout layout,
            int staminaDiv, int staminaMod, int dashes) {
        int previousBar = staminaDiv - 1;
        if (previousBar < 0) {
            fadeOut = Math.max(0.0F, fadeOut - 0.04F);
            return;
        }
        int fadeBar = staminaMod >= FADE_ADVANCE_STAMINA ? previousBar + 1 : previousBar;
        if (fadeBar < 0 || fadeBar >= dashes) {
            fadeOut = Math.max(0.0F, fadeOut - 0.04F);
            return;
        }
        int x = layout.x() + (layout.barWidth() + layout.gap()) * (fadeBar % 3);
        int y = layout.y() - layout.rowHeight() * (fadeBar / 3);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fadeOut);
        graphics.blit(OVERLAY_MISC, x, y, 76, 58, BAR_WIDTH, BAR_HEIGHT);
        fadeOut = Math.max(0.0F, fadeOut - 0.04F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private DashHud() {
    }
}

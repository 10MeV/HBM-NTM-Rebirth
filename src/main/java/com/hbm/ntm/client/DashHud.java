package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.renderer.LegacyScreenQuadRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class DashHud {
    private static final ResourceLocation OVERLAY_MISC = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/overlay_misc.png");
    private static final int BAR_WIDTH = 30;
    private static final int BAR_HEIGHT = 10;
    private static float fadeOut;

    public static void render(GuiGraphics graphics, int screenHeight) {
        int dashes = ClientHbmPlayerProperties.getDashCount();
        if (dashes <= 0) {
            fadeOut = 0.0F;
            return;
        }

        int stamina = Math.max(0, ClientHbmPlayerProperties.getStamina());
        LegacyScreenQuadRenderer.DashLayout layout = LegacyScreenQuadRenderer.dashLayout(screenHeight);
        List<LegacyScreenQuadRenderer.DashBarSegment> segments =
                LegacyScreenQuadRenderer.dashBarSegments(screenHeight, stamina, dashes);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(OVERLAY_MISC, layout.x() - 10, layout.y(), 107, 18, 7, BAR_HEIGHT);

        for (LegacyScreenQuadRenderer.DashBarSegment segment : segments) {
            graphics.blit(OVERLAY_MISC, segment.x(), segment.y(), 76, 48, BAR_WIDTH, BAR_HEIGHT);
            if (segment.fillWidth() > 0) {
                graphics.blit(OVERLAY_MISC, segment.x(), segment.y(), 76, 18 + BAR_HEIGHT * segment.status(),
                        segment.fillWidth(), BAR_HEIGHT);
            }

            if (segment.triggerFade()) {
                fadeOut = 1.0F;
            }
        }
        if (fadeOut > 0.0F) {
            renderFade(graphics, screenHeight, stamina, dashes);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderFade(GuiGraphics graphics, int screenHeight, int stamina, int dashes) {
        LegacyScreenQuadRenderer.DashFadePlan fadePlan =
                LegacyScreenQuadRenderer.dashFadePlan(screenHeight, stamina, dashes);
        if (!fadePlan.visible()) {
            fadeOut = Math.max(0.0F, fadeOut - 0.04F);
            return;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fadeOut);
        graphics.blit(OVERLAY_MISC, fadePlan.x(), fadePlan.y(), 76, 58, BAR_WIDTH, BAR_HEIGHT);
        fadeOut = Math.max(0.0F, fadeOut - 0.04F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private DashHud() {
    }
}

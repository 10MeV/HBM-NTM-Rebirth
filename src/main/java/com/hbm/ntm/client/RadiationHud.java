package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.renderer.LegacyScreenQuadRenderer;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class RadiationHud {
    private static final ResourceLocation OVERLAY_MISC = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/overlay_misc.png");
    private static long lastSurveyMs;
    private static float previousRadiation;
    private static float lastRadiation;

    public static boolean hasGeigerCounter(Player player) {
        if (player == null) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.GEIGER_COUNTER.get())) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(ModItems.GEIGER_COUNTER.get())) {
                return true;
            }
        }
        return false;
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        float radiation = ClientHbmLivingProperties.getRadiation();
        float rate = Math.max(0.0F, lastRadiation - previousRadiation);
        long now = System.currentTimeMillis();
        if (now >= lastSurveyMs + 1000L) {
            lastSurveyMs = now;
            previousRadiation = lastRadiation;
            lastRadiation = radiation;
        }

        int barLength = 74;
        int width = 94;
        int height = 18;
        int x = 16 + HbmClientConfig.geigerOffsetHorizontal();
        int y = screenHeight - 20 - HbmClientConfig.geigerOffsetVertical();
        int bar = LegacyScreenQuadRenderer.scaled(radiation, 1000.0D, barLength);

        graphics.blit(OVERLAY_MISC, x, y, 0, 0, width, height);
        LegacyScreenQuadRenderer.blitHorizontalProgress(OVERLAY_MISC, graphics, x + 1, y + 1, 1, 19, barLength, 16, bar);

        if (rate > 0.0F) {
            if (rate >= 25.0F) {
                graphics.blit(OVERLAY_MISC, x + barLength + 2, y - 18, 36, 36, 18, 18);
            } else if (rate >= 10.0F) {
                graphics.blit(OVERLAY_MISC, x + barLength + 2, y - 18, 18, 36, 18, 18);
            } else if (rate >= 2.5F) {
                graphics.blit(OVERLAY_MISC, x + barLength + 2, y - 18, 0, 36, 18, 18);
            }
            String label = rate > 1000.0F ? ">1000 RAD/s" : rate >= 1.0F ? Math.round(rate) + " RAD/s" : "<1 RAD/s";
            graphics.drawString(Minecraft.getInstance().font, label, x, y - 8, 0xFFFF0000, false);
        }
    }

    private RadiationHud() {
    }
}

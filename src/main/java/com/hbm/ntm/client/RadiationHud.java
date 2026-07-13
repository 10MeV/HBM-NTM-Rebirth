package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.renderer.LegacyScreenQuadRenderer;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.item.FsbArmorItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class RadiationHud {
    private static final ResourceLocation OVERLAY_MISC = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/overlay_misc.png");
    private static final int BAR_LENGTH = 74;
    private static long lastSurveyMs;
    private static float previousRadiation;
    private static float lastRadiation;

    public static boolean hasGeigerCounter(Player player) {
        if (player == null) {
            return false;
        }
        if (FsbArmorItem.hasCustomGeigerHud(player)) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
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

        int x = 16 + HbmClientConfig.geigerOffsetHorizontal();
        int y = screenHeight - 20 - HbmClientConfig.geigerOffsetVertical();
        int bar = LegacyScreenQuadRenderer.scaled(radiation, 1000.0D, BAR_LENGTH);
        graphics.blit(OVERLAY_MISC, x, y, 0, 0, 94, 18);
        if (bar > 0) {
            graphics.blit(OVERLAY_MISC, x + 1, y + 1, 1, 19, bar, 16);
        }
        int warningU = warningTextureU(rate);
        if (warningU >= 0) {
            graphics.blit(OVERLAY_MISC, x + BAR_LENGTH + 2, y - 18, warningU, 36, 18, 18);
        }
        String label = radiationLabel(rate);
        if (!label.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, label, x, y - 8, 0xFFFF0000, false);
        }
    }

    private static int warningTextureU(double radiationRate) {
        if (radiationRate >= 25.0D) {
            return 36;
        }
        if (radiationRate >= 10.0D) {
            return 18;
        }
        return radiationRate >= 2.5D ? 0 : -1;
    }

    private static String radiationLabel(double radiationRate) {
        if (radiationRate > 1000.0D) {
            return ">1000 RAD/s";
        }
        if (radiationRate >= 1.0D) {
            return Math.round(radiationRate) + " RAD/s";
        }
        return radiationRate > 0.0D ? "<1 RAD/s" : "";
    }

    private RadiationHud() {
    }
}

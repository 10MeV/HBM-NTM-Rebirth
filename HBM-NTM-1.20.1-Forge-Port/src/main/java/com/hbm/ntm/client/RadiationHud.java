package com.hbm.ntm.client;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class RadiationHud {
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
        float radiation = ClientRadiationData.getRadiation();
        long now = System.currentTimeMillis();
        if (now >= lastSurveyMs + 1000L) {
            lastSurveyMs = now;
            previousRadiation = lastRadiation;
            lastRadiation = radiation;
        }

        float rate = Math.max(ClientRadiationData.getEnvironment(), lastRadiation - previousRadiation);
        int width = 94;
        int height = 18;
        int x = screenWidth - width - 16;
        int y = screenHeight - height - 24;
        int bar = Math.min(74, Math.round(radiation / 1000.0F * 74.0F));

        graphics.fill(x, y, x + width, y + height, 0xAA101010);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xAA2A2A2A);
        graphics.fill(x + 2, y + 2, x + 2 + bar, y + height - 2, 0xFF72B83A);
        graphics.fill(x + 2 + bar, y + 2, x + 76, y + height - 2, 0xFF202020);
        graphics.drawString(Minecraft.getInstance().font, "RAD " + Math.round(radiation), x + 3, y + 5, 0xFFE5FFCC, false);

        if (rate > 0.0F) {
            String label = rate > 1000.0F ? ">1000 RAD/s" : rate >= 1.0F ? Math.round(rate) + " RAD/s" : "<1 RAD/s";
            graphics.drawString(Minecraft.getInstance().font, label, x, y - 10, 0xFFFF4444, false);
        }
    }

    private RadiationHud() {
    }
}

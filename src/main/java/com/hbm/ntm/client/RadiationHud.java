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

        LegacyScreenQuadRenderer.RadCounterPlan plan = LegacyScreenQuadRenderer.radCounterPlan(screenHeight,
                HbmClientConfig.geigerOffsetHorizontal(), HbmClientConfig.geigerOffsetVertical(), radiation, rate);
        graphics.blit(OVERLAY_MISC, plan.frame().x(), plan.frame().y(), plan.frameTexture().u(),
                plan.frameTexture().v(), plan.frame().width(), plan.frame().height());
        if (plan.fill().width() > 0) {
            graphics.blit(OVERLAY_MISC, plan.fill().x(), plan.fill().y(), plan.fillTexture().u(),
                    plan.fillTexture().v(), plan.fill().width(), plan.fill().height());
        }
        if (plan.warning().visible()) {
            graphics.blit(OVERLAY_MISC, plan.warning().rect().x(), plan.warning().rect().y(),
                    plan.warning().texture().u(), plan.warning().texture().v(),
                    plan.warning().rect().width(), plan.warning().rect().height());
        }
        if (!plan.label().isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, plan.label(), plan.labelX(), plan.labelY(),
                    0xFF000000 | plan.labelColor(), false);
        }
    }

    private RadiationHud() {
    }
}

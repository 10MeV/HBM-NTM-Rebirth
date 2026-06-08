package com.hbm.ntm.client;

import com.hbm.ntm.config.HbmClientConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class NukeHudEffects {
    private static final long FLASH_DURATION_MS = 5_000L;
    private static final long SHAKE_DURATION_MS = 1_500L;
    private static long flashTimestamp;
    private static long shakeTimestamp;

    public static void triggerFlash() {
        long now = System.currentTimeMillis();
        if (now - flashTimestamp > 1_000L) {
            flashTimestamp = now;
        }
    }

    public static boolean triggerShake() {
        long now = System.currentTimeMillis();
        if (now - shakeTimestamp > 1_000L) {
            shakeTimestamp = now;
            return true;
        }
        return false;
    }

    public static boolean hasFlash() {
        return HbmClientConfig.nukeHudFlash() && remainingFlash() > 0L;
    }

    public static boolean hasShake() {
        return HbmClientConfig.nukeHudShake() && remainingShake() > 0L;
    }

    public static void renderFlash(GuiGraphics graphics, int width, int height) {
        long remaining = remainingFlash();
        if (remaining <= 0L) {
            return;
        }

        float brightness = remaining / (float) FLASH_DURATION_MS;
        int alpha = Mth.clamp((int) (brightness * 255.0F), 0, 255);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        graphics.fill(0, 0, width, height, (alpha << 24) | 0xFFFFFF);
        graphics.flush();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public static void translateShake(GuiGraphics graphics) {
        long remaining = remainingShake();
        if (remaining <= 0L) {
            return;
        }

        double mult = remaining / (double) SHAKE_DURATION_MS * 2.0D;
        long now = System.currentTimeMillis();
        double horizontal = Mth.clamp(Math.sin(now * 0.02D), -0.7D, 0.7D) * 15.0D;
        double vertical = Mth.clamp(Math.sin(now * 0.01D + 2.0D), -0.7D, 0.7D) * 3.0D;
        graphics.pose().translate((float) (horizontal * mult), (float) (vertical * mult), 0.0F);
    }

    public static void clearAll() {
        flashTimestamp = 0L;
        shakeTimestamp = 0L;
    }

    private static long remainingFlash() {
        return flashTimestamp + FLASH_DURATION_MS - System.currentTimeMillis();
    }

    private static long remainingShake() {
        return shakeTimestamp + SHAKE_DURATION_MS - System.currentTimeMillis();
    }

    private NukeHudEffects() {
    }
}

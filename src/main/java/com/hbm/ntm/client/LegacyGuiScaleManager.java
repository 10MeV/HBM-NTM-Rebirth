package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Keeps tall legacy HBM container canvases entirely visible without placing
 * their slots, mouse coordinates, tooltips, or JEI overlays in a second
 * coordinate system.
 */
@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LegacyGuiScaleManager {
    private static final String HBM_SCREEN_PACKAGE = "com.hbm.ntm.client.screen";
    private static final double SCREEN_MARGIN = 4.0D;
    private static final double SCALE_EPSILON = 1.0E-6D;

    private static Screen managedScreen;
    private static double restoreGuiScale = -1.0D;
    private static double appliedGuiScale = -1.0D;
    private static boolean resizing;

    private LegacyGuiScaleManager() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.isCanceled() || resizing) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        restoreAppliedScale(minecraft);
        Screen newScreen = event.getNewScreen();
        if (newScreen instanceof AbstractContainerScreen<?> container && isHbmContainerScreen(newScreen)) {
            reconcileScale(minecraft, container, false);
        }
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == managedScreen) {
            restoreAppliedScale(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || resizing) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Screen currentScreen = minecraft.screen;
        if (!(currentScreen instanceof AbstractContainerScreen<?> container)
                || !isHbmContainerScreen(currentScreen)) {
            if (managedScreen != null) {
                restoreAppliedScale(minecraft);
            }
            return;
        }

        if (currentScreen != managedScreen) {
            restoreAppliedScale(minecraft);
        }
        reconcileScale(minecraft, container, true);
    }

    private static boolean isHbmContainerScreen(Screen screen) {
        return screen.getClass().getPackageName().equals(HBM_SCREEN_PACKAGE);
    }

    private static void reconcileScale(Minecraft minecraft, AbstractContainerScreen<?> screen,
            boolean resizeInitializedScreen) {
        Window window = minecraft.getWindow();
        double preferredScale = window.calculateScale(minecraft.options.guiScale().get(),
                minecraft.isEnforceUnicode());
        double maximumWholeScale = Math.max(1.0D, Math.floor(Math.min(
                window.getWidth() / (screen.getXSize() + SCREEN_MARGIN * 2.0D),
                window.getHeight() / (screen.getYSize() + SCREEN_MARGIN * 2.0D))));
        double targetScale = Math.min(preferredScale, maximumWholeScale);
        boolean reduced = targetScale + SCALE_EPSILON < preferredScale;

        managedScreen = screen;
        restoreGuiScale = preferredScale;
        appliedGuiScale = reduced ? targetScale : -1.0D;

        if (sameScale(window.getGuiScale(), targetScale)) {
            return;
        }

        window.setGuiScale(targetScale);
        if (resizeInitializedScreen) {
            resizeInitializedScreen(minecraft, screen);
        }
    }

    private static void resizeInitializedScreen(Minecraft minecraft, AbstractContainerScreen<?> screen) {
        Window window = minecraft.getWindow();
        resizing = true;
        try {
            screen.resize(minecraft, window.getGuiScaledWidth(), window.getGuiScaledHeight());
            ForgeHooksClient.resizeGuiLayers(minecraft, window.getGuiScaledWidth(), window.getGuiScaledHeight());
        } finally {
            resizing = false;
        }
    }

    private static void restoreAppliedScale(Minecraft minecraft) {
        if (managedScreen == null) {
            clearState();
            return;
        }

        Window window = minecraft.getWindow();
        if (appliedGuiScale > 0.0D && restoreGuiScale > 0.0D
                && sameScale(window.getGuiScale(), appliedGuiScale)) {
            window.setGuiScale(restoreGuiScale);
        }
        clearState();
    }

    private static boolean sameScale(double left, double right) {
        return Math.abs(left - right) < SCALE_EPSILON;
    }

    private static void clearState() {
        managedScreen = null;
        restoreGuiScale = -1.0D;
        appliedGuiScale = -1.0D;
    }
}

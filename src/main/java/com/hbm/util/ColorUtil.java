package com.hbm.util;

import java.util.HashMap;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for color helpers.
 */
@Deprecated(forRemoval = false)
public final class ColorUtil {
    public static final HashMap<String, Integer> nameToColor = new HashMap<>(com.hbm.ntm.util.ColorUtil.nameToColor);

    private ColorUtil() {
    }

    public static int color(int red, int green, int blue) {
        return com.hbm.ntm.util.ColorUtil.color(red, green, blue);
    }

    public static int ir(int color) {
        return com.hbm.ntm.util.ColorUtil.ir(color);
    }

    public static int ig(int color) {
        return com.hbm.ntm.util.ColorUtil.ig(color);
    }

    public static int ib(int color) {
        return com.hbm.ntm.util.ColorUtil.ib(color);
    }

    public static float fr(int color) {
        return com.hbm.ntm.util.ColorUtil.fr(color);
    }

    public static float fg(int color) {
        return com.hbm.ntm.util.ColorUtil.fg(color);
    }

    public static float fb(int color) {
        return com.hbm.ntm.util.ColorUtil.fb(color);
    }

    public static boolean isColorColorful(int hex) {
        return com.hbm.ntm.util.ColorUtil.isColorColorful(hex);
    }

    public static int amplifyColor(int hex) {
        return com.hbm.ntm.util.ColorUtil.amplifyColor(hex);
    }

    public static int amplifyColor(int hex, int limit) {
        return com.hbm.ntm.util.ColorUtil.amplifyColor(hex, limit);
    }

    public static int lightenColor(int hex, double percent) {
        return com.hbm.ntm.util.ColorUtil.lightenColor(hex, percent);
    }

    public static double getColorBrightness(int hex) {
        return com.hbm.ntm.util.ColorUtil.getColorBrightness(hex);
    }

    public static int getColorFromDye(ItemStack stack) {
        return com.hbm.ntm.util.ColorUtil.getColorFromDye(stack);
    }
}

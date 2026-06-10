package com.hbm.ntm.util;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Locale;

/**
 * Legacy-name facade for common color helpers.
 */
@Deprecated(forRemoval = false)
public final class ColorUtil {
    public static final HashMap<String, Integer> nameToColor = new HashMap<>(HbmColorUtil.NAME_TO_COLOR);

    private ColorUtil() {
    }

    public static int color(int red, int green, int blue) {
        return HbmColorUtil.color(red, green, blue);
    }

    public static int ir(int color) {
        return HbmColorUtil.ir(color);
    }

    public static int ig(int color) {
        return HbmColorUtil.ig(color);
    }

    public static int ib(int color) {
        return HbmColorUtil.ib(color);
    }

    public static float fr(int color) {
        return HbmColorUtil.fr(color);
    }

    public static float fg(int color) {
        return HbmColorUtil.fg(color);
    }

    public static float fb(int color) {
        return HbmColorUtil.fb(color);
    }

    public static boolean isColorColorful(int hex) {
        return HbmColorUtil.isColorColorful(hex);
    }

    public static int amplifyColor(int hex) {
        return HbmColorUtil.amplifyColor(hex);
    }

    public static int amplifyColor(int hex, int limit) {
        return HbmColorUtil.amplifyColor(hex, limit);
    }

    public static int lightenColor(int hex, double percent) {
        return HbmColorUtil.lightenColor(hex, percent);
    }

    public static double getColorBrightness(int hex) {
        return HbmColorUtil.getColorBrightness(hex);
    }

    public static int getColorFromDye(ItemStack stack) {
        for (String tagName : HbmItemStackUtil.getOreDictNames(stack)) {
            String color = legacyDyeColorName(tagName);
            if (color != null && nameToColor.containsKey(color)) {
                return nameToColor.get(color);
            }
        }
        return 0;
    }

    private static String legacyDyeColorName(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            return null;
        }
        String normalized = tagName.toLowerCase(Locale.US);
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < normalized.length()) {
            return normalized.substring(slash + 1).replace("_", "");
        }
        int colon = normalized.lastIndexOf(':');
        String tail = colon >= 0 ? normalized.substring(colon + 1) : normalized;
        return tail.startsWith("dye") && tail.length() > 3 ? tail.substring(3) : tail;
    }
}

package com.hbm.ntm.util;

import java.awt.Color;
import java.util.Locale;
import java.util.Map;

public final class HbmColorUtil {
    public static final Map<String, Integer> NAME_TO_COLOR = Map.ofEntries(
            Map.entry("black", 1973019),
            Map.entry("red", 11743532),
            Map.entry("green", 3887386),
            Map.entry("brown", 5320730),
            Map.entry("blue", 2437522),
            Map.entry("purple", 8073150),
            Map.entry("cyan", 2651799),
            Map.entry("silver", 11250603),
            Map.entry("lightgray", 11250603),
            Map.entry("gray", 4408131),
            Map.entry("pink", 14188952),
            Map.entry("lime", 4312372),
            Map.entry("yellow", 14602026),
            Map.entry("lightblue", 6719955),
            Map.entry("magenta", 12801229),
            Map.entry("orange", 15435844),
            Map.entry("white", 15790320));

    private HbmColorUtil() {
    }

    public static int color(int red, int green, int blue) {
        return ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
    }

    public static int ir(int color) {
        return (color & 0xff0000) >> 16;
    }

    public static int ig(int color) {
        return (color & 0x00ff00) >> 8;
    }

    public static int ib(int color) {
        return color & 0x0000ff;
    }

    public static float fr(int color) {
        return ir(color) / 255.0F;
    }

    public static float fg(int color) {
        return ig(color) / 255.0F;
    }

    public static float fb(int color) {
        return ib(color) / 255.0F;
    }

    public static boolean isColorColorful(int hex) {
        float[] hsb = Color.RGBtoHSB(ir(hex), ig(hex), ib(hex), new float[3]);
        return hsb[1] > 0.25F && hsb[2] > 0.25F;
    }

    public static int amplifyColor(int hex) {
        return amplifyColor(hex, 255);
    }

    public static int amplifyColor(int hex, int limit) {
        int red = ir(hex);
        int green = ig(hex);
        int blue = ib(hex);
        int max = Math.max(Math.max(1, red), Math.max(green, blue));
        return color(red * limit / max, green * limit / max, blue * limit / max);
    }

    public static int lightenColor(int hex, double percent) {
        int red = (int) (ir(hex) + (255 - ir(hex)) * percent);
        int green = (int) (ig(hex) + (255 - ig(hex)) * percent);
        int blue = (int) (ib(hex) + (255 - ib(hex)) * percent);
        return color(red, green, blue);
    }

    public static double getColorBrightness(int hex) {
        float[] hsb = Color.RGBtoHSB(ir(hex), ig(hex), ib(hex), new float[3]);
        return hsb[2];
    }

    public static int colorFromDyeName(String name) {
        if (name == null) {
            return 0;
        }
        return NAME_TO_COLOR.getOrDefault(name.toLowerCase(Locale.US), 0);
    }
}

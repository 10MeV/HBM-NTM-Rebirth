package com.hbm.ntm.client.obj;

import java.awt.Color;

public final class LegacyRenderColor {
    public static int color(int red, int green, int blue) {
        return clamp(red) << 16 | clamp(green) << 8 | clamp(blue);
    }

    public static int color(float red, float green, float blue) {
        return color(Math.round(red * 255.0F), Math.round(green * 255.0F), Math.round(blue * 255.0F));
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return clamp(alpha) << 24 | color(red, green, blue);
    }

    public static int withAlpha(int rgb, int alpha) {
        return clamp(alpha) << 24 | rgb & 0xFFFFFF;
    }

    public static int red(int color) {
        return color >> 16 & 255;
    }

    public static int green(int color) {
        return color >> 8 & 255;
    }

    public static int blue(int color) {
        return color & 255;
    }

    public static int alpha(int argb) {
        return argb >>> 24 & 255;
    }

    public static float redF(int color) {
        return red(color) / 255.0F;
    }

    public static float greenF(int color) {
        return green(color) / 255.0F;
    }

    public static float blueF(int color) {
        return blue(color) / 255.0F;
    }

    public static float alphaF(int argb) {
        return alpha(argb) / 255.0F;
    }

    public static int legacyColor3ub(byte red, byte green, byte blue) {
        return (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }

    public static int multiply(int left, int right) {
        return color(red(left) * red(right) / 255, green(left) * green(right) / 255, blue(left) * blue(right) / 255);
    }

    public static int scale(int color, float scale) {
        return color(Math.round(red(color) * scale), Math.round(green(color) * scale), Math.round(blue(color) * scale));
    }

    public static int lerp(int from, int to, float t) {
        float clamped = Math.max(0.0F, Math.min(1.0F, t));
        return color(
                Math.round(red(from) + (red(to) - red(from)) * clamped),
                Math.round(green(from) + (green(to) - green(from)) * clamped),
                Math.round(blue(from) + (blue(to) - blue(from)) * clamped));
    }

    public static int lerpArgb(int from, int to, float t) {
        float clamped = Math.max(0.0F, Math.min(1.0F, t));
        return argb(
                Math.round(alpha(from) + (alpha(to) - alpha(from)) * clamped),
                Math.round(red(from) + (red(to) - red(from)) * clamped),
                Math.round(green(from) + (green(to) - green(from)) * clamped),
                Math.round(blue(from) + (blue(to) - blue(from)) * clamped));
    }

    public static ColorComponents components(int argb) {
        return new ColorComponents(red(argb), green(argb), blue(argb), alpha(argb),
                redF(argb), greenF(argb), blueF(argb), alphaF(argb));
    }

    public static ColorMultiplyPlan multiplyPlan(int left, int right) {
        return new ColorMultiplyPlan(left, right, multiply(left, right));
    }

    public static ColorAlphaPlan alphaPlan(int rgb, int alpha) {
        return new ColorAlphaPlan(rgb & 0xFFFFFF, clamp(alpha), withAlpha(rgb, alpha));
    }

    public static int lightenColor(int rgb, double percent) {
        int red = (int) (red(rgb) + (255 - red(rgb)) * percent);
        int green = (int) (green(rgb) + (255 - green(rgb)) * percent);
        int blue = (int) (blue(rgb) + (255 - blue(rgb)) * percent);
        return color(red, green, blue);
    }

    public static int amplifyColor(int rgb) {
        return amplifyColor(rgb, 255);
    }

    public static int amplifyColor(int rgb, int limit) {
        int red = red(rgb);
        int green = green(rgb);
        int blue = blue(rgb);
        int max = Math.max(Math.max(1, red), Math.max(green, blue));
        return color(red * limit / max, green * limit / max, blue * limit / max);
    }

    public static boolean isColorful(int rgb) {
        float[] hsb = Color.RGBtoHSB(red(rgb), green(rgb), blue(rgb), new float[3]);
        return hsb[1] > 0.25F && hsb[2] > 0.25F;
    }

    public static double brightness(int rgb) {
        float[] hsb = Color.RGBtoHSB(red(rgb), green(rgb), blue(rgb), new float[3]);
        return hsb[2];
    }

    public static int anaglyph(int rgb) {
        int red = red(rgb);
        int green = green(rgb);
        int blue = blue(rgb);
        int anaglyphRed = (red * 30 + green * 59 + blue * 11) / 100;
        int anaglyphGreen = (red * 30 + green * 70) / 100;
        int anaglyphBlue = (red * 30 + blue * 70) / 100;
        return color(anaglyphRed, anaglyphGreen, anaglyphBlue);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public record ColorComponents(int red, int green, int blue, int alpha,
                                  float redFloat, float greenFloat, float blueFloat, float alphaFloat) {
    }

    public record ColorMultiplyPlan(int left, int right, int result) {
    }

    public record ColorAlphaPlan(int rgb, int alpha, int argb) {
    }

    private LegacyRenderColor() {
    }
}

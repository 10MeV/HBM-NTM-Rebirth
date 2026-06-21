package com.hbm.ntm.fluid.trait;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class FluidTooltipUtil {
    public static String shortNumber(long value) {
        long abs = Math.abs(value);
        double result;
        String suffix;
        if (abs >= 1_000_000_000_000_000_000L) {
            result = value / 1_000_000_000_000_000_000.0D;
            suffix = "E";
        } else if (abs >= 1_000_000_000_000_000L) {
            result = value / 1_000_000_000_000_000.0D;
            suffix = "P";
        } else if (abs >= 1_000_000_000_000L) {
            result = value / 1_000_000_000_000.0D;
            suffix = "T";
        } else if (abs >= 1_000_000_000L) {
            result = value / 1_000_000_000.0D;
            suffix = "G";
        } else if (abs >= 1_000_000L) {
            result = value / 1_000_000.0D;
            suffix = "M";
        } else if (abs >= 1_000L) {
            result = value / 1_000.0D;
            suffix = "k";
        } else {
            return Long.toString(value);
        }

        double rounded = result <= -100.0D ? Math.round(result * 10.0D) / 10.0D : Math.round(result * 100.0D) / 100.0D;
        return String.format(Locale.US, "%s%s", rounded, suffix);
    }

    public static MutableComponent efficiency(String label, double efficiency) {
        return Component.literal("[" + label + "] ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("Efficiency: " + (int) (efficiency * 100.0D) + "%")
                        .withStyle(ChatFormatting.AQUA));
    }

    private FluidTooltipUtil() {
    }
}

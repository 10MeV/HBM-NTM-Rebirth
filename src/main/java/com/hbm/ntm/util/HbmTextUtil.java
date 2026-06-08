package com.hbm.ntm.util;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntFunction;

public final class HbmTextUtil {
    public static final String MANUAL_LINE_BREAK = "$";

    private HbmTextUtil() {
    }

    public static String[] resolveKeyArray(String key, Object... args) {
        return splitManualLines(Component.translatable(key, args).getString()).toArray(String[]::new);
    }

    public static List<String> splitManualLines(String text) {
        if (text == null || text.isEmpty()) {
            return List.of("");
        }
        return Arrays.asList(text.split("\\$"));
    }

    public static List<String> autoBreakWithParagraphs(String text, int width, ToIntFunction<String> widthResolver) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : splitManualLines(text)) {
            lines.addAll(autoBreak(paragraph, width, widthResolver));
        }
        return lines;
    }

    public static List<String> autoBreak(String text, int width, ToIntFunction<String> widthResolver) {
        if (text == null || text.isEmpty()) {
            return List.of("");
        }
        if (widthResolver == null || width <= 0) {
            return List.of(text);
        }

        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();
        lines.add(words[0]);
        int currentWidth = widthResolver.applyAsInt(words[0]);

        for (int index = 1; index < words.length; index++) {
            String wordWithSpace = " " + words[index];
            int nextWidth = currentWidth + widthResolver.applyAsInt(wordWithSpace);
            if (nextWidth <= width) {
                int last = lines.size() - 1;
                lines.set(last, lines.get(last) + wordWithSpace);
                currentWidth = nextWidth;
            } else {
                lines.add(words[index]);
                currentWidth = widthResolver.applyAsInt(words[index]);
            }
        }
        return lines;
    }

    public static List<String> autoBreakByCharacters(String text, int width) {
        return autoBreak(text, width, String::length);
    }

    public static List<String> autoBreakWithParagraphsByCharacters(String text, int width) {
        return autoBreakWithParagraphs(text, width, String::length);
    }

    public static String shortNumber(long value) {
        double result;
        String suffix;
        double abs = Math.abs((double) value);
        if (abs >= 1_000_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000_000.0D;
            suffix = "E";
        } else if (abs >= 1_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000.0D;
            suffix = "P";
        } else if (abs >= 1_000_000_000_000.0D) {
            result = value / 1_000_000_000_000.0D;
            suffix = "T";
        } else if (abs >= 1_000_000_000.0D) {
            result = value / 1_000_000_000.0D;
            suffix = "G";
        } else if (abs >= 1_000_000.0D) {
            result = value / 1_000_000.0D;
            suffix = "M";
        } else if (abs >= 1_000.0D) {
            result = value / 1_000.0D;
            suffix = "k";
        } else {
            return Long.toString(value);
        }

        double rounded = result <= -100.0D ? Math.round(result * 10.0D) / 10.0D
                : Math.round(result * 100.0D) / 100.0D;
        return String.format(Locale.US, "%s%s", rounded, suffix);
    }
}

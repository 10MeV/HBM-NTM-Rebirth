package com.hbm.util.i18n;

import java.util.List;

/**
 * Legacy 1.7.10 package bridge for text translation helpers.
 */
@Deprecated(forRemoval = false)
public final class I18nUtil {
    private I18nUtil() {
    }

    public static String resolveKey(String key, Object... args) {
        return com.hbm.ntm.util.i18n.I18nUtil.resolveKey(key, args);
    }

    public static String format(String key, Object... args) {
        return com.hbm.ntm.util.i18n.I18nUtil.format(key, args);
    }

    public static String[] resolveKeyArray(String key, Object... args) {
        return com.hbm.ntm.util.i18n.I18nUtil.resolveKeyArray(key, args);
    }

    public static List<String> autoBreakWithParagraphs(Object fontRenderer, String text, int width) {
        return com.hbm.ntm.util.i18n.I18nUtil.autoBreakWithParagraphs(fontRenderer, text, width);
    }

    public static List<String> autoBreak(Object fontRenderer, String text, int width) {
        return com.hbm.ntm.util.i18n.I18nUtil.autoBreak(fontRenderer, text, width);
    }
}

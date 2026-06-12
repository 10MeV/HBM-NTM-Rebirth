package com.hbm.ntm.util.i18n;

import com.hbm.ntm.util.HbmTextUtil;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Legacy-name i18n facade.
 */
@Deprecated(forRemoval = false)
public final class I18nUtil {
    private I18nUtil() {
    }

    public static String resolveKey(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    public static String format(String key, Object... args) {
        return resolveKey(key, args);
    }

    public static String[] resolveKeyArray(String key, Object... args) {
        return HbmTextUtil.splitManualLines(resolveKey(key, args)).toArray(String[]::new);
    }

    public static List<String> autoBreakWithParagraphs(Object fontRenderer, String text, int width) {
        return HbmTextUtil.autoBreakWithParagraphsByCharacters(text, width);
    }

    public static List<String> autoBreak(Object fontRenderer, String text, int width) {
        return HbmTextUtil.autoBreakByCharacters(text, width);
    }
}

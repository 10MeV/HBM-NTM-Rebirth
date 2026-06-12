package com.hbm.ntm.util.i18n;

import java.util.List;

/**
 * Legacy-name i18n strategy interface.
 */
@Deprecated(forRemoval = false)
public interface ITranslate {
    String resolveKey(String s, Object... args);

    String[] resolveKeyArray(String s, Object... args);

    List<String> autoBreakWithParagraphs(Object fontRenderer, String text, int width);

    List<String> autoBreak(Object fontRenderer, String text, int width);
}

package com.hbm.ntm.util.i18n;

import java.util.List;

/**
 * Legacy-name server-side i18n sentinel.
 */
@Deprecated(forRemoval = false)
public class I18nServer implements ITranslate {
    public static String SARCASTIC_MESSAGE = "I18N CALL SERVERSIDE - GREAT JOB";

    @Override
    public String resolveKey(String s, Object... args) {
        return SARCASTIC_MESSAGE;
    }

    @Override
    public String[] resolveKeyArray(String s, Object... args) {
        return new String[]{SARCASTIC_MESSAGE};
    }

    @Override
    public List<String> autoBreakWithParagraphs(Object fontRenderer, String text, int width) {
        return List.of(SARCASTIC_MESSAGE);
    }

    @Override
    public List<String> autoBreak(Object fontRenderer, String text, int width) {
        return List.of(SARCASTIC_MESSAGE);
    }
}

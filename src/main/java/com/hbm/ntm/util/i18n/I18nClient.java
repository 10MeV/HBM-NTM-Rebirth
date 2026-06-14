package com.hbm.ntm.util.i18n;

import com.hbm.ntm.util.HbmTextUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Legacy-name client-side i18n strategy.
 */
@OnlyIn(Dist.CLIENT)
@Deprecated(forRemoval = false)
public class I18nClient implements ITranslate {
    @Override
    public String resolveKey(String s, Object... args) {
        return I18n.get(s, args);
    }

    @Override
    public String[] resolveKeyArray(String s, Object... args) {
        return HbmTextUtil.splitManualLines(resolveKey(s, args)).toArray(String[]::new);
    }

    @Override
    public List<String> autoBreakWithParagraphs(Object fontRenderer, String text, int width) {
        return HbmTextUtil.autoBreakWithParagraphs(text, width, value -> stringWidth(fontRenderer, value));
    }

    @Override
    public List<String> autoBreak(Object fontRenderer, String text, int width) {
        return HbmTextUtil.autoBreak(text, width, value -> stringWidth(fontRenderer, value));
    }

    private static int stringWidth(Object fontRenderer, String text) {
        if (fontRenderer instanceof Font font) {
            return font.width(text);
        }
        return text == null ? 0 : text.length();
    }
}

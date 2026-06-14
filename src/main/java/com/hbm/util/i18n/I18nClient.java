package com.hbm.util.i18n;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Legacy 1.7.10 package bridge for client-side i18n behavior.
 */
@OnlyIn(Dist.CLIENT)
@Deprecated(forRemoval = false)
public class I18nClient extends com.hbm.ntm.util.i18n.I18nClient implements ITranslate {
}

package com.hbm.main;

import com.hbm.util.i18n.I18nClient;
import com.hbm.util.i18n.ITranslate;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side legacy proxy facade. Detailed renderer registration stays in
 * modern client setup classes.
 */
@Deprecated(forRemoval = false)
public class ClientProxy extends ServerProxy {
    private static final I18nClient I18N = new I18nClient();

    @Override
    public ITranslate getI18n() {
        return I18N;
    }

    @Override
    public Player me() {
        return Minecraft.getInstance().player;
    }

    @Override
    public String getLanguageCode() {
        return Minecraft.getInstance().getLanguageManager().getSelected();
    }
}

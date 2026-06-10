package com.hbm.ntm.bullet;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public record BulletAmmo(ResourceLocation itemId, int metadata) {
    public static final BulletAmmo NOTHING = legacyItem("nothing");

    public static BulletAmmo legacyItem(String legacyName) {
        return new BulletAmmo(new ResourceLocation(HbmNtm.MOD_ID, sanitizeLegacyPath(legacyName)), 0);
    }

    private static String sanitizeLegacyPath(String legacyName) {
        String source = legacyName == null ? "" : legacyName.trim().toLowerCase(Locale.ROOT).replace(':', '_');
        if (source.isEmpty()) {
            return "nothing";
        }
        StringBuilder path = new StringBuilder(source.length());
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            path.append(isAllowedPathChar(c) ? c : '_');
        }
        return path.toString();
    }

    private static boolean isAllowedPathChar(char c) {
        return c >= 'a' && c <= 'z'
                || c >= '0' && c <= '9'
                || c == '/' || c == '.' || c == '_' || c == '-';
    }
}

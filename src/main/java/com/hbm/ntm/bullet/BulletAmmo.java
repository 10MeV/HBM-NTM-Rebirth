package com.hbm.ntm.bullet;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public record BulletAmmo(ResourceLocation itemId, int metadata) {
    public static final BulletAmmo NOTHING = legacyItem("nothing");

    public static BulletAmmo legacyItem(String legacyName) {
        return new BulletAmmo(new ResourceLocation(HbmNtm.MOD_ID, legacyName), 0);
    }
}

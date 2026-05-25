package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjArmorModels {
    public static final LegacyWavefrontModel PLAYER_FEM = model("player_fem").noSmooth();
    public static final LegacyWavefrontModel MOD_TESLA = model("mod_tesla").asVBO();

    public static final ResourceLocation MOD_TESLA_TEXTURE = texture("mod_tesla");

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/armor/" + name + ".obj"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/armor/" + name + ".png");
    }

    private ObjArmorModels() {
    }
}

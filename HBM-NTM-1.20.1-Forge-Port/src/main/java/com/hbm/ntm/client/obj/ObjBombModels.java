package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjBombModels {
    public static final LegacyWavefrontModel MINE_AP = model("ap_mine", "mine_ap_grass");
    public static final LegacyWavefrontModel MINE_MARELET = model("marelet", "mine_marelet");
    public static final LegacyWavefrontModel MINE_FAT = model("mine_fat", "mine_fat");
    public static final LegacyWavefrontModel MINE_NAVAL = model("naval_mine", "naval_mine");
    public static final LegacyWavefrontModel FAT_MAN = model("fat_man", "fat_man");
    public static final LegacyWavefrontModel FLEIJA = model("fleija", "fleija");
    public static final LegacyWavefrontModel GADGET = model("gadget", "gadget");
    public static final LegacyWavefrontModel IVYMIKE = model("ivymike", "ivymike");
    public static final LegacyWavefrontModel TSAR = model("tsar", "tsar");
    public static final LegacyWavefrontModel UFP = model("ufp", "ufp");
    public static final LegacyWavefrontModel N2 = model("n2", "n2");
    public static final LegacyWavefrontModel FSTBMB = model("fstbmb", "fstbmb");
    public static final LegacyWavefrontModel DUD_BALEFIRE = model("dud_balefire", "dud_balefire");
    public static final LegacyWavefrontModel DUD_CONVENTIONAL = model("dud_conventional", "dud_conventional");
    public static final LegacyWavefrontModel DUD_NUKE = model("dud_nuke", "dud_nuke");
    public static final LegacyWavefrontModel DUD_SALTED = model("dud_salted", "dud_salted");

    public static final ResourceLocation MINE_AP_GRASS_TEXTURE = texture("mine_ap_grass");
    public static final ResourceLocation MINE_AP_DESERT_TEXTURE = texture("mine_ap_desert");
    public static final ResourceLocation MINE_AP_SNOW_TEXTURE = texture("mine_ap_snow");
    public static final ResourceLocation MINE_AP_STONE_TEXTURE = texture("mine_ap_stone");
    public static final ResourceLocation MINE_SHRAP_TEXTURE = texture("mine_shrapnel");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/bombs/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/bombs/" + name + ".png");
    }

    private ObjBombModels() {
    }
}

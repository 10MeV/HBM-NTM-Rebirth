package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjBombModels {
    public static final LegacyWavefrontModel MINE_AP = model("ap_mine", "mine_ap_grass").asVBO();
    public static final LegacyWavefrontModel MINE_MARELET = model("marelet", "mine_marelet").asVBO();
    public static final LegacyWavefrontModel MINE_FAT = rootModel("mine_fat", rootTexture("mine_fat"));
    public static final LegacyWavefrontModel MINE_NAVAL = model("naval_mine", rootTexture("nmine")).asVBO();
    public static final LegacyWavefrontModel FAT_MAN = model("fat_man", rootTexture("fat_man")).asVBO();
    public static final LegacyWavefrontModel FLEIJA = model("fleija", "fleija").asVBO();
    public static final LegacyWavefrontModel GADGET = model("gadget", "gadget").asVBO();
    public static final LegacyWavefrontModel IVYMIKE = model("ivymike", "ivymike");
    public static final LegacyWavefrontModel TSAR = model("tsar", "tsar").asVBO();
    public static final LegacyWavefrontModel UFP = model("ufp", "ufp").asVBO();
    public static final LegacyWavefrontModel N2 = model("n2", "n2").asVBO();
    public static final LegacyWavefrontModel FSTBMB = model("fstbmb", "fstbmb").asVBO();
    public static final LegacyWavefrontModel DUD_BALEFIRE = model("dud_balefire", "dud_balefire").asVBO();
    public static final LegacyWavefrontModel DUD_CONVENTIONAL = model("dud_conventional", "dud_conventional").asVBO();
    public static final LegacyWavefrontModel DUD_NUKE = model("dud_nuke", "dud_nuke").asVBO();
    public static final LegacyWavefrontModel DUD_SALTED = model("dud_salted", "dud_salted").asVBO();

    public static final ResourceLocation MINE_AP_GRASS_TEXTURE = texture("mine_ap_grass");
    public static final ResourceLocation MINE_AP_DESERT_TEXTURE = texture("mine_ap_desert");
    public static final ResourceLocation MINE_AP_SNOW_TEXTURE = texture("mine_ap_snow");
    public static final ResourceLocation MINE_AP_STONE_TEXTURE = texture("mine_ap_stone");
    public static final ResourceLocation MINE_SHRAP_TEXTURE = texture("mine_shrapnel");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return model(modelName, texture(textureName));
    }

    public static LegacyWavefrontModel model(String modelName, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/bombs/" + modelName + ".obj"),
                texture);
    }

    public static LegacyWavefrontModel rootModel(String modelName, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/" + modelName + ".obj"),
                texture);
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/bombs/" + name + ".png");
    }

    public static ResourceLocation rootTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
    }

    private ObjBombModels() {
    }
}

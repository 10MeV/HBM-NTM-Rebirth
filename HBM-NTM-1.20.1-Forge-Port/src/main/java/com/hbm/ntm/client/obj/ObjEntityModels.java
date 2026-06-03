package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjEntityModels {
    public static final LegacyWavefrontModel TESLACRAB = model("teslacrab");
    public static final LegacyWavefrontModel MASKMAN = model("maskman");
    public static final LegacyWavefrontModel BLOCKSPIDER = model("blockspider");
    public static final LegacyWavefrontModel TAINTCRAB = model("taintcrab");
    public static final LegacyWavefrontModel UFO = model("ufo");
    public static final LegacyWavefrontModel MINI_UFO = model("mini_ufo");
    public static final LegacyWavefrontModel SIEGE_UFO = model("siege_ufo");
    public static final LegacyWavefrontModel GLYPHID = model("glyphid");
    public static final LegacyWavefrontModel QUADCOPTER = model("quadcopter");
    public static final LegacyWavefrontModel C130 = model("c130", "c130_0").asVBO();
    public static final LegacyWavefrontModel BOXCAR = model("boxcar");
    public static final LegacyWavefrontModel DUCHESS_GAMBIT = model("duchessgambit");
    public static final LegacyWavefrontModel DORNIER = model("dornier", "dornier_1");
    public static final LegacyWavefrontModel B29 = model("b29", "b29_0");

    public static final ResourceLocation TESLACRAB_TEXTURE = texture("teslacrab");
    public static final ResourceLocation MASKMAN_TEXTURE = texture("maskman");
    public static final ResourceLocation BLOCKSPIDER_TEXTURE = texture("blockspider");
    public static final ResourceLocation TAINTCRAB_TEXTURE = texture("taintcrab");
    public static final ResourceLocation TAINTCRAB_CLEAN_TEXTURE = texture("taintcrab_clean");
    public static final ResourceLocation UFO_TEXTURE = texture("ufo");
    public static final ResourceLocation MINI_UFO_TEXTURE = texture("mini_ufo");
    public static final ResourceLocation SIEGE_UFO_TEXTURE = texture("siege_ufo");
    public static final ResourceLocation GLYPHID_TEXTURE = texture("glyphid");
    public static final ResourceLocation GLYPHID_BRAWLER_TEXTURE = texture("glyphid_brawler");
    public static final ResourceLocation GLYPHID_BEHEMOTH_TEXTURE = texture("glyphid_behemoth");
    public static final ResourceLocation GLYPHID_BRENDA_TEXTURE = texture("glyphid_brenda");
    public static final ResourceLocation GLYPHID_BOMBARDIER_TEXTURE = texture("glyphid_bombardier");
    public static final ResourceLocation GLYPHID_BLASTER_TEXTURE = texture("glyphid_blaster");
    public static final ResourceLocation GLYPHID_SCOUT_TEXTURE = texture("glyphid_scout");
    public static final ResourceLocation GLYPHID_NUCLEAR_TEXTURE = texture("glyphid_nuclear");
    public static final ResourceLocation GLYPHID_DIGGER_TEXTURE = texture("glyphid_digger");
    public static final ResourceLocation QUADCOPTER_TEXTURE = texture("quadcopter");
    public static final ResourceLocation C130_TEXTURE = texture("c130_0");
    public static final ResourceLocation BOXCAR_TEXTURE = texture("boxcar");
    public static final ResourceLocation DUCHESS_GAMBIT_TEXTURE = texture("duchessgambit");
    public static final ResourceLocation DORNIER_1_TEXTURE = texture("dornier_1");
    public static final ResourceLocation DORNIER_2_TEXTURE = texture("dornier_2");
    public static final ResourceLocation DORNIER_4_TEXTURE = texture("dornier_4");
    public static final ResourceLocation B29_0_TEXTURE = texture("b29_0");
    public static final ResourceLocation B29_1_TEXTURE = texture("b29_1");
    public static final ResourceLocation B29_2_TEXTURE = texture("b29_2");
    public static final ResourceLocation B29_3_TEXTURE = texture("b29_3");

    public static LegacyWavefrontModel model(String name) {
        return model(name, name);
    }

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/entities/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/entities/" + name + ".png");
    }

    private ObjEntityModels() {
    }
}

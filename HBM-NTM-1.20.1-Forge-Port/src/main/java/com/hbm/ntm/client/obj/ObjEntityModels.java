package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjEntityModels {
    public static final LegacyWavefrontModel TESLACRAB = model("teslacrab");
    public static final LegacyWavefrontModel MASKMAN = model("maskman");
    public static final LegacyWavefrontModel BLOCKSPIDER = model("blockspider");
    public static final LegacyWavefrontModel UFO = model("ufo").asVBO();
    public static final LegacyWavefrontModel MINI_UFO = model("mini_ufo").asVBO();
    public static final LegacyWavefrontModel SIEGE_UFO = model("siege_ufo").asVBO();
    public static final LegacyWavefrontModel GLYPHID = model("glyphid");
    public static final LegacyWavefrontModel QUADCOPTER = model("quadcopter");

    public static final ResourceLocation TESLACRAB_TEXTURE = texture("teslacrab");
    public static final ResourceLocation MASKMAN_TEXTURE = texture("maskman");
    public static final ResourceLocation BLOCKSPIDER_TEXTURE = texture("blockspider");
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

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/entities/" + name + ".obj"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/entities/" + name + ".png");
    }

    private ObjEntityModels() {
    }
}

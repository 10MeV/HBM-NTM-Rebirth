package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjNukeModels {
    public static final LegacyWavefrontModel GADGET = model("gadget", "gadget").asVBO();
    public static final LegacyWavefrontModel BOY = model("boy", "boy");
    public static final LegacyWavefrontModel BOY_LEGACY = model("boy_legacy", "lilboy");
    public static final LegacyWavefrontModel MAN = model("man", "man").asVBO();
    public static final LegacyWavefrontModel TSAR = model("tsar", "tsar").asVBO();
    public static final LegacyWavefrontModel MIKE = model("mike", "mike");
    public static final LegacyWavefrontModel PROTOTYPE = model("prototype", "prototype").asVBO();
    public static final LegacyWavefrontModel FLEIJA = model("fleija", "fleija").asVBO();
    public static final LegacyWavefrontModel SOLINIUM = model("solinium", "solinium").asVBO();
    public static final LegacyWavefrontModel N2 = model("n2", "n2").asVBO();
    public static final LegacyWavefrontModel BOMB_MULTI_LEGACY = model("bomb_multi_legacy", "bomb_multi_legacy");
    public static final ResourceLocation CUSTOM_NUKE_TEXTURE = texture("custom_nuke");
    public static final ResourceLocation GADGET_LEGACY_TEXTURE = texture("gadget_legacy");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/nuke/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/nuke/" + name + ".png");
    }

    private ObjNukeModels() {
    }
}

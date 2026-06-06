package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ObjTrinketModels {
    public static final LegacyWavefrontModel LANTERN = legacyModel("lantern").noSmooth();
    public static final LegacyWavefrontModel BOBBLE_LEGACY = legacyModel("bobble_legacy", "socket");
    public static final LegacyWavefrontModel YOMI_LEGACY = legacyModel("yomi_legacy", "yomi").asVBO();
    public static final LegacyWavefrontModel HUNDUN_LEGACY = legacyModel("hundun_legacy", "hundun").asVBO();
    public static final LegacyWavefrontModel DERG_LEGACY = legacyModel("derg_legacy", "derg").asVBO();
    public static final LegacyWavefrontModel SNOWGLOBE_LEGACY = legacyModel("snowglobe_legacy", "snowglobe").asVBO();
    public static final LegacyWavefrontModel CHIP_LEGACY = legacyModel("chip_legacy", "chip_gold").asVBO();

    public static final ResourceLocation LANTERN_TEXTURE = texture("lantern");
    public static final ResourceLocation LANTERN_RUSTY_TEXTURE = texture("lantern_rusty");
    public static final ResourceLocation BOBBLE_SOCKET_TEXTURE = texture("socket");
    public static final ResourceLocation BOBBLE_GLOW_TEXTURE = texture("glow");
    public static final ResourceLocation BOBBLE_VAULTBOY_TEXTURE = texture("vaultboy");
    public static final ResourceLocation BOBBLE_HBM_TEXTURE = texture("hbm");
    public static final ResourceLocation BOBBLE_PU238_TEXTURE = texture("pellet");
    public static final ResourceLocation BOBBLE_FRIZZLE_TEXTURE = texture("frizzle");
    public static final ResourceLocation BOBBLE_VT_TEXTURE = texture("vt");
    public static final ResourceLocation BOBBLE_DOC_TEXTURE = texture("doctor17ph");
    public static final ResourceLocation BOBBLE_BLUE_TEXTURE = texture("thebluehat");
    public static final ResourceLocation BOBBLE_PHEO_TEXTURE = texture("pheo");
    public static final ResourceLocation BOBBLE_ADAM_TEXTURE = texture("adam29");
    public static final ResourceLocation BOBBLE_UFFR_TEXTURE = texture("uffr");
    public static final ResourceLocation BOBBLE_VAER_TEXTURE = texture("vaer");
    public static final ResourceLocation BOBBLE_NOS_TEXTURE = texture("nos");
    public static final ResourceLocation BOBBLE_DRILLGON_TEXTURE = texture("drillgon200");
    public static final ResourceLocation BOBBLE_CIRNO_TEXTURE = texture("cirno");
    public static final ResourceLocation BOBBLE_MICROWAVE_TEXTURE = texture("microwave");
    public static final ResourceLocation BOBBLE_PEEP_TEXTURE = texture("peep");
    public static final ResourceLocation BOBBLE_MELLOW_TEXTURE = texture("mellowrpg8");
    public static final ResourceLocation BOBBLE_MELLOW_GLOW_TEXTURE = texture("mellowrpg8_glow");
    public static final ResourceLocation BOBBLE_ABEL_TEXTURE = texture("abel");
    public static final ResourceLocation BOBBLE_ABEL_GLOW_TEXTURE = texture("abel_glow");
    public static final ResourceLocation YOMI_TEXTURE = texture("yomi");
    public static final ResourceLocation HUNDUN_TEXTURE = texture("hundun");
    public static final ResourceLocation DERG_TEXTURE = texture("derg");
    public static final ResourceLocation SNOWGLOBE_SOCKET_TEXTURE = texture("snowglobe");
    public static final ResourceLocation SNOWGLOBE_GLASS_TEXTURE = texture("snowglobe_glass");
    public static final ResourceLocation SNOWGLOBE_FEATURES_TEXTURE = texture("snowglobe_features");
    public static final ResourceLocation CHIP_TEXTURE = texture("chip");
    public static final ResourceLocation CHIP_GOLD_TEXTURE = texture("chip_gold");
    public static final ResourceLocation FLUORESCENT_LAMP_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/fluorescent_lamp.png");

    public static ObjModelPart part(String name, RenderType renderType) {
        return ObjModelLibrary.blockPart("trinkets/" + name, renderType);
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return legacyModel(name, name);
    }

    public static LegacyWavefrontModel legacyModel(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/trinkets/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/trinkets/" + name + ".png");
    }

    private ObjTrinketModels() {
    }
}

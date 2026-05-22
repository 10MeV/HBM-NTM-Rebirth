package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjReactorModels {
    public static final LegacyWavefrontModel SMALL_BASE = model("reactor_small_base");
    public static final LegacyWavefrontModel SMALL_RODS = model("reactor_small_rods");
    public static final LegacyWavefrontModel BREEDER = model("breeder");
    public static final LegacyWavefrontModel ICF = model("icf");
    public static final LegacyWavefrontModel WATZ = model("watz");
    public static final LegacyWavefrontModel WATZ_PUMP = model("watz_pump");
    public static final LegacyWavefrontModel LPW2 = model("lpw2");
    public static final LegacyWavefrontModel ZIRNOX = model("zirnox");
    public static final LegacyWavefrontModel ZIRNOX_DESTROYED = model("zirnox_destroyed");

    public static final ResourceLocation LPW2_TERM_TEXTURE = texture("lpw2_term");
    public static final ResourceLocation LPW2_TERM_ERROR_TEXTURE = texture("lpw2_term_error");
    public static final ResourceLocation ZIRNOX_DEBRIS_ELEMENT_TEXTURE = texture("zirnox_deb_element");

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/reactors/" + name + ".obj"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/reactors/" + name + ".png");
    }

    private ObjReactorModels() {
    }
}

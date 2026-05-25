package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjEffectModels {
    public static final LegacyWavefrontModel SPHERE_RUV = model("sphere_ruv").asVBO();
    public static final LegacyWavefrontModel SPHERE_IUV = model("sphere_iuv").asVBO();
    public static final LegacyWavefrontModel SPHERE_UV = model("sphere_uv").asVBO();
    public static final LegacyWavefrontModel SPHERE_NEW = model("sphere_new").asVBO();
    public static final LegacyWavefrontModel SPHERE = model("sphere").asVBO();

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(new ResourceLocation(HbmNtm.MOD_ID, "models/block/effects/" + name + ".obj"));
    }

    private ObjEffectModels() {
    }
}

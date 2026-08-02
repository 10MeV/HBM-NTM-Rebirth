package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

/** Source-backed OBJ resources for the independent 1.7.10 MK2 Pile devices. */
public final class ObjPileModels {
    public static final LegacyWavefrontModel LOADER = model("pile_loader").asVBO();
    public static final LegacyWavefrontModel VENT = model("pile_vent").asVBO();
    public static final LegacyWavefrontModel CONTROL = model("pile_control").asVBO();

    public static final ResourceLocation LOADER_TEXTURE = texture("pile_loader");
    public static final ResourceLocation VENT_TEXTURE = texture("pile_vent");
    public static final ResourceLocation CONTROL_TEXTURE = texture("pile_control");

    private static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(new ResourceLocation(HbmNtm.MOD_ID,
                "models/reactors/pile/" + name + ".obj"), texture(name));
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/pile/" + name + ".png");
    }

    private ObjPileModels() {
    }
}

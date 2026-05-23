package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ObjTrinketModels {
    public static final LegacyWavefrontModel LANTERN = legacyModel("lantern").noSmooth();

    public static ObjModelPart part(String name, RenderType renderType) {
        return ObjModelLibrary.blockPart("trinkets/" + name, renderType);
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/trinkets/" + name + ".obj"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/trinkets/" + name + ".png");
    }

    private ObjTrinketModels() {
    }
}

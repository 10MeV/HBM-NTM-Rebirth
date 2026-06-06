package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjLightModels {
    public static final ObjModelPart CAGE_LAMP = ObjModelLibrary.directBlockPart("legacy/cage_lamp_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLUORESCENT_LAMP_SINGLE = ObjModelLibrary.directBlockPart("legacy/fluorescent_lamp_single_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOOD_LAMP = ObjModelLibrary.directBlockPart("legacy/flood_lamp_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_BASE = ObjModelLibrary.directBlockPart("legacy/floodlight_base_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_LIGHTS = ObjModelLibrary.directBlockPart("legacy/floodlight_lights_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_LAMPS = ObjModelLibrary.directBlockPart("legacy/floodlight_lamps_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart DEMON_LAMP = ObjModelLibrary.directBlockPart("lamp_demon")
            .withOrigin(ObjPartTransform.IDENTITY);

    public static final LegacyWavefrontModel CAGE_LAMP_LEGACY = legacyModel("cage_lamp").noSmooth();
    public static final LegacyWavefrontModel FLUORESCENT_LAMP_LEGACY = legacyModel("fluorescent_lamp").noSmooth();
    public static final LegacyWavefrontModel FLOOD_LAMP_LEGACY = legacyModel("flood_lamp").noSmooth();
    public static final LegacyWavefrontModel FLOODLIGHT_LEGACY = legacyModel("floodlight", machineTexture("floodlight"));
    public static final LegacyWavefrontModel DEMON_LAMP_LEGACY = legacyModel("demon_lamp", machineTexture("demon_lamp"));

    public static LegacyWavefrontModel legacyModel(String name) {
        return legacyModel(name, texture(name));
    }

    public static LegacyWavefrontModel legacyModel(String name, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/lights/" + name + ".obj"),
                texture);
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/" + name + ".png");
    }

    public static ResourceLocation machineTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/" + name + ".png");
    }

    private ObjLightModels() {
    }
}

package com.hbm.ntm.client.obj;

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

    private ObjLightModels() {
    }
}

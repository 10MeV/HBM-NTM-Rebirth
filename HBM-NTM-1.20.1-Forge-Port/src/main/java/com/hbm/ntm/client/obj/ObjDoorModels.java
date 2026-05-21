package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.RenderType;

public final class ObjDoorModels {
    public static final ObjModelPart SILO_HATCH = part("silo_hatch");
    public static final ObjModelPart SILO_HATCH_HATCH = part("silo_hatch_hatch");
    public static final ObjModelPart SILO_HATCH_FRAME = part("silo_hatch_frame");
    public static final ObjModelPart SILO_HATCH_LARGE = part("silo_hatch_large");
    public static final ObjModelPart SILO_HATCH_LARGE_HATCH = part("silo_hatch_large_hatch");
    public static final ObjModelPart SILO_HATCH_LARGE_FRAME = part("silo_hatch_large_frame");
    public static final ObjModelPart BLAST_DOOR_BASE = part("blast_door_base");
    public static final ObjModelPart BLAST_DOOR_TOOTH = part("blast_door_tooth");
    public static final ObjModelPart BLAST_DOOR_SLIDER = part("blast_door_slider");
    public static final ObjModelPart BLAST_DOOR_BLOCK = part("blast_door_block");

    public static final ObjPartModel SILO_HATCH_NAMED = new ObjPartModel()
            .part("Hatch", SILO_HATCH_HATCH)
            .part("Frame", SILO_HATCH_FRAME)
            .legacyOrder("Hatch", "Frame");
    public static final ObjPartModel SILO_HATCH_LARGE_NAMED = new ObjPartModel()
            .part("Hatch", SILO_HATCH_LARGE_HATCH)
            .part("Frame", SILO_HATCH_LARGE_FRAME)
            .legacyOrder("Hatch", "Frame");

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("doors/" + name, RenderType.cutout());
    }

    private ObjDoorModels() {
    }
}

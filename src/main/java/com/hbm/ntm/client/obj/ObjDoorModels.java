package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ObjDoorModels {
    public static final LegacyWavefrontModel SILO_HATCH_LEGACY = doorLegacyModel("silo_hatch").asVBO();
    public static final LegacyWavefrontModel SILO_HATCH_LARGE_LEGACY = doorLegacyModel("silo_hatch_large").asVBO();
    public static final LegacyWavefrontModel BLAST_DOOR_BASE_LEGACY = rootLegacyModel("blast_door_base");
    public static final LegacyWavefrontModel BLAST_DOOR_TOOTH_LEGACY = rootLegacyModel("blast_door_tooth");
    public static final LegacyWavefrontModel BLAST_DOOR_SLIDER_LEGACY = rootLegacyModel("blast_door_slider");
    public static final LegacyWavefrontModel BLAST_DOOR_BLOCK_LEGACY = rootLegacyModel("blast_door_block");

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

    public static final ResourceLocation SILO_HATCH_TEXTURE = doorTexture("silo_hatch");
    public static final ResourceLocation SILO_HATCH_LARGE_TEXTURE = doorTexture("silo_hatch_large");
    public static final ResourceLocation BLAST_DOOR_BASE_TEXTURE = rootTexture("blast_door_base");
    public static final ResourceLocation BLAST_DOOR_TOOTH_TEXTURE = rootTexture("blast_door_tooth");
    public static final ResourceLocation BLAST_DOOR_SLIDER_TEXTURE = rootTexture("blast_door_slider");
    public static final ResourceLocation BLAST_DOOR_BLOCK_TEXTURE = rootTexture("blast_door_block");

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("doors/" + name, RenderType.cutout());
    }

    public static LegacyWavefrontModel doorLegacyModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/doors/" + name + ".obj"),
                doorTexture(name)).asVBO();
    }

    public static LegacyWavefrontModel rootLegacyModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/" + name + ".obj"),
                rootTexture(name)).asVBO();
    }

    public static ResourceLocation doorTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/doors/" + name + ".png");
    }

    public static ResourceLocation rootTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
    }

    private ObjDoorModels() {
    }
}

package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ObjNetworkModels {
    public static final ObjModelPart CONNECTOR = part("connector");
    public static final ObjModelPart CONNECTOR_SUPER = part("connector_super");
    public static final ObjModelPart FLUID_DIODE = part("fluid_diode");
    public static final ObjModelPart PIPE_ANCHOR = part("pipe_anchor");
    public static final ObjModelPart PYLON_LARGE = part("pylon_large");
    public static final ObjModelPart PYLON_MEDIUM = part("pylon_medium");
    public static final ObjModelPart SUBSTATION = part("substation");

    public static final LegacyWavefrontModel CONNECTOR_LEGACY = legacyModel("connector").noSmooth().asVBO();
    public static final LegacyWavefrontModel CONNECTOR_SUPER_LEGACY = legacyModel("connector_super").noSmooth().asVBO();
    public static final LegacyWavefrontModel PYLON_LARGE_LEGACY = legacyModel("pylon_large").noSmooth().asVBO();
    public static final LegacyWavefrontModel PYLON_MEDIUM_LEGACY = legacyModel("pylon_medium").noSmooth().asVBO();
    public static final LegacyWavefrontModel SUBSTATION_LEGACY = legacyModel("substation").asVBO();
    public static final LegacyWavefrontModel PIPE_ANCHOR_LEGACY = legacyModel("pipe_anchor").asVBO();
    public static final LegacyWavefrontModel FLUID_DIODE_LEGACY = legacyModel("fluid_diode").asVBO();
    public static final ResourceLocation FLUID_DIODE_TEXTURE = texture("fluid_diode");
    public static final ResourceLocation CONNECTOR_TEXTURE = texture("connector");
    public static final ResourceLocation CONNECTOR_SUPER_TEXTURE = texture("connector_super");
    public static final ResourceLocation PYLON_LARGE_TEXTURE = texture("pylon_large");
    public static final ResourceLocation PYLON_MEDIUM_TEXTURE = texture("pylon_medium");
    public static final ResourceLocation PYLON_MEDIUM_STEEL_TEXTURE = texture("pylon_medium_steel");
    public static final ResourceLocation SUBSTATION_TEXTURE = texture("substation");

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("network/" + name, RenderType.cutout());
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/network/" + name + ".obj"),
                texture(name)).asVBO();
    }

    public static ResourceLocation texture(String name) {
        return switch (name) {
            case "connector", "connector_super", "pylon_large", "pylon_medium",
                 "pylon_medium_steel", "substation", "pipe_anchor", "fluid_diode",
                 "wire", "wire_greyscale" ->
                    new ResourceLocation(HbmNtm.MOD_ID, "textures/models/network/" + name + ".png");
            default -> new ResourceLocation(HbmNtm.MOD_ID, "textures/block/network/" + name + ".png");
        };
    }

    private ObjNetworkModels() {
    }
}

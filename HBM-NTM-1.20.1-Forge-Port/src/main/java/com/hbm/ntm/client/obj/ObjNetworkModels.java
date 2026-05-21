package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.RenderType;

public final class ObjNetworkModels {
    public static final ObjModelPart CONNECTOR = part("connector");
    public static final ObjModelPart CONNECTOR_SUPER = part("connector_super");
    public static final ObjModelPart FLUID_DIODE = part("fluid_diode");
    public static final ObjModelPart PIPE_ANCHOR = part("pipe_anchor");
    public static final ObjModelPart PYLON_LARGE = part("pylon_large");
    public static final ObjModelPart PYLON_MEDIUM = part("pylon_medium");
    public static final ObjModelPart SUBSTATION = part("substation");

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("network/" + name, RenderType.cutout());
    }

    private ObjNetworkModels() {
    }
}

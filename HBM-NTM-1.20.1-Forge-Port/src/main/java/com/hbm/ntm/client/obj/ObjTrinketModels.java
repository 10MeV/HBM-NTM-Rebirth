package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.RenderType;

public final class ObjTrinketModels {
    public static ObjModelPart part(String name, RenderType renderType) {
        return ObjModelLibrary.blockPart("trinkets/" + name, renderType);
    }

    private ObjTrinketModels() {
    }
}

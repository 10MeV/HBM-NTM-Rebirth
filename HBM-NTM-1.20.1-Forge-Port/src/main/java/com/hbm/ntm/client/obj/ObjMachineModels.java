package com.hbm.ntm.client.obj;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;

public final class ObjMachineModels {
    public static final ObjModelPart PRESS_HEAD = ObjModelLibrary.directBlockPart("press_head")
            .withRenderType(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS))
            .withLightMultiplier(0.82F)
            .withOrigin(ObjPartTransform.BLOCK_CENTER.withScale(0.99F, 1.0F, 0.99F));
    public static final ObjPartModel PRESS = new ObjPartModel()
            .part("Head", PRESS_HEAD);

    public static ObjModelPart part(String name) {
        return part(name, RenderType.cutout());
    }

    public static ObjModelPart part(String name, RenderType renderType) {
        return ObjModelLibrary.blockPart("machines/" + name, renderType);
    }

    public static ObjModelLibrary.ObjModelPartBuilder partBuilder(String name, RenderType renderType) {
        return ObjModelLibrary.blockPartBuilder("machines/" + name, renderType);
    }

    public static ObjModelLibrary.ObjModelPartBuilder directPart(String name) {
        return partBuilder(name, RenderType.cutout()).direct();
    }

    private ObjMachineModels() {
    }
}

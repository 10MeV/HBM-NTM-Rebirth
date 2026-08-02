package com.hbm.ntm.client.model;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.PlatemetalBlockEntity;
import net.minecraft.resources.ResourceLocation;

/** Parameter binding of the shared red-wire eight-neighbour CT model pipeline for legacy platemetal. */
public final class PlatemetalModelLoader extends RedWireCoatedModelLoader {
    public PlatemetalModelLoader() {
        super(new ResourceLocation(HbmNtm.MOD_ID, "block/platemetal.base"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/platemetal.base_ct"),
                PlatemetalBlockEntity.CT_PROPERTY, new ResourceLocation(HbmNtm.MOD_ID, "block/platemetal"));
    }
}

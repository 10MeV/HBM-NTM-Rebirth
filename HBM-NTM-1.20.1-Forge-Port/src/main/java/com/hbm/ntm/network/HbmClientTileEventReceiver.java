package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface HbmClientTileEventReceiver {
    void handleClientTileEvent(ResourceLocation eventType, CompoundTag data);
}

package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface HbmClientEntityEventReceiver {
    void handleClientEntityEvent(ResourceLocation eventType, CompoundTag data);
}

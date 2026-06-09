package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
interface ClientPlayerSyncDataListener {
    void onClientPlayerSyncData(ResourceLocation dataType, CompoundTag data);
}

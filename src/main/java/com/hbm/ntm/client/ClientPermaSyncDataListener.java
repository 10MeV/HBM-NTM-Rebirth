package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;

@FunctionalInterface
public interface ClientPermaSyncDataListener {
    void onClientPermaSyncData(CompoundTag data);
}

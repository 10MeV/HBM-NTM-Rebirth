package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ClientPanelDataListener {
    void onClientPanelData(ResourceLocation panelType, int legacyType, CompoundTag data);
}

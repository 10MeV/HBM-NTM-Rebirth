package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientPlayerSyncData {
    private static final Map<ResourceLocation, CompoundTag> DATA = new HashMap<>();

    public static void update(ResourceLocation dataType, CompoundTag tag) {
        DATA.put(dataType, tag == null ? new CompoundTag() : tag.copy());
    }

    public static Optional<CompoundTag> get(ResourceLocation dataType) {
        CompoundTag tag = DATA.get(dataType);
        return tag == null ? Optional.empty() : Optional.of(tag.copy());
    }

    public static void clearAll() {
        DATA.clear();
    }

    private ClientPlayerSyncData() {
    }
}

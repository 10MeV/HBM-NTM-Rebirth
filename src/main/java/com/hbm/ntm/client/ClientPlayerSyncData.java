package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ClientPlayerSyncData {
    private static final Map<ResourceLocation, CompoundTag> DATA = new HashMap<>();
    private static final List<ClientPlayerSyncDataListener> LISTENERS = new ArrayList<>();

    public static void update(ResourceLocation dataType, CompoundTag tag) {
        CompoundTag safeTag = tag == null ? new CompoundTag() : tag.copy();
        DATA.put(dataType, safeTag.copy());
        for (ClientPlayerSyncDataListener listener : List.copyOf(LISTENERS)) {
            listener.onClientPlayerSyncData(dataType, safeTag.copy());
        }
    }

    public static Optional<CompoundTag> get(ResourceLocation dataType) {
        CompoundTag tag = DATA.get(dataType);
        return tag == null ? Optional.empty() : Optional.of(tag.copy());
    }

    public static int entryCount() {
        return DATA.size();
    }

    public static void addListener(ClientPlayerSyncDataListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientPlayerSyncDataListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    public static void clearAll() {
        DATA.clear();
        LISTENERS.clear();
    }

    private ClientPlayerSyncData() {
    }
}

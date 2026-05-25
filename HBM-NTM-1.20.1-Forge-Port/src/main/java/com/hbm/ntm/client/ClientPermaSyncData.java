package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public final class ClientPermaSyncData {
    private static CompoundTag data = new CompoundTag();
    private static final List<ClientPermaSyncDataListener> LISTENERS = new ArrayList<>();

    public static void update(CompoundTag tag) {
        data = tag == null ? new CompoundTag() : tag.copy();
        for (ClientPermaSyncDataListener listener : List.copyOf(LISTENERS)) {
            listener.onClientPermaSyncData(data.copy());
        }
    }

    public static CompoundTag get() {
        return data.copy();
    }

    public static boolean getBoolean(String key) {
        return data.getBoolean(key);
    }

    public static int keyCount() {
        return data.size();
    }

    public static float getFloat(String key) {
        return data.getFloat(key);
    }

    public static void addListener(ClientPermaSyncDataListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientPermaSyncDataListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    public static void clearAll() {
        data = new CompoundTag();
        LISTENERS.clear();
    }

    private ClientPermaSyncData() {
    }
}

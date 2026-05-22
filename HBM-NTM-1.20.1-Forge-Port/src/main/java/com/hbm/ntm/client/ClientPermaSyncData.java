package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;

public final class ClientPermaSyncData {
    private static CompoundTag data = new CompoundTag();

    public static void update(CompoundTag tag) {
        data = tag == null ? new CompoundTag() : tag.copy();
    }

    public static CompoundTag get() {
        return data.copy();
    }

    public static boolean getBoolean(String key) {
        return data.getBoolean(key);
    }

    public static float getFloat(String key) {
        return data.getFloat(key);
    }

    private ClientPermaSyncData() {
    }
}

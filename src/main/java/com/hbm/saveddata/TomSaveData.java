package com.hbm.saveddata;

import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class TomSaveData extends TomImpactSavedData {
    public static final String key = TomImpactSavedData.DATA_NAME;

    private static TomSaveData lastCachedUnsafe;

    public TomSaveData(String tagName) {
        super(tagName);
    }

    public static TomSaveData load(CompoundTag tag) {
        return asTomSaveData(TomImpactSavedData.load(tag));
    }

    public static TomSaveData forWorld(ServerLevel level) {
        return cache(asTomSaveData(TomImpactSavedData.forWorld(level)));
    }

    public static TomSaveData forWorldOrNull(Level level) {
        return level instanceof ServerLevel serverLevel ? forWorld(serverLevel) : null;
    }

    public static TomSaveData forWorld(MinecraftServer server) {
        return cache(asTomSaveData(TomImpactSavedData.forWorld(server)));
    }

    public static TomSaveData getData(ServerLevel level) {
        return forWorld(level);
    }

    public static TomSaveData getDataOrNull(Level level) {
        return forWorldOrNull(level);
    }

    public static TomSaveData getData(MinecraftServer server) {
        return forWorld(server);
    }

    public static TomSaveData getLastCachedOrNull() {
        TomImpactSavedData modern = TomImpactSavedData.getLastCachedOrNull();
        if (modern instanceof TomSaveData data) {
            return data;
        }
        return lastCachedUnsafe;
    }

    public static void resetLastCached() {
        TomImpactSavedData.resetLastCached();
        lastCachedUnsafe = null;
    }

    public void markDirty() {
        setDirty();
    }

    private static TomSaveData cache(TomSaveData data) {
        lastCachedUnsafe = data;
        return data;
    }

    private static TomSaveData asTomSaveData(TomImpactSavedData data) {
        if (data instanceof TomSaveData tomSaveData) {
            return tomSaveData;
        }
        throw new IllegalStateException("impactData was created outside the legacy TomSaveData facade");
    }
}

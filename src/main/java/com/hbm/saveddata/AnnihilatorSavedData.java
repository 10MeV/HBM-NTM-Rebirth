package com.hbm.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class AnnihilatorSavedData extends com.hbm.ntm.world.saveddata.AnnihilatorSavedData {
    public static final String KEY = com.hbm.ntm.world.saveddata.AnnihilatorSavedData.KEY;

    public AnnihilatorSavedData() {
        super();
    }

    public AnnihilatorSavedData(String name) {
        super(name);
    }

    public static AnnihilatorSavedData load(CompoundTag tag) {
        return asAnnihilatorSavedData(com.hbm.ntm.world.saveddata.AnnihilatorSavedData.load(tag));
    }

    public static AnnihilatorSavedData getData(ServerLevel level) {
        return asAnnihilatorSavedData(com.hbm.ntm.world.saveddata.AnnihilatorSavedData.getData(level));
    }

    public static AnnihilatorSavedData getDataOrNull(Level level) {
        return level instanceof ServerLevel serverLevel ? getData(serverLevel) : null;
    }

    public static AnnihilatorSavedData getData(MinecraftServer server) {
        return asAnnihilatorSavedData(com.hbm.ntm.world.saveddata.AnnihilatorSavedData.getData(server));
    }

    public static AnnihilatorSavedData forWorld(ServerLevel level) {
        return asAnnihilatorSavedData(com.hbm.ntm.world.saveddata.AnnihilatorSavedData.forWorld(level));
    }

    public static AnnihilatorSavedData forWorldOrNull(Level level) {
        return getDataOrNull(level);
    }

    public static AnnihilatorSavedData forWorld(MinecraftServer server) {
        return asAnnihilatorSavedData(com.hbm.ntm.world.saveddata.AnnihilatorSavedData.forWorld(server));
    }

    public void markDirty() {
        setDirty();
    }

    private static AnnihilatorSavedData asAnnihilatorSavedData(
            com.hbm.ntm.world.saveddata.AnnihilatorSavedData data) {
        if (data instanceof AnnihilatorSavedData legacyData) {
            return legacyData;
        }
        throw new IllegalStateException("annihilator data was created outside the legacy AnnihilatorSavedData facade");
    }
}

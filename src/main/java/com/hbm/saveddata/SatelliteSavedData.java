package com.hbm.saveddata;

import com.hbm.saveddata.satellites.Satellite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class SatelliteSavedData extends com.hbm.ntm.satellite.SatelliteSavedData {
    public static final String DATA_NAME = com.hbm.ntm.satellite.SatelliteSavedData.DATA_NAME;
    public static final String KEY = DATA_NAME;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public final HashMap<Integer, Satellite> sats = (HashMap) super.sats;

    public SatelliteSavedData() {
        super();
    }

    public SatelliteSavedData(String name) {
        super(name);
    }

    public static SatelliteSavedData load(CompoundTag tag) {
        return asSatelliteSavedData(com.hbm.ntm.satellite.SatelliteSavedData.load(tag));
    }

    public static SatelliteSavedData get(ServerLevel level) {
        return asSatelliteSavedData(com.hbm.ntm.satellite.SatelliteSavedData.get(level));
    }

    public static SatelliteSavedData getOrNull(Level level) {
        return level instanceof ServerLevel serverLevel ? get(serverLevel) : null;
    }

    public static SatelliteSavedData get(MinecraftServer server) {
        return asSatelliteSavedData(com.hbm.ntm.satellite.SatelliteSavedData.get(server));
    }

    public static SatelliteSavedData getData(ServerLevel level) {
        return get(level);
    }

    public static SatelliteSavedData getDataOrNull(Level level) {
        return getOrNull(level);
    }

    public static SatelliteSavedData getData(MinecraftServer server) {
        return get(server);
    }

    public static SatelliteSavedData forWorld(ServerLevel level) {
        return get(level);
    }

    public static SatelliteSavedData forWorldOrNull(Level level) {
        return getOrNull(level);
    }

    public static SatelliteSavedData forWorld(MinecraftServer server) {
        return get(server);
    }

    @Override
    public Satellite getSatFromFreq(int frequency) {
        return (Satellite) super.getSatFromFreq(frequency);
    }

    @Override
    public Satellite getSatellite(int frequency) {
        return (Satellite) super.getSatellite(frequency);
    }

    @Override
    public Satellite getCargoSatellite(int frequency) {
        return (Satellite) super.getCargoSatellite(frequency);
    }

    public void markDirty() {
        setDirty();
    }

    private static SatelliteSavedData asSatelliteSavedData(
            com.hbm.ntm.satellite.SatelliteSavedData data) {
        if (data instanceof SatelliteSavedData legacyData) {
            return legacyData;
        }
        throw new IllegalStateException("satellites data was created outside the legacy SatelliteSavedData facade");
    }
}

package com.hbm.saveddata.satellites;

import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.satellite.LegacySatelliteType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Optional;

public class SatelliteMiner extends Satellite {
    private static final HashMap<Class<? extends SatelliteMiner>, String> CARGO = new HashMap<>();

    public long lastOp;

    static {
        registerCargo(SatelliteMiner.class, HbmItemPoolIds.POOL_SAT_MINER);
    }

    public SatelliteMiner() {
        this.satIface = Interfaces.NONE;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.MINER;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong("lastOp", lastOp);
    }

    @Override
    public void load(CompoundTag tag) {
        lastOp = tag.getLong("lastOp");
    }

    @Override
    public long lastOperationMillis() {
        return lastOp;
    }

    @Override
    public void setLastOperationMillis(long timeMillis) {
        lastOp = timeMillis;
    }

    public static void registerCargo(Class<? extends SatelliteMiner> minerSatelliteClass, String cargo) {
        CARGO.put(minerSatelliteClass, cargo);
        com.hbm.ntm.satellite.Satellite.registerCargoForClass(minerSatelliteClass, cargo);
    }

    public String getCargo() {
        return CARGO.get(getClass());
    }

    public static String getCargoForClass(Class<? extends com.hbm.ntm.satellite.Satellite> satelliteClass) {
        if (satelliteClass == null || !SatelliteMiner.class.isAssignableFrom(satelliteClass)) {
            return null;
        }
        return CARGO.get(satelliteClass.asSubclass(SatelliteMiner.class));
    }

    @Override
    public Optional<String> cargoPool() {
        return Optional.ofNullable(getCargo());
    }

    public static String getCargoForItem(Item satelliteItem) {
        Class<? extends Satellite> satelliteClass = itemToClass.getOrDefault(satelliteItem, null);
        return satelliteClass != null ? CARGO.getOrDefault(satelliteClass, null) : null;
    }
}

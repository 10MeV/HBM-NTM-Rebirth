package com.hbm.ntm.satellite;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public class SatelliteMiner extends Satellite {
    private long lastOperationMillis;

    public SatelliteMiner() {
        setSatelliteInterface(Interfaces.NONE);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.MINER;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong("lastOp", lastOperationMillis);
    }

    @Override
    public void load(CompoundTag tag) {
        lastOperationMillis = tag.getLong("lastOp");
    }

    @Override
    public long lastOperationMillis() {
        return lastOperationMillis;
    }

    @Override
    public void setLastOperationMillis(long timeMillis) {
        lastOperationMillis = timeMillis;
    }

    public String getCargo() {
        return getCargoForSatellite(this);
    }

    public static String getCargoForItem(Item satelliteItem) {
        return Satellite.getCargoForItem(satelliteItem);
    }

}

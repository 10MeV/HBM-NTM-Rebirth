package com.hbm.ntm.satellite;

import net.minecraft.nbt.CompoundTag;

public final class SatelliteHorizons extends Satellite {
    private boolean used;

    public SatelliteHorizons() {
        setSatelliteInterface(Interfaces.SAT_COORD);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.HORIZONS;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("used", used);
    }

    @Override
    public void load(CompoundTag tag) {
        used = tag.getBoolean("used");
    }

    public boolean used() {
        return used;
    }
}

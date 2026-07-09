package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;
import net.minecraft.nbt.CompoundTag;

public class SatelliteHorizons extends Satellite {
    boolean used = false;

    public SatelliteHorizons() {
        this.satIface = Interfaces.SAT_COORD;
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

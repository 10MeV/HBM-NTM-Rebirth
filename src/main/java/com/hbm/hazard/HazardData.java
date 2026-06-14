package com.hbm.hazard;

import com.hbm.hazard.type.HazardTypeBase;
import com.hbm.ntm.radiation.HazardType;

/**
 * Legacy package facade for 1.7.10 HazardData.
 */
@Deprecated(forRemoval = false)
public class HazardData extends com.hbm.ntm.radiation.HazardData {
    public HazardData addEntry(HazardTypeBase hazard) {
        return addEntry(hazard, 1.0F, false);
    }

    public HazardData addEntry(HazardTypeBase hazard, float level) {
        return addEntry(hazard, level, false);
    }

    public HazardData addEntry(HazardTypeBase hazard, float level, boolean override) {
        super.addEntry(hazard.modernType(), level, override);
        return this;
    }

    @Override
    public HazardData addEntry(HazardType type) {
        super.addEntry(type);
        return this;
    }

    @Override
    public HazardData addEntry(HazardType type, float level) {
        super.addEntry(type, level);
        return this;
    }

    @Override
    public HazardData addEntry(HazardType type, float level, boolean override) {
        super.addEntry(type, level, override);
        return this;
    }

    public HazardData addEntry(HazardEntry entry) {
        super.addEntry(entry.toModern());
        return this;
    }

    @Override
    public HazardData addEntry(com.hbm.ntm.radiation.HazardEntry entry) {
        super.addEntry(entry);
        return this;
    }

    @Override
    public HazardData setMutex(int mutexBits) {
        super.setMutex(mutexBits);
        return this;
    }

    @Override
    public HazardData setOverrides(boolean overrides) {
        super.setOverrides(overrides);
        return this;
    }

    public int getMutex() {
        return mutexBits();
    }
}

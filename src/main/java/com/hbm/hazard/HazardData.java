package com.hbm.hazard;

import com.hbm.hazard.type.HazardTypeBase;
import com.hbm.ntm.radiation.HazardType;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy package facade for 1.7.10 HazardData.
 */
@Deprecated(forRemoval = false)
public class HazardData extends com.hbm.ntm.radiation.HazardData {
    boolean doesOverride = false;
    int mutexBits = 0;
    List<HazardEntry> entries = new ArrayList<>();

    public HazardData addEntry(HazardTypeBase hazard) {
        return addEntry(hazard, 1.0F, false);
    }

    public HazardData addEntry(HazardTypeBase hazard, float level) {
        return addEntry(hazard, level, false);
    }

    public HazardData addEntry(HazardTypeBase hazard, float level, boolean override) {
        entries.add(new HazardEntry(hazard, level));
        doesOverride = override;
        return this;
    }

    @Override
    public HazardData addEntry(HazardType type) {
        return addEntry(HazardTypeBase.fromModern(type), 1.0F, false);
    }

    @Override
    public HazardData addEntry(HazardType type, float level) {
        return addEntry(HazardTypeBase.fromModern(type), level, false);
    }

    @Override
    public HazardData addEntry(HazardType type, float level, boolean override) {
        return addEntry(HazardTypeBase.fromModern(type), level, override);
    }

    public HazardData addEntry(HazardEntry entry) {
        entries.add(entry);
        return this;
    }

    @Override
    public HazardData addEntry(com.hbm.ntm.radiation.HazardEntry entry) {
        entries.add(HazardEntry.fromModern(entry));
        return this;
    }

    @Override
    public HazardData setMutex(int mutexBits) {
        this.mutexBits = mutexBits;
        return this;
    }

    @Override
    public HazardData setOverrides(boolean overrides) {
        this.doesOverride = overrides;
        return this;
    }

    @Override
    public List<com.hbm.ntm.radiation.HazardEntry> entries() {
        return new AbstractList<>() {
            @Override
            public com.hbm.ntm.radiation.HazardEntry get(int index) {
                return entries.get(index).toModern();
            }

            @Override
            public int size() {
                return entries.size();
            }

            @Override
            public com.hbm.ntm.radiation.HazardEntry set(int index, com.hbm.ntm.radiation.HazardEntry element) {
                return entries.set(index, HazardEntry.fromModern(element)).toModern();
            }

            @Override
            public void add(int index, com.hbm.ntm.radiation.HazardEntry element) {
                entries.add(index, HazardEntry.fromModern(element));
            }

            @Override
            public com.hbm.ntm.radiation.HazardEntry remove(int index) {
                return entries.remove(index).toModern();
            }
        };
    }

    @Override
    public boolean overrides() {
        return doesOverride;
    }

    @Override
    public int mutexBits() {
        return mutexBits;
    }

    public int getMutex() {
        return mutexBits;
    }
}

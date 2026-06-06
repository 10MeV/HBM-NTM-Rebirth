package com.hbm.ntm.radiation;

import java.util.ArrayList;
import java.util.List;

public class HazardData {
    private final List<HazardEntry> entries = new ArrayList<>();
    private boolean overrides;
    private int mutexBits;

    public HazardData addEntry(HazardType type) {
        return addEntry(type, 1.0F, false);
    }

    public HazardData addEntry(HazardType type, float level) {
        return addEntry(type, level, false);
    }

    public HazardData addEntry(HazardType type, float level, boolean override) {
        entries.add(new HazardEntry(type, level));
        overrides = override;
        return this;
    }

    public HazardData addEntry(HazardEntry entry) {
        entries.add(entry);
        return this;
    }

    public HazardData setMutex(int mutexBits) {
        this.mutexBits = mutexBits;
        return this;
    }

    public HazardData setOverrides(boolean overrides) {
        this.overrides = overrides;
        return this;
    }

    public List<HazardEntry> entries() {
        return entries;
    }

    public boolean overrides() {
        return overrides;
    }

    public int mutexBits() {
        return mutexBits;
    }
}

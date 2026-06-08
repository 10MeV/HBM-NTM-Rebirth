package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class PileFuelState implements PileNeutronReceiver {
    public static final String TAG_HEAT = "heat";
    public static final String TAG_PROGRESS = "progress";
    public static final String TAG_NEUTRONS = "neutrons";

    private int heat;
    private int neutrons;
    private int lastNeutrons;
    private int progress;

    public int heat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = Math.max(0, heat);
    }

    public int neutrons() {
        return neutrons;
    }

    public void setNeutrons(int neutrons) {
        this.neutrons = Math.max(0, neutrons);
    }

    public int lastNeutrons() {
        return lastNeutrons;
    }

    public void setLastNeutrons(int lastNeutrons) {
        this.lastNeutrons = Math.max(0, lastNeutrons);
    }

    public int progress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
    }

    @Override
    public void receiveNeutrons(int neutrons) {
        setNeutrons(this.neutrons + Math.max(0, neutrons));
    }

    public void loadFuelTag(CompoundTag tag) {
        setHeat(tag.getInt(TAG_HEAT));
        setProgress(tag.getInt(TAG_PROGRESS));
        setNeutrons(tag.getInt(TAG_NEUTRONS));
    }

    public void saveFuelTag(CompoundTag tag) {
        tag.putInt(TAG_HEAT, heat);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_NEUTRONS, neutrons);
    }

    public void loadBreedingFuelTag(CompoundTag tag) {
        setProgress(tag.getInt(TAG_PROGRESS));
        setNeutrons(tag.getInt(TAG_NEUTRONS));
    }

    public void saveBreedingFuelTag(CompoundTag tag) {
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_NEUTRONS, neutrons);
    }
}

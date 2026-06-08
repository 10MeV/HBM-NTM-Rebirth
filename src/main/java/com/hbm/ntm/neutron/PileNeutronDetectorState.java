package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class PileNeutronDetectorState implements PileNeutronReceiver {
    public static final String TAG_MAX_NEUTRONS = "maxNeutrons";
    public static final int DEFAULT_MAX_NEUTRONS = 10;

    private int lastNeutrons;
    private int neutrons;
    private int maxNeutrons = DEFAULT_MAX_NEUTRONS;

    public int lastNeutrons() {
        return lastNeutrons;
    }

    public int neutrons() {
        return neutrons;
    }

    public int maxNeutrons() {
        return maxNeutrons;
    }

    public void setMaxNeutrons(int maxNeutrons) {
        this.maxNeutrons = Math.max(1, maxNeutrons);
    }

    @Override
    public void receiveNeutrons(int neutrons) {
        this.neutrons += Math.max(0, neutrons);
    }

    public DetectorTickResult tick(boolean metadataBit8Set) {
        boolean triggerRods = false;
        if (neutrons >= maxNeutrons && metadataBit8Set) {
            triggerRods = true;
        }
        if (neutrons < maxNeutrons && lastNeutrons < maxNeutrons && !metadataBit8Set) {
            triggerRods = true;
        }

        int absorbed = neutrons;
        lastNeutrons = neutrons;
        neutrons = 0;
        return new DetectorTickResult(absorbed, triggerRods);
    }

    public void loadDetectorTag(CompoundTag tag) {
        if (tag.contains(TAG_MAX_NEUTRONS)) {
            setMaxNeutrons(tag.getInt(TAG_MAX_NEUTRONS));
        }
    }

    public void saveDetectorTag(CompoundTag tag) {
        tag.putInt(TAG_MAX_NEUTRONS, maxNeutrons);
    }

    public record DetectorTickResult(int absorbedNeutrons, boolean triggerRods) {
    }
}

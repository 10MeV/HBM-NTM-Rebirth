package com.hbm.ntm.api.tile;

public interface HeatSource {
    int getHeatStored();

    void useUpHeat(int heat);
}

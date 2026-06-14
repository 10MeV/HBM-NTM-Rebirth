package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;

import java.util.List;

public final class RadarHostRedstoneProfile {
    private RadarHostRedstoneProfile() {
    }

    public static RadarRedstoneMode mode(boolean redstoneProximityMode) {
        return RadarRedstoneMode.fromLegacyFlag(redstoneProximityMode);
    }

    public static int power(List<RadarEntry> entries, BlockPos origin, int range,
            boolean redstoneProximityMode) {
        return mode(redstoneProximityMode).power(entries != null ? entries : List.of(), origin, range);
    }
}

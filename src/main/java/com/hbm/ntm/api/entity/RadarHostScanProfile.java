package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public final class RadarHostScanProfile {
    private RadarHostScanProfile() {
    }

    public static Allocation allocate(ServerLevel level, BlockPos origin, int range, int verticalBuffer,
            int minimumAltitude, long power, long consumption, RadarDetectable.RadarScanParams params) {
        RadarHostTickProfile.ScanPowerPlan powerPlan = RadarHostTickProfile.scanPowerPlan(origin.getY(),
                minimumAltitude, power, consumption);
        if (!powerPlan.scan()) {
            return new Allocation(powerPlan.powerAfter(), false, RadarScanResult.EMPTY);
        }

        RadarScanResult result = RadarScanner.scan(RadarContext.legacy(level, origin, range, verticalBuffer,
                minimumAltitude, params));
        return new Allocation(powerPlan.powerAfter(), true, result);
    }

    public record Allocation(long powerAfter, boolean scanPerformed, RadarScanResult result) {
        public Allocation {
            result = result != null ? result : RadarScanResult.EMPTY;
        }

        public boolean jammed() {
            return result.jammed();
        }

        public List<RadarEntry> entries() {
            return result.entries();
        }
    }
}

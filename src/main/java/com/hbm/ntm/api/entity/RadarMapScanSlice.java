package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record RadarMapScanSlice(int slice, int start, List<Sample> samples) {
    public RadarMapScanSlice {
        if (slice < 0 || slice >= RadarMap.SLICE_COUNT) {
            slice = RadarMapUpdate.NO_SLICE;
            start = 0;
            samples = List.of();
        } else {
            start = RadarMap.sliceStart(slice);
            samples = List.copyOf(samples != null ? samples : List.of());
        }
    }

    public static RadarMapScanSlice forGameTime(BlockPos origin, int range, long gameTime) {
        return of(origin, range, RadarMap.sliceForGameTime(gameTime));
    }

    public static RadarMapScanSlice of(BlockPos origin, int range, int slice) {
        int start = RadarMap.sliceStart(slice);
        List<Sample> samples = new ArrayList<>(RadarMap.SLICE_SIZE);
        for (int offset = 0; offset < RadarMap.SLICE_SIZE; offset++) {
            int index = start + offset;
            samples.add(new Sample(index, RadarMap.sampleX(origin, range, index),
                    RadarMap.sampleZ(origin, range, index)));
        }
        return new RadarMapScanSlice(slice, start, samples);
    }

    public boolean valid() {
        return slice != RadarMapUpdate.NO_SLICE;
    }

    public record Sample(int index, int x, int z) {
        public int chunkX() {
            return x >> 4;
        }

        public int chunkZ() {
            return z >> 4;
        }
    }
}

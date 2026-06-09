package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class RBMKCoolerRuntime {
    public static final int CACHE_REFRESH_INTERVAL = 60;
    public static final int COOLANT_PER_OPERATION = 50;
    public static final double HEAT_REMOVED_PER_COLUMN = 200.0D;
    public static final int NEIGHBOR_SCAN_RADIUS = 2;

    private RBMKCoolerRuntime() {
    }

    public static CacheTickResult tickCacheTimer(RBMKCoolerState state) {
        if (state.timer() <= 0) {
            state.setTimer(CACHE_REFRESH_INTERVAL);
            return new CacheTickResult(true, state.timer());
        }
        state.setTimer(state.timer() - 1);
        return new CacheTickResult(false, state.timer());
    }

    public static List<BlockPos> scanPositions(BlockPos origin) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = -NEIGHBOR_SCAN_RADIUS; x <= NEIGHBOR_SCAN_RADIUS; x++) {
            for (int z = -NEIGHBOR_SCAN_RADIUS; z <= NEIGHBOR_SCAN_RADIUS; z++) {
                positions.add(origin.offset(x, 0, z));
            }
        }
        return positions;
    }

    public static CoolerTickResult tickCooler(RBMKCoolerState state, List<RBMKThermalState> cachedColumns) {
        CacheTickResult cache = tickCacheTimer(state);
        if (state.coldFill() < COOLANT_PER_OPERATION
                || state.hotMax() - state.hotFill() < COOLANT_PER_OPERATION) {
            return new CoolerTickResult(cache.refreshCache(), false, 0, 0, List.of());
        }

        state.setColdFill(state.coldFill() - COOLANT_PER_OPERATION);
        state.setHotFill(state.hotFill() + COOLANT_PER_OPERATION);

        List<Integer> cooledColumnIndexes = new ArrayList<>();
        if (cachedColumns != null) {
            for (int i = 0; i < cachedColumns.size(); i++) {
                RBMKThermalState neighbor = cachedColumns.get(i);
                if (neighbor != null) {
                    neighbor.setHeat(Math.max(RBMKThermalState.MIN_PASSIVE_HEAT,
                            neighbor.heat() - HEAT_REMOVED_PER_COLUMN));
                    cooledColumnIndexes.add(i);
                }
            }
        }

        return new CoolerTickResult(
                cache.refreshCache(),
                true,
                COOLANT_PER_OPERATION,
                COOLANT_PER_OPERATION,
                cooledColumnIndexes);
    }

    public record CacheTickResult(boolean refreshCache, int timerAfterTick) {
    }

    public record CoolerTickResult(
            boolean refreshCache,
            boolean convertedCoolant,
            int coldConsumed,
            int hotProduced,
            List<Integer> cooledColumnIndexes) {
    }
}

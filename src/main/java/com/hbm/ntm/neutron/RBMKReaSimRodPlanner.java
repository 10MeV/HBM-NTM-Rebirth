package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKReaSimRodPlanner {
    public static final String CONTAINER_NAME = "container.rbmkReaSim";
    public static final int STREAM_COUNT = 8;
    public static final double RANDOM_YAW_STEP_DEGREES = 9.0D;
    public static final int RANDOM_YAW_STEPS = 4;
    public static final double STREAM_YAW_STEP_DEGREES = 45.0D;
    public static final double STREAM_FLUX_MULTIPLIER = 0.75D;

    private RBMKReaSimRodPlanner() {
    }

    public static ReaSimRodContract contract() {
        return new ReaSimRodContract(
                CONTAINER_NAME,
                RBMKConsolePlanner.ColumnType.FUEL_SIM,
                RBMKNeutronHandler.RBMKType.ROD,
                true,
                STREAM_COUNT,
                RANDOM_YAW_STEP_DEGREES,
                STREAM_YAW_STEP_DEGREES,
                STREAM_FLUX_MULTIPLIER);
    }

    public static ReaSimSpreadPlan planSpreadFlux(double flux, double ratio, int randomYawStep) {
        int yawStep = Math.floorMod(randomYawStep, RANDOM_YAW_STEPS);
        if (flux == 0.0D) {
            return new ReaSimSpreadPlan(true, false, true, List.of());
        }

        List<ReaSimStreamPlan> streams = new ArrayList<>(STREAM_COUNT);
        double yaw = yawStep * RANDOM_YAW_STEP_DEGREES;
        for (int i = 0; i < STREAM_COUNT; i++) {
            streams.add(new ReaSimStreamPlan(i, yaw, STREAM_FLUX_MULTIPLIER * flux, ratio));
            yaw += STREAM_YAW_STEP_DEGREES;
        }
        return new ReaSimSpreadPlan(false, true, false, List.copyOf(streams));
    }

    public static List<ReaSimCachePos> cachePositions(int range) {
        int safeRange = Math.max(1, range);
        List<ReaSimCachePos> positions = new ArrayList<>();
        for (int x = -safeRange; x <= safeRange; x++) {
            for (int z = -safeRange; z <= safeRange; z++) {
                if (x * x + z * z <= safeRange * safeRange) {
                    positions.add(new ReaSimCachePos(x, z));
                }
            }
        }
        return List.copyOf(positions);
    }

    public record ReaSimRodContract(
            String containerName,
            RBMKConsolePlanner.ColumnType consoleType,
            RBMKNeutronHandler.RBMKType neutronType,
            boolean markerInterface,
            int streamCount,
            double randomYawStepDegrees,
            double streamYawStepDegrees,
            double streamFluxMultiplier) {
    }

    public record ReaSimSpreadPlan(
            boolean removeCachedNode,
            boolean ensureNode,
            boolean skipStreamCreation,
            List<ReaSimStreamPlan> streams) {
    }

    public record ReaSimStreamPlan(int index, double yawDegrees, double flux, double ratio) {
    }

    public record ReaSimCachePos(int offsetX, int offsetZ) {
    }
}

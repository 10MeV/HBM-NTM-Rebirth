package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class RBMKMeltPlanner {
    private RBMKMeltPlanner() {
    }

    public static StandardMeltPlan standardMelt(BlockPos origin, int columnHeight, int reduce, boolean extraReduce) {
        int height = Math.max(1, columnHeight);
        int effectiveReduce = Mth.clamp(reduce, 1, height);
        if (extraReduce) {
            effectiveReduce++;
        }

        List<LayerMutation> mutations = new ArrayList<>();
        for (int y = height; y >= 0; y--) {
            MeltLayerState state;
            if (y <= height + 1 - effectiveReduce) {
                state = effectiveReduce > 1 && y == height + 1 - effectiveReduce
                        ? MeltLayerState.PRIBRIS_BURNING
                        : MeltLayerState.PRIBRIS;
            } else {
                state = MeltLayerState.AIR;
            }
            mutations.add(new LayerMutation(origin.above(y), state));
        }
        return new StandardMeltPlan(effectiveReduce, mutations);
    }

    public static MeltdownBounds bounds(Iterable<BlockPos> columns) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int count = 0;
        for (BlockPos pos : columns) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
            count++;
        }
        if (count == 0) {
            return MeltdownBounds.EMPTY;
        }
        return new MeltdownBounds(minX, maxX, minZ, maxZ, minX + (maxX - minX) / 2, minZ + (maxZ - minZ) / 2);
    }

    public static int edgeReduce(BlockPos column, MeltdownBounds bounds) {
        if (bounds == null || bounds == MeltdownBounds.EMPTY) {
            return 1;
        }
        int distFromMinX = column.getX() - bounds.minX();
        int distFromMaxX = bounds.maxX() - column.getX();
        int distFromMinZ = column.getZ() - bounds.minZ();
        int distFromMaxZ = bounds.maxZ() - column.getZ();
        return Math.min(distFromMinX, Math.min(distFromMaxX, Math.min(distFromMinZ, distFromMaxZ))) + 1;
    }

    public enum MeltLayerState {
        AIR,
        PRIBRIS,
        PRIBRIS_BURNING
    }

    public record LayerMutation(BlockPos pos, MeltLayerState state) {
    }

    public record StandardMeltPlan(int effectiveReduce, List<LayerMutation> layers) {
    }

    public record MeltdownBounds(int minX, int maxX, int minZ, int maxZ, int centerX, int centerZ) {
        public static final MeltdownBounds EMPTY = new MeltdownBounds(0, 0, 0, 0, 0, 0);
    }
}

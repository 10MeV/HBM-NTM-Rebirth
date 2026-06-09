package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class RBMKIOPlanner {
    public static final int INLET_WATER_CAPACITY = 32_000;
    public static final int OUTLET_STEAM_CAPACITY = 32_000;
    public static final String INLET_FLUID_ID = "water";
    public static final String OUTLET_FLUID_ID = "superhot_steam";
    public static final double ACTIVE_BASE_USE_DISTANCE_SQ = 128.0D;
    public static final int DEFAULT_STACK_LIMIT = 64;

    private static final Direction[] HORIZONTAL_LEGACY_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    private RBMKIOPlanner() {
    }

    public static List<BlockPos> horizontalNeighborPositions(BlockPos origin) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        List<BlockPos> positions = new ArrayList<>(HORIZONTAL_LEGACY_DIRECTIONS.length);
        for (Direction direction : HORIZONTAL_LEGACY_DIRECTIONS) {
            positions.add(safeOrigin.relative(direction));
        }
        return List.copyOf(positions);
    }

    public static InletTransferPlan planInletTransfer(int inletFill, List<ReaSimColumnFluidState> adjacentColumns,
            boolean reasimBoilers) {
        int remaining = clamp(inletFill, 0, INLET_WATER_CAPACITY);
        List<ColumnTransfer> transfers = new ArrayList<>();
        if (reasimBoilers && adjacentColumns != null) {
            for (ReaSimColumnFluidState column : adjacentColumns) {
                if (column == null) {
                    continue;
                }
                int moved = Math.min(Math.max(0, column.maxWater() - column.water()), remaining);
                if (moved > 0) {
                    remaining -= moved;
                    transfers.add(new ColumnTransfer(column.pos(), moved));
                }
            }
        }
        return new InletTransferPlan(inletFill, remaining, transfers);
    }

    public static OutletTransferPlan planOutletTransfer(int outletFill, List<ReaSimColumnFluidState> adjacentColumns,
            boolean reasimBoilers) {
        int fill = clamp(outletFill, 0, OUTLET_STEAM_CAPACITY);
        List<ColumnTransfer> transfers = new ArrayList<>();
        if (reasimBoilers && adjacentColumns != null) {
            for (ReaSimColumnFluidState column : adjacentColumns) {
                if (column == null) {
                    continue;
                }
                int moved = Math.min(OUTLET_STEAM_CAPACITY - fill, Math.max(0, column.steam()));
                if (moved > 0) {
                    fill += moved;
                    transfers.add(new ColumnTransfer(column.pos(), moved));
                }
                if (fill >= OUTLET_STEAM_CAPACITY) {
                    break;
                }
            }
        }
        return new OutletTransferPlan(outletFill, fill, transfers, allAdjacentDirections());
    }

    public static SlottedAccessPlan slottedAccess(boolean sameBlockEntity, double playerDistanceSq) {
        return new SlottedAccessPlan(sameBlockEntity && playerDistanceSq <= ACTIVE_BASE_USE_DISTANCE_SQ,
                DEFAULT_STACK_LIMIT,
                List.of(),
                false,
                false);
    }

    public static SlotDecreasePlan decreaseSlot(boolean occupied, int stackSize, int amount) {
        if (!occupied || stackSize <= 0 || amount <= 0) {
            return new SlotDecreasePlan(false, 0, 0, false);
        }
        if (stackSize <= amount) {
            return new SlotDecreasePlan(true, stackSize, 0, true);
        }
        int removed = amount;
        int remaining = stackSize - amount;
        return new SlotDecreasePlan(true, removed, remaining, false);
    }

    public static int clampStackSize(int stackSize) {
        return clamp(stackSize, 0, DEFAULT_STACK_LIMIT);
    }

    private static List<Direction> allAdjacentDirections() {
        return List.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public record ReaSimColumnFluidState(BlockPos pos, int water, int maxWater, int steam) {
        public ReaSimColumnFluidState {
            pos = pos == null ? BlockPos.ZERO : pos;
            water = Math.max(0, water);
            maxWater = Math.max(0, maxWater);
            steam = Math.max(0, steam);
        }
    }

    public record ColumnTransfer(BlockPos columnPos, int amount) {
    }

    public record InletTransferPlan(int initialWater, int remainingWater, List<ColumnTransfer> toColumns) {
    }

    public record OutletTransferPlan(
            int initialSteam,
            int finalSteam,
            List<ColumnTransfer> fromColumns,
            List<Direction> sendDirections) {
    }

    public record SlottedAccessPlan(
            boolean usableByPlayer,
            int stackLimit,
            List<Integer> defaultAccessibleSlots,
            boolean defaultCanInsert,
            boolean defaultCanExtract) {
    }

    public record SlotDecreasePlan(boolean changed, int removedCount, int remainingCount, boolean emptiedSlot) {
    }
}

package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class RBMKDebrisBlockPlanner {
    public static final int BURNING_TICK_RATE_BASE = 100;
    public static final int BURNING_TICK_RATE_RANDOM = 20;
    public static final int RADIATING_TICK_RATE_BASE = 20;
    public static final int RADIATING_TICK_RATE_RANDOM = 20;
    public static final int FLAME_RANDOM_DENOMINATOR = 5;
    public static final int MELTDOWN_GAS_RANDOM_DENOMINATOR = 10;
    public static final int BURNING_EXTINGUISH_CHANCE = 100;
    public static final int BURNING_FOAM_OR_BORON_EXTINGUISH_CHANCE = 10;
    public static final int RADIATING_DECAY_CHANCE = 1000;
    public static final int RADIATING_BORON_DECAY_CHANCE = 25;
    public static final int RADIATING_MAX_META = 15;
    public static final int DIGAMMA_SPREAD_TICK_DELAY = 2;
    public static final String FLAME_PARTICLE = "rbmkflame";
    public static final int FLAME_MAX_AGE = 300;
    public static final int FLAME_PACKET_RANGE = 75;
    public static final String FIRE_SOUND = "fire.fire";
    public static final float RADIATING_BASE_RADS = 1_000_000.0F;
    public static final double RADIATING_RANGE = 100.0D;
    public static final double RADIATING_FIRE_DAMAGE_RANGE = 5.0D;
    public static final double RADIATING_MARSHMALLOW_RANGE = 10.0D;
    public static final int MARSHMALLOW_TOAST_RANDOM_DENOMINATOR = 100;

    private RBMKDebrisBlockPlanner() {
    }

    public static int burningTickDelay(int randomPart) {
        return BURNING_TICK_RATE_BASE + Math.floorMod(randomPart, BURNING_TICK_RATE_RANDOM);
    }

    public static int radiatingTickDelay(int randomPart) {
        return RADIATING_TICK_RATE_BASE + Math.floorMod(randomPart, RADIATING_TICK_RATE_RANDOM);
    }

    public static DebrisBlockTickPlan planBurningTick(BlockPos origin, Direction sampledDirection,
            DebrisNeighborKind neighborKind, boolean flameRoll, boolean gasRoll, boolean extinguishRoll,
            int nextTickRandomPart) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        Direction direction = sampledDirection == null ? Direction.NORTH : sampledDirection;
        DebrisNeighborKind safeNeighbor = neighborKind == null ? DebrisNeighborKind.OTHER : neighborKind;
        List<DebrisBlockAction> actions = new ArrayList<>();

        if (flameRoll) {
            actions.add(DebrisBlockAction.flame(safeOrigin, true));
        }
        if (gasRoll && safeNeighbor == DebrisNeighborKind.AIR) {
            actions.add(DebrisBlockAction.setBlock(safeOrigin.relative(direction), DebrisBlockState.MELTDOWN_GAS));
        }
        if (extinguishRoll) {
            actions.add(DebrisBlockAction.setBlock(safeOrigin, DebrisBlockState.PRIBRIS));
            return new DebrisBlockTickPlan(actions, 0, false);
        }

        return new DebrisBlockTickPlan(actions, burningTickDelay(nextTickRandomPart), true);
    }

    public static DebrisBlockTickPlan planRadiatingTick(BlockPos origin, int meta, Direction sampledDirection,
            DebrisNeighborKind neighborKind, boolean flameRoll, boolean gasRoll, boolean decayRoll,
            int nextTickRandomPart) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        Direction direction = sampledDirection == null ? Direction.NORTH : sampledDirection;
        DebrisNeighborKind safeNeighbor = neighborKind == null ? DebrisNeighborKind.OTHER : neighborKind;
        List<DebrisBlockAction> actions = new ArrayList<>();
        actions.add(DebrisBlockAction.radiate(safeOrigin));

        if (flameRoll) {
            actions.add(DebrisBlockAction.flame(safeOrigin, true));
        }
        if (gasRoll && safeNeighbor == DebrisNeighborKind.AIR) {
            actions.add(DebrisBlockAction.setBlock(safeOrigin.relative(direction), DebrisBlockState.MELTDOWN_GAS));
        }
        if (decayRoll) {
            if (meta < RADIATING_MAX_META) {
                actions.add(DebrisBlockAction.setMeta(safeOrigin, meta + 1));
                return new DebrisBlockTickPlan(actions, radiatingTickDelay(nextTickRandomPart), true);
            }
            actions.add(DebrisBlockAction.setBlock(safeOrigin, DebrisBlockState.PRIBRIS_BURNING));
            return new DebrisBlockTickPlan(actions, 0, false);
        }

        return new DebrisBlockTickPlan(actions, radiatingTickDelay(nextTickRandomPart), true);
    }

    public static List<DebrisBlockAction> planDigammaSpread(BlockPos origin, List<NeighborSample> neighbors) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        List<DebrisBlockAction> actions = new ArrayList<>();
        if (neighbors != null) {
            for (NeighborSample neighbor : neighbors) {
                if (neighbor == null || !isDigammaConvertible(neighbor.kind())) {
                    continue;
                }
                actions.add(DebrisBlockAction.setBlock(
                        safeOrigin.relative(neighbor.direction() == null ? Direction.NORTH : neighbor.direction()),
                        DebrisBlockState.PRIBRIS_DIGAMMA));
            }
        }
        return List.copyOf(actions);
    }

    public static AddBlockPlan planBurningAdded(BlockPos origin, boolean flameRoll, int nextTickRandomPart) {
        List<DebrisBlockAction> actions = flameRoll
                ? List.of(DebrisBlockAction.flame(origin == null ? BlockPos.ZERO : origin, false))
                : List.of();
        return new AddBlockPlan(actions, burningTickDelay(nextTickRandomPart));
    }

    public static AddBlockPlan planDigammaAdded() {
        return new AddBlockPlan(List.of(), DIGAMMA_SPREAD_TICK_DELAY);
    }

    public static int burningExtinguishChance(DebrisNeighborKind neighborKind) {
        return neighborKind == DebrisNeighborKind.FOAM || neighborKind == DebrisNeighborKind.BORON
                ? BURNING_FOAM_OR_BORON_EXTINGUISH_CHANCE
                : BURNING_EXTINGUISH_CHANCE;
    }

    public static int radiatingDecayChance(DebrisNeighborKind neighborKind) {
        return neighborKind == DebrisNeighborKind.BORON
                ? RADIATING_BORON_DECAY_CHANCE
                : RADIATING_DECAY_CHANCE;
    }

    private static boolean isDigammaConvertible(DebrisNeighborKind kind) {
        return kind == DebrisNeighborKind.DEBRIS
                || kind == DebrisNeighborKind.PRIBRIS
                || kind == DebrisNeighborKind.PRIBRIS_BURNING
                || kind == DebrisNeighborKind.PRIBRIS_RADIATING
                || kind == DebrisNeighborKind.CORIUM_BLOCK;
    }

    public enum DebrisNeighborKind {
        AIR,
        FOAM,
        BORON,
        DEBRIS,
        PRIBRIS,
        PRIBRIS_BURNING,
        PRIBRIS_RADIATING,
        CORIUM_BLOCK,
        OTHER
    }

    public enum DebrisBlockState {
        PRIBRIS,
        PRIBRIS_BURNING,
        PRIBRIS_RADIATING,
        PRIBRIS_DIGAMMA,
        MELTDOWN_GAS
    }

    public enum DebrisBlockActionType {
        SET_BLOCK,
        SET_META,
        SPAWN_FLAME,
        RADIATE
    }

    public record NeighborSample(Direction direction, DebrisNeighborKind kind) {
    }

    public record DebrisBlockAction(
            DebrisBlockActionType type,
            BlockPos pos,
            DebrisBlockState state,
            int meta,
            boolean playFireSound) {
        private static DebrisBlockAction setBlock(BlockPos pos, DebrisBlockState state) {
            return new DebrisBlockAction(DebrisBlockActionType.SET_BLOCK, pos, state, 0, false);
        }

        private static DebrisBlockAction setMeta(BlockPos pos, int meta) {
            return new DebrisBlockAction(DebrisBlockActionType.SET_META, pos, null, meta, false);
        }

        private static DebrisBlockAction flame(BlockPos pos, boolean playFireSound) {
            return new DebrisBlockAction(DebrisBlockActionType.SPAWN_FLAME, pos, null, 0, playFireSound);
        }

        private static DebrisBlockAction radiate(BlockPos pos) {
            return new DebrisBlockAction(DebrisBlockActionType.RADIATE, pos, null, 0, false);
        }
    }

    public record DebrisBlockTickPlan(List<DebrisBlockAction> actions, int nextTickDelay, boolean scheduleNextTick) {
    }

    public record AddBlockPlan(List<DebrisBlockAction> actions, int scheduledTickDelay) {
    }
}

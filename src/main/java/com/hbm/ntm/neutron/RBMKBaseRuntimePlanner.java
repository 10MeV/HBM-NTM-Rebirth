package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;

public final class RBMKBaseRuntimePlanner {
    public static final int DEFAULT_TRACKING_RANGE = 15;
    public static final int DEFAULT_COLUMN_RENDER_HEIGHT = 17;
    public static final double DEFAULT_MAX_HEAT = 1500.0D;
    public static final boolean DEFAULT_MODERATED = false;

    private static final Direction[] LEGACY_NEIGHBOR_DIRECTIONS = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };

    private static final List<String> DIAGNOSTIC_EXCLUDED_KEYS = List.of(
            "x",
            "y",
            "z",
            "items",
            "id",
            "muffled");

    private RBMKBaseRuntimePlanner() {
    }

    public static BaseTickPlan planServerTick(
            RBMKThermalState primary,
            List<RBMKThermalState> cachedNeighbors,
            RBMKRuntimeSettings settings) {
        RBMKRuntimeSettings safeSettings = settings == null ? RBMKRuntimeSettings.legacyDefaults() : settings;
        if (primary == null) {
            return BaseTickPlan.empty(DEFAULT_TRACKING_RANGE);
        }

        int craneIndicatorBefore = primary.craneIndicator();
        if (primary.craneIndicator() > 0) {
            primary.setCraneIndicator(primary.craneIndicator() - 1);
        }

        List<RBMKThermalState> neighbors = nonNullNeighbors(cachedNeighbors);
        boolean heatMoved = RBMKThermalRuntime.equalizeWithNeighbors(primary, neighbors, safeSettings);
        int connectedNeighbors = neighbors.size();

        double heatBeforeCooling = primary.heat();
        RBMKThermalRuntime.coolPassively(primary, safeSettings, connectedNeighbors);
        double passiveCoolingApplied = Math.max(0.0D, heatBeforeCooling - primary.heat());

        int reasimBoiledWater = safeSettings.reasimBoilers()
                ? RBMKThermalRuntime.boilWater(primary, safeSettings)
                : 0;

        return new BaseTickPlan(
                true,
                craneIndicatorBefore,
                primary.craneIndicator(),
                connectedNeighbors + 1,
                heatMoved,
                passiveCoolingApplied,
                safeSettings.reasimBoilers(),
                reasimBoiledWater,
                DEFAULT_TRACKING_RANGE,
                true);
    }

    public static List<BlockPos> neighborPositions(BlockPos origin) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        List<BlockPos> positions = new ArrayList<>(LEGACY_NEIGHBOR_DIRECTIONS.length);
        for (Direction direction : LEGACY_NEIGHBOR_DIRECTIONS) {
            positions.add(safeOrigin.relative(direction));
        }
        return List.copyOf(positions);
    }

    public static NeighborCachePlan planNeighborCacheRefresh(
            BlockPos origin,
            List<Boolean> cachedPresent,
            List<Boolean> cachedInvalid) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        List<NeighborCacheSlot> slots = new ArrayList<>(LEGACY_NEIGHBOR_DIRECTIONS.length);

        for (int i = 0; i < LEGACY_NEIGHBOR_DIRECTIONS.length; i++) {
            Direction direction = LEGACY_NEIGHBOR_DIRECTIONS[i];
            boolean present = flagAt(cachedPresent, i);
            boolean invalid = flagAt(cachedInvalid, i);
            boolean lookup = !present || invalid;
            slots.add(new NeighborCacheSlot(
                    i,
                    direction,
                    safeOrigin.relative(direction),
                    present && !invalid,
                    lookup,
                    invalid));
        }

        return new NeighborCachePlan(slots);
    }

    public static SerializationSnapshot serializationSnapshot(RBMKThermalState state) {
        if (state == null) {
            return new SerializationSnapshot(0.0D, 0, 0, (byte) 0);
        }
        return new SerializationSnapshot(
                state.heat(),
                state.reasimWater(),
                state.reasimSteam(),
                (byte) state.craneIndicator());
    }

    public static List<DiagnosticEntry> diagnosticEntries(CompoundTag tag) {
        if (tag == null) {
            return List.of();
        }

        List<DiagnosticEntry> entries = new ArrayList<>();
        tag.getAllKeys().stream()
                .filter(key -> !DIAGNOSTIC_EXCLUDED_KEYS.contains(key))
                .sorted(Comparator.naturalOrder())
                .forEach(key -> {
                    Tag value = tag.get(key);
                    if (value != null) {
                        entries.add(new DiagnosticEntry(key, stripLegacyNumericSuffix(value.toString())));
                    }
                });
        return List.copyOf(entries);
    }

    public static List<String> diagnosticExcludedKeys() {
        return DIAGNOSTIC_EXCLUDED_KEYS;
    }

    public static AABB renderBoundingBox(BlockPos origin) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        return new AABB(
                safeOrigin.getX(),
                safeOrigin.getY(),
                safeOrigin.getZ(),
                safeOrigin.getX() + 1.0D,
                safeOrigin.getY() + DEFAULT_COLUMN_RENDER_HEIGHT,
                safeOrigin.getZ() + 1.0D);
    }

    public static NodeRemovalPlan planNodeRemoval(BlockPos origin) {
        return new NodeRemovalPlan(origin == null ? BlockPos.ZERO : origin, true);
    }

    public static DefaultColumnContract defaultColumnContract() {
        return new DefaultColumnContract(
                DEFAULT_MAX_HEAT,
                RBMKColumnLifecyclePlanner.ColumnKind.OTHER,
                DEFAULT_MODERATED,
                true,
                true);
    }

    private static List<RBMKThermalState> nonNullNeighbors(List<RBMKThermalState> cachedNeighbors) {
        if (cachedNeighbors == null || cachedNeighbors.isEmpty()) {
            return List.of();
        }
        List<RBMKThermalState> neighbors = new ArrayList<>();
        for (RBMKThermalState neighbor : cachedNeighbors) {
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    private static boolean flagAt(List<Boolean> flags, int index) {
        return flags != null && index >= 0 && index < flags.size() && Boolean.TRUE.equals(flags.get(index));
    }

    private static String stripLegacyNumericSuffix(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char lastChar = value.charAt(value.length() - 1);
        if (lastChar == 'd' || lastChar == 's' || lastChar == 'b') {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    public record BaseTickPlan(
            boolean ticked,
            int craneIndicatorBefore,
            int craneIndicatorAfter,
            int thermalMembers,
            boolean heatMoved,
            double passiveCoolingApplied,
            boolean reasimBoilersEnabled,
            int reasimBoiledWater,
            int trackingRange,
            boolean networkPacketRequested) {
        private static BaseTickPlan empty(int trackingRange) {
            return new BaseTickPlan(false, 0, 0, 0, false, 0.0D, false, 0, trackingRange, false);
        }
    }

    public record NeighborCacheSlot(
            int index,
            Direction direction,
            BlockPos pos,
            boolean keepCached,
            boolean lookupRequired,
            boolean invalidatedCachedEntry) {
    }

    public record NeighborCachePlan(List<NeighborCacheSlot> slots) {
    }

    public record SerializationSnapshot(double heat, int reasimWater, int reasimSteam, byte craneIndicatorByte) {
    }

    public record DiagnosticEntry(String key, String legacyValue) {
    }

    public record NodeRemovalPlan(BlockPos pos, boolean removeNeutronNode) {
    }

    public record DefaultColumnContract(
            double maxHeat,
            RBMKColumnLifecyclePlanner.ColumnKind type,
            boolean moderated,
            boolean consoleNbtDefaultsToNull,
            boolean fancyStatsDefaultsToNull) {
    }
}

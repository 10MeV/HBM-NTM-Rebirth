package com.hbm.ntm.block;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hbm.ntm.block.LegacyDoorDefinition.BoundsProfile;
import static com.hbm.ntm.block.LegacyDoorDefinition.DoorOpenProgress;
import static com.hbm.ntm.block.LegacyDoorDefinition.DoorOpenRange;
import static com.hbm.ntm.block.LegacyDoorDefinition.LegacyDoorDimensions;

/**
 * Complete source-backed mapping of 1.7.10 {@code ModBlocks}' fourteen
 * {@code BlockDoorGeneric(..., DoorDecl.*)} registrations.
 *
 * <p>It intentionally does not touch {@code ModBlocks}, block entities, client registries, or
 * sounds.  The registration/implementation migration consumes this table in a later isolated
 * slice.</p>
 */
public final class LegacyDoorRegistry {

    public static final LegacyDoorDefinition TRANSITION_SEAL = door("transition_seal", 480,
            ranges(range(-9, 2, 0, 20, 20, 1)), dimensions(23, 0, 0, 0, 13, 12), List.of(),
            0, 0, false, DoorOpenProgress.defaultDuration(), BoundsProfile.DEFAULT);
    public static final LegacyDoorDefinition VAULT_DOOR = door("vault_door", 120,
            ranges(range(-1, 1, 0, 3, 3, 2)), dimensions(4, 0, 0, 0, 2, 2),
            List.of(dimensions(0, 0, 1, -1, 2, 2)), 0, 7, false,
            DoorOpenProgress.defaultDuration(), BoundsProfile.VAULT);
    public static final LegacyDoorDefinition FIRE_DOOR = door("fire_door", 160,
            ranges(range(-1, 0, 0, 3, 4, 1)), dimensions(2, 0, 0, 0, 2, 1), List.of(),
            0, 5, false, DoorOpenProgress.defaultDuration(), BoundsProfile.FIRE);
    public static final LegacyDoorDefinition SLIDING_BLAST_DOOR = door("sliding_blast_door", 24,
            ranges(range(-2, 0, 0, 4, 5, 1)), dimensions(3, 0, 0, 0, 3, 3), List.of(),
            0, 3, false, DoorOpenProgress.defaultDuration(), BoundsProfile.SLIDING_BLAST);
    public static final LegacyDoorDefinition SLIDING_SEAL_DOOR = door("sliding_seal_door", 20,
            ranges(range(0, 0, 0, 1, 2, 2)), dimensions(1, 0, 0, 0, 0, 0), List.of(),
            0, 0, false, DoorOpenProgress.defaultDuration(), BoundsProfile.SLIDING_SEAL);
    public static final LegacyDoorDefinition SECURE_ACCESS_DOOR = door("secure_access_door", 120,
            ranges(range(-2, 1, 0, 4, 5, 1)), dimensions(4, 0, 0, 0, 2, 2), List.of(),
            0, 4, false, DoorOpenProgress.defaultDuration(), BoundsProfile.SECURE_ACCESS);
    public static final LegacyDoorDefinition ROUND_AIRLOCK_DOOR = door("round_airlock_door", 60,
            ranges(range(0, 0, 0, -2, 4, 2), range(0, 0, 0, 3, 4, 2)),
            dimensions(3, 0, 0, 0, 2, 1), List.of(), 0, 3, false,
            DoorOpenProgress.defaultDuration(), BoundsProfile.ROUND_AIRLOCK);
    public static final LegacyDoorDefinition QE_SLIDING_DOOR = door("qe_sliding_door", 10,
            ranges(range(0, 0, 0, 2, 2, 2)), dimensions(1, 0, 0, 0, 1, 0), List.of(),
            0, 0, false, DoorOpenProgress.defaultDuration(), BoundsProfile.QE_SLIDING);
    public static final LegacyDoorDefinition QE_CONTAINMENT = door("qe_containment", 160,
            ranges(range(-1, 0, 0, 3, 3, 1)), dimensions(2, 0, 0, 0, 1, 1), List.of(),
            0, 3, false, DoorOpenProgress.defaultDuration(), BoundsProfile.QE_CONTAINMENT);
    public static final LegacyDoorDefinition WATER_DOOR = door("water_door", 60,
            ranges(range(1, 0, 0, -3, 3, 2)), dimensions(2, 0, 0, 0, 1, 1), List.of(),
            0, 2, false, DoorOpenProgress.fixedWindow(35, 40), BoundsProfile.WATER);
    public static final LegacyDoorDefinition SILO_HATCH = door("silo_hatch", 60,
            ranges(range(1, 0, 1, -3, 3, 0), range(0, 0, 1, -3, 3, 0),
                    range(-1, 0, 1, -3, 3, 0)),
            dimensions(0, 0, 2, 2, 2, 2), List.of(), 2, 0, true,
            DoorOpenProgress.fixedWindow(20, 20), BoundsProfile.DEFAULT);
    public static final LegacyDoorDefinition SILO_HATCH_LARGE = door("silo_hatch_large", 60,
            ranges(range(2, 0, 1, -3, 3, 0), range(1, 0, 2, -5, 3, 0),
                    range(0, 0, 2, -5, 3, 0), range(-1, 0, 2, -5, 3, 0),
                    range(-2, 0, 1, -3, 3, 0)),
            dimensions(0, 0, 3, 3, 3, 3), List.of(), 3, 0, true,
            DoorOpenProgress.fixedWindow(20, 20), BoundsProfile.DEFAULT);
    public static final LegacyDoorDefinition LARGE_VEHICLE_DOOR = door("large_vehicle_door", 60,
            ranges(range(0, 0, 0, -4, 6, 2), range(0, 0, 0, 4, 6, 2)),
            dimensions(5, 0, 0, 0, 3, 3), List.of(), 0, 0, false,
            DoorOpenProgress.defaultDuration(), BoundsProfile.LARGE_VEHICLE);
    public static final LegacyDoorDefinition CARGO_DOOR = door("cargo_door", 60,
            ranges(range(-1, -1, 0, 3, 3, 1)), dimensions(2, 0, 0, 0, 1, 1), List.of(),
            0, 1, false, DoorOpenProgress.defaultDuration(), BoundsProfile.CARGO);

    private static final List<LegacyDoorDefinition> ALL = List.of(
            TRANSITION_SEAL, VAULT_DOOR, FIRE_DOOR, SLIDING_BLAST_DOOR, SLIDING_SEAL_DOOR,
            SECURE_ACCESS_DOOR, ROUND_AIRLOCK_DOOR, QE_SLIDING_DOOR, QE_CONTAINMENT, WATER_DOOR,
            SILO_HATCH, SILO_HATCH_LARGE, LARGE_VEHICLE_DOOR, CARGO_DOOR);
    private static final Map<String, LegacyDoorDefinition> BY_ID = byId(ALL);

    private LegacyDoorRegistry() {
    }

    public static List<LegacyDoorDefinition> all() {
        return ALL;
    }

    public static Optional<LegacyDoorDefinition> find(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static LegacyDoorDefinition require(String id) {
        LegacyDoorDefinition definition = BY_ID.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("No legacy generic-door definition for " + id);
        }
        return definition;
    }

    private static LegacyDoorDefinition door(String id, int timeToOpen, List<DoorOpenRange> ranges,
                                              LegacyDoorDimensions dimensions,
                                              List<LegacyDoorDimensions> extraDimensions, int blockOffset,
                                              int skinCount, boolean remoteControllable,
                                              DoorOpenProgress openProgress, BoundsProfile boundsProfile) {
        return new LegacyDoorDefinition(id, timeToOpen, ranges, dimensions, extraDimensions, blockOffset,
                skinCount, remoteControllable, openProgress, boundsProfile);
    }

    private static List<DoorOpenRange> ranges(DoorOpenRange... ranges) {
        return List.of(ranges);
    }

    private static DoorOpenRange range(int x, int y, int z, int tangentAmount1,
                                       int tangentAmount2, int axis) {
        return new DoorOpenRange(x, y, z, tangentAmount1, tangentAmount2, axis);
    }

    private static LegacyDoorDimensions dimensions(int x1, int y1, int z1, int x2, int y2, int z2) {
        return new LegacyDoorDimensions(x1, y1, z1, x2, y2, z2);
    }

    private static Map<String, LegacyDoorDefinition> byId(List<LegacyDoorDefinition> definitions) {
        Map<String, LegacyDoorDefinition> byId = new LinkedHashMap<>();
        for (LegacyDoorDefinition definition : definitions) {
            if (byId.put(definition.id(), definition) != null) {
                throw new IllegalStateException("Duplicate legacy generic-door id: " + definition.id());
            }
        }
        return Map.copyOf(byId);
    }
}

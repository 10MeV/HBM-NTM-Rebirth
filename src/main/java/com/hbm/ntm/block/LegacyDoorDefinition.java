package com.hbm.ntm.block;

import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;

/**
 * Immutable, server-safe transcription of one 1.7.10 {@code DoorDecl}.
 *
 * <p>This deliberately contains no block registration, block-entity state, rendering, audio, or
 * placement logic.  A later generic-door implementation must use this definition for the legacy
 * dummy layout and its per-local-block collision/selection contract instead of duplicating data
 * from {@code DoorDecl}.</p>
 */
public final class LegacyDoorDefinition {

    private static final DoorBounds FULL_BLOCK = new DoorBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final DoorBounds EMPTY = new DoorBounds(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    private final String id;
    private final int timeToOpen;
    private final List<DoorOpenRange> doorOpenRanges;
    private final LegacyDoorDimensions dimensions;
    private final List<LegacyDoorDimensions> extraDimensions;
    private final int blockOffset;
    private final int skinCount;
    private final boolean remoteControllable;
    private final DoorOpenProgress openProgress;
    private final BoundsProfile boundsProfile;

    LegacyDoorDefinition(String id, int timeToOpen, List<DoorOpenRange> doorOpenRanges,
                         LegacyDoorDimensions dimensions, List<LegacyDoorDimensions> extraDimensions,
                         int blockOffset, int skinCount, boolean remoteControllable,
                         DoorOpenProgress openProgress, BoundsProfile boundsProfile) {
        this.id = Objects.requireNonNull(id, "id");
        if (timeToOpen <= 0) {
            throw new IllegalArgumentException("timeToOpen must be positive");
        }
        if (skinCount < 0) {
            throw new IllegalArgumentException("skinCount cannot be negative");
        }
        this.timeToOpen = timeToOpen;
        this.doorOpenRanges = List.copyOf(doorOpenRanges);
        this.dimensions = Objects.requireNonNull(dimensions, "dimensions");
        this.extraDimensions = List.copyOf(extraDimensions);
        this.blockOffset = blockOffset;
        this.skinCount = skinCount;
        this.remoteControllable = remoteControllable;
        this.openProgress = Objects.requireNonNull(openProgress, "openProgress");
        this.boundsProfile = Objects.requireNonNull(boundsProfile, "boundsProfile");
    }

    public String id() {
        return id;
    }

    public int timeToOpen() {
        return timeToOpen;
    }

    /** Raw six-integer ranges consumed by legacy {@code TileEntityDoorGeneric#updateEntity}. */
    public List<DoorOpenRange> doorOpenRanges() {
        return doorOpenRanges;
    }

    /** Raw six-integer {@code BlockDummyable#getDimensions()} result. */
    public LegacyDoorDimensions dimensions() {
        return dimensions;
    }

    /** Additional raw six-integer requirement layouts, normally empty. */
    public List<LegacyDoorDimensions> extraDimensions() {
        return extraDimensions;
    }

    public int blockOffset() {
        return blockOffset;
    }

    public int skinCount() {
        return skinCount;
    }

    public boolean hasSkins() {
        return skinCount > 0;
    }

    public boolean remoteControllable() {
        return remoteControllable;
    }

    public DoorOpenProgress openProgress() {
        return openProgress;
    }

    /**
     * Exact old {@code getDoorRangeOpenTime(ticks, rangeIndex)} result.  The {@code rangeIndex}
     * remains an argument because the legacy API has one, although all fourteen declarations use
     * one common window for each of their ranges.
     */
    public float doorRangeOpenTime(int ticks, int rangeIndex) {
        if (rangeIndex < 0 || rangeIndex >= doorOpenRanges.size()) {
            throw new IndexOutOfBoundsException("rangeIndex=" + rangeIndex);
        }
        return openProgress.normalized(ticks, timeToOpen);
    }

    public BoundsProfile boundsProfile() {
        return boundsProfile;
    }

    /**
     * Exact local {@code DoorDecl#getBlockBound} result before the block's legacy facing rotation.
     * A degenerate box means that the original generic-door block had no collision at that local
     * position; selection may intentionally differ from collision for two declarations.
     */
    public AABB blockBounds(int x, int y, int z, boolean open, boolean forCollision) {
        return boundsProfile.bounds(x, y, z, open, forCollision);
    }

    public record DoorOpenRange(int startX, int startY, int startZ, int tangentAmount1,
                                int tangentAmount2, int axis) {
        public DoorOpenRange {
            if (axis < 0 || axis > 2) {
                throw new IllegalArgumentException("legacy door range axis must be 0, 1, or 2");
            }
        }
    }

    /**
     * A direct named form of the six-int legacy XR layout data.  The values are intentionally not
     * normalized: {@code BlockDummyable} and {@code MultiblockHandlerXR} own their interpretation.
     */
    public record LegacyDoorDimensions(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    /**
     * Legacy {@code getNormTime(ticks, min, max)} window.  Equal bounds are preserved exactly;
     * the old float division produces NaN/infinity rather than a modernized special case.
     */
    public record DoorOpenProgress(int minTicks, int maxTicks, boolean usesDefaultDoorDuration) {
        public static DoorOpenProgress defaultDuration() {
            return new DoorOpenProgress(0, 0, true);
        }

        public static DoorOpenProgress fixedWindow(int minTicks, int maxTicks) {
            return new DoorOpenProgress(minTicks, maxTicks, false);
        }

        float normalized(int ticks, int timeToOpen) {
            int min = usesDefaultDoorDuration ? 0 : minTicks;
            int max = usesDefaultDoorDuration ? timeToOpen : maxTicks;
            float normalized = (ticks - min) / (float) (max - min);
            return Math.max(0.0F, Math.min(1.0F, normalized));
        }
    }

    public record DoorBounds(double minX, double minY, double minZ,
                             double maxX, double maxY, double maxZ) {
        private AABB asAabb() {
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    /** Source-specific local AABB profiles from 1.7.10 {@code DoorDecl#getBlockBound}. */
    public enum BoundsProfile {
        DEFAULT {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                return open ? EMPTY : FULL_BLOCK;
            }
        },
        VAULT {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                return !open || y == 0 ? FULL_BLOCK : EMPTY;
            }
        },
        FIRE {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!open) return FULL_BLOCK;
                if (z == 1) return box(0.5D, 0, 0, 1, 1, 1);
                if (z == -2) return box(0, 0, 0, 0.5D, 1, 1);
                if (y > 1) return box(0, 0.75D, 0, 1, 1, 1);
                if (y == 0) return box(0, 0, 0, 1, forCollision ? 0 : 0.1D, 1);
                return EMPTY;
            }
        },
        SLIDING_BLAST {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (open && y == 3) return box(0, 0.5D, 0, 1, 1, 1);
                if (open && y == 0) return box(0, 0, 0, 1, forCollision ? 0 : 0.08D, 1);
                return DEFAULT.localBounds(x, y, z, open, forCollision);
            }
        },
        SLIDING_SEAL {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                return forCollision && open ? EMPTY : box(0, 0, 0.75D, 1, 1, 1);
            }
        },
        SECURE_ACCESS {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!open) return y > 0 ? box(0, 0, 0.375D, 1, 1, 0.625D) : FULL_BLOCK;
                if (y == 1) return box(0, 0, 0, 1, forCollision ? 0 : 0.0625D, 1);
                if (y == 4) return box(0, 0.5D, 0.15D, 1, 1, 0.85D);
                return y == 0 ? FULL_BLOCK : EMPTY;
            }
        },
        ROUND_AIRLOCK {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!open) return FULL_BLOCK;
                if (z == 1) return box(0.4D, 0, 0, 1, 1, 1);
                if (z == -2) return box(0, 0, 0, 0.6D, 1, 1);
                if (y == 3) return box(0, 0.5D, 0, 1, 1, 1);
                if (y == 0) return box(0, 0, 0, 1, forCollision ? 0 : 0.0625D, 1);
                return EMPTY;
            }
        },
        QE_SLIDING {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!forCollision || !open) return box(0, 0, 0.8125D, 1, 1, 1);
                return z == 0
                        ? box(0.875D, 0, 0.8125D, 1, 1, 1)
                        : box(0, 0, 0.8125D, 0.125D, 1, 1);
            }
        },
        QE_CONTAINMENT {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!open) return box(0, 0, 0.5D, 1, 1, 1);
                if (y > 1) return box(0, 0.25D, 0.5D, 1, 1, 1);
                if (y == 0) return box(0, 0, 0.5D, 1, forCollision ? 0 : 0.125D, 1);
                return EMPTY;
            }
        },
        WATER {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!open) return box(0, 0, 0.75D, 1, 1, 1);
                if (y > 1) return box(0, 0.85D, 0.75D, 1, 1, 1);
                if (y == 0) return box(0, 0, 0.75D, 1, forCollision ? 0 : 0.15D, 1);
                return EMPTY;
            }
        },
        LARGE_VEHICLE {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!open) return FULL_BLOCK;
                if (z == 3) return box(0.4D, 0, 0, 1, 1, 1);
                if (z == -3) return box(0, 0, 0, 0.6D, 1, 1);
                if (y == 0) return box(0, 0, 0, 1, forCollision ? 0 : 0.0625D, 1);
                return EMPTY;
            }
        },
        CARGO {
            @Override
            DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision) {
                if (!open) return box(0, 0, 0.375D, 1, 1, 0.625D);
                if (y > 1) return box(0, 0.25D, 0.375D, 1, 1, 0.625D);
                if (y == 0) return box(0, 0, 0.375D, 1, forCollision ? 0 : 0.125D, 0.625D);
                return EMPTY;
            }
        };

        public AABB bounds(int x, int y, int z, boolean open, boolean forCollision) {
            return localBounds(x, y, z, open, forCollision).asAabb();
        }

        abstract DoorBounds localBounds(int x, int y, int z, boolean open, boolean forCollision);

        private static DoorBounds box(double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ) {
            return new DoorBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}

package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Describes a legacy multiblock footprint as offsets from the core block.
 */
public final class LegacyMultiblockLayout {
    private final List<BlockPos> offsets;
    private final List<BlockPos> checkOnlyOffsets;
    private final Set<BlockPos> legacyExtraOffsets;
    private final Function<BlockPos, LegacyProxyMode> proxyModes;

    private LegacyMultiblockLayout(Iterable<BlockPos> offsets, Function<BlockPos, LegacyProxyMode> proxyModes) {
        this(offsets, List.of(), List.of(), proxyModes);
    }

    private LegacyMultiblockLayout(Iterable<BlockPos> offsets, Iterable<BlockPos> checkOnlyOffsets,
            Iterable<BlockPos> legacyExtraOffsets,
            Function<BlockPos, LegacyProxyMode> proxyModes) {
        this.offsets = List.copyOf(copyOffsets(offsets));
        this.checkOnlyOffsets = List.copyOf(copyOffsets(checkOnlyOffsets));
        this.legacyExtraOffsets = Set.copyOf(copyOffsets(legacyExtraOffsets));
        this.proxyModes = proxyModes;
    }

    public static LegacyMultiblockLayout ofOffsets(Iterable<BlockPos> offsets) {
        return new LegacyMultiblockLayout(offsets, offset -> LegacyProxyMode.none());
    }

    public static LegacyMultiblockLayout ofLegacyXr(int[] dimensions, Direction facing) {
        return ofExtents(MultiblockExtents.ofLegacyXr(dimensions, facing));
    }

    public static LegacyMultiblockLayout ofLegacyXr(int[] dimensions, Direction facing, Predicate<BlockPos> proxyOffsets) {
        return ofLegacyXr(dimensions, facing).withProxyPredicate(proxyOffsets);
    }

    public static LegacyMultiblockLayout ofExtents(MultiblockExtents extents) {
        List<BlockPos> offsets = new ArrayList<>();
        offsets.add(BlockPos.ZERO);
        offsets.addAll(extents.offsets());
        return ofOffsets(offsets);
    }

    public LegacyMultiblockLayout withProxyPredicate(Predicate<BlockPos> proxyOffsets) {
        return withProxyPredicate(proxyOffsets, LegacyProxyMode.fullCombo());
    }

    public LegacyMultiblockLayout withProxyPredicate(Predicate<BlockPos> proxyOffsets, LegacyProxyMode mode) {
        return withProxyModes(offset -> proxyOffsets.test(offset) ? mode : LegacyProxyMode.none());
    }

    public LegacyMultiblockLayout withProxyModes(Function<BlockPos, LegacyProxyMode> proxyModes) {
        Function<BlockPos, LegacyProxyMode> previousProxyModes = this.proxyModes;
        return new LegacyMultiblockLayout(offsets, checkOnlyOffsets, legacyExtraOffsets,
                mergeProxyModes(previousProxyModes, proxyModes));
    }

    public LegacyMultiblockLayout withProxyOffsets(Iterable<BlockPos> proxyOffsets) {
        return withProxyOffsets(proxyOffsets, LegacyProxyMode.fullCombo());
    }

    public LegacyMultiblockLayout withProxyOffsets(Iterable<BlockPos> proxyOffsets, LegacyProxyMode mode) {
        Set<BlockPos> proxies = copyOffsets(proxyOffsets);
        return withProxyPredicate(proxies::contains, mode);
    }

    public LegacyMultiblockLayout withExtraOffsets(Iterable<BlockPos> extraOffsets) {
        return withExtraOffsets(extraOffsets, (Function<BlockPos, LegacyProxyMode>) offset -> LegacyProxyMode.none());
    }

    public LegacyMultiblockLayout withExtraProxyOffsets(Iterable<BlockPos> extraOffsets) {
        return withExtraProxyOffsets(extraOffsets, LegacyProxyMode.fullCombo());
    }

    public LegacyMultiblockLayout withExtraProxyOffsets(Iterable<BlockPos> extraOffsets, LegacyProxyMode mode) {
        Set<BlockPos> extras = copyOffsets(extraOffsets);
        return withExtraOffsets(extras,
                (Function<BlockPos, LegacyProxyMode>) offset -> extras.contains(offset) ? mode : LegacyProxyMode.none())
                .withLegacyExtraOffsets(extras);
    }

    public LegacyMultiblockLayout withExtraOffsets(Iterable<BlockPos> extraOffsets, Predicate<BlockPos> extraProxyOffsets) {
        return withExtraOffsets(extraOffsets,
                (Function<BlockPos, LegacyProxyMode>) offset -> extraProxyOffsets.test(offset) ? LegacyProxyMode.fullCombo() : LegacyProxyMode.none());
    }

    public LegacyMultiblockLayout withExtraOffsets(Iterable<BlockPos> extraOffsets,
            Function<BlockPos, LegacyProxyMode> extraProxyModes) {
        Set<BlockPos> merged = copyOffsets(offsets);
        merged.addAll(copyOffsets(extraOffsets));
        Function<BlockPos, LegacyProxyMode> previousProxyModes = proxyModes;
        return new LegacyMultiblockLayout(merged, checkOnlyOffsets, legacyExtraOffsets,
                mergeProxyModes(previousProxyModes, extraProxyModes));
    }

    public LegacyMultiblockLayout withLegacyExtraOffsets(Iterable<BlockPos> extraOffsets) {
        Set<BlockPos> legacyExtras = copyOffsets(legacyExtraOffsets);
        legacyExtras.addAll(copyOffsets(extraOffsets));
        return new LegacyMultiblockLayout(offsets, checkOnlyOffsets, legacyExtras, proxyModes);
    }

    public LegacyMultiblockLayout withLegacyExtraProxyOffsets(Iterable<BlockPos> extraOffsets, LegacyProxyMode mode) {
        return withExtraProxyOffsets(extraOffsets, mode).withLegacyExtraOffsets(extraOffsets);
    }

    public LegacyMultiblockLayout withCheckOnlyOffsets(Iterable<BlockPos> extraOffsets) {
        Set<BlockPos> merged = copyOffsets(checkOffsets());
        merged.addAll(copyOffsets(extraOffsets));
        return new LegacyMultiblockLayout(offsets, merged, legacyExtraOffsets, proxyModes);
    }

    public List<BlockPos> offsets() {
        return offsets;
    }

    public boolean containsOffset(BlockPos offset) {
        return offsets.contains(offset);
    }

    public List<BlockPos> checkOffsets() {
        Set<BlockPos> merged = copyOffsets(offsets);
        merged.addAll(checkOnlyOffsets);
        return List.copyOf(merged);
    }

    public boolean isProxyOffset(BlockPos offset) {
        return proxyMode(offset).isProxy();
    }

    public boolean isLegacyExtraOffset(BlockPos offset) {
        return legacyExtraOffsets.contains(offset) || proxyMode(offset).isProxy();
    }

    public LegacyProxyMode proxyMode(BlockPos offset) {
        LegacyProxyMode mode = proxyModes.apply(offset);
        return mode == null ? LegacyProxyMode.none() : mode;
    }

    public AABB renderBoundingBox(BlockPos corePos, double padding) {
        int minX = 0;
        int minY = 0;
        int minZ = 0;
        int maxX = 1;
        int maxY = 1;
        int maxZ = 1;
        for (BlockPos offset : checkOffsets()) {
            minX = Math.min(minX, offset.getX());
            minY = Math.min(minY, offset.getY());
            minZ = Math.min(minZ, offset.getZ());
            maxX = Math.max(maxX, offset.getX() + 1);
            maxY = Math.max(maxY, offset.getY() + 1);
            maxZ = Math.max(maxZ, offset.getZ() + 1);
        }
        return new AABB(
                corePos.getX() + minX - padding,
                corePos.getY() + minY - padding,
                corePos.getZ() + minZ - padding,
                corePos.getX() + maxX + padding,
                corePos.getY() + maxY + padding,
                corePos.getZ() + maxZ + padding);
    }

    public VoxelShape shape(double height) {
        int minX = 0;
        int minY = 0;
        int minZ = 0;
        int maxX = 1;
        double maxY = height;
        int maxZ = 1;
        for (BlockPos offset : offsets) {
            minX = Math.min(minX, offset.getX());
            minY = Math.min(minY, offset.getY());
            minZ = Math.min(minZ, offset.getZ());
            maxX = Math.max(maxX, offset.getX() + 1);
            maxY = Math.max(maxY, offset.getY() + height);
            maxZ = Math.max(maxZ, offset.getZ() + 1);
        }
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static BlockPos behind(Direction facing) {
        return relative(facing.getOpposite());
    }

    public static BlockPos clockwise(Direction facing) {
        return relative(facing.getClockWise());
    }

    public static BlockPos relative(Direction direction) {
        return new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    private static Set<BlockPos> copyOffsets(Iterable<BlockPos> offsets) {
        Set<BlockPos> copied = new LinkedHashSet<>();
        for (BlockPos offset : offsets) {
            copied.add(offset.immutable());
        }
        return copied;
    }

    private static Function<BlockPos, LegacyProxyMode> mergeProxyModes(
            Function<BlockPos, LegacyProxyMode> previousModes,
            Function<BlockPos, LegacyProxyMode> addedModes) {
        return offset -> {
            LegacyProxyMode previous = proxyModeOrNone(previousModes, offset);
            return previous.isProxy() ? previous : proxyModeOrNone(addedModes, offset);
        };
    }

    private static LegacyProxyMode proxyModeOrNone(Function<BlockPos, LegacyProxyMode> proxyModes, BlockPos offset) {
        LegacyProxyMode mode = proxyModes.apply(offset);
        return mode == null ? LegacyProxyMode.none() : mode;
    }
}

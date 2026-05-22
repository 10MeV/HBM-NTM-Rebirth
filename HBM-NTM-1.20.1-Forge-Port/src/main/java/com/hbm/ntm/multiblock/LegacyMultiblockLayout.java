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
import java.util.function.Predicate;

/**
 * Describes a legacy multiblock footprint as offsets from the core block.
 */
public final class LegacyMultiblockLayout {
    private final List<BlockPos> offsets;
    private final List<BlockPos> checkOnlyOffsets;
    private final Predicate<BlockPos> proxyOffsets;

    private LegacyMultiblockLayout(Iterable<BlockPos> offsets, Predicate<BlockPos> proxyOffsets) {
        this(offsets, List.of(), proxyOffsets);
    }

    private LegacyMultiblockLayout(Iterable<BlockPos> offsets, Iterable<BlockPos> checkOnlyOffsets,
            Predicate<BlockPos> proxyOffsets) {
        this.offsets = List.copyOf(copyOffsets(offsets));
        this.checkOnlyOffsets = List.copyOf(copyOffsets(checkOnlyOffsets));
        this.proxyOffsets = proxyOffsets;
    }

    public static LegacyMultiblockLayout ofOffsets(Iterable<BlockPos> offsets) {
        return new LegacyMultiblockLayout(offsets, offset -> false);
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
        return new LegacyMultiblockLayout(offsets, checkOnlyOffsets, proxyOffsets);
    }

    public LegacyMultiblockLayout withProxyOffsets(Iterable<BlockPos> proxyOffsets) {
        Set<BlockPos> proxies = copyOffsets(proxyOffsets);
        return withProxyPredicate(proxies::contains);
    }

    public LegacyMultiblockLayout withExtraOffsets(Iterable<BlockPos> extraOffsets) {
        return withExtraOffsets(extraOffsets, offset -> false);
    }

    public LegacyMultiblockLayout withExtraProxyOffsets(Iterable<BlockPos> extraOffsets) {
        Set<BlockPos> extras = copyOffsets(extraOffsets);
        return withExtraOffsets(extras, extras::contains);
    }

    public LegacyMultiblockLayout withExtraOffsets(Iterable<BlockPos> extraOffsets, Predicate<BlockPos> extraProxyOffsets) {
        Set<BlockPos> merged = copyOffsets(offsets);
        merged.addAll(copyOffsets(extraOffsets));
        Predicate<BlockPos> previousProxyOffsets = proxyOffsets;
        return new LegacyMultiblockLayout(merged, checkOnlyOffsets,
                offset -> previousProxyOffsets.test(offset) || extraProxyOffsets.test(offset));
    }

    public LegacyMultiblockLayout withCheckOnlyOffsets(Iterable<BlockPos> extraOffsets) {
        Set<BlockPos> merged = copyOffsets(checkOffsets());
        merged.addAll(copyOffsets(extraOffsets));
        return new LegacyMultiblockLayout(offsets, merged, proxyOffsets);
    }

    public List<BlockPos> offsets() {
        return offsets;
    }

    public List<BlockPos> checkOffsets() {
        Set<BlockPos> merged = copyOffsets(offsets);
        merged.addAll(checkOnlyOffsets);
        return List.copyOf(merged);
    }

    public boolean isProxyOffset(BlockPos offset) {
        return proxyOffsets.test(offset);
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
        int minZ = 0;
        int maxX = 1;
        int maxZ = 1;
        for (BlockPos offset : offsets) {
            minX = Math.min(minX, offset.getX());
            minZ = Math.min(minZ, offset.getZ());
            maxX = Math.max(maxX, offset.getX() + 1);
            maxZ = Math.max(maxZ, offset.getZ() + 1);
        }
        return Shapes.box(minX, 0.0D, minZ, maxX, height, maxZ);
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
}

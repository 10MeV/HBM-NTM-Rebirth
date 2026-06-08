package com.hbm.ntm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

/**
 * Migration carrier for 1.7.10-style NetworkRegistry.TargetPoint data.
 */
public final class LegacyTargetPoint {
    private static final int LEGACY_FACTORY_COUNT = 1;
    private static final int MODERN_FACTORY_COUNT = 8;

    private final Integer legacyDimensionId;
    private final ResourceKey<Level> dimension;
    private final double x;
    private final double y;
    private final double z;
    private final double range;

    private LegacyTargetPoint(Integer legacyDimensionId, ResourceKey<Level> dimension,
                              double x, double y, double z, double range) {
        this.legacyDimensionId = legacyDimensionId;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.range = range;
    }

    public static LegacyTargetPoint legacy(int dimensionId, double x, double y, double z, double range) {
        return new LegacyTargetPoint(dimensionId, null, x, y, z, range);
    }

    public static LegacyTargetPoint modern(ResourceKey<Level> dimension, double x, double y, double z, double range) {
        return new LegacyTargetPoint(null, dimension, x, y, z, range);
    }

    public static LegacyTargetPoint modern(ResourceKey<Level> dimension, BlockPos pos, double range) {
        if (pos == null) {
            return null;
        }
        return modern(dimension, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, range);
    }

    public static LegacyTargetPoint from(ServerLevel level, double x, double y, double z, double range) {
        if (level == null) {
            return null;
        }
        return modern(level.dimension(), x, y, z, range);
    }

    public static LegacyTargetPoint from(Level level, double x, double y, double z, double range) {
        if (level == null) {
            return null;
        }
        return modern(level.dimension(), x, y, z, range);
    }

    public static LegacyTargetPoint from(ServerLevel level, BlockPos pos, double range) {
        if (level == null || pos == null) {
            return null;
        }
        return modern(level.dimension(), pos, range);
    }

    public static LegacyTargetPoint from(Level level, BlockPos pos, double range) {
        if (level == null || pos == null) {
            return null;
        }
        return modern(level.dimension(), pos, range);
    }

    public static LegacyTargetPoint from(Entity entity, double range) {
        if (entity == null) {
            return null;
        }
        return from(entity.level(), entity.getX(), entity.getY(), entity.getZ(), range);
    }

    public static LegacyTargetPoint from(BlockEntity blockEntity, double range) {
        if (blockEntity == null) {
            return null;
        }
        return from(blockEntity.getLevel(), blockEntity.getBlockPos(), range);
    }

    public boolean hasModernDimension() {
        return dimension != null;
    }

    public boolean hasLegacyDimensionId() {
        return legacyDimensionId != null;
    }

    public int legacyDimensionIdOrZero() {
        return legacyDimensionId == null ? 0 : legacyDimensionId;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public double range() {
        return range;
    }

    public PacketDistributor.TargetPoint toModernTargetPoint() {
        if (dimension == null) {
            return null;
        }
        return new PacketDistributor.TargetPoint(x, y, z, range, dimension);
    }

    public String summary() {
        return hasModernDimension()
                ? "dimensionKey=" + dimension.location() + " pos=" + x + "," + y + "," + z + " range=" + range
                : "dimensionId=" + legacyDimensionIdOrZero() + " pos=" + x + "," + y + "," + z + " range=" + range;
    }

    public static int legacyFactoryCount() {
        return LEGACY_FACTORY_COUNT;
    }

    public static int modernFactoryCount() {
        return MODERN_FACTORY_COUNT;
    }

    public static String compatibilitySummary() {
        return "legacyTargetPoint=NetworkRegistry.TargetPoint carrier"
                + " legacyFactories=" + legacyFactoryCount()
                + " modernFactories=" + modernFactoryCount()
                + " note=legacy int dimension ids remain blocked; use ResourceKey<Level>, ServerLevel, Entity, or BlockEntity factories";
    }
}

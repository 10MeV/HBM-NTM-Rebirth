package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public record RadarScanVolume(ServerLevel level, BlockPos origin, int range, int verticalBuffer,
                              int minimumAltitude) {
    public RadarScanVolume {
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }
        if (origin == null) {
            throw new IllegalArgumentException("origin cannot be null");
        }
        origin = origin.immutable();
        range = Math.max(0, range);
        verticalBuffer = Math.max(0, verticalBuffer);
    }

    public static RadarScanVolume fromContext(RadarContext context) {
        return new RadarScanVolume(context.level(), context.origin(), context.range(), context.verticalBuffer(),
                context.minimumAltitude());
    }

    public boolean isOperationalAltitude() {
        return origin.getY() >= minimumAltitude;
    }

    public AABB bounds() {
        double centerX = origin.getX() + 0.5D;
        double centerZ = origin.getZ() + 0.5D;
        return new AABB(
                centerX - range,
                origin.getY() + verticalBuffer,
                centerZ - range,
                centerX + range,
                level.getMaxBuildHeight(),
                centerZ + range);
    }

    public boolean contains(Entity entity) {
        return entity != null
                && entity.level() == level
                && Math.abs(entity.getX() - (origin.getX() + 0.5D)) <= range
                && Math.abs(entity.getZ() - (origin.getZ() + 0.5D)) <= range
                && entity.getY() - origin.getY() > verticalBuffer;
    }
}

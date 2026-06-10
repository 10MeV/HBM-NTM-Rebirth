package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public enum RadarCommandTargetProfile {
    LAUNCH_PAD_BASE(EntityMode.FLOOR_XZ, PositionMode.BLOCK),
    LAUNCH_TABLE(EntityMode.LEGACY_X_MIRRORED, PositionMode.BLOCK),
    COMPACT_LAUNCHER(EntityMode.LEGACY_X_MIRRORED, PositionMode.BLOCK),
    ARTILLERY_TURRET(EntityMode.EXACT, PositionMode.CENTERED);

    private final EntityMode entityMode;
    private final PositionMode positionMode;

    RadarCommandTargetProfile(EntityMode entityMode, PositionMode positionMode) {
        this.entityMode = entityMode;
        this.positionMode = positionMode;
    }

    public BlockPos entityBlockTarget(Entity target, int receiverY) {
        if (entityMode == EntityMode.LEGACY_X_MIRRORED) {
            return RadarCommandReceiver.legacyXMirroredEntityTarget(target, receiverY);
        }
        return RadarCommandReceiver.entityTarget(target);
    }

    public Vec3 entityVectorTarget(Entity target, int receiverY) {
        if (entityMode == EntityMode.EXACT) {
            return target.position();
        }
        BlockPos pos = entityBlockTarget(target, receiverY);
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos positionBlockTarget(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    public Vec3 positionVectorTarget(int x, int y, int z) {
        if (positionMode == PositionMode.CENTERED) {
            return RadarCommandReceiver.centeredPositionTarget(x, y, z);
        }
        return new Vec3(x, y, z);
    }

    private enum EntityMode {
        FLOOR_XZ,
        LEGACY_X_MIRRORED,
        EXACT
    }

    private enum PositionMode {
        BLOCK,
        CENTERED
    }
}

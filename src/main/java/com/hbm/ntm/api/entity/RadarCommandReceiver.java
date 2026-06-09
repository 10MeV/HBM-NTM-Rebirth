package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface RadarCommandReceiver {
    boolean sendCommandPosition(int x, int y, int z);

    boolean sendCommandEntity(Entity target);

    default RadarCommandResult sendCommandPositionResult(int x, int y, int z) {
        return RadarCommandResult.fromBoolean(sendCommandPosition(x, y, z), RadarCommandResult.TRIGGERED);
    }

    default RadarCommandResult sendCommandEntityResult(Entity target) {
        return RadarCommandResult.fromBoolean(sendCommandEntity(target), RadarCommandResult.TRIGGERED);
    }

    static BlockPos entityTarget(Entity target) {
        return new BlockPos(floor(target.getX()), floor(target.getY()), floor(target.getZ()));
    }

    static BlockPos entityTargetAtY(Entity target, int y) {
        return new BlockPos(floor(target.getX()), y, floor(target.getZ()));
    }

    static BlockPos legacyXMirroredEntityTarget(Entity target, int y) {
        int x = floor(target.getX());
        return new BlockPos(x, y, x);
    }

    static Vec3 centeredPositionTarget(int x, int y, int z) {
        return new Vec3(x + 0.5D, y, z + 0.5D);
    }

    static int floor(double value) {
        return (int) Math.floor(value);
    }
}

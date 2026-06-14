package com.hbm.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy 1.7.10 package bridge for the mutable Vec3NT helper.
 */
@Deprecated(forRemoval = false)
public class Vec3NT extends com.hbm.ntm.util.Vec3NT {
    public Vec3NT() {
        super();
    }

    public Vec3NT(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec3NT(Vec3 vec) {
        super(vec);
    }

    public Vec3NT(com.hbm.ntm.util.HbmMutableVec3 vec) {
        super(vec);
    }

    public Vec3NT(Entity entity) {
        super(entity);
    }

    @Override
    public Vec3NT eyeHeight(Entity entity) {
        super.eyeHeight(entity);
        return this;
    }

    @Override
    public Vec3NT halfHeight(Entity entity) {
        super.halfHeight(entity);
        return this;
    }

    @Override
    public Vec3NT normalizeSelf() {
        super.normalizeSelf();
        return this;
    }

    @Override
    public Vec3NT add(double x, double y, double z) {
        super.add(x, y, z);
        return this;
    }

    @Override
    public Vec3NT add(Vec3 vec) {
        super.add(vec);
        return this;
    }

    public Vec3NT add(Vec3NT vec) {
        super.add(vec);
        return this;
    }

    @Override
    public Vec3NT add(com.hbm.ntm.util.Vec3NT vec) {
        super.add(vec);
        return this;
    }

    @Override
    public Vec3NT multiply(double multiplier) {
        super.multiply(multiplier);
        return this;
    }

    @Override
    public Vec3NT multiply(double x, double y, double z) {
        super.multiply(x, y, z);
        return this;
    }

    @Override
    public Vec3NT setComponents(double x, double y, double z) {
        super.setComponents(x, y, z);
        return this;
    }

    @Override
    public Vec3NT rotateAroundXRad(double alpha) {
        super.rotateAroundXRad(alpha);
        return this;
    }

    @Override
    public Vec3NT rotateAroundYRad(double alpha) {
        super.rotateAroundYRad(alpha);
        return this;
    }

    @Override
    public Vec3NT rotateAroundZRad(double alpha) {
        super.rotateAroundZRad(alpha);
        return this;
    }

    @Override
    public Vec3NT rotateAroundXDeg(double alpha) {
        super.rotateAroundXDeg(alpha);
        return this;
    }

    @Override
    public Vec3NT rotateAroundYDeg(double alpha) {
        super.rotateAroundYDeg(alpha);
        return this;
    }

    @Override
    public Vec3NT rotateAroundZDeg(double alpha) {
        super.rotateAroundZDeg(alpha);
        return this;
    }
}

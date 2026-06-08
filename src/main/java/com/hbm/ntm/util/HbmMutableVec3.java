package com.hbm.ntm.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class HbmMutableVec3 {
    public double x;
    public double y;
    public double z;

    public HbmMutableVec3() {
        this(0.0D, 0.0D, 0.0D);
    }

    public HbmMutableVec3(double x, double y, double z) {
        set(x, y, z);
    }

    public HbmMutableVec3(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public HbmMutableVec3(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public HbmMutableVec3 eyeHeight(Entity entity) {
        return add(0.0D, entity.getEyeHeight(), 0.0D);
    }

    public HbmMutableVec3 halfHeight(Entity entity) {
        return add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    public HbmMutableVec3 normalizeSelf() {
        double length = Math.sqrt(x * x + y * y + z * z);
        return length < 1.0E-4D ? multiply(0.0D) : multiply(1.0D / length);
    }

    public HbmMutableVec3 add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public HbmMutableVec3 add(Vec3 vec) {
        return add(vec.x, vec.y, vec.z);
    }

    public HbmMutableVec3 add(HbmMutableVec3 vec) {
        return add(vec.x, vec.y, vec.z);
    }

    public HbmMutableVec3 multiply(double multiplier) {
        return multiply(multiplier, multiplier, multiplier);
    }

    public HbmMutableVec3 multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public double distanceTo(double x, double y, double z) {
        double dx = x - this.x;
        double dy = y - this.y;
        double dz = z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public HbmMutableVec3 set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public HbmMutableVec3 rotateAroundXRad(double alpha) {
        double cos = Math.cos(alpha);
        double sin = Math.sin(alpha);
        return set(x, y * cos + z * sin, z * cos - y * sin);
    }

    public HbmMutableVec3 rotateAroundYRad(double alpha) {
        double cos = Math.cos(alpha);
        double sin = Math.sin(alpha);
        return set(x * cos + z * sin, y, z * cos - x * sin);
    }

    public HbmMutableVec3 rotateAroundZRad(double alpha) {
        double cos = Math.cos(alpha);
        double sin = Math.sin(alpha);
        return set(x * cos + y * sin, y * cos - x * sin, z);
    }

    public HbmMutableVec3 rotateAroundXDeg(double alpha) {
        return rotateAroundXRad(alpha / 180.0D * Math.PI);
    }

    public HbmMutableVec3 rotateAroundYDeg(double alpha) {
        return rotateAroundYRad(alpha / 180.0D * Math.PI);
    }

    public HbmMutableVec3 rotateAroundZDeg(double alpha) {
        return rotateAroundZRad(alpha / 180.0D * Math.PI);
    }

    public Vec3 immutable() {
        return new Vec3(x, y, z);
    }

    public static double getMinX(HbmMutableVec3... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (HbmMutableVec3 vec : vecs) {
            if (vec.x < min) {
                min = vec.x;
            }
        }
        return min;
    }

    public static double getMinY(HbmMutableVec3... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (HbmMutableVec3 vec : vecs) {
            if (vec.y < min) {
                min = vec.y;
            }
        }
        return min;
    }

    public static double getMinZ(HbmMutableVec3... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (HbmMutableVec3 vec : vecs) {
            if (vec.z < min) {
                min = vec.z;
            }
        }
        return min;
    }

    public static double getMaxX(HbmMutableVec3... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (HbmMutableVec3 vec : vecs) {
            if (vec.x > max) {
                max = vec.x;
            }
        }
        return max;
    }

    public static double getMaxY(HbmMutableVec3... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (HbmMutableVec3 vec : vecs) {
            if (vec.y > max) {
                max = vec.y;
            }
        }
        return max;
    }

    public static double getMaxZ(HbmMutableVec3... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (HbmMutableVec3 vec : vecs) {
            if (vec.z > max) {
                max = vec.z;
            }
        }
        return max;
    }
}

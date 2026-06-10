package com.hbm.ntm.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Mutable vector helper matching the old Vec3NT call surface.
 */
@Deprecated(forRemoval = false)
public class Vec3NT {
    public double xCoord;
    public double yCoord;
    public double zCoord;

    public Vec3NT() {
        this(0.0D, 0.0D, 0.0D);
    }

    public Vec3NT(double x, double y, double z) {
        setComponents(x, y, z);
    }

    public Vec3NT(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3NT(HbmMutableVec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3NT(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public Vec3NT eyeHeight(Entity entity) {
        return add(0.0D, entity.getEyeHeight(), 0.0D);
    }

    public Vec3NT halfHeight(Entity entity) {
        return add(0.0D, entity.getBbHeight() / 2.0D, 0.0D);
    }

    public Vec3NT normalizeSelf() {
        double length = lengthVector();
        return length < 1.0E-4D ? multiply(0.0D) : multiply(1.0D / length);
    }

    public Vec3NT add(double x, double y, double z) {
        xCoord += x;
        yCoord += y;
        zCoord += z;
        return this;
    }

    public Vec3NT add(Vec3 vec) {
        return add(vec.x, vec.y, vec.z);
    }

    public Vec3NT add(Vec3NT vec) {
        return add(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3NT multiply(double multiplier) {
        return multiply(multiplier, multiplier, multiplier);
    }

    public Vec3NT multiply(double x, double y, double z) {
        xCoord *= x;
        yCoord *= y;
        zCoord *= z;
        return this;
    }

    public double distanceTo(double x, double y, double z) {
        double dX = x - xCoord;
        double dY = y - yCoord;
        double dZ = z - zCoord;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public double lengthVector() {
        return Math.sqrt(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
    }

    public Vec3NT setComponents(double x, double y, double z) {
        xCoord = x;
        yCoord = y;
        zCoord = z;
        return this;
    }

    public Vec3NT rotateAroundXRad(double alpha) {
        double cos = Math.cos(alpha);
        double sin = Math.sin(alpha);
        return setComponents(xCoord, yCoord * cos + zCoord * sin, zCoord * cos - yCoord * sin);
    }

    public Vec3NT rotateAroundYRad(double alpha) {
        double cos = Math.cos(alpha);
        double sin = Math.sin(alpha);
        return setComponents(xCoord * cos + zCoord * sin, yCoord, zCoord * cos - xCoord * sin);
    }

    public Vec3NT rotateAroundZRad(double alpha) {
        double cos = Math.cos(alpha);
        double sin = Math.sin(alpha);
        return setComponents(xCoord * cos + yCoord * sin, yCoord * cos - xCoord * sin, zCoord);
    }

    public Vec3NT rotateAroundXDeg(double alpha) {
        return rotateAroundXRad(alpha / 180.0D * Math.PI);
    }

    public Vec3NT rotateAroundYDeg(double alpha) {
        return rotateAroundYRad(alpha / 180.0D * Math.PI);
    }

    public Vec3NT rotateAroundZDeg(double alpha) {
        return rotateAroundZRad(alpha / 180.0D * Math.PI);
    }

    public Vec3 immutable() {
        return new Vec3(xCoord, yCoord, zCoord);
    }

    public HbmMutableVec3 mutable() {
        return new HbmMutableVec3(xCoord, yCoord, zCoord);
    }

    public static double getMinX(Vec3NT... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (Vec3NT vec : vecs) {
            if (vec.xCoord < min) {
                min = vec.xCoord;
            }
        }
        return min;
    }

    public static double getMinY(Vec3NT... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (Vec3NT vec : vecs) {
            if (vec.yCoord < min) {
                min = vec.yCoord;
            }
        }
        return min;
    }

    public static double getMinZ(Vec3NT... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (Vec3NT vec : vecs) {
            if (vec.zCoord < min) {
                min = vec.zCoord;
            }
        }
        return min;
    }

    public static double getMaxX(Vec3NT... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (Vec3NT vec : vecs) {
            if (vec.xCoord > max) {
                max = vec.xCoord;
            }
        }
        return max;
    }

    public static double getMaxY(Vec3NT... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (Vec3NT vec : vecs) {
            if (vec.yCoord > max) {
                max = vec.yCoord;
            }
        }
        return max;
    }

    public static double getMaxZ(Vec3NT... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (Vec3NT vec : vecs) {
            if (vec.zCoord > max) {
                max = vec.zCoord;
            }
        }
        return max;
    }
}

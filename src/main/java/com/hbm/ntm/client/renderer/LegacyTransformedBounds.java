package com.hbm.ntm.client.renderer;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

final class LegacyTransformedBounds {
    private LegacyTransformedBounds() {
    }

    static AABB transform(AABB bounds, PointTransform transform) {
        Accumulator accumulator = new Accumulator();
        double x0 = bounds.minX;
        double x1 = bounds.maxX;
        double y0 = bounds.minY;
        double y1 = bounds.maxY;
        double z0 = bounds.minZ;
        double z1 = bounds.maxZ;

        for (int corner = 0; corner < 8; corner++) {
            double x = (corner & 1) == 0 ? x0 : x1;
            double y = (corner & 2) == 0 ? y0 : y1;
            double z = (corner & 4) == 0 ? z0 : z1;
            transform.apply(x, y, z, accumulator);
        }

        return accumulator.toAabb();
    }

    static double sinDeg(float degrees) {
        return Math.sin(degrees * Mth.DEG_TO_RAD);
    }

    static double cosDeg(float degrees) {
        return Math.cos(degrees * Mth.DEG_TO_RAD);
    }

    static double rotateYX(double x, double z, double sin, double cos) {
        return x * cos + z * sin;
    }

    static double rotateYZ(double x, double z, double sin, double cos) {
        return z * cos - x * sin;
    }

    static void includeRotatedY(Accumulator accumulator, double x, double y, double z, double sin, double cos) {
        accumulator.include(rotateYX(x, z, sin, cos), y, rotateYZ(x, z, sin, cos));
    }

    @FunctionalInterface
    interface PointTransform {
        void apply(double x, double y, double z, Accumulator accumulator);
    }

    static final class Accumulator {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        void include(double x, double y, double z) {
            this.minX = Math.min(this.minX, x);
            this.minY = Math.min(this.minY, y);
            this.minZ = Math.min(this.minZ, z);
            this.maxX = Math.max(this.maxX, x);
            this.maxY = Math.max(this.maxY, y);
            this.maxZ = Math.max(this.maxZ, z);
        }

        AABB toAabb() {
            return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        }
    }
}

package com.hbm.ntm.client.obj;

import java.util.Random;

public final class LegacySparkRenderer {
    public static void renderSpark(ObjRenderContext context, int seed, double x, double y, double z,
            float length, int min, int max, int colorOuter, int colorInner) {
        Random random = new Random(seed);
        double dirX = random.nextDouble() - 0.5D;
        double dirY = random.nextDouble() - 0.5D;
        double dirZ = random.nextDouble() - 0.5D;
        double dirLength = length(dirX, dirY, dirZ);
        if (dirLength <= 1.0E-6D) {
            dirY = 1.0D;
            dirLength = 1.0D;
        }
        dirX /= dirLength;
        dirY /= dirLength;
        dirZ /= dirLength;

        int segments = min + (max > 0 ? random.nextInt(max) : 0);
        for (int i = 0; i < segments; i++) {
            double prevX = x;
            double prevY = y;
            double prevZ = z;

            x += dirX * length * random.nextFloat();
            y += dirY * length * random.nextFloat();
            z += dirZ * length * random.nextFloat();

            LegacyLineRenderer.line(context, 5.0F, prevX, prevY, prevZ, x, y, z, colorOuter, 255);
            LegacyLineRenderer.line(context, 2.0F, prevX, prevY, prevZ, x, y, z, colorInner, 255);
        }
    }

    private static double length(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    private LegacySparkRenderer() {
    }
}

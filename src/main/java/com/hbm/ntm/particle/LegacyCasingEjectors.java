package com.hbm.ntm.particle;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;

public final class LegacyCasingEjectors {
    public static final int TURRET_CHEKHOV = 0;
    public static final int TURRET_FRIENDLY = 1;
    public static final int TURRET_HOWARD = 2;
    public static final int TURRET_SENTRY = 3;
    public static final int MOTION_X = 0;
    public static final int MOTION_Y = 1;
    public static final int MOTION_Z = 2;
    public static final int OFFSET_X = 3;
    public static final int OFFSET_Y = 4;
    public static final int OFFSET_Z = 5;

    private static final Map<Integer, LegacyCasingEjector> BY_ID = new HashMap<>();
    private static final ThreadLocal<double[]> MOTION_AND_OFFSET = ThreadLocal.withInitial(() -> new double[6]);

    static {
        register(TURRET_CHEKHOV, new LegacyCasingEjector(0.0D, 0.0D, 0.0D, -0.8D, 0.8D, 0.0D, 1, 0.1F, 0.1F));
        register(TURRET_FRIENDLY, new LegacyCasingEjector(0.0D, 0.0D, 0.0D, -0.3D, 0.6D, 0.0D, 1, 0.02F, 0.05F));
        register(TURRET_HOWARD, new LegacyCasingEjector(0.0D, 0.0D, 0.0D, 0.4D, 0.0D, 0.0D, 1, 0.02F, 0.03F));
        register(TURRET_SENTRY, new LegacyCasingEjector(0.0D, 0.0D, 0.0D, 0.2D, 0.2D, 0.0D, 1, 0.01F, 0.01F));
    }

    public static LegacyCasingEjector byId(int id) {
        return BY_ID.get(id);
    }

    public static double[] scratchMotionAndOffset() {
        return MOTION_AND_OFFSET.get();
    }

    private static void register(int id, LegacyCasingEjector ejector) {
        BY_ID.put(id, ejector);
    }

    public record LegacyCasingEjector(double offsetX, double offsetY, double offsetZ,
            double initialMotionX, double initialMotionY, double initialMotionZ,
            int amount, float randomYaw, float randomPitch) {
        public void writeMotionAndOffset(float pitchRadians, float yawRadians, boolean crouched, RandomSource random,
                double[] out) {
            double pitch = pitchRadians + random.nextGaussian() * randomPitch;
            double yaw = yawRadians + random.nextGaussian() * randomPitch;
            double legacySpread = randomPitch;
            double motionX = initialMotionX + random.nextGaussian() * legacySpread;
            double motionY = initialMotionY + random.nextGaussian() * randomPitch;
            double motionZ = initialMotionZ + random.nextGaussian() * legacySpread;
            writeRotatedXThenY(motionX, motionY, motionZ, pitch, yaw, out, MOTION_X);

            double localOffsetX = crouched ? 0.0D : offsetX;
            writeRotatedXThenY(localOffsetX, offsetY, offsetZ, pitchRadians, yawRadians, out, OFFSET_X);
        }
    }

    private static void writeRotatedXThenY(double x, double y, double z, double pitchRadians, double yawRadians,
            double[] out, int index) {
        double pitchCos = Math.cos(pitchRadians);
        double pitchSin = Math.sin(pitchRadians);
        double rotatedY = y * pitchCos - z * pitchSin;
        double rotatedZ = y * pitchSin + z * pitchCos;
        float yaw = (float) -yawRadians;
        float yawCos = Mth.cos(yaw);
        float yawSin = Mth.sin(yaw);
        out[index] = x * yawCos + rotatedZ * yawSin;
        out[index + 1] = rotatedY;
        out[index + 2] = rotatedZ * yawCos - x * yawSin;
    }

    private LegacyCasingEjectors() {
    }
}

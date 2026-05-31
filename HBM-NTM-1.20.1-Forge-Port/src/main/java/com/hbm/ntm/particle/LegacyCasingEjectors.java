package com.hbm.ntm.particle;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class LegacyCasingEjectors {
    public static final int TURRET_CHEKHOV = 0;
    public static final int TURRET_FRIENDLY = 1;
    public static final int TURRET_HOWARD = 2;
    public static final int TURRET_SENTRY = 3;

    private static final Map<Integer, LegacyCasingEjector> BY_ID = new HashMap<>();

    static {
        register(TURRET_CHEKHOV, new LegacyCasingEjector(Vec3.ZERO, new Vec3(-0.8D, 0.8D, 0.0D), 1, 0.1F, 0.1F));
        register(TURRET_FRIENDLY, new LegacyCasingEjector(Vec3.ZERO, new Vec3(-0.3D, 0.6D, 0.0D), 1, 0.02F, 0.05F));
        register(TURRET_HOWARD, new LegacyCasingEjector(Vec3.ZERO, new Vec3(0.4D, 0.0D, 0.0D), 1, 0.02F, 0.03F));
        register(TURRET_SENTRY, new LegacyCasingEjector(Vec3.ZERO, new Vec3(0.2D, 0.2D, 0.0D), 1, 0.01F, 0.01F));
    }

    public static LegacyCasingEjector byId(int id) {
        return BY_ID.get(id);
    }

    private static void register(int id, LegacyCasingEjector ejector) {
        BY_ID.put(id, ejector);
    }

    public record LegacyCasingEjector(Vec3 offset, Vec3 initialMotion, int amount, float randomYaw, float randomPitch) {
        public Vec3 motion(float pitchRadians, float yawRadians, RandomSource random) {
            double pitch = pitchRadians + random.nextGaussian() * randomPitch;
            double yaw = yawRadians + random.nextGaussian() * randomPitch;
            double legacySpread = randomPitch;
            Vec3 vector = new Vec3(
                    initialMotion.x + random.nextGaussian() * legacySpread,
                    initialMotion.y + random.nextGaussian() * randomPitch,
                    initialMotion.z + random.nextGaussian() * legacySpread);
            return rotateX(vector, pitch).yRot((float) -yaw);
        }

        public Vec3 positionOffset(float pitchRadians, float yawRadians, boolean crouched) {
            double offsetX = crouched ? 0.0D : offset.x;
            return rotateX(new Vec3(offsetX, offset.y, offset.z), pitchRadians).yRot(-yawRadians);
        }
    }

    private static Vec3 rotateX(Vec3 vector, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double y = vector.y * cos - vector.z * sin;
        double z = vector.y * sin + vector.z * cos;
        return new Vec3(vector.x, y, z);
    }

    private LegacyCasingEjectors() {
    }
}

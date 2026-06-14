package com.hbm.ntm.api.entity;

public final class RadarHostTickProfile {
    public static final int ENERGY_CONNECTION_REFRESH_INTERVAL_TICKS = 20;
    public static final int SONAR_PING_INTERVAL_TICKS = 80;
    public static final float ROTATION_STEP = 5.0F;
    public static final float FULL_ROTATION = 360.0F;

    private RadarHostTickProfile() {
    }

    public static boolean shouldRefreshEnergyConnections(long gameTime) {
        return gameTime % ENERGY_CONNECTION_REFRESH_INTERVAL_TICKS == 0L;
    }

    public static SonarPing sonarPing(int pingTimer, long power) {
        int nextTimer = pingTimer + 1;
        if (power > 0L && nextTimer >= SONAR_PING_INTERVAL_TICKS) {
            return new SonarPing(0, true);
        }
        return new SonarPing(nextTimer, false);
    }

    public static Rotation advanceRotation(float previousRotation, float rotation, boolean powered) {
        previousRotation = rotation;
        if (powered) {
            rotation += ROTATION_STEP;
            if (rotation >= FULL_ROTATION) {
                rotation -= FULL_ROTATION;
                previousRotation -= FULL_ROTATION;
            }
        }
        return new Rotation(previousRotation, rotation);
    }

    public static ScanPowerPlan scanPowerPlan(int radarY, int minimumAltitude, long power, long consumption) {
        if (radarY < minimumAltitude) {
            return new ScanPowerPlan(false, Math.max(0L, power));
        }
        if (power < consumption) {
            return new ScanPowerPlan(false, 0L);
        }
        return new ScanPowerPlan(true, power - consumption);
    }

    public record SonarPing(int timer, boolean playSound) {
    }

    public record Rotation(float previous, float current) {
    }

    public record ScanPowerPlan(boolean scan, long powerAfter) {
    }
}

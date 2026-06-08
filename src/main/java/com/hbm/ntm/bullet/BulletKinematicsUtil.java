package com.hbm.ntm.bullet;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class BulletKinematicsUtil {
    public static final float ENTITY_SIZE = 0.5F;
    public static final double RENDER_DISTANCE_WEIGHT = 10.0D;
    public static final double SIDE_OFFSET = 0.16D;
    public static final double EYE_Y_OFFSET = 0.1D;
    public static final float DEFAULT_THROW_FORCE = 1.0F;
    public static final double HEADING_FORCE_MULTIPLIER = 1.0D;
    public static final float AIR_DRAG = 1.0F;
    public static final float WATER_DRAG = 1.0F;

    public static Vec3 shooterSpawnPosition(LivingEntity shooter) {
        return shooterSpawnPosition(shooter, true);
    }

    public static Vec3 shooterSpawnPosition(LivingEntity shooter, boolean offsetShot) {
        if (shooter == null) {
            return Vec3.ZERO;
        }

        double x = shooter.getX();
        double y = shooter.getY() + shooter.getEyeHeight();
        double z = shooter.getZ();
        if (offsetShot) {
            float yawRadians = shooter.getYRot() * Mth.DEG_TO_RAD;
            x -= Mth.cos(yawRadians) * SIDE_OFFSET;
            y -= EYE_Y_OFFSET;
            z -= Mth.sin(yawRadians) * SIDE_OFFSET;
        } else {
            y -= EYE_Y_OFFSET;
        }
        return new Vec3(x, y, z);
    }

    public static Vec3 directionFromRotation(float yaw, float pitch) {
        float yawRadians = yaw * Mth.DEG_TO_RAD;
        float pitchRadians = pitch * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yawRadians) * Mth.cos(pitchRadians),
                -Mth.sin(pitchRadians),
                Mth.cos(yawRadians) * Mth.cos(pitchRadians));
    }

    public static Vec3 shootWithSpread(Vec3 direction, float velocity, float inaccuracy, RandomSource random) {
        if (direction == null || direction.lengthSqr() == 0.0D) {
            return Vec3.ZERO;
        }

        RandomSource roll = random == null ? RandomSource.create() : random;
        Vec3 normalized = direction.normalize();
        Vec3 spread = new Vec3(
                normalized.x + roll.nextGaussian() * HEADING_FORCE_MULTIPLIER * inaccuracy,
                normalized.y + roll.nextGaussian() * HEADING_FORCE_MULTIPLIER * inaccuracy,
                normalized.z + roll.nextGaussian() * HEADING_FORCE_MULTIPLIER * inaccuracy);
        return spread.scale(velocity);
    }

    public static Vec3 shootFromRotation(float yaw, float pitch, float velocity, float inaccuracy, RandomSource random) {
        return shootWithSpread(directionFromRotation(yaw, pitch), velocity, inaccuracy, random);
    }

    public static Vec3 movementDelta(BulletConfig config, Vec3 motion) {
        if (config == null || motion == null) {
            return Vec3.ZERO;
        }
        return motion.scale(config.velocity());
    }

    public static Vec3 applyPostMovePhysics(BulletConfig config, Vec3 motion) {
        if (config == null || motion == null) {
            return Vec3.ZERO;
        }
        return new Vec3(motion.x * AIR_DRAG, motion.y * AIR_DRAG - config.gravity(), motion.z * AIR_DRAG);
    }

    public static Vec3 applyPostMoveWaterPhysics(BulletConfig config, Vec3 motion) {
        if (config == null || motion == null) {
            return Vec3.ZERO;
        }
        return new Vec3(motion.x * WATER_DRAG, motion.y * WATER_DRAG - config.gravity(), motion.z * WATER_DRAG);
    }

    public static LifetimeCheck checkLifetime(BulletConfig config, int ticksExisted) {
        if (config == null) {
            return new LifetimeCheck(true, false);
        }
        boolean zeroMaxAge = config.maxAge() == 0;
        boolean agedOut = config.maxAge() > 0 && ticksExisted > config.maxAge();
        return new LifetimeCheck(zeroMaxAge, agedOut);
    }

    public record LifetimeCheck(boolean zeroMaxAge, boolean agedOut) {
        public boolean shouldDiscard() {
            return zeroMaxAge || agedOut;
        }
    }

    private BulletKinematicsUtil() {
    }
}

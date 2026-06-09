package com.hbm.ntm.bullet;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BulletLaunchUtil {
    public static final double AIMED_SHOT_Y_OFFSET = 0.10000000149011612D;
    public static final double AIMED_SHOT_MIN_HORIZONTAL_DISTANCE = 1.0E-7D;

    public static int rollProjectileCount(BulletConfig config, RandomSource random) {
        if (config == null) {
            return 0;
        }
        int lower = Math.min(config.bulletsMin(), config.bulletsMax());
        int upper = Math.max(config.bulletsMin(), config.bulletsMax());
        if (upper <= 0) {
            return 0;
        }
        if (lower < 0) {
            lower = 0;
        }
        if (upper <= lower) {
            return upper;
        }
        RandomSource roll = random == null ? RandomSource.create() : random;
        return lower + roll.nextInt(upper - lower + 1);
    }

    public static LaunchBatch shooterBatch(BulletConfig config, LivingEntity shooter, RandomSource random,
            boolean accuracyBoost) {
        int count = rollProjectileCount(config, random);
        LaunchPlan[] plans = new LaunchPlan[count];
        for (int i = 0; i < count; i++) {
            plans[i] = shooterLaunchPlan(config, shooter, random, accuracyBoost);
        }
        return new LaunchBatch(plans);
    }

    public static LaunchBatch shooterBatchWithLegacyCasing(BulletConfig config, LivingEntity shooter,
            RandomSource random, boolean accuracyBoost, int ejectorId) {
        LaunchBatch batch = shooterBatch(config, shooter, random, accuracyBoost);
        if (config == null || shooter == null || config.spentCasingName().isBlank()) {
            return batch;
        }
        return batch.withCasingRequest(BulletCasingEjectUtil.legacyEjectorFromShooter(shooter, ejectorId,
                config.spentCasingName()));
    }

    public static LaunchPlan shooterLaunchPlan(BulletConfig config, LivingEntity shooter, RandomSource random,
            boolean accuracyBoost) {
        if (config == null || shooter == null) {
            return LaunchPlan.INVALID;
        }
        Vec3 position = BulletKinematicsUtil.shooterSpawnPosition(shooter, true);
        Vec3 direction = BulletKinematicsUtil.directionFromRotation(shooter.getYRot(), shooter.getXRot());
        float spread = config.spread() * (accuracyBoost ? 0.25F : 1.0F);
        Vec3 motion = BulletKinematicsUtil.shootWithSpread(direction, BulletKinematicsUtil.DEFAULT_THROW_FORCE,
                spread, random);
        return launchPlan(config, position, motion, shooter.getYRot(), shooter.getXRot(), true);
    }

    public static LaunchPlan aimedLaunchPlan(BulletConfig config, LivingEntity shooter, LivingEntity target,
            float throwForce, float deviation, RandomSource random) {
        if (config == null || shooter == null || target == null) {
            return LaunchPlan.INVALID;
        }

        double y = shooter.getY() + shooter.getEyeHeight() - AIMED_SHOT_Y_OFFSET;
        double dx = target.getX() - shooter.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 3.0D - y;
        double dz = target.getZ() - shooter.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < AIMED_SHOT_MIN_HORIZONTAL_DISTANCE) {
            return LaunchPlan.INVALID;
        }

        double x = shooter.getX() + dx / horizontal;
        double z = shooter.getZ() + dz / horizontal;
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0D / Math.PI));
        Vec3 motion = BulletKinematicsUtil.shootWithSpread(new Vec3(dx, dy, dz), throwForce, deviation, random);
        return launchPlan(config, new Vec3(x, y, z), motion, yaw, pitch, true);
    }

    public static LaunchPlan directedLaunchPlan(BulletConfig config, Vec3 position, Vec3 heading, float throwForce,
            float deviation, RandomSource random) {
        if (config == null || position == null || heading == null || heading.lengthSqr() <= 1.0E-7D) {
            return LaunchPlan.INVALID;
        }
        Vec3 motion = BulletKinematicsUtil.shootWithSpread(heading, throwForce, deviation, random);
        Rotation rotation = rotationFromMotion(motion);
        return launchPlan(config, position, motion, rotation.yaw(), rotation.pitch(), true);
    }

    public static LaunchPlan withMotion(LaunchPlan plan, Vec3 motion) {
        if (plan == null || !plan.valid() || motion == null) {
            return LaunchPlan.INVALID;
        }
        Rotation rotation = rotationFromMotion(motion);
        return launchPlan(plan.config(), plan.position(), motion, rotation.yaw(), rotation.pitch(), true);
    }

    public static LaunchPlan offsetMotion(LaunchPlan plan, Vec3 delta) {
        if (plan == null || !plan.valid() || delta == null) {
            return LaunchPlan.INVALID;
        }
        return withMotion(plan, plan.motion().add(delta));
    }

    private static LaunchPlan launchPlan(BulletConfig config, Vec3 position, Vec3 motion, float yaw, float pitch,
            boolean valid) {
        return new LaunchPlan(config, BulletConfigSyncRegistry.syncedState(config), position, motion, yaw, pitch,
                BulletKinematicsUtil.ENTITY_SIZE, BulletKinematicsUtil.RENDER_DISTANCE_WEIGHT, valid);
    }

    private static Rotation rotationFromMotion(Vec3 motion) {
        if (motion == null || motion.lengthSqr() <= 1.0E-7D) {
            return new Rotation(0.0F, 0.0F);
        }
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yaw = (float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI);
        float pitch = (float) (Math.atan2(motion.y, horizontal) * 180.0D / Math.PI);
        return new Rotation(Mth.wrapDegrees(yaw), Mth.wrapDegrees(pitch));
    }

    public record LaunchBatch(LaunchPlan[] plans, BulletCasingEjectUtil.CasingRequest casingRequest) {
        public LaunchBatch(LaunchPlan[] plans) {
            this(plans, BulletCasingEjectUtil.CasingRequest.NONE);
        }

        public LaunchBatch {
            plans = plans == null ? new LaunchPlan[0] : plans;
            casingRequest = casingRequest == null ? BulletCasingEjectUtil.CasingRequest.NONE : casingRequest;
        }

        public int count() {
            return plans.length;
        }

        public boolean hasCasingRequest() {
            return casingRequest.valid();
        }

        public LaunchBatch withCasingRequest(BulletCasingEjectUtil.CasingRequest request) {
            return new LaunchBatch(plans, request);
        }

        public boolean executeCasing(Level level) {
            return BulletCasingEjectUtil.execute(level, casingRequest);
        }
    }

    public record LaunchPlan(@Nullable BulletConfig config, BulletSyncedState syncedState, Vec3 position, Vec3 motion,
            float yaw, float pitch, float entitySize, double renderDistanceWeight, boolean valid) {
        public static final LaunchPlan INVALID = new LaunchPlan(null, BulletSyncedState.EMPTY, Vec3.ZERO, Vec3.ZERO,
                0.0F, 0.0F, BulletKinematicsUtil.ENTITY_SIZE, BulletKinematicsUtil.RENDER_DISTANCE_WEIGHT, false);
    }

    public record Rotation(float yaw, float pitch) {
    }

    private BulletLaunchUtil() {
    }
}

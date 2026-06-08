package com.hbm.ntm.bullet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BulletHomingUtil {
    public static final double CHLOROPHYTE_RANGE = 30.0D;
    public static final double CHLOROPHYTE_ANGLE = 180.0D;
    public static final double UFO_RANGE = 100.0D;
    public static final double UFO_ANGLE = 90.0D;
    public static final double UFO_BLAST_DISTANCE_SQR = 5.0D;

    public static Optional<LivingEntity> findChlorophyteTarget(Entity projectile, @Nullable Entity shooter, Vec3 motion) {
        return findTarget(projectile, shooter, motion, CHLOROPHYTE_RANGE, CHLOROPHYTE_ANGLE);
    }

    public static Optional<LivingEntity> findUfoTarget(Entity projectile, @Nullable Entity shooter, Vec3 motion) {
        return findTarget(projectile, shooter, motion, UFO_RANGE, UFO_ANGLE);
    }

    public static Optional<LivingEntity> findTarget(Entity projectile, @Nullable Entity shooter, Vec3 motion,
            double range, double maxAngle) {
        if (projectile == null) {
            return Optional.empty();
        }
        return findTarget(projectile.level(), projectile.position(), projectile.getBoundingBox(), motion, shooter,
                projectile, range, maxAngle);
    }

    public static Optional<LivingEntity> findTarget(Level level, Vec3 position, AABB projectileBounds, Vec3 motion,
            @Nullable Entity shooter, @Nullable Entity rayOwner, double range, double maxAngle) {
        if (level == null || position == null || projectileBounds == null || motion == null
                || range <= 0.0D || maxAngle <= 0.0D || motion.lengthSqr() <= 1.0E-7D) {
            return Optional.empty();
        }

        AABB search = projectileBounds.inflate(range, range, range);
        double rangeSquared = range * range;
        LivingEntity target = null;
        double targetAngle = maxAngle;

        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, search)) {
            if (!candidate.isAlive() || candidate == shooter) {
                continue;
            }
            Vec3 targetCenter = center(candidate);
            Vec3 delta = targetCenter.subtract(position);
            if (delta.lengthSqr() >= rangeSquared || isObstructed(level, position, targetCenter, rayOwner)) {
                continue;
            }
            double angle = crossAngleDegrees(motion, delta);
            if (angle < targetAngle) {
                target = candidate;
                targetAngle = angle;
            }
        }

        return Optional.ofNullable(target);
    }

    public static Vec3 steerTowards(Entity target, Vec3 position, Vec3 currentMotion) {
        if (target == null) {
            return currentMotion;
        }
        return steerTowards(center(target), position, currentMotion);
    }

    public static Vec3 steerTowards(Vec3 targetCenter, Vec3 position, Vec3 currentMotion) {
        if (targetCenter == null || position == null || currentMotion == null) {
            return currentMotion;
        }
        double speed = currentMotion.length();
        Vec3 delta = targetCenter.subtract(position);
        if (speed <= 1.0E-7D || delta.lengthSqr() <= 1.0E-7D) {
            return currentMotion;
        }
        return delta.normalize().scale(speed);
    }

    public static boolean shouldTriggerUfoBlast(Entity projectile, Entity target) {
        return projectile != null && target != null && projectile.distanceToSqr(target) < UFO_BLAST_DISTANCE_SQR;
    }

    public static Vec3 center(Entity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ());
    }

    private static boolean isObstructed(Level level, Vec3 start, Vec3 end, @Nullable Entity rayOwner) {
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rayOwner);
        return level.clip(context).getType() != HitResult.Type.MISS;
    }

    private static double crossAngleDegrees(Vec3 first, Vec3 second) {
        double firstLength = first.length();
        double secondLength = second.length();
        if (firstLength <= 1.0E-7D || secondLength <= 1.0E-7D) {
            return 180.0D;
        }
        double cosine = first.dot(second) / (firstLength * secondLength);
        double angle = Math.acos(Math.max(-1.0D, Math.min(1.0D, cosine))) * 180.0D / Math.PI;
        return angle >= 180.0D ? angle - 180.0D : angle;
    }

    private BulletHomingUtil() {
    }
}

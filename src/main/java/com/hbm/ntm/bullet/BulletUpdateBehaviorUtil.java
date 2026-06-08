package com.hbm.ntm.bullet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BulletUpdateBehaviorUtil {
    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable LivingEntity currentHomingTarget) {
        if (config == null || projectile == null || motion == null || projectile.level().isClientSide()) {
            return new KnownUpdateResult(motion, currentHomingTarget, false, false);
        }

        if (config.hasBehavior(BulletBehaviorTag.UFO_HOMING)) {
            HomingResult homing = updateHoming(projectile, shooter, motion, currentHomingTarget,
                    BulletHomingUtil.UFO_RANGE, BulletHomingUtil.UFO_ANGLE);
            boolean blast = false;
            if (config.hasBehavior(BulletBehaviorTag.UFO_BLAST) && homing.target() != null
                    && BulletHomingUtil.shouldTriggerUfoBlast(projectile, homing.target())) {
                BulletImpactUtil.applyUfoBlast(projectile.level(), projectile.position());
                blast = true;
            }
            return new KnownUpdateResult(homing.motion(), homing.target(), homing.acquiredTarget(), blast);
        }

        if (config.hasBehavior(BulletBehaviorTag.CHLOROPHYTE_HOMING)) {
            HomingResult homing = updateHoming(projectile, shooter, motion, currentHomingTarget,
                    BulletHomingUtil.CHLOROPHYTE_RANGE, BulletHomingUtil.CHLOROPHYTE_ANGLE);
            return new KnownUpdateResult(homing.motion(), homing.target(), homing.acquiredTarget(), false);
        }

        return new KnownUpdateResult(motion, currentHomingTarget, false, false);
    }

    public static HomingResult updateHoming(Entity projectile, @Nullable Entity shooter, Vec3 motion,
            @Nullable LivingEntity currentTarget, double range, double angle) {
        if (projectile == null || motion == null) {
            return new HomingResult(motion, currentTarget, false);
        }

        LivingEntity target = currentTarget != null && currentTarget.isAlive() ? currentTarget : null;
        boolean acquired = false;
        if (target == null) {
            Optional<LivingEntity> found = BulletHomingUtil.findTarget(projectile, shooter, motion, range, angle);
            target = found.orElse(null);
            acquired = target != null;
        }

        Vec3 steered = target == null ? motion : BulletHomingUtil.steerTowards(target, projectile.position(), motion);
        return new HomingResult(steered, target, acquired);
    }

    public record KnownUpdateResult(Vec3 motion, @Nullable LivingEntity homingTarget, boolean acquiredHomingTarget,
            boolean triggeredUfoBlast) {
        public static final KnownUpdateResult NONE = new KnownUpdateResult(Vec3.ZERO, null, false, false);

        public boolean discardProjectile() {
            return triggeredUfoBlast;
        }
    }

    public record HomingResult(Vec3 motion, @Nullable LivingEntity target, boolean acquiredTarget) {
    }

    private BulletUpdateBehaviorUtil() {
    }
}

package com.hbm.ntm.bullet;

import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.RayTraceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BulletUpdateBehaviorUtil {
    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable LivingEntity currentHomingTarget) {
        return applyKnownPreMoveUpdate(config, projectile, shooter, motion, currentHomingTarget, null, 0.0F);
    }

    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable LivingEntity currentHomingTarget,
            @Nullable Vec3 previousPosition) {
        return applyKnownPreMoveUpdate(config, projectile, shooter, motion, currentHomingTarget, previousPosition,
                0.0F);
    }

    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable LivingEntity currentHomingTarget,
            @Nullable Vec3 previousPosition, float currentAcceleration) {
        if (config == null || projectile == null || motion == null) {
            return new KnownUpdateResult(motion, currentHomingTarget, false, false, 0, currentAcceleration, false);
        }

        float acceleration = applyRocketAcceleration(config, shooter, currentAcceleration);
        Vec3 updatedMotion = applyRocketSteering(config, projectile, shooter, motion);
        int brokenInPath = applyCoilBreakInPath(config, projectile.level(),
                previousPosition == null ? projectile.position().subtract(
                        BulletKinematicsUtil.movementDelta(config, motion, currentAcceleration))
                        : previousPosition,
                projectile.position());
        if (projectile.level().isClientSide()) {
            return new KnownUpdateResult(updatedMotion, currentHomingTarget, false, false, brokenInPath,
                    acceleration, acceleration != currentAcceleration);
        }
        if (applyFireExtinguisherWaterUpdate(config, projectile)) {
            return new KnownUpdateResult(updatedMotion, currentHomingTarget, false, false, brokenInPath,
                    acceleration, acceleration != currentAcceleration, true);
        }
        if (config.hasBehavior(BulletBehaviorTag.UFO_HOMING)) {
            HomingResult homing = updateHoming(projectile, shooter, updatedMotion, currentHomingTarget,
                    BulletHomingUtil.UFO_RANGE, BulletHomingUtil.UFO_ANGLE);
            boolean blast = false;
            if (config.hasBehavior(BulletBehaviorTag.UFO_BLAST) && homing.target() != null
                    && BulletHomingUtil.shouldTriggerUfoBlast(projectile, homing.target())) {
                BulletImpactUtil.applyUfoBlast(projectile.level(), projectile.position());
                blast = true;
            }
            return new KnownUpdateResult(homing.motion(), homing.target(), homing.acquiredTarget(), blast,
                    brokenInPath, acceleration, acceleration != currentAcceleration);
        }

        if (config.hasBehavior(BulletBehaviorTag.CHLOROPHYTE_HOMING)) {
            HomingResult homing = updateHoming(projectile, shooter, updatedMotion, currentHomingTarget,
                    BulletHomingUtil.CHLOROPHYTE_RANGE, BulletHomingUtil.CHLOROPHYTE_ANGLE);
            return new KnownUpdateResult(homing.motion(), homing.target(), homing.acquiredTarget(), false,
                    brokenInPath, acceleration, acceleration != currentAcceleration);
        }

        return new KnownUpdateResult(updatedMotion, currentHomingTarget, false, false, brokenInPath,
                acceleration, acceleration != currentAcceleration);
    }

    private static boolean applyFireExtinguisherWaterUpdate(BulletConfig config, Entity projectile) {
        if (!config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_WATER)
                || projectile == null || projectile.level().isClientSide()) {
            return false;
        }
        BlockPos pos = BlockPos.containing(projectile.position());
        if (!projectile.level().getBlockState(pos).is(ModBlocks.VOLCANIC_LAVA_BLOCK.get())) {
            return false;
        }
        return projectile.level().setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
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

    private static int applyCoilBreakInPath(BulletConfig config, Level level, Vec3 previousPosition,
            Vec3 currentPosition) {
        float threshold = coilBreakThreshold(config);
        if (threshold <= 0.0F || level == null || previousPosition == null || currentPosition == null) {
            return 0;
        }
        Vec3 delta = currentPosition.subtract(previousPosition);
        double motion = Math.max(delta.length(), 0.1D);
        Vec3 direction = delta.lengthSqr() < 1.0E-7D ? Vec3.ZERO : delta.normalize();
        int changed = 0;
        for (double distance = 0.0D; distance < motion; distance += 0.5D) {
            Vec3 point = currentPosition.subtract(direction.scale(distance));
            if (level.isClientSide()) {
                ParticleUtil.spawnVanillaExt(level, point.x, point.y, point.z, ParticleUtil.VANILLA_FIREWORKS,
                        0.0D, 0.0D, 0.0D);
                changed++;
                continue;
            }
            BlockPos pos = new BlockPos((int) Math.floor(point.x), (int) Math.floor(point.y),
                    (int) Math.floor(point.z));
            BlockState state = level.getBlockState(pos);
            float hardness = state.getDestroySpeed(level, pos);
            if (!state.isAir() && hardness >= 0.0F && hardness < threshold && level.destroyBlock(pos, false)) {
                changed++;
            }
        }
        return changed;
    }

    private static float coilBreakThreshold(BulletConfig config) {
        if (config.hasBehavior(BulletBehaviorTag.COIL_BREAK_WEAK_BLOCKS)) {
            return 1.25F;
        }
        if (config.hasBehavior(BulletBehaviorTag.COIL_BREAK_STRONGER_BLOCKS)) {
            return 2.5F;
        }
        return 0.0F;
    }

    private static float applyRocketAcceleration(BulletConfig config, @Nullable Entity shooter,
            float currentAcceleration) {
        if (config.hasBehavior(BulletBehaviorTag.ROCKET_ACCELERATE)) {
            return currentAcceleration < 7.0F ? currentAcceleration + 0.4F : currentAcceleration;
        }
        if (config.hasBehavior(BulletBehaviorTag.ROCKET_STEER)) {
            float limit = isQdSteeringRocket(config) && !(shooter instanceof Player) ? 7.0F : 4.0F;
            return currentAcceleration < limit ? currentAcceleration + 0.4F : currentAcceleration;
        }
        return currentAcceleration;
    }

    private static Vec3 applyRocketSteering(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            Vec3 motion) {
        if (!config.hasBehavior(BulletBehaviorTag.ROCKET_STEER)
                || !(shooter instanceof Player player)
                || !canSteerRocket(config, player)
                || projectile.position().distanceTo(player.position()) > 100.0D) {
            return motion;
        }

        HitResult hit = RayTraceUtil.getMouseOver(player, 200.0D, 0.0D, 1.0F);
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return motion;
        }
        Vec3 target = hit.getLocation().subtract(projectile.position());
        if (target.length() < 3.0D || motion.lengthSqr() <= 1.0E-7D) {
            return motion;
        }
        return target.normalize().scale(motion.length());
    }

    private static boolean isAlwaysSteeringRocket(BulletConfig config) {
        return config.legacyName().startsWith("rocket_ncrpa_steer");
    }

    private static boolean isQdSteeringRocket(BulletConfig config) {
        return config.legacyName().startsWith("rocket_qd_");
    }

    private static boolean canSteerRocket(BulletConfig config, Player player) {
        if (isAlwaysSteeringRocket(config)) {
            return true;
        }
        return isQdSteeringRocket(config) && player.isShiftKeyDown() && isHoldingSednaGun(player);
    }

    private static boolean isHoldingSednaGun(Player player) {
        return player.getMainHandItem().getItem() instanceof SednaGunItem
                || player.getOffhandItem().getItem() instanceof SednaGunItem;
    }

    public record KnownUpdateResult(Vec3 motion, @Nullable LivingEntity homingTarget, boolean acquiredHomingTarget,
            boolean triggeredUfoBlast, int coilBlocksBroken, float acceleration, boolean accelerated,
            boolean discardProjectile) {
        public static final KnownUpdateResult NONE = new KnownUpdateResult(Vec3.ZERO, null, false, false, 0,
                0.0F, false);

        public KnownUpdateResult(Vec3 motion, @Nullable LivingEntity homingTarget, boolean acquiredHomingTarget,
                boolean triggeredUfoBlast, int coilBlocksBroken, float acceleration, boolean accelerated) {
            this(motion, homingTarget, acquiredHomingTarget, triggeredUfoBlast, coilBlocksBroken, acceleration,
                    accelerated, triggeredUfoBlast);
        }
    }

    public record HomingResult(Vec3 motion, @Nullable LivingEntity target, boolean acquiredTarget) {
    }

    private BulletUpdateBehaviorUtil() {
    }
}

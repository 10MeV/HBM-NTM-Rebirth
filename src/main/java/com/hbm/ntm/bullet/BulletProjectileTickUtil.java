package com.hbm.ntm.bullet;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulletProjectileTickUtil {
    public static TickResult applyEntityTick(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            @Nullable LivingEntity currentHomingTarget, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, @Nullable RandomSource random, float overrideDamage, boolean inGround,
            float acceleration) {
        if (projectile == null) {
            return TickResult.NONE;
        }
        return applyTick(config, projectile.level(), projectile, shooter, currentHomingTarget,
                projectile.getBoundingBox(), projectile.position(), projectile.getDeltaMovement(), ticksExisted,
                ticksInAir, hasTauTrailNodes, previousPosition, projectile.isInWater(), random, overrideDamage,
                inGround, acceleration);
    }

    public static TickResult applyEntityTick(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            @Nullable LivingEntity currentHomingTarget, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, @Nullable RandomSource random, float overrideDamage, boolean inGround) {
        return applyEntityTick(config, projectile, shooter, currentHomingTarget, ticksExisted, ticksInAir,
                hasTauTrailNodes, previousPosition, random, overrideDamage, inGround, 0.0F);
    }

    public static TickResult applyTick(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, @Nullable LivingEntity currentHomingTarget, AABB projectileBounds,
            Vec3 position, Vec3 motion, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, boolean inWater, @Nullable RandomSource random, float overrideDamage,
            boolean inGround) {
        return applyTick(config, level, projectile, shooter, currentHomingTarget, projectileBounds, position, motion,
                ticksExisted, ticksInAir, hasTauTrailNodes, previousPosition, inWater, random, overrideDamage,
                inGround, 0.0F);
    }

    public static TickResult applyTick(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, @Nullable LivingEntity currentHomingTarget, AABB projectileBounds,
            Vec3 position, Vec3 motion, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, boolean inWater, @Nullable RandomSource random, float overrideDamage,
            boolean inGround, float acceleration) {
        if (config == null || level == null || position == null || motion == null) {
            return TickResult.NONE;
        }

        RandomSource roll = random == null ? level.random : random;
        BulletTauTrailUtil.TauTrailAppend tauTrail =
                BulletTauTrailUtil.appendClientNode(config, level.isClientSide(), hasTauTrailNodes, motion);
        int preMoveParticles = BulletFlightVisualUtil.spawnBlackPowderBurst(config, level, position, motion,
                ticksExisted, roll);
        int meteorFlameParticles = BulletFlightVisualUtil.spawnMeteorFlameParticles(config, level, position, roll);
        int flameTrailParticles = BulletFlightVisualUtil.spawnFlamethrowerTrail(config, level, position);
        int fireExtinguisherParticles = BulletFlightVisualUtil.spawnFireExtinguisherTrail(config, level, position,
                motion, roll);
        List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests = new ArrayList<>(
                BulletSpecialSpawnUtil.collectPreMoveSpawnRequests(config, projectile, shooter, ticksExisted, roll));

        BulletUpdateBehaviorUtil.KnownUpdateResult update =
                BulletUpdateBehaviorUtil.applyKnownPreMoveUpdate(config, projectile, shooter, motion,
                        currentHomingTarget, previousPosition, acceleration, overrideDamage);
        Vec3 updatedMotion = update.motion();
        float updatedAcceleration = update.acceleration();
        BulletKinematicsUtil.LifetimeCheck lifetime = BulletKinematicsUtil.checkLifetime(config, ticksExisted);
        boolean discardProjectile = lifetime.shouldDiscard() || update.discardProjectile();
        if (lifetime.zeroMaxAge() || update.discardProjectile()) {
            return new TickResult(position, updatedMotion, update.homingTarget(),
                    BulletProjectileHitUtil.HitApplication.NONE, update, tauTrail, preMoveParticles,
                    flameTrailParticles + fireExtinguisherParticles, lifetime, true, false, meteorFlameParticles,
                    Collections.unmodifiableList(spawnRequests),
                    updatedAcceleration);
        }

        BulletCollisionUtil.CollisionScan scan = projectile == null
                ? BulletCollisionUtil.scan(config, level, null, shooter, projectileBounds, position, updatedMotion,
                        ticksInAir, updatedAcceleration)
                : BulletCollisionUtil.scan(config, level, projectile, shooter, projectileBounds, position,
                        updatedMotion, ticksInAir, updatedAcceleration);
        boolean ni4niCoinInterrupt = config.hasBehavior(BulletBehaviorTag.NI4NI_COIN_RICOCHET)
                && Ni4NiCoinRicochetUtil.interceptsBeforePrimary(level, projectile, scan);
        BulletProjectileHitUtil.HitApplication hit = projectile == null
                ? BulletProjectileHitUtil.HitApplication.NONE
                : ni4niCoinInterrupt
                ? new BulletProjectileHitUtil.HitApplication(scan, Collections.emptyList(),
                        BulletRicochetUtil.BlockHitResult.NONE, updatedMotion, false, false,
                        Collections.emptyList(), 0.0F, false)
                : BulletProjectileHitUtil.applyScannedHits(config, level, projectile, shooter, updatedMotion, scan,
                        roll, overrideDamage, inGround, ticksInAir);
        discardProjectile |= hit.discardProjectile();
        spawnRequests.addAll(hit.spawnRequests());

        Vec3 postHitMotion = hit == BulletProjectileHitUtil.HitApplication.NONE ? updatedMotion : hit.resultingMotion();
        Vec3 moveBase = moveBase(position, scan, hit);
        Vec3 nextPosition = moveBase.add(BulletKinematicsUtil.movementDelta(config, postHitMotion,
                updatedAcceleration));
        Vec3 nextMotion = inWater
                ? BulletKinematicsUtil.applyPostMoveWaterPhysics(config, postHitMotion)
                : BulletKinematicsUtil.applyPostMovePhysics(config, postHitMotion);
        int trailParticles = flameTrailParticles + fireExtinguisherParticles
                + BulletFlightVisualUtil.spawnVanillaTrail(config, level,
                previousPosition == null ? position : previousPosition, nextPosition);

        return new TickResult(nextPosition, nextMotion, update.homingTarget(), hit, update, tauTrail,
                preMoveParticles, trailParticles, lifetime, discardProjectile, hit.enteredPortal(),
                meteorFlameParticles, Collections.unmodifiableList(spawnRequests), updatedAcceleration);
    }

    private static Vec3 moveBase(Vec3 position, BulletCollisionUtil.CollisionScan scan,
            BulletProjectileHitUtil.HitApplication hit) {
        if (hit.blockHit().ricocheted() && scan.blockHit() != null) {
            return scan.blockHit().location();
        }
        return position;
    }

    public record TickResult(Vec3 nextPosition, Vec3 nextMotion, @Nullable LivingEntity homingTarget,
            BulletProjectileHitUtil.HitApplication hit, BulletUpdateBehaviorUtil.KnownUpdateResult update,
            BulletTauTrailUtil.TauTrailAppend tauTrail, int preMoveParticles, int trailParticles,
            BulletKinematicsUtil.LifetimeCheck lifetime, boolean discardProjectile, boolean enteredPortal,
            int meteorFlameParticles, List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests,
            float acceleration) {
        public static final TickResult NONE = new TickResult(Vec3.ZERO, Vec3.ZERO, null,
                BulletProjectileHitUtil.HitApplication.NONE,
                BulletUpdateBehaviorUtil.KnownUpdateResult.NONE,
                BulletTauTrailUtil.TauTrailAppend.NONE, 0, 0,
                new BulletKinematicsUtil.LifetimeCheck(true, false), true, false, 0, Collections.emptyList(),
                0.0F);
    }

    private BulletProjectileTickUtil() {
    }
}

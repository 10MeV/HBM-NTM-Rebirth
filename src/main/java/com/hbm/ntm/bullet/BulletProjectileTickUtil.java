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
            @Nullable Vec3 previousPosition, @Nullable RandomSource random, float overrideDamage, boolean inGround) {
        if (projectile == null) {
            return TickResult.NONE;
        }
        return applyTick(config, projectile.level(), projectile, shooter, currentHomingTarget,
                projectile.getBoundingBox(), projectile.position(), projectile.getDeltaMovement(), ticksExisted,
                ticksInAir, hasTauTrailNodes, previousPosition, projectile.isInWater(), random, overrideDamage,
                inGround);
    }

    public static TickResult applyTick(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, @Nullable LivingEntity currentHomingTarget, AABB projectileBounds,
            Vec3 position, Vec3 motion, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, boolean inWater, @Nullable RandomSource random, float overrideDamage,
            boolean inGround) {
        if (config == null || level == null || position == null || motion == null) {
            return TickResult.NONE;
        }

        RandomSource roll = random == null ? level.random : random;
        BulletTauTrailUtil.TauTrailAppend tauTrail =
                BulletTauTrailUtil.appendClientNode(config, level.isClientSide(), hasTauTrailNodes, motion);
        int preMoveParticles = BulletFlightVisualUtil.spawnBlackPowderBurst(config, level, position, motion,
                ticksExisted, roll);
        int meteorFlameParticles = BulletFlightVisualUtil.spawnMeteorFlameParticles(config, level, position, roll);
        List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests = new ArrayList<>(
                BulletSpecialSpawnUtil.collectPreMoveSpawnRequests(config, projectile, shooter, ticksExisted, roll));

        BulletUpdateBehaviorUtil.KnownUpdateResult update =
                BulletUpdateBehaviorUtil.applyKnownPreMoveUpdate(config, projectile, shooter, motion,
                        currentHomingTarget);
        Vec3 updatedMotion = update.motion();
        BulletKinematicsUtil.LifetimeCheck lifetime = BulletKinematicsUtil.checkLifetime(config, ticksExisted);
        boolean discardProjectile = lifetime.shouldDiscard() || update.discardProjectile();
        if (lifetime.zeroMaxAge() || update.discardProjectile()) {
            return new TickResult(position, updatedMotion, update.homingTarget(),
                    BulletProjectileHitUtil.HitApplication.NONE, update, tauTrail, preMoveParticles, 0, lifetime,
                    true, false, meteorFlameParticles, Collections.unmodifiableList(spawnRequests));
        }

        BulletCollisionUtil.CollisionScan scan = projectile == null
                ? BulletCollisionUtil.scan(config, level, null, shooter, projectileBounds, position, updatedMotion,
                        ticksInAir)
                : BulletCollisionUtil.scan(config, level, projectile, shooter, projectileBounds, position,
                        updatedMotion, ticksInAir);
        BulletProjectileHitUtil.HitApplication hit = projectile == null
                ? BulletProjectileHitUtil.HitApplication.NONE
                : BulletProjectileHitUtil.applyScannedHits(config, level, projectile, shooter, updatedMotion, scan,
                        roll, overrideDamage, inGround);
        discardProjectile |= hit.discardProjectile();
        spawnRequests.addAll(hit.spawnRequests());

        Vec3 postHitMotion = hit == BulletProjectileHitUtil.HitApplication.NONE ? updatedMotion : hit.resultingMotion();
        Vec3 moveBase = moveBase(position, scan, hit);
        Vec3 nextPosition = moveBase.add(BulletKinematicsUtil.movementDelta(config, postHitMotion));
        Vec3 nextMotion = inWater
                ? BulletKinematicsUtil.applyPostMoveWaterPhysics(config, postHitMotion)
                : BulletKinematicsUtil.applyPostMovePhysics(config, postHitMotion);
        int trailParticles = BulletFlightVisualUtil.spawnVanillaTrail(config, level,
                previousPosition == null ? position : previousPosition, nextPosition);

        return new TickResult(nextPosition, nextMotion, update.homingTarget(), hit, update, tauTrail,
                preMoveParticles, trailParticles, lifetime, discardProjectile, hit.enteredPortal(),
                meteorFlameParticles, Collections.unmodifiableList(spawnRequests));
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
            int meteorFlameParticles, List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests) {
        public static final TickResult NONE = new TickResult(Vec3.ZERO, Vec3.ZERO, null,
                BulletProjectileHitUtil.HitApplication.NONE,
                BulletUpdateBehaviorUtil.KnownUpdateResult.NONE,
                BulletTauTrailUtil.TauTrailAppend.NONE, 0, 0,
                new BulletKinematicsUtil.LifetimeCheck(true, false), true, false, 0, Collections.emptyList());
    }

    private BulletProjectileTickUtil() {
    }
}

package com.hbm.ntm.bullet;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulletProjectileTickUtil {
    public static final double LEGACY_BEAM_RANGE = 250.0D;

    public static TickResult applyEntityTick(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            @Nullable Entity currentHomingTarget, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, @Nullable RandomSource random, float overrideDamage, boolean inGround,
            float acceleration, double currentBeamLength) {
        if (projectile == null) {
            return TickResult.NONE;
        }
        return applyTick(config, projectile.level(), projectile, shooter, currentHomingTarget,
                projectile.getBoundingBox(), projectile.position(), projectile.getDeltaMovement(), ticksExisted,
                ticksInAir, hasTauTrailNodes, previousPosition, projectile.isInWater(), random, overrideDamage,
                inGround, acceleration, currentBeamLength);
    }

    public static TickResult applyEntityTick(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            @Nullable Entity currentHomingTarget, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, @Nullable RandomSource random, float overrideDamage, boolean inGround,
            float acceleration) {
        return applyEntityTick(config, projectile, shooter, currentHomingTarget, ticksExisted, ticksInAir,
                hasTauTrailNodes, previousPosition, random, overrideDamage, inGround, acceleration, 0.0D);
    }

    public static TickResult applyEntityTick(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            @Nullable Entity currentHomingTarget, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, @Nullable RandomSource random, float overrideDamage, boolean inGround) {
        return applyEntityTick(config, projectile, shooter, currentHomingTarget, ticksExisted, ticksInAir,
                hasTauTrailNodes, previousPosition, random, overrideDamage, inGround, 0.0F);
    }

    public static TickResult applyTick(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, @Nullable Entity currentHomingTarget, AABB projectileBounds,
            Vec3 position, Vec3 motion, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, boolean inWater, @Nullable RandomSource random, float overrideDamage,
            boolean inGround) {
        return applyTick(config, level, projectile, shooter, currentHomingTarget, projectileBounds, position, motion,
                ticksExisted, ticksInAir, hasTauTrailNodes, previousPosition, inWater, random, overrideDamage,
                inGround, 0.0F);
    }

    public static TickResult applyTick(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, @Nullable Entity currentHomingTarget, AABB projectileBounds,
            Vec3 position, Vec3 motion, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, boolean inWater, @Nullable RandomSource random, float overrideDamage,
            boolean inGround, float acceleration) {
        return applyTick(config, level, projectile, shooter, currentHomingTarget, projectileBounds, position, motion,
                ticksExisted, ticksInAir, hasTauTrailNodes, previousPosition, inWater, random, overrideDamage,
                inGround, acceleration, 0.0D);
    }

    public static TickResult applyTick(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, @Nullable Entity currentHomingTarget, AABB projectileBounds,
            Vec3 position, Vec3 motion, int ticksExisted, int ticksInAir, boolean hasTauTrailNodes,
            @Nullable Vec3 previousPosition, boolean inWater, @Nullable RandomSource random, float overrideDamage,
            boolean inGround, float acceleration, double currentBeamLength) {
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
                    updatedAcceleration, 0.0D);
        }

        if (config.plink() == BulletPlink.ENERGY) {
            return applyBeamTick(config, level, projectile, shooter, currentHomingTarget, projectileBounds, position,
                    motion, ticksInAir, update, tauTrail, preMoveParticles, flameTrailParticles,
                    fireExtinguisherParticles, lifetime, meteorFlameParticles, spawnRequests, random, overrideDamage,
                    inGround, updatedAcceleration, currentBeamLength);
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
        BulletUpdateBehaviorUtil.KnownPostMoveResult postMoveUpdate =
                BulletUpdateBehaviorUtil.applyKnownPostMoveUpdate(config, projectile, shooter,
                        update.homingTarget(), nextPosition, nextMotion, updatedAcceleration);
        nextMotion = postMoveUpdate.motion();
        updatedAcceleration = postMoveUpdate.acceleration();
        int trailParticles = flameTrailParticles + fireExtinguisherParticles
                + BulletFlightVisualUtil.spawnVanillaTrail(config, level,
                previousPosition == null ? position : previousPosition, nextPosition);

        return new TickResult(nextPosition, nextMotion, postMoveUpdate.homingTarget(), hit, update, tauTrail,
                preMoveParticles, trailParticles, lifetime, discardProjectile, hit.enteredPortal(),
                meteorFlameParticles, Collections.unmodifiableList(spawnRequests), updatedAcceleration, 0.0D);
    }

    private static TickResult applyBeamTick(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, @Nullable Entity currentHomingTarget, AABB projectileBounds,
            Vec3 position, Vec3 motion, int ticksInAir, BulletUpdateBehaviorUtil.KnownUpdateResult update,
            BulletTauTrailUtil.TauTrailAppend tauTrail, int preMoveParticles, int flameTrailParticles,
            int fireExtinguisherParticles, BulletKinematicsUtil.LifetimeCheck lifetime, int meteorFlameParticles,
            List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests, @Nullable RandomSource random,
            float overrideDamage, boolean inGround, float updatedAcceleration, double currentBeamLength) {
        if (ticksInAir > 1) {
            double beamLength = currentBeamLength > 1.0E-7D ? currentBeamLength : LEGACY_BEAM_RANGE;
            if (projectile != null) {
                BulletUpdateBehaviorUtil.applyKnownBeamUpdate(config, projectile, shooter, motion, overrideDamage,
                        beamLength);
            }
            return new TickResult(position, motion, update.homingTarget(),
                    BulletProjectileHitUtil.HitApplication.NONE, update, tauTrail, preMoveParticles,
                    flameTrailParticles + fireExtinguisherParticles, lifetime,
                    lifetime.shouldDiscard() || update.discardProjectile(), false, meteorFlameParticles,
                    Collections.unmodifiableList(spawnRequests), updatedAcceleration, beamLength);
        }

        Vec3 direction = beamDirection(projectile, motion);
        Vec3 rangedMotion = direction.scale(LEGACY_BEAM_RANGE / Math.max(config.velocity(), 1.0E-7F));
        BulletCollisionUtil.CollisionScan scan = projectile == null
                ? BulletCollisionUtil.scan(config, level, null, shooter, projectileBounds, position, rangedMotion,
                        ticksInAir, 0.0F)
                : BulletCollisionUtil.scan(config, level, projectile, shooter, projectileBounds, position,
                        rangedMotion, ticksInAir, 0.0F);
        Ni4NiCoinRicochetUtil.CoinHit legacyBeamCoinHit =
                Ni4NiCoinRicochetUtil.findLegacyBeamCoinHit(level, projectile, scan);
        BulletCollisionUtil.CollisionScan effectiveScan = legacyBeamCoinHit == null
                ? scan
                : scanUntilLegacyBeamCoin(scan, legacyBeamCoinHit, config.penetrates());
        boolean legacyBeamCoinInterrupt = legacyBeamCoinHit != null;
        BulletProjectileHitUtil.HitApplication hit = projectile != null && ticksInAir <= 1 && hasLegacyBeamImpact(config)
                && !(legacyBeamCoinInterrupt && !config.penetrates())
                ? BulletProjectileHitUtil.applyScannedHits(config, level, projectile, shooter, rangedMotion,
                        effectiveScan,
                        random, overrideDamage, inGround, ticksInAir)
                : new BulletProjectileHitUtil.HitApplication(effectiveScan, Collections.emptyList(),
                        BulletRicochetUtil.BlockHitResult.NONE, rangedMotion, false, false, Collections.emptyList(),
                        overrideDamage, false);
        spawnRequests.addAll(hit.spawnRequests());
        boolean discardProjectile = lifetime.shouldDiscard() || update.discardProjectile() || hit.discardProjectile();
        double beamLength = beamLength(effectiveScan);
        if (projectile != null) {
            BulletUpdateBehaviorUtil.applyKnownBeamUpdate(config, projectile, shooter, motion, overrideDamage,
                    beamLength);
        }
        return new TickResult(position, motion, update.homingTarget(), hit, update, tauTrail, preMoveParticles,
                flameTrailParticles + fireExtinguisherParticles, lifetime, discardProjectile, hit.enteredPortal(),
                meteorFlameParticles, Collections.unmodifiableList(spawnRequests), updatedAcceleration, beamLength);
    }

    private static BulletCollisionUtil.CollisionScan scanUntilLegacyBeamCoin(
            BulletCollisionUtil.CollisionScan scan, Ni4NiCoinRicochetUtil.CoinHit coinHit,
            boolean keepEntityHitsBeforeCoin) {
        if (scan == null || coinHit == null) {
            return scan;
        }
        List<BulletCollisionUtil.EntityCollision> entityHits = Collections.emptyList();
        if (keepEntityHitsBeforeCoin && !scan.entityHits().isEmpty()) {
            entityHits = new ArrayList<>();
            for (BulletCollisionUtil.EntityCollision hit : scan.entityHits()) {
                if (hit.distanceSqr() < coinHit.distanceSqr()) {
                    entityHits.add(hit);
                }
            }
            entityHits = Collections.unmodifiableList(entityHits);
        }
        return new BulletCollisionUtil.CollisionScan(scan.start(), scan.end(), coinHit.location(),
                coinHit.location().subtract(scan.start()), null, entityHits, null,
                BulletCollisionUtil.PrimaryHit.MISS, null);
    }

    private static boolean hasLegacyBeamImpact(BulletConfig config) {
        return config.hasBehavior(BulletBehaviorTag.STANDARD_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.LIGHTNING_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.LIGHTNING_BEAM_SPLIT)
                || config.hasBehavior(BulletBehaviorTag.INFRARED_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.BLACK_FIRE_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.SHREDDER_BEAM_SPLIT)
                || config.hasBehavior(BulletBehaviorTag.GRENADE_LASER_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM);
    }

    private static Vec3 beamDirection(@Nullable Entity projectile, Vec3 motion) {
        if (motion != null && motion.lengthSqr() > 1.0E-7D) {
            return motion.normalize();
        }
        if (projectile != null) {
            float yaw = projectile.getYRot() * ((float) Math.PI / 180.0F);
            float pitch = projectile.getXRot() * ((float) Math.PI / 180.0F);
            return new Vec3(Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch),
                    Math.cos(yaw) * Math.cos(pitch)).normalize();
        }
        return new Vec3(0.0D, 0.0D, 1.0D);
    }

    private static double beamLength(BulletCollisionUtil.CollisionScan scan) {
        if (scan == null || scan.start() == null) {
            return LEGACY_BEAM_RANGE;
        }
        Vec3 end = scan.primaryHit() == BulletCollisionUtil.PrimaryHit.MISS
                ? scan.clippedEnd()
                : scan.primaryLocation();
        return end == null ? LEGACY_BEAM_RANGE : scan.start().distanceTo(end);
    }

    private static Vec3 moveBase(Vec3 position, BulletCollisionUtil.CollisionScan scan,
            BulletProjectileHitUtil.HitApplication hit) {
        if (hit.blockHit().ricocheted() && scan.blockHit() != null) {
            return scan.blockHit().location();
        }
        return position;
    }

    public record TickResult(Vec3 nextPosition, Vec3 nextMotion, @Nullable Entity homingTarget,
            BulletProjectileHitUtil.HitApplication hit, BulletUpdateBehaviorUtil.KnownUpdateResult update,
            BulletTauTrailUtil.TauTrailAppend tauTrail, int preMoveParticles, int trailParticles,
            BulletKinematicsUtil.LifetimeCheck lifetime, boolean discardProjectile, boolean enteredPortal,
            int meteorFlameParticles, List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests,
            float acceleration, double beamLength) {
        public static final TickResult NONE = new TickResult(Vec3.ZERO, Vec3.ZERO, null,
                BulletProjectileHitUtil.HitApplication.NONE,
                BulletUpdateBehaviorUtil.KnownUpdateResult.NONE,
                BulletTauTrailUtil.TauTrailAppend.NONE, 0, 0,
                new BulletKinematicsUtil.LifetimeCheck(true, false), true, false, 0, Collections.emptyList(),
                0.0F, 0.0D);
    }

    private BulletProjectileTickUtil() {
    }
}

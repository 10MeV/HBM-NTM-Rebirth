package com.hbm.ntm.bullet;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulletProjectileHitUtil {
    public static HitApplication applyScannedHits(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            Vec3 motion, int ticksInAir, @Nullable RandomSource random, float overrideDamage, boolean inGround) {
        if (projectile == null) {
            return HitApplication.NONE;
        }
        BulletCollisionUtil.CollisionScan scan =
                BulletCollisionUtil.scan(config, projectile.level(), projectile, shooter,
                        projectile.position(), motion, ticksInAir);
        return applyScannedHits(config, projectile.level(), projectile, shooter, motion, scan, random,
                overrideDamage, inGround, ticksInAir);
    }

    public static HitApplication applyScannedHits(BulletConfig config, Level level, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, BulletCollisionUtil.CollisionScan scan,
            @Nullable RandomSource random, float overrideDamage, boolean inGround) {
        return applyScannedHits(config, level, projectile, shooter, motion, scan, random, overrideDamage, inGround,
                Integer.MAX_VALUE);
    }

    public static HitApplication applyScannedHits(BulletConfig config, Level level, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, BulletCollisionUtil.CollisionScan scan,
            @Nullable RandomSource random, float overrideDamage, boolean inGround, int ticksInAir) {
        if (config == null || level == null || projectile == null || motion == null || scan == null) {
            return HitApplication.NONE;
        }
        if (skipsEarlyGrenadePelletImpact(config, ticksInAir)) {
            return HitApplication.NONE;
        }

        RandomSource roll = random == null ? level.random : random;
        Vec3 resultingMotion = motion;
        boolean discardProjectile = false;
        boolean enteredPortal = false;
        boolean overrideDamageChanged = false;
        float currentOverrideDamage = overrideDamage;
        List<EntityHitApplication> entityHits = new ArrayList<>();
        List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests = new ArrayList<>();
        BulletRicochetUtil.BlockHitResult blockHit = BulletRicochetUtil.BlockHitResult.NONE;

        if (config.penetrates()) {
            for (BulletCollisionUtil.EntityCollision entityHit : scan.entityHits()) {
                BulletDamageUtil.EntityHitResult result = BulletDamageUtil.applyEntityHit(config, projectile, shooter,
                        entityHit.entity(), entityHit.location(), roll, currentOverrideDamage, ticksInAir);
                entityHits.add(new EntityHitApplication(entityHit, result));
                spawnRequests.addAll(result.blockImpact().spawnRequests());
                discardProjectile |= result.discardProjectile();
                if (result.overrideDamageChanged()) {
                    currentOverrideDamage = result.nextOverrideDamage();
                    overrideDamageChanged = true;
                }
            }
            BlockApplication block = applyBlockHit(config, level, shooter, motion, scan.blockHit(), roll, inGround,
                    currentOverrideDamage);
            blockHit = block.result();
            resultingMotion = block.resultingMotion();
            discardProjectile |= blockHit.blockImpact().discardProjectile();
            spawnRequests.addAll(blockHit.blockImpact().spawnRequests());
            enteredPortal = block.enteredPortal();
        } else if (scan.primaryHit() == BulletCollisionUtil.PrimaryHit.ENTITY && scan.nearestEntityHit() != null) {
            BulletCollisionUtil.EntityCollision entityHit = scan.nearestEntityHit();
            BulletDamageUtil.EntityHitResult result = BulletDamageUtil.applyEntityHit(config, projectile, shooter,
                    entityHit.entity(), entityHit.location(), roll, currentOverrideDamage, ticksInAir);
            entityHits.add(new EntityHitApplication(entityHit, result));
            spawnRequests.addAll(result.blockImpact().spawnRequests());
            discardProjectile |= result.discardProjectile();
            if (result.overrideDamageChanged()) {
                currentOverrideDamage = result.nextOverrideDamage();
                overrideDamageChanged = true;
            }
        } else if (scan.primaryHit() == BulletCollisionUtil.PrimaryHit.BLOCK) {
            BlockApplication block = applyBlockHit(config, level, shooter, motion, scan.blockHit(), roll, inGround,
                    overrideDamage);
            blockHit = block.result();
            resultingMotion = block.resultingMotion();
            discardProjectile |= blockHit.blockImpact().discardProjectile();
            spawnRequests.addAll(blockHit.blockImpact().spawnRequests());
            enteredPortal = block.enteredPortal();
        }

        return new HitApplication(scan, Collections.unmodifiableList(entityHits), blockHit, resultingMotion,
                discardProjectile, enteredPortal, Collections.unmodifiableList(spawnRequests),
                currentOverrideDamage, overrideDamageChanged);
    }

    private static BlockApplication applyBlockHit(BulletConfig config, Level level, @Nullable Entity shooter,
            Vec3 motion, @Nullable BulletCollisionUtil.BlockCollision blockHit, RandomSource random,
            boolean inGround) {
        return applyBlockHit(config, level, shooter, motion, blockHit, random, inGround, 0.0F);
    }

    private static BlockApplication applyBlockHit(BulletConfig config, Level level, @Nullable Entity shooter,
            Vec3 motion, @Nullable BulletCollisionUtil.BlockCollision blockHit, RandomSource random,
            boolean inGround, float impactDamage) {
        if (blockHit == null) {
            return new BlockApplication(BulletRicochetUtil.BlockHitResult.NONE, motion, false);
        }
        if (blockHit.portal()) {
            return new BlockApplication(BulletRicochetUtil.BlockHitResult.NONE, motion, true);
        }

        BulletRicochetUtil.BlockHitResult result = BulletRicochetUtil.applyBlockHit(config, level,
                blockHit.location(), motion, blockHit.side(), random, shooter, blockHit.blockPos(), inGround,
                impactDamage);
        return new BlockApplication(result, result.motion(), false);
    }

    private static boolean skipsEarlyGrenadePelletImpact(BulletConfig config, int ticksInAir) {
        return ticksInAir < 2
                && (config.hasBehavior(BulletBehaviorTag.GRENADE_TINY_EXPLOSIVE_PELLET)
                || config.hasBehavior(BulletBehaviorTag.GRENADE_HEAVY_EXPLOSIVE_PELLET));
    }

    public record HitApplication(BulletCollisionUtil.CollisionScan scan, List<EntityHitApplication> entityHits,
            BulletRicochetUtil.BlockHitResult blockHit, Vec3 resultingMotion, boolean discardProjectile,
            boolean enteredPortal, List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests,
            float nextOverrideDamage, boolean overrideDamageChanged) {
        public static final HitApplication NONE = new HitApplication(BulletCollisionUtil.CollisionScan.NONE,
                Collections.emptyList(), BulletRicochetUtil.BlockHitResult.NONE, Vec3.ZERO, false, false,
                Collections.emptyList(), 0.0F, false);
    }

    public record EntityHitApplication(BulletCollisionUtil.EntityCollision collision,
            BulletDamageUtil.EntityHitResult result) {
    }

    private record BlockApplication(BulletRicochetUtil.BlockHitResult result, Vec3 resultingMotion,
            boolean enteredPortal) {
    }

    private BulletProjectileHitUtil() {
    }
}

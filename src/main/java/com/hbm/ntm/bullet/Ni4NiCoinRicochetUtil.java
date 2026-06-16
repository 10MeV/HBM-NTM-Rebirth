package com.hbm.ntm.bullet;

import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.entity.projectile.CoinEntity;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Ni4NiCoinRicochetUtil {
    private static final double COIN_HITBOX_INFLATE = 0.3D;
    private static final double TARGET_RANGE = 50.0D;
    private static final double REDIRECT_RANGE = 250.0D;
    private static final float DAMAGE_MULTIPLIER = 1.25F;

    public static boolean apply(BulletProjectileEntity bullet, BulletConfig config,
            BulletProjectileTickUtil.TickResult tickResult, float overrideDamage) {
        if (bullet == null || config == null || tickResult == null || bullet.level().isClientSide()
                || !isLegacyBeamCoinRedirect(config)) {
            return false;
        }
        BulletCollisionUtil.CollisionScan scan = tickResult.hit().scan();
        if (scan == null) {
            return false;
        }

        CoinHit coinHit = findCoinHit(bullet.level(), bullet, scan.start(), scan.clippedEnd());
        if (coinHit == null) {
            return false;
        }

        CoinEntity coin = coinHit.coin();
        Entity owner = coin.getOwner();
        Entity target = chooseTarget(bullet.level(), coin, owner, coinHit.location());
        Vec3 direction = redirectDirection(bullet.level(), target, coinHit.location());
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(config, coinHit.location(),
                direction, BulletKinematicsUtil.DEFAULT_THROW_FORCE, 0.0F, bullet.level().random);
        plan = BulletLaunchUtil.withMotion(plan, redirectedMotion(config, direction));
        if (!plan.valid()) {
            return false;
        }

        coin.discard();
        BulletProjectileEntity redirected = BulletProjectileEntity.fromLaunchPlan(bullet.level(), plan,
                owner == null ? bullet.getOwner() : owner);
        redirected.overrideDamage = redirectedDamage(config, bullet.level(), overrideDamage);
        bullet.level().addFreshEntity(redirected);
        if (bullet.level() instanceof ServerLevel serverLevel) {
            ParticleUtil.spawnVanillaExtLargeExplode(serverLevel, coinHit.location().x,
                    coinHit.location().y, coinHit.location().z, 1.5F, 1);
        }
        return true;
    }

    private static boolean isLegacyBeamCoinRedirect(BulletConfig config) {
        return config != null && config.plink() == BulletPlink.ENERGY;
    }

    private static float redirectedDamage(BulletConfig config, Level level, float overrideDamage) {
        return BulletRuntimeUtil.rollBaseDamage(config, level.random, overrideDamage) * DAMAGE_MULTIPLIER;
    }

    private static Vec3 redirectedMotion(BulletConfig config, Vec3 direction) {
        if (config == null || direction == null || direction.lengthSqr() <= 1.0E-7D) {
            return Vec3.ZERO;
        }
        double velocity = Math.abs(config.velocity());
        if (velocity <= 1.0E-7D) {
            return direction.normalize();
        }
        // Legacy EntityBulletBeamBase#performHitscanExternal(250D) traces the ricochet beam in one long segment.
        return direction.normalize().scale(REDIRECT_RANGE / velocity);
    }

    public static boolean interceptsBeforePrimary(Level level, Entity projectile,
            BulletCollisionUtil.CollisionScan scan) {
        if (level == null || projectile == null || scan == null) {
            return false;
        }
        CoinHit coinHit = findCoinHit(level, projectile, scan.start(), scan.clippedEnd());
        if (coinHit == null) {
            return false;
        }
        Vec3 primary = scan.primaryLocation();
        return primary == null || coinHit.distanceSqr() < scan.start().distanceToSqr(primary);
    }

    public static boolean interceptsBeam(Level level, Entity projectile, BulletCollisionUtil.CollisionScan scan) {
        if (level == null || projectile == null || scan == null) {
            return false;
        }
        return findCoinHit(level, projectile, scan.start(), scan.clippedEnd()) != null;
    }

    @Nullable
    public static CoinHit findLegacyBeamCoinHit(Level level, @Nullable Entity projectile,
            BulletCollisionUtil.CollisionScan scan) {
        if (level == null || scan == null) {
            return null;
        }
        return findCoinHit(level, projectile, scan.start(), scan.clippedEnd());
    }

    @Nullable
    private static CoinHit findCoinHit(Level level, @Nullable Entity projectile, Vec3 start, Vec3 end) {
        if (level == null || start == null || end == null) {
            return null;
        }
        AABB search = new AABB(start, end).inflate(1.0D);
        List<CoinEntity> coins = level.getEntitiesOfClass(CoinEntity.class, search,
                coin -> coin != projectile && coin.isAlive());
        CoinHit nearest = null;
        for (CoinEntity coin : coins) {
            Vec3 hit = coin.getBoundingBox().inflate(COIN_HITBOX_INFLATE).clip(start, end).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = start.distanceToSqr(hit);
            if (nearest == null || distance < nearest.distanceSqr()) {
                nearest = new CoinHit(coin, hit, distance);
            }
        }
        return nearest;
    }

    @Nullable
    private static Entity chooseTarget(Level level, CoinEntity coin, @Nullable Entity owner, Vec3 origin) {
        AABB search = new AABB(origin, origin).inflate(TARGET_RANGE);
        Entity nearestCoin = nearest(level.getEntitiesOfClass(CoinEntity.class, search,
                candidate -> candidate != coin && candidate.isAlive()), origin, owner);
        if (nearestCoin != null) {
            return nearestCoin;
        }
        Entity nearestPlayer = nearest(level.getEntitiesOfClass(Player.class, search, LivingEntity::isAlive),
                origin, owner);
        if (nearestPlayer != null) {
            return nearestPlayer;
        }
        Entity nearestMob = nearest(level.getEntitiesOfClass(Mob.class, search, LivingEntity::isAlive), origin, owner);
        if (nearestMob != null) {
            return nearestMob;
        }
        return nearest(level.getEntitiesOfClass(LivingEntity.class, search, LivingEntity::isAlive), origin, owner);
    }

    @Nullable
    private static Entity nearest(List<? extends Entity> candidates, Vec3 origin, @Nullable Entity owner) {
        Entity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity candidate : candidates) {
            if (candidate == owner || !candidate.isAlive()) {
                continue;
            }
            double distance = candidate.distanceToSqr(origin);
            if (distance < best) {
                nearest = candidate;
                best = distance;
            }
        }
        return nearest;
    }

    private static Vec3 redirectDirection(Level level, @Nullable Entity target, Vec3 origin) {
        if (target != null) {
            return target.getBoundingBox().getCenter().subtract(origin);
        }
        return new Vec3(level.random.nextGaussian() * 0.5D, -1.0D,
                level.random.nextGaussian() * 0.5D);
    }

    public record CoinHit(CoinEntity coin, Vec3 location, double distanceSqr) {
    }

    private Ni4NiCoinRicochetUtil() {
    }
}

package com.hbm.ntm.energy;

import com.hbm.ntm.bullet.BulletCollisionUtil;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletImpactUtil;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.BulletProjectileHitUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class HbmEnergyDischargeEffects {
    private static final double SELF_CHARGING_RANGE = 15.0D;
    private static final float SELF_CHARGING_BEAM_DAMAGE = 50.0F;

    private HbmEnergyDischargeEffects() {
    }

    public static void dischargeSelfChargingSocket(ServerLevel level, BlockPos socketPos, Direction facing) {
        Vec3 origin = selfChargingDischargeOrigin(socketPos, facing);
        AABB bounds = new AABB(origin, origin).inflate(SELF_CHARGING_RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive);
        Collections.shuffle(targets, new Random(level.random.nextLong()));

        for (LivingEntity target : targets) {
            Vec3 targetPoint = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (origin.distanceToSqr(targetPoint) > SELF_CHARGING_RANGE * SELF_CHARGING_RANGE) {
                continue;
            }
            dischargeArc(level, socketPos, origin, targetPoint);
        }

        Vec3 randomBlast = origin.add(
                level.random.nextGaussian() * 0.5D,
                level.random.nextGaussian() * 0.5D,
                level.random.nextGaussian() * 0.5D);
        explodeElectricDischarge(level, randomBlast);
    }

    private static Vec3 selfChargingDischargeOrigin(BlockPos socketPos, Direction facing) {
        Direction side = facing.getClockWise();
        return new Vec3(
                socketPos.getX() + 0.5D - facing.getStepX() * 0.5D + side.getStepX() * 0.5D,
                socketPos.getY() + 1.0D,
                socketPos.getZ() + 0.5D - facing.getStepZ() * 0.5D + side.getStepX() * 0.5D);
    }

    private static void dischargeArc(ServerLevel level, BlockPos socketPos, Vec3 origin, Vec3 targetPoint) {
        Vec3 delta = targetPoint.subtract(origin);
        if (delta.lengthSqr() <= 1.0E-7D) {
            return;
        }

        Vec3 normalized = delta.normalize();
        double dominantAxis = Math.max(Math.abs(normalized.x), Math.max(Math.abs(normalized.y), Math.abs(normalized.z)));
        if (dominantAxis <= 1.0E-7D) {
            return;
        }

        Vec3 legacyOffset = normalized.scale(1.125D / dominantAxis);
        Vec3 beamStart = new Vec3(socketPos.getX() + legacyOffset.x, socketPos.getY() + legacyOffset.y,
                socketPos.getZ() + legacyOffset.z);
        Vec3 actualDelta = targetPoint.subtract(beamStart);
        if (actualDelta.lengthSqr() <= 1.0E-7D) {
            return;
        }

        BulletConfig config = LegacySednaRuntimeBulletConfigs.BATTERY_SOCKET_DISCHARGE;
        float throwForce = config.velocity() <= 0.0F
                ? (float) actualDelta.length()
                : (float) (actualDelta.length() / config.velocity());
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(config, beamStart, actualDelta,
                throwForce, 0.0F, level.random);
        BulletProjectileEntity projectile = BulletProjectileEntity.fromLaunchPlan(level, plan, null);
        projectile.overrideDamage = SELF_CHARGING_BEAM_DAMAGE;

        BulletCollisionUtil.CollisionScan scan = BulletCollisionUtil.scan(config, level, projectile, null,
                projectile.getBoundingBox(), beamStart, plan.motion(), 3);
        BulletProjectileHitUtil.HitApplication hit = BulletProjectileHitUtil.applyScannedHits(config, level,
                projectile, null, plan.motion(), scan, level.random, SELF_CHARGING_BEAM_DAMAGE, false, 3);
        BulletProjectileEntity.spawnAll(level, hit.spawnRequests());

        Vec3 impact = scan.blockHit() != null ? scan.blockHit().location() : targetPoint;
        spawnElectricArc(level, beamStart, impact);
    }

    private static void explodeElectricDischarge(ServerLevel level, Vec3 position) {
        BulletImpactUtil.applyBatterySocketDischarge(level, position, null);
    }

    private static void spawnElectricArc(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int steps = Math.max(3, (int) Math.ceil(delta.length() * 2.0D));
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / (double) steps;
            Vec3 point = start.add(delta.scale(progress)).add(
                    (level.random.nextDouble() - 0.5D) * 0.12D,
                    (level.random.nextDouble() - 0.5D) * 0.12D,
                    (level.random.nextDouble() - 0.5D) * 0.12D);
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.02D);
        }
    }
}

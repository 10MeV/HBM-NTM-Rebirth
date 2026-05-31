package com.hbm.ntm.energy;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class HbmEnergyDischargeEffects {
    private static final double SELF_CHARGING_RANGE = 15.0D;
    private static final double SELF_CHARGING_BLAST_RADIUS = 5.0D;
    private static final float SELF_CHARGING_BEAM_DAMAGE = 50.0F;
    private static final float SELF_CHARGING_BLAST_DAMAGE = 20.0F;

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
            dischargeArc(level, origin, target, targetPoint);
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
                socketPos.getZ() + 0.5D - facing.getStepZ() * 0.5D + side.getStepZ() * 0.5D);
    }

    private static void dischargeArc(ServerLevel level, Vec3 origin, LivingEntity target, Vec3 targetPoint) {
        BlockHitResult hit = level.clip(new ClipContext(origin, targetPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target));
        Vec3 impact = targetPoint;
        if (hit.getType() == HitResult.Type.BLOCK && hit.getLocation().distanceToSqr(origin) + 0.25D < targetPoint.distanceToSqr(origin)) {
            BlockPos hitPos = hit.getBlockPos();
            if (!level.getBlockState(hitPos).isAir()) {
                level.destroyBlock(hitPos, false);
            }
            impact = hit.getLocation();
        } else {
            EntityDamageUtil.attackEntityFromNt(target, ModDamageSources.electric(level), SELF_CHARGING_BEAM_DAMAGE);
        }

        spawnElectricArc(level, origin, impact);
        explodeElectricDischarge(level, impact);
    }

    private static void explodeElectricDischarge(ServerLevel level, Vec3 position) {
        AABB bounds = new AABB(position, position).inflate(SELF_CHARGING_BLAST_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            double distance = Math.sqrt(entity.distanceToSqr(position));
            if (distance > SELF_CHARGING_BLAST_RADIUS) {
                continue;
            }
            float damage = (float) (SELF_CHARGING_BLAST_DAMAGE * (1.0D - distance / SELF_CHARGING_BLAST_RADIUS));
            if (damage <= 0.0F) {
                continue;
            }
            EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.electric(level), damage);
            Vec3 push = entity.position().subtract(position);
            if (push.lengthSqr() > 0.0001D) {
                entity.push(push.x * 0.08D, 0.04D, push.z * 0.08D);
            }
        }

        level.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, position.x, position.y, position.z, 18, 0.6D, 0.6D, 0.6D, 0.08D);
        level.playSound(null, position.x, position.y, position.z, ModSounds.ENTITY_UFO_BLAST.get(), SoundSource.BLOCKS,
                5.0F, 0.9F + level.random.nextFloat() * 0.2F);
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

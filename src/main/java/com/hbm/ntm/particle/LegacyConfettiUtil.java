package com.hbm.ntm.particle;

import com.hbm.ntm.damage.DamageClass;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

public final class LegacyConfettiUtil {
    public static void decideConfetti(LivingEntity entity, DamageClass damageClass) {
        if (entity == null || entity.isAlive()) {
            return;
        }
        switch (damageClass == null ? DamageClass.OTHER : damageClass) {
            case LASER, ELECTRIC -> pulverize(entity);
            case PLASMA, FIRE -> cremate(entity);
            case EXPLOSIVE -> gib(entity);
            default -> {
            }
        }
    }

    public static void pulverize(LivingEntity entity) {
        int amount = ashAmount(entity);
        ParticleUtil.spawnAshes(entity, amount, 0.125F);
        ParticleUtil.spawnSkeleton(entity, 1.0F);
        playDisintegration(entity);
    }

    public static void cremate(LivingEntity entity) {
        int amount = ashAmount(entity);
        ParticleUtil.spawnAshes(entity, amount, 0.125F);
        ParticleUtil.spawnSkeleton(entity, 0.25F);
        playDisintegration(entity);
    }

    public static void gib(LivingEntity entity) {
        if (entity instanceof Ocelot) {
            return;
        }

        ParticleUtil.spawnSkeletonGib(entity, 0.25F);
        if (entity instanceof Skeleton) {
            return;
        }

        ParticleUtil.spawnGiblets(entity, gibType(entity));
        Level level = entity.level();
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,
                SoundSource.HOSTILE, 2.0F, 0.95F + entity.getRandom().nextFloat() * 0.2F);
    }

    private static int ashAmount(LivingEntity entity) {
        return Mth.clamp((int) (entity.getBbWidth() * entity.getBbHeight() * entity.getBbWidth() * 25.0F), 5, 50);
    }

    private static int gibType(LivingEntity entity) {
        if (entity instanceof Slime || entity instanceof MagmaCube || entity instanceof Creeper) {
            return ParticleUtil.GIBLET_SLIME;
        }
        if (entity instanceof AbstractGolem || entity instanceof Blaze || entity.getType() == EntityType.IRON_GOLEM) {
            return ParticleUtil.GIBLET_METAL;
        }
        return ParticleUtil.GIBLET_MEAT;
    }

    private static void playDisintegration(LivingEntity entity) {
        Level level = entity.level();
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.WEAPON_FIRE_DISINTEGRATION.get(),
                SoundSource.HOSTILE, 2.0F, 0.9F + entity.getRandom().nextFloat() * 0.2F);
    }

    private LegacyConfettiUtil() {
    }
}

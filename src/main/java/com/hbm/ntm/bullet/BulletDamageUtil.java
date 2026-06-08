package com.hbm.ntm.bullet;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BulletDamageUtil {
    public static EntityHitResult applyEntityHit(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            Entity target, @Nullable Vec3 hitLocation, @Nullable RandomSource random, float overrideDamage) {
        if (config == null || projectile == null || target == null) {
            return EntityHitResult.NONE;
        }

        Level level = target.level();
        RandomSource rollRandom = random == null ? level.random : random;
        BulletImpactUtil.EntityHurtResult hurt;
        BulletImpactUtil.BlockImpactResult blockImpact;
        boolean discardProjectile;
        if (config.penetrates()) {
            hurt = BulletImpactUtil.applyEntityHurtEffects(config, target, rollRandom);
            blockImpact = BulletImpactUtil.BlockImpactResult.NONE;
            discardProjectile = false;
        } else {
            BulletImpactUtil.EntityImpactResult impact =
                    BulletImpactUtil.applyEntityImpactEffects(config, target, shooter, hitLocation, rollRandom);
            hurt = impact.hurt();
            blockImpact = impact.blockImpact();
            discardProjectile = impact.discardProjectile();
        }

        BulletRuntimeUtil.DamageRoll roll =
                BulletRuntimeUtil.rollDamage(config, target, hitLocation, rollRandom, overrideDamage);
        DamageSource damageSource = config.damageSource(level, projectile, shooter);
        EntityDamageUtil.DamageApplication first =
                EntityDamageUtil.attackEntityFromNtDetailed(target, damageSource, roll.finalDamage(),
                        false, true, 0.0D, 0.0F, 0.0F);
        boolean retriedIgnoringIFrames = false;
        EntityDamageUtil.DamageApplication applied = first;
        if (!first.damaged()) {
            retriedIgnoringIFrames = true;
            applied = EntityDamageUtil.attackEntityFromNtDetailed(target, damageSource, roll.finalDamage(),
                    true, true, 0.0D, 0.0F, 0.0F);
        }

        boolean headshotEffect = roll.headshot() && applied.damaged();
        if (headshotEffect) {
            spawnHeadshotEffects(level, target, rollRandom);
        }

        return new EntityHitResult(discardProjectile, hurt, blockImpact, roll, applied,
                retriedIgnoringIFrames, headshotEffect);
    }

    public static void spawnHeadshotEffects(Level level, Entity target, @Nullable RandomSource random) {
        if (level == null || level.isClientSide() || !(target instanceof LivingEntity living)) {
            return;
        }
        RandomSource roll = random == null ? level.random : random;
        double head = living.getBbHeight() - living.getEyeHeight();
        double x = living.getX();
        double y = living.getY() + living.getBbHeight() - head;
        double z = living.getZ();
        ParticleUtil.spawnVanillaBlockDustBurst(level, x, y, z, 15, 0.1D, Blocks.REDSTONE_BLOCK);
        level.playSound(null, x, living.getY(), z, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,
                SoundSource.HOSTILE, 1.0F, 0.95F + roll.nextFloat() * 0.2F);
    }

    public record EntityHitResult(boolean discardProjectile, BulletImpactUtil.EntityHurtResult hurt,
            BulletImpactUtil.BlockImpactResult blockImpact, BulletRuntimeUtil.DamageRoll damageRoll,
            EntityDamageUtil.DamageApplication damageApplication, boolean retriedIgnoringIFrames,
            boolean headshotEffect) {
        public static final EntityHitResult NONE = new EntityHitResult(false,
                BulletImpactUtil.EntityHurtResult.NONE, BulletImpactUtil.BlockImpactResult.NONE,
                new BulletRuntimeUtil.DamageRoll(0.0F, 0.0F, false),
                new EntityDamageUtil.DamageApplication(false, false, 0.0F, 0.0F, "skipped"),
                false, false);
    }

    private BulletDamageUtil() {
    }
}

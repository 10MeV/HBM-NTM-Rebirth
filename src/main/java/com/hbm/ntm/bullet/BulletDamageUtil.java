package com.hbm.ntm.bullet;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
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
        return applyEntityHit(config, projectile, shooter, target, hitLocation, random, overrideDamage,
                Integer.MAX_VALUE);
    }

    public static EntityHitResult applyEntityHit(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            Entity target, @Nullable Vec3 hitLocation, @Nullable RandomSource random, float overrideDamage,
            int ticksInAir) {
        if (config == null || projectile == null || target == null) {
            return EntityHitResult.NONE;
        }
        if (skipsEarlyExplosiveEntityImpact(config, shooter, target, ticksInAir)) {
            return EntityHitResult.NONE;
        }

        Level level = target.level();
        RandomSource rollRandom = random == null ? level.random : random;
        BulletImpactUtil.EntityHurtResult hurt;
        BulletImpactUtil.BlockImpactResult blockImpact;
        boolean discardProjectile;
        if (config.penetrates() && !appliesPenetratingEntityImpact(config)) {
            hurt = BulletImpactUtil.applyEntityHurtEffects(config, target, rollRandom);
            blockImpact = BulletImpactUtil.BlockImpactResult.NONE;
            discardProjectile = false;
        } else {
            BulletImpactUtil.EntityImpactResult impact =
                    BulletImpactUtil.applyEntityImpactEffects(config, target, shooter, hitLocation, rollRandom,
                            overrideDamage);
            hurt = impact.hurt();
            blockImpact = impact.blockImpact();
            discardProjectile = impact.discardProjectile();
        }
        if (skipsDirectEntityDamage(config)) {
            HeatBonus heatBonus = applyHeatDirectHitBonus(config, level, projectile, shooter, target, rollRandom,
                    overrideDamage);
            return new EntityHitResult(discardProjectile, hurt, blockImpact,
                    heatBonus.damageRoll(),
                    heatBonus.damageApplication(),
                    heatBonus.retriedIgnoringIFrames(), false, false, overrideDamage, false);
        }

        BulletRuntimeUtil.DamageRoll roll =
                BulletRuntimeUtil.rollDamage(config, target, hitLocation, rollRandom, overrideDamage);
        DamageSource damageSource = config.damageSource(level, projectile, shooter);
        boolean legacyBeamDamage = usesLegacyBeamDamage(config);
        boolean allowSpecialCancel = !config.hasBehavior(BulletBehaviorTag.BEAM_HIT);
        float previousHealth = health(target);
        EntityDamageUtil.DamageApplication first =
                EntityDamageUtil.attackEntityFromNtDetailed(target, damageSource, roll.finalDamage(),
                        legacyBeamDamage, allowSpecialCancel, config.knockbackMultiplier(),
                        config.armorThresholdNegation(), config.armorPiercingPercent());
        boolean retriedIgnoringIFrames = false;
        EntityDamageUtil.DamageApplication applied = first;
        if (!legacyBeamDamage && first.shouldRetryIgnoringIFrames()) {
            retriedIgnoringIFrames = true;
            applied = EntityDamageUtil.attackEntityFromNtDetailed(target, damageSource, roll.finalDamage(),
                    true, true, config.knockbackMultiplier(), config.armorThresholdNegation(),
                    config.armorPiercingPercent());
        }
        DamageFalloff falloff = applyDamageFalloff(config, target, previousHealth, overrideDamage,
                roll.baseDamage());

        boolean headshotEffect = roll.headshot() && applied.damaged();
        if (headshotEffect) {
            spawnHeadshotEffects(level, target, rollRandom);
        }
        boolean resetHomingTarget = BulletHomingStateUtil.shouldResetTargetAfterEntityHurt(config);

        return new EntityHitResult(discardProjectile || falloff.depleted(), hurt, blockImpact, roll, applied,
                retriedIgnoringIFrames, headshotEffect, resetHomingTarget, falloff.nextOverrideDamage(),
                falloff.changed());
    }

    private static boolean appliesPenetratingEntityImpact(BulletConfig config) {
        return config.hasBehavior(BulletBehaviorTag.LIGHTNING_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.LIGHTNING_BEAM_SPLIT)
                || config.hasBehavior(BulletBehaviorTag.INFRARED_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.BLACK_FIRE_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM);
    }

    private static boolean skipsDirectEntityDamage(BulletConfig config) {
        return config.hasBehavior(BulletBehaviorTag.LIGHTNING_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.LIGHTNING_BEAM_SPLIT)
                || config.hasBehavior(BulletBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM)
                || config.hasBehavior(BulletBehaviorTag.FOLLY_SUPERMATTER_BEAM)
                || config.hasBehavior(BulletBehaviorTag.SHREDDER_BEAM_SPLIT)
                || hasExplosiveImpactBehavior(config);
    }

    private static boolean skipsEarlyExplosiveEntityImpact(BulletConfig config, @Nullable Entity shooter,
            Entity target, int ticksInAir) {
        if (ticksInAir >= 3 || !hasExplosiveImpactBehavior(config)) {
            return false;
        }
        String legacyName = config.legacyName();
        if ("folly_nuke".equals(legacyName) && ticksInAir < 2) {
            return true;
        }
        if (legacyName.startsWith("rocket_")) {
            return true;
        }
        if (target == shooter && skipsEarlySelfExplosiveImpact(legacyName)) {
            return true;
        }
        return target == shooter
                && legacyName.startsWith("g40_")
                && !config.hasBehavior(BulletBehaviorTag.STANDARD_EXPLODE);
    }

    private static boolean skipsEarlySelfExplosiveImpact(String legacyName) {
        return "r762_he".equals(legacyName)
                || "g10_explosive".equals(legacyName)
                || "bmg50_he".equals(legacyName)
                || "b75".equals(legacyName)
                || legacyName.startsWith("nuke_");
    }

    private static boolean hasExplosiveImpactBehavior(BulletConfig config) {
        return config.hasBehavior(BulletBehaviorTag.TINY_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.GRENADE_TINY_EXPLOSIVE_PELLET)
                || config.hasBehavior(BulletBehaviorTag.STANDARD_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.HEAT_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.DEMO_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.INCENDIARY_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.PHOSPHORUS_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.TURRET_240_STANDARD_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.TURRET_240_VNT_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.CHARGE_MORTAR_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.CHARGE_MORTAR_CHARGE_EXPLODE)
                || config.hasBehavior(BulletBehaviorTag.MINI_NUKE_STANDARD)
                || config.hasBehavior(BulletBehaviorTag.MINI_NUKE_DEMO)
                || config.hasBehavior(BulletBehaviorTag.MINI_NUKE_HIGH)
                || config.hasBehavior(BulletBehaviorTag.MINI_NUKE_TINYTOT)
                || config.hasBehavior(BulletBehaviorTag.MINI_NUKE_HIVE)
                || config.hasBehavior(BulletBehaviorTag.MINI_NUKE_BALEFIRE)
                || config.hasBehavior(BulletBehaviorTag.FOLLY_NUKE_IMPACT);
    }

    private static boolean usesLegacyBeamDamage(BulletConfig config) {
        return config.hasBehavior(BulletBehaviorTag.STANDARD_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.GRENADE_LASER_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.INFRARED_BEAM_HIT)
                || config.hasBehavior(BulletBehaviorTag.BLACK_FIRE_BEAM_HIT);
    }

    private static HeatBonus applyHeatDirectHitBonus(BulletConfig config, Level level, Entity projectile,
            @Nullable Entity shooter, Entity target, RandomSource random, float overrideDamage) {
        if (!config.hasBehavior(BulletBehaviorTag.HEAT_EXPLODE)) {
            return HeatBonus.NONE;
        }
        float baseDamage = BulletRuntimeUtil.rollBaseDamage(config, random, overrideDamage);
        float heatDamage = baseDamage * 3.0F;
        float pierceDt = config.legacyName().startsWith("g40_") ? 3.0F : 5.0F;
        float pierceDr = config.legacyName().startsWith("g40_") ? 0.15F : 0.2F;
        DamageSource source = projectile == null
                ? ModDamageSources.source(level, ModDamageSources.EXPLOSION, shooter)
                : ModDamageSources.indirect(level, ModDamageSources.EXPLOSION, projectile, shooter);
        EntityDamageUtil.DamageApplication first =
                EntityDamageUtil.attackEntityFromNtDetailed(target, source, heatDamage,
                        true, true, 0.5D, pierceDt, pierceDr);
        boolean retried = false;
        EntityDamageUtil.DamageApplication applied = first;
        if (first.shouldRetryIgnoringIFrames()) {
            retried = true;
            applied = EntityDamageUtil.attackEntityFromNtDetailed(target, source, heatDamage,
                    true, true, 0.5D, pierceDt, pierceDr);
        }
        return new HeatBonus(new BulletRuntimeUtil.DamageRoll(baseDamage, heatDamage, false), applied, retried);
    }

    private static float health(Entity target) {
        return target instanceof LivingEntity living ? living.getHealth() : 0.0F;
    }

    private static DamageFalloff applyDamageFalloff(BulletConfig config, Entity target, float previousHealth,
            float overrideDamage, float baseDamage) {
        if (!config.penetrates() || !config.damageFalloffByPenetration() || !(target instanceof LivingEntity living)) {
            return DamageFalloff.unchanged(overrideDamage);
        }
        float healthLost = Math.max(previousHealth - living.getHealth(), 0.0F);
        if (healthLost <= 0.0F) {
            return DamageFalloff.unchanged(overrideDamage);
        }
        float currentDamage = overrideDamage != 0.0F ? overrideDamage : baseDamage;
        float nextDamage = currentDamage - healthLost * 0.5F;
        return new DamageFalloff(nextDamage, true, nextDamage < 0.0F);
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
            boolean headshotEffect, boolean resetHomingTarget, float nextOverrideDamage,
            boolean overrideDamageChanged) {
        public static final EntityHitResult NONE = new EntityHitResult(false,
                BulletImpactUtil.EntityHurtResult.NONE, BulletImpactUtil.BlockImpactResult.NONE,
                new BulletRuntimeUtil.DamageRoll(0.0F, 0.0F, false),
                new EntityDamageUtil.DamageApplication(false, false, 0.0F, 0.0F, EntityDamageUtil.OUTCOME_SKIPPED),
                false, false, false, 0.0F, false);
    }

    private record HeatBonus(BulletRuntimeUtil.DamageRoll damageRoll,
            EntityDamageUtil.DamageApplication damageApplication, boolean retriedIgnoringIFrames) {
        private static final HeatBonus NONE = new HeatBonus(
                new BulletRuntimeUtil.DamageRoll(0.0F, 0.0F, false),
                new EntityDamageUtil.DamageApplication(false, false, 0.0F, 0.0F,
                        EntityDamageUtil.OUTCOME_SKIPPED),
                false);
    }

    private record DamageFalloff(float nextOverrideDamage, boolean changed, boolean depleted) {
        private static DamageFalloff unchanged(float overrideDamage) {
            return new DamageFalloff(overrideDamage, false, false);
        }
    }

    private BulletDamageUtil() {
    }
}

package com.hbm.ntm.entity.mob;

import com.hbm.ntm.blockentity.TeslaBlockEntity;
import com.hbm.ntm.bullet.BulletKinematicsUtil;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityTaintCrab extends EntityCyberCrab {
    public EntityTaintCrab(EntityType<? extends EntityTaintCrab> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    protected RangedAttackGoal createRangedAttackGoal() {
        return new RangedAttackGoal(this, 0.5D, 5, 5, 50.0F);
    }

    @Override
    public void aiStep() {
        if (!level().isClientSide()) {
            setTeslaTargets(TeslaBlockEntity.zap(level(), new Vec3(getX(), getY() + 1.25D, getZ()), 10.0D, this));
            for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class,
                    new AABB(getX() - 5.0D, getY() - 5.0D, getZ() - 5.0D,
                            getX() + 5.0D, getY() + 5.0D, getZ() + 5.0D))) {
                if (!(living instanceof EntityCyberCrab)) {
                    living.addEffect(new MobEffectInstance(ModEffects.RADIATION.get(), 10, 15));
                }
            }
        }
        super.aiStep();
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (level().isClientSide()) {
            return;
        }
        Vec3 origin = BulletKinematicsUtil.shooterSpawnPosition(this, true);
        Vec3 targetCenter = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedMk4LaunchPlan(
                LegacySednaRuntimeBulletConfigs.R762_FMJ, origin, targetCenter.subtract(origin), 10.0F, 0.0F, random);
        if (!plan.valid()) {
            return;
        }
        BulletProjectileEntity bullet = BulletProjectileEntity.fromLaunchPlan(level(), plan, this);
        bullet.overrideDamage = 50.0F;
        level().addFreshEntity(bullet);
        spawnFlameTrail(bullet);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.WEAPON_SAW_SHOOT.get(), SoundSource.HOSTILE,
                1.0F, 0.5F);
    }

    private void spawnFlameTrail(BulletProjectileEntity bullet) {
        Vec3 motion = bullet.getDeltaMovement();
        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_VANILLA);
        data.putString("mode", ParticleUtil.VANILLA_FLAME);
        data.putDouble("mX", motion.x * 0.3D);
        data.putDouble("mY", motion.y * 0.3D);
        data.putDouble("mZ", motion.z * 0.3D);
        ParticleUtil.spawnAux(level(), bullet.getX(), bullet.getY(), bullet.getZ(), data, 50.0D);
    }
}

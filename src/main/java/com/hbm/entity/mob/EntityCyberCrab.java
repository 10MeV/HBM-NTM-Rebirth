package com.hbm.entity.mob;

import api.hbm.entity.IRadiationImmune;
import com.hbm.ntm.blockentity.TeslaBlockEntity;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.network.HbmEntitySyncable;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge and shared runtime for the cyber crab family.
 */
@Deprecated(forRemoval = false)
public class EntityCyberCrab extends Monster implements net.minecraft.world.entity.monster.RangedAttackMob,
        IRadiationImmune, HbmEntitySyncable {
    private static final String TAG_TARGETS = "Targets";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";
    private static final Predicate<LivingEntity> TARGET_SELECTOR =
            target -> !(target instanceof EntityCyberCrab) && !(target instanceof Creeper);

    private final List<TeslaBlockEntity.TeslaTarget> targets = new ArrayList<>();
    private boolean exploded;

    public EntityCyberCrab(EntityType<? extends EntityCyberCrab> type, Level level) {
        super(type, level);
        getNavigation().setCanFloat(false);
    }

    public EntityCyberCrab(Level level) {
        this(ModEntityTypes.CYBER_CRAB.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.75D);
    }

    @Override
    protected void registerGoals() {
        if (!(this instanceof EntityTaintCrab)) {
            goalSelector.addGoal(0, new PanicGoal(this, 0.75D));
        }
        goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        goalSelector.addGoal(4, createRangedAttackGoal());
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, true,
                TARGET_SELECTOR));
    }

    protected RangedAttackGoal createRangedAttackGoal() {
        return new RangedAttackGoal(this, 0.5D, 60, 80, 15.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (ModDamageSources.isTau(source)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive() && (isInWaterOrRain() || isOnFire())) {
            hurt(damageSources().generic(), 10.0F);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        explodeOnDeath();
    }

    protected float deathExplosionRadius() {
        return this instanceof EntityTaintCrab ? 3.0F : 0.1F;
    }

    private void explodeOnDeath() {
        if (exploded || level().isClientSide()) {
            return;
        }
        exploded = true;
        level().explode(this, getX(), getY(), getZ(), deathExplosionRadius(), false, Level.ExplosionInteraction.NONE);
        discard();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_CYBERCRAB.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_CYBERCRAB.get();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return true;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (level().isClientSide()) {
            return;
        }
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.aimedLaunchPlan(
                LegacySednaRuntimeBulletConfigs.TAU_URANIUM, this, target, 1.6F, 2.0F, random);
        if (!plan.valid()) {
            return;
        }
        BulletProjectileEntity bullet = BulletProjectileEntity.fromLaunchPlan(level(), plan, this);
        bullet.overrideDamage = 3.0F;
        level().addFreshEntity(bullet);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.WEAPON_SAW_SHOOT.get(), SoundSource.HOSTILE,
                1.0F, 2.0F);
    }

    public List<TeslaBlockEntity.TeslaTarget> getTeslaTargets() {
        return List.copyOf(targets);
    }

    protected void setTeslaTargets(List<TeslaBlockEntity.TeslaTarget> nextTargets) {
        List<TeslaBlockEntity.TeslaTarget> next = nextTargets == null ? List.of() : nextTargets;
        if (sameTargets(targets, next)) {
            return;
        }
        targets.clear();
        targets.addAll(next);
        if (!level().isClientSide()) {
            ModMessages.syncEntityToTracking(this, this);
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeTargets(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readTargets(tag);
    }

    private void writeTargets(CompoundTag tag) {
        ListTag list = new ListTag();
        for (TeslaBlockEntity.TeslaTarget target : targets) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble(TAG_X, target.x());
            entry.putDouble(TAG_Y, target.y());
            entry.putDouble(TAG_Z, target.z());
            list.add(entry);
        }
        tag.put(TAG_TARGETS, list);
    }

    private void readTargets(CompoundTag tag) {
        targets.clear();
        ListTag list = tag.getList(TAG_TARGETS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            targets.add(new TeslaBlockEntity.TeslaTarget(
                    entry.getDouble(TAG_X), entry.getDouble(TAG_Y), entry.getDouble(TAG_Z)));
        }
    }

    private static boolean sameTargets(List<TeslaBlockEntity.TeslaTarget> current,
            List<TeslaBlockEntity.TeslaTarget> next) {
        if (current.size() != next.size()) {
            return false;
        }
        for (int i = 0; i < current.size(); i++) {
            TeslaBlockEntity.TeslaTarget a = current.get(i);
            TeslaBlockEntity.TeslaTarget b = next.get(i);
            if (Double.compare(a.x(), b.x()) != 0
                    || Double.compare(a.y(), b.y()) != 0
                    || Double.compare(a.z(), b.z()) != 0) {
                return false;
            }
        }
        return true;
    }
}

package com.hbm.ntm.entity.mob;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.ntm.api.entity.IRadiationImmune;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.RegistryObject;

public class EntityRADBeast extends Monster implements IRadiationImmune {
    private static final EntityDataAccessor<Integer> DATA_BEAM_TARGET_ID =
            SynchedEntityData.defineId(EntityRADBeast.class, EntityDataSerializers.INT);
    private static final int LEGACY_ATTACK_COOLDOWN = 20;
    private static final double RADIATION_ATTACK_RANGE = 30.0D;

    private float heightOffset = 0.5F;
    private int heightOffsetUpdateTime;
    private int legacyAttackCooldown;

    public EntityRADBeast(EntityType<? extends EntityRADBeast> type, Level level) {
        super(type, level);
        xpReward = 30;
        noCulling = true;
        getNavigation().setCanFloat(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 8.0D);
    }

    public EntityRADBeast makeLeader() {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(360.0D);
        heal(getMaxHealth());
        return this;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LegacyRADAttackGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_BEAM_TARGET_ID, 0);
    }

    @Override
    public void tick() {
        if (legacyAttackCooldown > 0) {
            legacyAttackCooldown--;
        }

        if (!level().isClientSide()) {
            serverLegacyTick();
        }

        if (!onGround() && getDeltaMovement().y < 0.0D) {
            setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }

        spawnLegacyParticles();
        super.tick();
    }

    private void serverLegacyTick() {
        if (isInWaterRainOrBubble()) {
            hurt(damageSources().drown(), 1.0F);
        }

        heightOffsetUpdateTime--;
        if (heightOffsetUpdateTime <= 0) {
            heightOffsetUpdateTime = 100;
            heightOffset = 0.5F + (float) random.nextGaussian() * 3.0F;
        }

        LivingEntity target = getTarget();
        if (target != null
                && target.getY() + target.getEyeHeight() > getY() + getEyeHeight() + heightOffset) {
            setDeltaMovement(getDeltaMovement().add(0.0D,
                    (0.30000001192092896D - getDeltaMovement().y) * 0.30000001192092896D, 0.0D));
            hasImpulse = true;
        }

        entityData.set(DATA_BEAM_TARGET_ID,
                target != null && legacyAttackCooldown < 10 ? target.getId() : 0);
    }

    private void spawnLegacyParticles() {
        if (getMaxHealth() <= 150.0F) {
            for (int i = 0; i < 6; i++) {
                ParticleUtil.spawnTownAura(level(),
                        getX() + (random.nextDouble() - 0.5D) * getBbWidth() * 1.5D,
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5D) * getBbWidth() * 1.5D);
            }
            if (random.nextInt(6) == 0) {
                ParticleUtil.spawnVanillaFlame(level(),
                        getX() + (random.nextDouble() - 0.5D) * getBbWidth(),
                        getY() + random.nextDouble() * getBbHeight() * 0.75D,
                        getZ() + (random.nextDouble() - 0.5D) * getBbWidth());
            }
            return;
        }

        double x = getX() + (random.nextDouble() - 0.5D) * getBbWidth();
        double y = getY() + random.nextDouble() * getBbHeight() * 0.75D;
        double z = getZ() + (random.nextDouble() - 0.5D) * getBbWidth();
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        } else {
            level().addParticle(ParticleTypes.LAVA, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    private void legacyAttack(LivingEntity target) {
        float distance = distanceTo(target);
        AABB targetBox = target.getBoundingBox();
        AABB selfBox = getBoundingBox();

        if (legacyAttackCooldown <= 0
                && distance < 2.0F
                && targetBox.maxY > selfBox.minY
                && targetBox.minY < selfBox.maxY) {
            legacyAttackCooldown = LEGACY_ATTACK_COOLDOWN;
            doHurtTarget(target);
            return;
        }

        if (distance >= RADIATION_ATTACK_RANGE) {
            return;
        }

        double deltaX = target.getX() - getX();
        double deltaZ = target.getZ() - getZ();

        if (legacyAttackCooldown == 0 && getTarget() != null) {
            ChunkRadiationManager.proxy.incrementRad(level(), Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()),
                    100.0F);
            target.hurt(ModDamageSources.radiation(level()), 16.0F);
            swing(InteractionHand.MAIN_HAND);
            playSound(randomGeigerSound(), getSoundVolume(), getVoicePitch());
            legacyAttackCooldown = LEGACY_ATTACK_COOLDOWN;
        }

        setYRot((float) (Mth.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F);
        yBodyRot = getYRot();
    }

    public Entity getUnfortunateSoul() {
        int id = entityData.get(DATA_BEAM_TARGET_ID);
        return id == 0 ? null : level().getEntity(id);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(ModDamageSources.RADIATION)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return randomGeigerSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.STEP_IRON.get();
    }

    private SoundEvent randomGeigerSound() {
        return switch (1 + random.nextInt(6)) {
            case 2 -> ModSounds.ITEM_GEIGER_2.get();
            case 3 -> ModSounds.ITEM_GEIGER_3.get();
            case 4 -> ModSounds.ITEM_GEIGER_4.get();
            case 5 -> ModSounds.ITEM_GEIGER_5.get();
            case 6 -> ModSounds.ITEM_GEIGER_6.get();
            default -> ModSounds.ITEM_GEIGER_1.get();
        };
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (!recentlyHit) {
            return;
        }

        if (looting > 0) {
            dropLegacyItem("nugget_polonium", looting);
        }

        boolean wet = isInWaterRainOrBubble();
        int count = random.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            switch (random.nextInt(3)) {
                case 0 -> dropLegacyItem(wet ? "waste_uranium" : "rod_zirnox_uranium_fuel_depleted", wet ? 2 : 1);
                case 1 -> dropLegacyItem(wet ? "waste_mox" : "rod_zirnox_mox_fuel_depleted", wet ? 2 : 1);
                case 2 -> dropLegacyItem(wet ? "waste_plutonium" : "rod_zirnox_plutonium_fuel_depleted", wet ? 2 : 1);
                default -> {
                }
            }
        }
    }

    private void dropLegacyItem(String name, int count) {
        if (count <= 0) {
            return;
        }
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item != null) {
            spawnAtLocation(new ItemStack(item.get(), count));
        }
    }

    private static final class LegacyRADAttackGoal extends Goal {
        private final EntityRADBeast mob;
        private int repathDelay;

        private LegacyRADAttackGoal(EntityRADBeast mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) {
                return;
            }

            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceSqr = mob.distanceToSqr(target);
            if (--repathDelay <= 0) {
                repathDelay = 10;
                if (distanceSqr > 4.0D) {
                    mob.getNavigation().moveTo(target, 1.0D);
                } else {
                    mob.getNavigation().stop();
                }
            }
            mob.legacyAttack(target);
        }
    }
}

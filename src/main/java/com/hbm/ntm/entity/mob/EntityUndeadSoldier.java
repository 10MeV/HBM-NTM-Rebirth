package com.hbm.ntm.entity.mob;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/** Direct 1.20.1 migration of 1.7.10's armed zombie/skeleton soldier. */
public final class EntityUndeadSoldier extends Monster {
    private static final EntityDataAccessor<Boolean> SKELETON_TYPE =
            SynchedEntityData.defineId(EntityUndeadSoldier.class, EntityDataSerializers.BOOLEAN);

    public EntityUndeadSoldier(EntityType<? extends EntityUndeadSoldier> type, Level level) {
        super(type, level);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HAND || slot.getType() == EquipmentSlot.Type.ARMOR) {
                setDropChance(slot, 0.0F);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKELETON_TYPE, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData spawnData, net.minecraft.nbt.CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        entityData.set(SKELETON_TYPE, getRandom().nextBoolean());
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.TAURUN_HELMET.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.TAURUN_PLATE.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.TAURUN_LEGS.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.TAURUN_BOOTS.get()));
        setItemSlot(EquipmentSlot.MAINHAND, switch (getRandom().nextInt(5)) {
            case 0 -> new ItemStack(ModItems.GUN_HEAVY_REVOLVER.get());
            case 1 -> new ItemStack(ModItems.GUN_LIGHT_REVOLVER.get());
            case 2 -> new ItemStack(ModItems.GUN_CARBINE.get());
            case 3 -> new ItemStack(ModItems.GUN_MARESLEG.get());
            default -> new ItemStack(ModItems.GUN_GREASEGUN.get());
        });
        return result;
    }

    public boolean isLegacySkeleton() {
        return entityData.get(SKELETON_TYPE);
    }

    @Override
    protected SoundEvent getAmbientSound() { return isLegacySkeleton() ? SoundEvents.SKELETON_AMBIENT : SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return isLegacySkeleton() ? SoundEvents.SKELETON_HURT : SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return isLegacySkeleton() ? SoundEvents.SKELETON_DEATH : SoundEvents.ZOMBIE_DEATH; }

    @Override
    public MobType getMobType() { return MobType.UNDEAD; }
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) { return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance); }
}

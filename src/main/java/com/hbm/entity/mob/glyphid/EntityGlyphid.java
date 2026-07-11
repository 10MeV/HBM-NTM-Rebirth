package com.hbm.entity.mob.glyphid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated(forRemoval = false)
public class EntityGlyphid extends Monster {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_INFECTED = 1;
    public static final int TYPE_RADIOACTIVE = 2;

    public static final int DW_WALL = 16;
    public static final int DW_ARMOR = 17;
    public static final int DW_SUBTYPE = 18;

    private static final EntityDataAccessor<Boolean> DATA_WALL =
            SynchedEntityData.defineId(EntityGlyphid.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_ARMOR =
            SynchedEntityData.defineId(EntityGlyphid.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_SUBTYPE =
            SynchedEntityData.defineId(EntityGlyphid.class, EntityDataSerializers.BYTE);

    protected boolean hasHome;
    protected int homeX;
    protected int homeY;
    protected int homeZ;

    public EntityGlyphid(EntityType<? extends EntityGlyphid> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_WALL, false);
        entityData.define(DATA_ARMOR, (byte) 0b11111);
        entityData.define(DATA_SUBTYPE, (byte) TYPE_NORMAL);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && !hasHome) {
            homeX = blockPosition().getX();
            homeY = blockPosition().getY();
            homeZ = blockPosition().getZ();
            hasHome = true;
        }
    }

    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid.png");
    }

    public float getScale() {
        return 1.0F;
    }

    public int getSubtype() {
        return entityData.get(DATA_SUBTYPE);
    }

    public void setSubtype(int subtype) {
        entityData.set(DATA_SUBTYPE, (byte) subtype);
    }

    public byte getArmorBits() {
        return entityData.get(DATA_ARMOR);
    }

    public void setArmorBits(byte armor) {
        entityData.set(DATA_ARMOR, (byte) (armor & 0b11111));
    }

    public int getGlyphidArmor() {
        byte armor = getArmorBits();
        int total = 0;
        for (int i = 0; i < 5; i++) {
            if ((armor & (1 << i)) != 0) {
                total++;
            }
        }
        return total;
    }

    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.6D, 2.0D), 100.0D);
    }

    public void breakOffArmor() {
        byte armor = getArmorBits();
        List<Integer> indices = new ArrayList<>(List.of(0, 1, 2, 3, 4));
        Collections.shuffle(indices);
        for (int index : indices) {
            byte bit = (byte) (1 << index);
            if ((armor & bit) != 0) {
                setArmorBits((byte) (armor & ~bit));
                break;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity sourceEntity = source.getEntity();
        if (sourceEntity instanceof EntityGlyphid) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide() && isArmorBroken(amount)) {
            breakOffArmor();
        }
        return hurt;
    }

    public boolean attackSuperclass(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    public boolean getCanSpawnHere() {
        return checkSpawnRules(level(), MobSpawnType.NATURAL) && checkSpawnObstruction(level());
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return super.checkSpawnRules(level, reason) && isValidLightLevel(level, blockPosition());
    }

    public boolean isValidLightLevel() {
        return isValidLightLevel(level(), blockPosition());
    }

    protected boolean isValidLightLevel(LevelAccessor level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (!recentlyHit || random.nextInt(2) != 0) {
            return;
        }
        RegistryObject<Item> item = isOnFire() ? ModItems.GLYPHID_MEAT_GRILLED : ModItems.GLYPHID_MEAT;
        int count = ((int) getScale() * 2) + looting;
        if (count > 0) {
            spawnAtLocation(new ItemStack(item.get(), count));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("armor", getArmorBits());
        tag.putByte("subtype", (byte) getSubtype());
        tag.putBoolean("hasHome", hasHome);
        tag.putInt("homeX", homeX);
        tag.putInt("homeY", homeY);
        tag.putInt("homeZ", homeZ);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setArmorBits(tag.getByte("armor"));
        setSubtype(tag.getByte("subtype"));
        hasHome = tag.getBoolean("hasHome");
        homeX = tag.getInt("homeX");
        homeY = tag.getInt("homeY");
        homeZ = tag.getInt("homeZ");
    }

    public static boolean canSpawnAt(ServerLevelAccessor level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }
}

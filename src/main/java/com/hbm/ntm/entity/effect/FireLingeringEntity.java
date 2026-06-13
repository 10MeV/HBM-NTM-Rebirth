package com.hbm.ntm.entity.effect;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class FireLingeringEntity extends Entity {
    public static final int TYPE_DIESEL = 0;
    public static final int TYPE_BALEFIRE = 1;
    public static final int TYPE_PHOSPHORUS = 2;
    public static final int TYPE_OXY = 3;
    public static final int TYPE_BLACK = 4;

    private static final EntityDataAccessor<Integer> FIRE_TYPE =
            SynchedEntityData.defineId(FireLingeringEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AREA_WIDTH =
            SynchedEntityData.defineId(FireLingeringEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AREA_HEIGHT =
            SynchedEntityData.defineId(FireLingeringEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_AGE =
            SynchedEntityData.defineId(FireLingeringEntity.class, EntityDataSerializers.INT);

    public FireLingeringEntity(EntityType<? extends FireLingeringEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public FireLingeringEntity(Level level) {
        this(ModEntityTypes.FIRE_LINGERING.get(), level);
    }

    public static FireLingeringEntity create(Level level, double x, double y, double z, int fireType,
            float width, float height, int duration) {
        FireLingeringEntity entity = new FireLingeringEntity(level);
        entity.setPos(x, y, z);
        entity.setArea(width, height);
        entity.setDuration(duration);
        entity.setFireType(fireType);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            spawnClientFlames();
            return;
        }
        if (tickCount >= getMaxAge()) {
            discard();
            return;
        }
        affectEntities();
    }

    public FireLingeringEntity setArea(float width, float height) {
        entityData.set(AREA_WIDTH, Math.max(0.0F, width));
        entityData.set(AREA_HEIGHT, Math.max(0.0F, height));
        return this;
    }

    public FireLingeringEntity setDuration(int duration) {
        entityData.set(MAX_AGE, Math.max(1, duration));
        return this;
    }

    public FireLingeringEntity setFireType(int fireType) {
        entityData.set(FIRE_TYPE, Math.max(TYPE_DIESEL, Math.min(TYPE_BLACK, fireType)));
        return this;
    }

    public int getFireType() {
        return entityData.get(FIRE_TYPE);
    }

    public float getAreaWidth() {
        return entityData.get(AREA_WIDTH);
    }

    public float getAreaHeight() {
        return entityData.get(AREA_HEIGHT);
    }

    public int getMaxAge() {
        return entityData.get(MAX_AGE);
    }

    private void affectEntities() {
        float width = getAreaWidth();
        float height = getAreaHeight();
        AABB area = new AABB(getX() - width / 2.0F, getY(), getZ() - width / 2.0F,
                getX() + width / 2.0F, getY() + height, getZ() + width / 2.0F);
        List<Entity> affected = level().getEntities(this, area, Entity::isAlive);
        for (Entity entity : affected) {
            if (entity instanceof LivingEntity living) {
                affectLiving(living);
            } else {
                entity.setSecondsOnFire(4);
            }
        }
    }

    private void affectLiving(LivingEntity living) {
        switch (getFireType()) {
            case TYPE_BALEFIRE -> HbmLivingProperties.ensureBalefire(living, 100);
            case TYPE_PHOSPHORUS -> HbmLivingProperties.ensureFire(living, 300);
            case TYPE_BLACK -> {
                if (HbmLivingProperties.getBlackFire(living) < 200) {
                    HbmLivingProperties.setBlackFire(living, 200);
                } else {
                    HbmLivingProperties.addBlackFire(living, 5);
                }
            }
            default -> HbmLivingProperties.ensureFire(living, 60);
        }
    }

    private void spawnClientFlames() {
        float width = getAreaWidth();
        float height = getAreaHeight();
        int count = width >= 5.0F ? 2 : 1;
        for (int i = 0; i < count; i++) {
            double x = getX() - width / 2.0D + random.nextDouble() * width;
            double z = getZ() - width / 2.0D + random.nextDouble() * width;
            Vec3 up = new Vec3(x, getY() + height, z);
            Vec3 down = new Vec3(x, getY() - height, z);
            HitResult hit = level().clip(new ClipContext(up, down, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            if (hit.getType() == HitResult.Type.BLOCK) {
                down = hit.getLocation();
            }
            ParticleUtil.spawnLegacyFlameEffectClient(level(), x, down.y, z, flameMeta());
        }
    }

    private int flameMeta() {
        return switch (getFireType()) {
            case TYPE_BALEFIRE -> ParticleUtil.FLAMETHROWER_META_BALEFIRE;
            case TYPE_BLACK -> ParticleUtil.FLAMETHROWER_META_BLACK;
            case TYPE_OXY -> ParticleUtil.FLAMETHROWER_META_OXY;
            default -> ParticleUtil.FLAMETHROWER_META_FIRE;
        };
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(FIRE_TYPE, TYPE_DIESEL);
        entityData.define(AREA_WIDTH, 0.0F);
        entityData.define(AREA_HEIGHT, 0.0F);
        entityData.define(MAX_AGE, 150);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

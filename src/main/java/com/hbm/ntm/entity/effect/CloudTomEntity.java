package com.hbm.ntm.entity.effect;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Direct modern entity counterpart of 1.7.10 {@code EntityCloudTom}. */
public final class CloudTomEntity extends Entity {
    private static final EntityDataAccessor<Integer> MAX_AGE =
            SynchedEntityData.defineId(CloudTomEntity.class, EntityDataSerializers.INT);

    private int age;
    private boolean legacyLargeCloud;

    public CloudTomEntity(EntityType<? extends CloudTomEntity> type, Level level) {
        super(type, level);
        // EntityCloudTom(World) is the legacy no-argument constructor used by
        // deserialization. Its only render-physics flag is ignoreFrustumCheck.
        noCulling = true;
        // The registered modern EntityType has the live TOM cloud's 20x40
        // dimensions.  The legacy deserialization constructor itself called
        // setSize(1, 4), so rebuild the bounding box now rather than leaving
        // a saved cloud at the registry default until a later state change.
        refreshDimensions();
    }

    public CloudTomEntity(Level level, int maxAge) {
        this(ModEntityTypes.CLOUD_TOM.get(), level);
        // EntityTom creates EntityCloudTom(World, 500). Unlike the legacy
        // no-argument constructor this path never enables ignoreFrustumCheck,
        // and neither constructor opts into no-physics.
        noCulling = false;
        legacyLargeCloud = true;
        refreshDimensions();
        setMaxAge(maxAge);
    }

    public static CloudTomEntity create(Level level, double x, double y, double z, int maxAge) {
        CloudTomEntity cloud = new CloudTomEntity(level, maxAge);
        cloud.setPos(x, y, z);
        return cloud;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // EntityCloudTom(World) calls setSize(1, 4), while the only live TOM
        // spawn path, EntityCloudTom(World, maxAge), calls setSize(20, 40).
        // The flag deliberately is not saved: an old NBT reload runs the
        // former constructor and returns to its smaller default shape.
        return EntityDimensions.scalable(legacyLargeCloud ? 20.0F : 1.0F,
                legacyLargeCloud ? 40.0F : 4.0F);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MAX_AGE, 100);
    }

    @Override
    public void tick() {
        // EntityCloudTom owns the complete 1.7.10 update body and never calls
        // Entity#onUpdate.  In particular, do not advance the modern parent
        // tick counter here: its visual lifetime is the explicit age field.
        age++;
        level().setSkyFlashTime(2);
        if (age >= maxAge()) {
            discard();
        }
    }

    public int age() {
        return age;
    }

    public void setMaxAge(int maxAge) {
        entityData.set(MAX_AGE, maxAge);
    }

    public int maxAge() {
        return entityData.get(MAX_AGE);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getShort("age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("age", (short) age);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

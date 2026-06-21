package com.hbm.ntm.entity.effect;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class EmpBlastEntity extends Entity {
    private static final EntityDataAccessor<Integer> MAX_AGE =
            SynchedEntityData.defineId(EmpBlastEntity.class, EntityDataSerializers.INT);

    private int age;
    private float scale;

    public EmpBlastEntity(EntityType<? extends EmpBlastEntity> type, Level level) {
        super(type, level);
        noCulling = true;
        noPhysics = true;
        setNoGravity(true);
    }

    public EmpBlastEntity(Level level, int maxAge) {
        this(ModEntityTypes.EMP_BLAST.get(), level);
        setMaxAge(maxAge);
    }

    public static EmpBlastEntity create(Level level, double x, double y, double z, int maxAge) {
        EmpBlastEntity entity = new EmpBlastEntity(level, maxAge);
        entity.setPos(x, y, z);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (!level().isClientSide() && age >= getMaxAge()) {
            age = 0;
            discard();
        }
        scale++;
    }

    public void setMaxAge(int maxAge) {
        entityData.set(MAX_AGE, Math.max(1, maxAge));
    }

    public int getMaxAge() {
        return Math.max(1, entityData.get(MAX_AGE));
    }

    public int getAge() {
        return age;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 65536.0D;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MAX_AGE, 100);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getShort("age");
        scale = tag.getShort("scale");
        if (tag.contains("maxAge")) {
            setMaxAge(tag.getInt("maxAge"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("age", (short) age);
        tag.putShort("scale", (short) scale);
        tag.putInt("maxAge", getMaxAge());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

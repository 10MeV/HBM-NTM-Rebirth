package com.hbm.ntm.entity.logic;

import com.hbm.ntm.explosion.ExplosionBalefire;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class BalefireExplosionEntity extends ExplosionChunkLoadingEntity {
    private int age;
    private int destructionRange;
    private int speed = 1;
    private boolean initialized;
    private ExplosionBalefire explosion;

    public BalefireExplosionEntity(EntityType<? extends BalefireExplosionEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public BalefireExplosionEntity(Level level, int destructionRange) {
        this(ModEntityTypes.BALEFIRE_EXPLOSION.get(), level);
        this.destructionRange = destructionRange;
    }

    public static BalefireExplosionEntity create(Level level, double x, double y, double z, int destructionRange) {
        BalefireExplosionEntity entity = new BalefireExplosionEntity(level, destructionRange);
        entity.setPos(x, y, z);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        if (destructionRange <= 0) {
            discard();
            return;
        }

        forceCenterChunk();
        loadChunk((int) Math.floor(getX() / 16.0D), (int) Math.floor(getZ() / 16.0D));

        if (!initialized) {
            initializeExplosion();
        }

        speed++;
        boolean complete = false;
        for (int i = 0; i < speed && !complete; i++) {
            complete = explosion.update();
        }

        if (complete) {
            discard();
            return;
        }

        ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), destructionRange * 2.0D);
        age++;
    }

    private void initializeExplosion() {
        explosion = new ExplosionBalefire((int) getX(), (int) getY(), (int) getZ(), level(), destructionRange);
        initialized = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("age");
        destructionRange = tag.getInt("destructionRange");
        speed = Math.max(1, tag.getInt("speed"));
        initialized = tag.getBoolean("did");
        readChunkLoader(tag);

        if (initialized && destructionRange > 0) {
            initializeExplosion();
            explosion.readFromNbt(tag, "exp_");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", age);
        tag.putInt("destructionRange", destructionRange);
        tag.putInt("speed", speed);
        tag.putBoolean("did", initialized);
        saveChunkLoader(tag);
        if (explosion != null) {
            explosion.saveToNbt(tag, "exp_");
        }
    }
}

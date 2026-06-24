package com.hbm.ntm.entity.logic;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.HbmCommonConfig;
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
    private boolean expiredFromSave;
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

        if (expiredFromSave) {
            discard();
            return;
        }

        if (destructionRange <= 0) {
            discard();
            return;
        }

        forceCenterChunk();

        if (!initialized) {
            initializeExplosion(true);
        }

        speed++;
        boolean complete = false;
        for (int i = 0; i < speed && !complete; i++) {
            loadChunkForCurrentColumn();
            complete = explosion.update();
        }

        if (complete) {
            discard();
            return;
        }

        ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), destructionRange * 2.0D);
        age++;
    }

    private void initializeExplosion(boolean logInitialization) {
        if (logInitialization && extendedLoggingEnabled()) {
            HbmNtm.LOGGER.info("[NUKE] Initialized BF explosion at {} / {} / {} with strength {}!",
                    getX(), getY(), getZ(), destructionRange);
        }
        explosion = new ExplosionBalefire((int) getX(), (int) getY(), (int) getZ(), level(), destructionRange);
        initialized = true;
    }

    private void loadChunkForCurrentColumn() {
        loadChunk(Math.floorDiv(explosion.currentWorldX(), 16), Math.floorDiv(explosion.currentWorldZ(), 16));
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("age");
        destructionRange = tag.getInt("destructionRange");
        speed = tag.getInt("speed");
        initialized = tag.getBoolean("did");
        readChunkLoader(tag);
        expiredFromSave = shouldExpireFromSave(tag);

        if (initialized && !expiredFromSave && destructionRange > 0) {
            initializeExplosion(false);
            explosion.readFromNbt(tag, "exp_");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", age);
        tag.putInt("destructionRange", destructionRange);
        tag.putInt("speed", speed);
        tag.putBoolean("did", initialized);
        tag.putLong("milliTime", System.currentTimeMillis());
        saveChunkLoader(tag);
        if (explosion != null) {
            explosion.saveToNbt(tag, "exp_");
        }
    }

    private static boolean extendedLoggingEnabled() {
        return HbmCommonConfig.extendedLoggingEnabled();
    }
}

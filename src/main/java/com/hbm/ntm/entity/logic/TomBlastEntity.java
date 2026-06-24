package com.hbm.ntm.entity.logic;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.ExplosionTom;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class TomBlastEntity extends ExplosionChunkLoadingEntity {
    private int age;
    private int destructionRange;
    private ExplosionTom explosion;
    private int speed = 1;
    private boolean initialized;
    private boolean expiredFromSave;

    public TomBlastEntity(EntityType<? extends TomBlastEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public TomBlastEntity(Level level, int destructionRange) {
        this(ModEntityTypes.TOM_BLAST.get(), level);
        this.destructionRange = destructionRange;
    }

    public static TomBlastEntity create(Level level, double x, double y, double z, int destructionRange) {
        TomBlastEntity entity = new TomBlastEntity(level, destructionRange);
        entity.setPos(x, y, z);
        entity.loadChunk((int) Math.floor(x / 16.0D), (int) Math.floor(z / 16.0D));
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        if (expiredFromSave || destructionRange <= 0) {
            discard();
            return;
        }

        forceCenterChunk();

        if (!initialized) {
            initializeExplosion(true);
        }

        speed += 1;
        boolean complete = false;
        for (int i = 0; i < speed && !complete; i++) {
            loadChunkForCurrentColumn();
            complete = explosion.update();
        }

        if (complete) {
            markTomImpact();
            discard();
            return;
        }

        if (random.nextInt(5) == 0) {
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "random.explode",
                    SoundSource.BLOCKS, 10000.0F, 0.8F + random.nextFloat() * 0.2F);
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
                10000.0F, 0.8F + random.nextFloat() * 0.2F);
        ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), destructionRange * 2.0D);
        age++;
    }

    private void initializeExplosion(boolean logInitialization) {
        if (logInitialization && HbmCommonConfig.extendedLoggingEnabled()) {
            HbmNtm.LOGGER.info("[NUKE] Initialized TOM explosion at {} / {} / {} with strength {}!",
                    getX(), getY(), getZ(), destructionRange);
        }
        explosion = new ExplosionTom((int) getX(), (int) getY(), (int) getZ(), level(), destructionRange);
        initialized = true;
    }

    private void loadChunkForCurrentColumn() {
        loadChunk(Math.floorDiv(explosion.currentWorldX(), 16), Math.floorDiv(explosion.currentWorldZ(), 16));
    }

    private void markTomImpact() {
        if (level() instanceof ServerLevel serverLevel) {
            TomImpactSavedData.forWorld(serverLevel).beginTomImpactFire();
        }
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

        if (!expiredFromSave && destructionRange > 0) {
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
}

package com.hbm.ntm.entity.effect;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class VortexEntity extends BlackHoleEntity {
    private static final float DEFAULT_SHRINK_RATE = 0.0025F;
    private float shrinkRate = DEFAULT_SHRINK_RATE;

    public VortexEntity(EntityType<? extends VortexEntity> type, Level level) {
        super(type, level);
    }

    public VortexEntity(Level level) {
        this(ModEntityTypes.VORTEX.get(), level);
    }

    public VortexEntity(Level level, float size) {
        this(level);
        setSize(size);
    }

    public VortexEntity setShrinkRate(float shrinkRate) {
        this.shrinkRate = Math.max(0.0F, shrinkRate);
        return this;
    }

    public float shrinkRate() {
        return shrinkRate;
    }

    @Override
    public void tick() {
        setSize(getSize() - shrinkRate);
        if (getSize() <= 0.0F) {
            discard();
            return;
        }
        super.tick();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        shrinkRate = tag.contains("shrinkRate") ? tag.getFloat("shrinkRate") : DEFAULT_SHRINK_RATE;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("shrinkRate", shrinkRate);
    }
}

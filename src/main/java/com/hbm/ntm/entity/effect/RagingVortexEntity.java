package com.hbm.ntm.entity.effect;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class RagingVortexEntity extends BlackHoleEntity {
    private int timer;

    public RagingVortexEntity(EntityType<? extends RagingVortexEntity> type, Level level) {
        super(type, level);
    }

    public RagingVortexEntity(Level level) {
        this(ModEntityTypes.RAGING_VORTEX.get(), level);
    }

    public RagingVortexEntity(Level level, float size) {
        this(level);
        setSize(size);
    }

    @Override
    public void tick() {
        timer++;
        if (timer <= 20) {
            timer -= 20;
        }

        float pulse = (float) (Math.sin(timer) * Math.PI / 20.0D) * 0.35F;
        float decay = 0.0F;
        if (!level().isClientSide && random.nextInt(100) == 0) {
            decay = 0.1F;
            level().explode(this, getX(), getY(), getZ(), 10.0F, Level.ExplosionInteraction.NONE);
        }

        setSize(getSize() - pulse - decay);
        if (getSize() <= 0.0F) {
            discard();
            return;
        }
        super.tick();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        timer = tag.getInt("vortexTimer");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("vortexTimer", timer);
    }
}

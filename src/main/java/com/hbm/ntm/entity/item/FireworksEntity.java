package com.hbm.ntm.entity.item;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Flight, explosion and NBT contract of legacy {@code EntityFireworks}. */
public final class FireworksEntity extends Entity {
    private int color;
    private int character;

    public FireworksEntity(EntityType<? extends FireworksEntity> type, Level level) {
        super(type, level);
    }

    public FireworksEntity(EntityType<? extends FireworksEntity> type, Level level, double x, double y, double z,
            int color, char character) {
        this(type, level);
        setPos(x, y, z);
        this.color = color;
        this.character = character;
    }

    @Override
    public void tick() {
        super.tick();
        move(MoverType.SELF, new Vec3(0.0D, 3.0D, 0.0D));
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, -0.3D, 0.0D);
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0.0D, -0.2D, 0.0D);
            return;
        }
        if (tickCount > 30) {
            LegacySoundPlayer.playLegacyFireworksBlast(level(), position(), 20.0F,
                    1.0F + random.nextFloat() * 0.2F);
            discard();
            ParticleUtil.spawnFireworks(level(), getX(), getY(), getZ(), color, (char) character);
        }
    }

    @Override
    protected void defineSynchedData() {
        // The legacy renderer has no color/character-dependent geometry; the server-only aux payload carries both.
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        character = tag.getInt("char");
        color = tag.getInt("color");
        tickCount = tag.getInt("ticksExisted");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("char", character);
        tag.putInt("color", color);
        tag.putInt("ticksExisted", tickCount);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}

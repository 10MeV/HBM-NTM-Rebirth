package com.hbm.ntm.entity.logic;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class DeathBlastEntity extends Entity {
    public static final int MAX_AGE = 60;
    private static final double DAMAGE_RADIUS = 40.0D;
    private static final float MAX_DAMAGE = 250.0F;

    public DeathBlastEntity(EntityType<? extends DeathBlastEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public DeathBlastEntity(Level level) {
        this(ModEntityTypes.DEATH_BLAST.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount >= MAX_AGE) {
            discard();
            applyDamageOnlyBlast();
            ParticleUtil.spawnNuclearBurstVisual(level(), getX(), getY() + 0.5D, getZ(), ParticleUtil.TYPE_MUKE, false);
            LegacySoundPlayer.playLegacyMukeExplosion(level(), getX(), getY(), getZ(), 25.0F, 0.9F);
        }
    }

    private void applyDamageOnlyBlast() {
        AABB bounds = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(DAMAGE_RADIUS);
        List<Entity> targets = level().getEntities(this, bounds,
                entity -> entity.isAlive() && !entity.isSpectator());
        for (Entity target : targets) {
            double distance = target.distanceTo(this);
            if (distance > DAMAGE_RADIUS) {
                continue;
            }
            float damage = (float) ((1.0D - distance / DAMAGE_RADIUS) * MAX_DAMAGE);
            if (damage > 0.0F) {
                EntityDamageUtil.attackEntityFromNt(target,
                        ModDamageSources.source(level(), ModDamageSources.LASER, this),
                        damage, true);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 25000.0D;
    }
}

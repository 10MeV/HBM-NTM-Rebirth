package com.hbm.ntm.entity.logic;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

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
            level().playSound(null, getX(), getY(), getZ(), ModSounds.WEAPON_MUKE_EXPLOSION.get(),
                    SoundSource.BLOCKS, 15.0F, 0.9F);
        }
    }

    private void applyDamageOnlyBlast() {
        AABB bounds = new AABB(
                getX() - DAMAGE_RADIUS,
                getY() - DAMAGE_RADIUS,
                getZ() - DAMAGE_RADIUS,
                getX() + DAMAGE_RADIUS,
                getY() + DAMAGE_RADIUS,
                getZ() + DAMAGE_RADIUS);
        Vec3 origin = position();
        for (Entity entity : level().getEntities(this, bounds, target -> target.isAlive() && !target.isSpectator())) {
            if (entity instanceof Player player && player.isCreative()) {
                continue;
            }
            double distance = entity.distanceToSqr(origin);
            if (distance > DAMAGE_RADIUS * DAMAGE_RADIUS) {
                continue;
            }
            double linearDistance = Math.sqrt(distance);
            float damage = (float) (MAX_DAMAGE * (DAMAGE_RADIUS - linearDistance) / DAMAGE_RADIUS);
            if (damage <= 0.0F) {
                continue;
            }
            if (entity instanceof LivingEntity) {
                EntityDamageUtil.attackEntityFromNt(entity,
                        ModDamageSources.source(level(), ModDamageSources.LASER, this), damage, true, true,
                        0.0D, 100.0F, 0.0F);
            } else {
                entity.hurt(ModDamageSources.source(level(), ModDamageSources.LASER, this), damage);
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

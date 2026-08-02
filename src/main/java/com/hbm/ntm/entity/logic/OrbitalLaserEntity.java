package com.hbm.ntm.entity.logic;

import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Source-backed port of 1.7.10 {@code EntityOrbitalLaser}. */
public final class OrbitalLaserEntity extends Entity {
    public static final int MAX_AGE = 5;

    public OrbitalLaserEntity(EntityType<? extends OrbitalLaserEntity> type, Level level) {
        super(type, level);
        // Legacy set ignoreFrustumCheck.  The project-wide 512-block renderer
        // cutoff is still enforced below rather than replaced with noCulling.
        noCulling = true;
    }

    public OrbitalLaserEntity(Level level) {
        this(ModEntityTypes.ORBITAL_LASER.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        // EntityOrbitalLaser#onUpdate did not call super.onUpdate().  Retain
        // that source timing rather than introducing modern movement/age state.
        if (!level().isClientSide && tickCount >= MAX_AGE) {
            discard();
        }
    }

    /** The old satellite calls this synchronously before adding the entity. */
    public void explode() {
        WeaponExplosionUtil.orbitalLaser(level(), getX(), getY(), getZ(), this).explode();
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
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
}

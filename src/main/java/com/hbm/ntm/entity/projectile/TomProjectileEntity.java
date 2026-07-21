package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.entity.effect.CloudTomEntity;
import com.hbm.ntm.entity.logic.TomBlastEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Direct modern entity counterpart of 1.7.10 {@code EntityTom}. */
public final class TomProjectileEntity extends Entity {
    public static final int BLAST_RANGE = 600;
    public static final int CLOUD_MAX_AGE = 500;

    public TomProjectileEntity(EntityType<? extends TomProjectileEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public TomProjectileEntity(Level level) {
        this(ModEntityTypes.TOM_PROJECTILE.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        // EntityTom overrides onUpdate without invoking EntityThrowable or Entity.
        // Besides avoiding the thrown-projectile raytrace path, that leaves its
        // ticksExisted field unchanged: the old `% 100 == 0` alarm condition is
        // true every tick.  Do not normalize this into modern base ticking.
        xo = getX();
        yo = getY();
        zo = getZ();

        // EntityTom owns movement instead of EntityThrowable's raytrace path.
        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (tickCount % 100 == 0) {
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "hbm:alarm.chime",
                    SoundSource.BLOCKS, 10000.0F, 1.0F);
        }

        setDeltaMovement(getDeltaMovement().x, -0.5D, getDeltaMovement().z);
        BlockPos current = new BlockPos((int) getX(), (int) getY(), (int) getZ());
        if (!level().isEmptyBlock(current) || getY() < 10.0D) {
            if (!level().isClientSide) {
                level().addFreshEntity(TomBlastEntity.create(level(), getX(), getY(), getZ(), BLAST_RANGE));
                level().addFreshEntity(CloudTomEntity.create(level(), getX(), getY(), getZ(), CLOUD_MAX_AGE));
            }
            discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // EntityTom stores no projectile-local state.
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // EntityTom stores no projectile-local state.
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

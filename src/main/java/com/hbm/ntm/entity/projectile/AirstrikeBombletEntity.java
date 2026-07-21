package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.ntm.entity.logic.AirstrikeBomberEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** The non-colliding 1.7.10 Zeta bomblet: it detonates only after entering a non-air block. */
public class AirstrikeBombletEntity extends Entity {
    private static final EntityDataAccessor<Integer> PAYLOAD_TYPE =
            SynchedEntityData.defineId(AirstrikeBombletEntity.class, EntityDataSerializers.INT);

    public AirstrikeBombletEntity(EntityType<? extends AirstrikeBombletEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public AirstrikeBombletEntity(Level level) {
        this(ModEntityTypes.AIRSTRIKE_BOMBLET.get(), level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
        setDeltaMovement(getDeltaMovement().multiply(0.99D, 1.0D, 0.99D).add(0.0D, -0.05D, 0.0D));
        updateRotationFromMotion();
        if (!level().getBlockState(blockPosition()).isAir()) {
            detonate();
        }
    }

    private void detonate() {
        int type = payloadType();
        if (type == AirstrikeBomberEntity.TYPE_CARPET) {
            ExplosionLarge.explode(level(), getX() + 0.5D, getY() + 1.5D, getZ() + 0.5D,
                    4.0F, true, false, false, this);
        } else if (type == AirstrikeBomberEntity.TYPE_NAPALM) {
            ExplosionLarge.explodeFire(level(), getX() + 0.5D, getY() + 1.5D, getZ() + 0.5D,
                    4.0F, true, false, false, this);
        } else if (type == AirstrikeBomberEntity.TYPE_CHLORINE) {
            LegacySoundPlayer.playSoundEffectRandomPitch(level(), getX() + 0.5D, getY() + 0.5D, getZ() + 0.5D,
                    "random.fizz", SoundSource.BLOCKS, 5.0F, 2.6F, 0.8F);
            MistEntity mist = MistEntity.create(level(), getX() - getDeltaMovement().x, getY() - getDeltaMovement().y,
                    getZ() - getDeltaMovement().z, HbmFluids.CHLORINE, 15.0F, 7.5F, 150);
            level().addFreshEntity(mist);
        } else if (type == AirstrikeBomberEntity.TYPE_ATOMIC) {
            level().addFreshEntity(NukeExplosionMk5Entity.create(level(), (int) (BombConfig.fatmanRadius() * 1.5D),
                    getX(), getY(), getZ()));
            LegacySoundPlayer.playLegacyMukeExplosion(level(), getX(), getY(), getZ());
        }
        discard();
    }

    public void setPayloadType(int type) {
        entityData.set(PAYLOAD_TYPE, Mth.clamp(type, AirstrikeBomberEntity.TYPE_CARPET, AirstrikeBomberEntity.TYPE_ATOMIC));
    }

    public int payloadType() {
        return entityData.get(PAYLOAD_TYPE);
    }

    public void updateRotationFromMotion() {
        double horizontal = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
        setYRot((float) (Mth.atan2(getDeltaMovement().x, getDeltaMovement().z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(getDeltaMovement().y, horizontal) * Mth.RAD_TO_DEG - 90.0D));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(PAYLOAD_TYPE, AirstrikeBomberEntity.TYPE_CARPET);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setPayloadType(tag.getInt("type"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("type", payloadType());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

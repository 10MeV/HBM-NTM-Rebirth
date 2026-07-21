package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Direct modern counterpart of 1.7.10 {@code EntityBurningFOEQ}. */
public final class BurningFoeqEntity extends Entity {
    /**
     * {@code EntityBurningFOEQ#getBrightnessForRender(float)} returned the old
     * full-bright lightmap value ({@code 15728880 == 0xF000F0}). RenderFOEQ
     * kept lighting enabled, so this is an entity light-query contract rather
     * than a renderer-local full-bright bypass.
     */
    public static final int LEGACY_FULL_BRIGHT_LIGHT = 0xF000F0;

    public BurningFoeqEntity(EntityType<? extends BurningFoeqEntity> type, Level level) {
        super(type, level);
        // EntityBurningFOEQ set ignoreFrustumCheck.  Model visibility is instead
        // governed by the project's explicit 512-block modern requirement.
        noCulling = true;
    }

    public BurningFoeqEntity(Level level) {
        this(ModEntityTypes.BURNING_FOEQ.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        // The legacy EntityThrowable override owns this entire update body and
        // does not invoke the parent projectile/entity tick.
        xo = getX();
        yo = getY();
        zo = getZ();
        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (getDeltaMovement().y > -4.0D) {
            setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y - 0.1D, getDeltaMovement().z);
        }

        rotation();

        // EntityBurningFOEQ used Java casts, not floor conversion.
        BlockPos current = new BlockPos((int) getX(), (int) getY(), (int) getZ());
        if (!level().getBlockState(current).isAir()) {
            if (!level().isClientSide) {
                for (int i = 0; i < 25; i++) {
                    ExplosionLarge.explode(level(), getX() + 0.5D + random.nextGaussian() * 5.0D,
                            getY() + 0.5D + random.nextGaussian() * 5.0D,
                            getZ() + 0.5D + random.nextGaussian() * 5.0D,
                            10.0F, random.nextBoolean(), false, false);
                }
                ExplosionNukeGeneric.waste(level(), (int) getX(), (int) getY(), (int) getZ(), 35);
            }
            discard();
        }
    }

    /** Preserves EntityBurningFOEQ's manual yaw/pitch interpolation preparation. */
    public void rotation() {
        float horizontal = Mth.sqrt((float) (getDeltaMovement().x * getDeltaMovement().x
                + getDeltaMovement().z * getDeltaMovement().z));
        setYRot((float) (Math.atan2(getDeltaMovement().x, getDeltaMovement().z) * 180.0D / Math.PI));
        setXRot((float) (Math.atan2(getDeltaMovement().y, horizontal) * 180.0D / Math.PI) - 90.0F);

        while (getXRot() - xRotO < -180.0F) {
            xRotO -= 360.0F;
        }
        while (getXRot() - xRotO >= 180.0F) {
            xRotO += 360.0F;
        }
        while (getYRot() - yRotO < -180.0F) {
            yRotO -= 360.0F;
        }
        while (getYRot() - yRotO >= 180.0F) {
            yRotO += 360.0F;
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // EntityBurningFOEQ stores no local NBT.
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // EntityBurningFOEQ stores no local NBT.
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

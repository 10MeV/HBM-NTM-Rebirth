package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Source carrier for 1.7.10 {@code EntityDisperserCanister}. */
public final class DisperserCanisterEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> FLUID_ID = SynchedEntityData.defineId(
            DisperserCanisterEntity.class, EntityDataSerializers.INT);

    public DisperserCanisterEntity(EntityType<? extends DisperserCanisterEntity> type, Level level) {
        super(type, level);
    }

    public DisperserCanisterEntity(Level level, LivingEntity thrower) {
        super(ModEntityTypes.DISPERSER_CANISTER.get(), thrower, level);
    }

    public void setFluidType(FluidType fluid) {
        entityData.set(FLUID_ID, fluid == null ? HbmFluids.NONE.getId() : fluid.getId());
    }

    public FluidType getFluidType() {
        return HbmFluids.fromId(entityData.get(FLUID_ID));
    }

    @Override
    public void tick() {
        super.tick();

        xRotO = getXRot();
        Vec3 motion = getDeltaMovement();
        setXRot(getXRot() - (float) (motion.length() * 25.0D));
        setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        while (getYRot() - yRotO < -180.0F) {
            yRotO -= 360.0F;
        }
        while (getYRot() - yRotO >= 180.0F) {
            yRotO += 360.0F;
        }
        setYRot(yRotO + (getYRot() - yRotO) * 0.2F);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level().isClientSide) {
            return;
        }
        level().addFreshEntity(MistEntity.create(level(), getX(), getY(), getZ(), getFluidType(), 10.0F, 5.0F, 80));
        discard();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DISPERSER_CANISTER.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FLUID_ID, HbmFluids.NONE.getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFluidType(HbmFluids.fromId(tag.getInt("fluid")));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("fluid", getFluidType().getId());
    }
}

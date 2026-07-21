package com.hbm.ntm.entity.train;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

/** Dynamic passenger seat used by the old rail-car rider layer. */
public final class RailCarSeatDummyEntity extends Entity {
    private static final EntityDataAccessor<Integer> TRAIN_ID = SynchedEntityData.defineId(
            RailCarSeatDummyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SEAT_INDEX = SynchedEntityData.defineId(
            RailCarSeatDummyEntity.class, EntityDataSerializers.INT);
    private LegacyRailCarRidableEntity train;

    public RailCarSeatDummyEntity(EntityType<? extends RailCarSeatDummyEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public RailCarSeatDummyEntity(Level level, LegacyRailCarRidableEntity train, int index) {
        this(ModEntityTypes.RAIL_CAR_SEAT_DUMMY.get(), level);
        this.train = train;
        entityData.set(TRAIN_ID, train.getId());
        entityData.set(SEAT_INDEX, index);
    }

    public int seatIndex() {
        return entityData.get(SEAT_INDEX);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.5F, 0.1F);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TRAIN_ID, 0);
        entityData.define(SEAT_INDEX, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (train == null) {
            Entity owner = level().getEntity(entityData.get(TRAIN_ID));
            if (owner instanceof LegacyRailCarRidableEntity railCar) {
                train = railCar;
            }
        }
        if (train == null || train.isRemoved() || seatIndex() < 0 || seatIndex() >= train.getPassengerSeats().length) {
            discard();
            return;
        }
        setPos(train.getPassengerSeatWorldPosition(seatIndex()).subtract(0.0D, 1.0D, 0.0D));
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (train == null) {
            callback.accept(passenger, getX(), getY() + 1.0D, getZ());
            return;
        }
        var position = train.getPassengerSeatWorldPosition(seatIndex());
        callback.accept(passenger, position.x, position.y, position.z);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}

package com.hbm.ntm.entity.train;

import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 1.7.10 EntityRailCarRidable driver, parking-brake and dynamic-seat contract. */
public abstract class LegacyRailCarRidableEntity extends LegacyRailCarCargoEntity {
    private double engineSpeed;
    private final RailCarSeatDummyEntity[] passengerSeats;

    protected LegacyRailCarRidableEntity(EntityType<? extends LegacyRailCarRidableEntity> type, Level level) {
        super(type, level);
        passengerSeats = new RailCarSeatDummyEntity[getPassengerSeats().length];
    }

    public abstract double getPoweredAcceleration();
    public abstract double getPassiveBrake();
    public abstract boolean shouldUseEngineBrake(Player player);
    public abstract double getMaxPoweredSpeed();
    public abstract Vec3 getRiderSeatPosition();
    public abstract Vec3[] getPassengerSeats();

    public boolean canAccelerate() { return true; }
    public void consumeFuel() { }
    public double getGravitySpeed() { return 0.0D; }
    protected double getEngineSpeed() { return engineSpeed; }

    @Override
    public double getCurrentSpeed() {
        if (getControllingPassenger() instanceof Player player) {
            if (canAccelerate()) {
                if (player.zza > 0.0F) {
                    engineSpeed += getPoweredAcceleration();
                    consumeFuel();
                } else if (player.zza < 0.0F) {
                    engineSpeed -= getPoweredAcceleration();
                    consumeFuel();
                } else if (shouldUseEngineBrake(player)) {
                    engineSpeed *= getPassiveBrake();
                } else {
                    consumeFuel();
                }
            } else {
                engineSpeed *= getPassiveBrake();
            }
        } else {
            engineSpeed *= getPassiveBrake();
        }
        engineSpeed = Mth.clamp(engineSpeed, -getMaxPoweredSpeed(), getMaxPoweredSpeed());
        return engineSpeed + getGravitySpeed();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult base = super.interact(player, hand);
        if (base.consumesAction() || level().isClientSide) {
            return base.consumesAction() ? base : InteractionResult.SUCCESS;
        }
        int nearest = getNearestSeat(player);
        if (nearest == -1) {
            player.startRiding(this);
        } else if (nearest >= 0) {
            RailCarSeatDummyEntity seat = new RailCarSeatDummyEntity(level(), this, nearest);
            seat.setPos(getPassengerSeatWorldPosition(nearest).subtract(0.0D, 1.0D, 0.0D));
            level().addFreshEntity(seat);
            passengerSeats[nearest] = seat;
            player.startRiding(seat);
        }
        return InteractionResult.SUCCESS;
    }

    private int getNearestSeat(Player player) {
        double nearestDistance = Double.POSITIVE_INFINITY;
        int nearest = -3;
        Vec3 look = player.getEyePosition().add(player.getLookAngle().scale(2.0D));
        Vec3[] seats = getPassengerSeats();
        for (int index = 0; index < seats.length; index++) {
            if (passengerSeats[index] != null && !passengerSeats[index].isRemoved()) {
                continue;
            }
            double distance = look.distanceTo(getPassengerSeatWorldPosition(index));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = index;
            }
        }
        if (getControllingPassenger() == null) {
            double distance = look.distanceTo(getDriverSeatWorldPosition());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = -1;
            }
        }
        return nearestDistance > 180.0D ? -2 : nearest;
    }

    Vec3 getPassengerSeatWorldPosition(int index) {
        return transformSeatOffset(getPassengerSeats()[index]);
    }

    private Vec3 getDriverSeatWorldPosition() {
        return transformSeatOffset(getRiderSeatPosition());
    }

    private Vec3 transformSeatOffset(Vec3 offset) {
        Vec3 pitched = rotateX(offset, getXRot());
        Vec3 rotated = rotateY(pitched, getYRot());
        return new Vec3(getRenderX() + rotated.x, getRenderY() + rotated.y, getRenderZ() + rotated.z);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        for (int index = 0; index < passengerSeats.length; index++) {
            RailCarSeatDummyEntity seat = passengerSeats[index];
            if (seat == null) {
                continue;
            }
            if (seat.isRemoved() || seat.getPassengers().isEmpty()) {
                seat.discard();
                passengerSeats[index] = null;
            } else {
                seat.setPos(getPassengerSeatWorldPosition(index).subtract(0.0D, 1.0D, 0.0D));
            }
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (hasPassenger(passenger)) {
            Vec3 position = getDriverSeatWorldPosition();
            callback.accept(passenger, position.x, position.y, position.z);
        }
    }
}

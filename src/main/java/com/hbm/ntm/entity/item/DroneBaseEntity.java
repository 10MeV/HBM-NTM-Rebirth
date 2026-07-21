package com.hbm.ntm.entity.item;

import com.hbm.ntm.blockentity.LegacyClientAnimationLod;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Common server flight/client interpolation-and-exhaust contract of EntityDroneBase. */
public abstract class DroneBaseEntity extends Entity {
    private static final EntityDataAccessor<Byte> APPEARANCE = SynchedEntityData.defineId(DroneBaseEntity.class, EntityDataSerializers.BYTE);
    // EntityDroneBase used targetY == -1 as its persisted "no target" sentinel.  Keep
    // that state-machine contract even though 1.20.1 worlds can now contain Y=-1.
    protected double targetX = -1.0D, targetY = -1.0D, targetZ = -1.0D;
    /** Client-only network interpolation state from EntityDroneBase#setPositionAndRotation2. */
    private int turnProgress;
    private double syncPosX;
    private double syncPosY;
    private double syncPosZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    protected DroneBaseEntity(EntityType<? extends DroneBaseEntity> type, Level level) {
        super(type, level);
        noPhysics = false;
        // Legacy EntityDroneBase moved only through its own target controller.  Modern
        // Entity#baseTick applies gravity by default, so explicitly opt out to preserve
        // the old stationary hover and horizontal route contract.
        setNoGravity(true);
    }

    public void setTarget(double x, double y, double z) { targetX = x; targetY = y; targetZ = z; }
    public boolean hasTarget() { return targetY != -1.0D; }
    public void setAppearance(int appearance) { entityData.set(APPEARANCE, (byte) appearance); }
    public int appearance() { return entityData.get(APPEARANCE); }
    public double speed() { return 0.125D; }

    @Override public void tick() {
        if (level().isClientSide) {
            applyClientPositionInterpolation();
            spawnClientExhaust();
        } else {
            Vec3 motion = Vec3.ZERO;
            if (hasTarget()) {
                Vec3 distance = new Vec3(targetX - getX(), targetY - getY(), targetZ - getZ());
                double length = distance.length();
                // Legacy EntityDroneBase clamps its step to the remaining distance. This
                // reaches the programmed target exactly rather than overshooting it.
                if (length > 0.0D) motion = distance.normalize().scale(Math.min(speed(), length));
            }
            if (horizontalCollision) motion = motion.add(0.0D, 1.0D, 0.0D);
            setDeltaMovement(motion);
            beforeServerMove(motion);
            move(MoverType.SELF, motion);
        }
        // EntityDroneBase performed client interpolation/server flight before invoking
        // Entity#onUpdate.  Keeping the base tick last preserves its post-move previous
        // position contract, which the OBJ renderer uses for partial-tick presentation.
        super.tick();
    }

    /** Server movement extension point for source-backed entity state such as drone chunk tickets. */
    protected void beforeServerMove(Vec3 motion) { }

    /**
     * 1.20.1's replacement for EntityDroneBase#setVelocity and
     * EntityDroneBase#setPositionAndRotation2.  The legacy entity did not run its flight
     * controller client-side: it only approached the latest server position for the supplied
     * turn count while preserving the last synchronized velocity.
     */
    @Override
    public void lerpMotion(double x, double y, double z) {
        velocityX = x;
        velocityY = y;
        velocityZ = z;
        setDeltaMovement(x, y, z);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        syncPosX = x;
        syncPosY = y;
        syncPosZ = z;
        turnProgress = steps;
        setDeltaMovement(velocityX, velocityY, velocityZ);
    }

    private void applyClientPositionInterpolation() {
        if (turnProgress <= 0) {
            return;
        }
        setPos(getX() + (syncPosX - getX()) / turnProgress,
                getY() + (syncPosY - getY()) / turnProgress,
                getZ() + (syncPosZ - getZ()) / turnProgress);
        --turnProgress;
    }

    private void spawnClientExhaust() {
        if (LegacyClientAnimationLod.shouldSkipAnimationUpdate(level(), blockPosition())) {
            return;
        }
        double x = getX(), y = getY() + 0.75D, z = getZ();
        level().addParticle(ParticleTypes.SMOKE, x + 1.125D, y, z, 0.0D, -0.2D, 0.0D);
        level().addParticle(ParticleTypes.SMOKE, x - 1.125D, y, z, 0.0D, -0.2D, 0.0D);
        level().addParticle(ParticleTypes.SMOKE, x, y, z + 1.125D, 0.0D, -0.2D, 0.0D);
        level().addParticle(ParticleTypes.SMOKE, x, y, z - 1.125D, 0.0D, -0.2D, 0.0D);
    }

    @Override protected void defineSynchedData() { entityData.define(APPEARANCE, (byte) 0); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("tX", targetX); tag.putDouble("tY", targetY); tag.putDouble("tZ", targetZ);
        tag.putByte("app", entityData.get(APPEARANCE));
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        // EntityDroneBase restored its persisted target fields directly.  In particular, do
        // not dispatch through RequestDroneEntity#setTarget here: that override applies the
        // one-block flight offset when programming a route, but applying it again while
        // loading NBT raises a saved request-drone target by one block on every reload.
        if (tag.contains("tY")) {
            targetX = tag.getDouble("tX");
            targetY = tag.getDouble("tY");
            targetZ = tag.getDouble("tZ");
        }
        setAppearance(tag.getByte("app"));
    }
    @Override public boolean isPickable() { return true; }

    /**
     * The legacy FML entity tracker spawned both drone subclasses automatically. Route the
     * 1.20.1 equivalent through the project's standard Forge custom-entity spawn packet so
     * the client can create either drone before rendering its OBJ, interpolation, and exhaust.
     */
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    /**
     * {@code EntityDroneBase#canTriggerWalking()} was false.  Its modern equivalent keeps
     * logistics drones from activating pressure plates, tripwires, and other block triggers
     * while crossing a route.
     */
    @Override public boolean isIgnoringBlockTriggers() { return true; }
}

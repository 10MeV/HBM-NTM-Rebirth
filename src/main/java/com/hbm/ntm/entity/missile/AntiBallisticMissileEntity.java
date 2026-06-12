package com.hbm.ntm.entity.missile;

import com.hbm.ntm.api.entity.LegacyMissileRadarDetectable;
import com.hbm.ntm.api.entity.LegacyMissileRadarProfile;
import com.hbm.ntm.api.entity.RadarScanner;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class AntiBallisticMissileEntity extends Entity implements LegacyMissileRadarDetectable {
    private static final double BASE_SPEED = 1.5D;
    private static final double MAX_VELOCITY = 6.0D;
    private static final double TARGET_RANGE = 1_000.0D;
    private static final int ACTIVATION_TICKS = 40;
    private static final int MAX_NO_TARGET_AGE = 600;
    private static final int LEGACY_VOID_HEIGHT = 2_000;

    @Nullable
    private Entity tracking;
    private double velocity;
    private int activationTimer;

    public AntiBallisticMissileEntity(EntityType<? extends AntiBallisticMissileEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noCulling = true;
        setDeltaMovement(0.0D, BASE_SPEED, 0.0D);
    }

    public void configureLaunch(double x, double y, double z) {
        setPos(x, y, z);
        setDeltaMovement(0.0D, BASE_SPEED, 0.0D);
    }

    public void setTrackingTarget(@Nullable Entity target) {
        tracking = target;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 move = getDeltaMovement().scale(velocity);
        HitResult hit = traceBlockHit(move);
        if (hit.getType() != HitResult.Type.MISS) {
            setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
            if (!level().isClientSide && activationTimer >= ACTIVATION_TICKS) {
                explode(20.0F);
                return;
            }
        } else {
            setPos(getX() + move.x, getY() + move.y, getZ() + move.z);
        }

        if (level().isClientSide) {
            spawnContrail();
        } else {
            tickServerGuidance();
        }
        updateRotationFromMotion(getDeltaMovement());
    }

    private HitResult traceBlockHit(Vec3 move) {
        Vec3 start = position();
        Vec3 end = start.add(move);
        return level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    private void tickServerGuidance() {
        if (velocity < MAX_VELOCITY) {
            velocity = Math.min(MAX_VELOCITY, velocity + 0.1D);
        }

        if (activationTimer < ACTIVATION_TICKS) {
            activationTimer++;
            setDeltaMovement(0.0D, BASE_SPEED, 0.0D);
            return;
        }

        Entity previous = tracking;
        if (!isValidTarget(tracking)) {
            targetMissile();
        }
        if (previous == null && tracking != null) {
            ExplosionLarge.spawnShock(level(), getX(), getY(), getZ(), 24, 3.0F);
        }

        if (isValidTarget(tracking)) {
            aimAtTarget();
        } else if (tickCount > MAX_NO_TARGET_AGE || getY() > LEGACY_VOID_HEIGHT) {
            discard();
        }
    }

    private void targetMissile() {
        Entity selected = null;
        for (Entity entity : RadarScanner.matchingEntitiesSnapshot()) {
            if (isValidTarget(entity) && entity.distanceToSqr(this) < TARGET_RANGE * TARGET_RANGE) {
                selected = entity;
            }
        }
        if (selected == null) {
            AABB bounds = getBoundingBox().inflate(TARGET_RANGE);
            for (MissileEntity missile : level().getEntitiesOfClass(MissileEntity.class, bounds)) {
                if (isValidTarget(missile) && missile.distanceToSqr(this) < TARGET_RANGE * TARGET_RANGE) {
                    selected = missile;
                }
            }
        }
        tracking = selected;
    }

    private boolean isValidTarget(@Nullable Entity entity) {
        return entity instanceof MissileEntity missile
                && entity.isAlive()
                && entity.level() == level()
                && missile.radarProfile().canBeSeenBy(null);
    }

    private void aimAtTarget() {
        if (tracking == null) {
            return;
        }
        Vec3 delta = tracking.position().subtract(position());
        double distance = delta.length();
        if (distance < 10.0D) {
            explode(15.0F);
            return;
        }

        double intercept = distance / (BASE_SPEED * Math.max(velocity, 0.1D));
        Vec3 targetMotion = new Vec3(tracking.getX() - tracking.xo, tracking.getY() - tracking.yo,
                tracking.getZ() - tracking.zo);
        Vec3 predicted = tracking.position().add(targetMotion.scale(intercept));
        Vec3 motion = predicted.subtract(position());
        if (motion.lengthSqr() > 1.0E-7D) {
            setDeltaMovement(motion.normalize().scale(BASE_SPEED));
        }
    }

    private void explode(float strength) {
        ExplosionLarge.explode(level(), getX(), getY(), getZ(), strength, true, false, false, this);
        discard();
    }

    private void spawnContrail() {
        Vec3 motion = getDeltaMovement();
        Vec3 offset = motion.lengthSqr() > 1.0E-7D ? motion.normalize() : Vec3.ZERO;
        ParticleUtil.spawnAbmContrail(level(), getX() - offset.x, getY() - offset.y, getZ() - offset.z);
    }

    private void updateRotationFromMotion(Vec3 motion) {
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal > 1.0E-4D || Math.abs(motion.y) > 1.0E-4D) {
            setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
            setXRot((float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG));
            yRotO = getYRot();
            xRotO = getXRot();
        }
    }

    @Override
    public LegacyMissileRadarProfile radarProfile() {
        return LegacyMissileRadarProfile.ANTI_BALLISTIC;
    }

    @Override
    public double radarVerticalMotion() {
        return getDeltaMovement().y * velocity;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        velocity = tag.getDouble("veloc");
        if (tag.contains("activationTimer")) {
            activationTimer = tag.getInt("activationTimer");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("veloc", velocity);
        tag.putInt("activationTimer", activationTimer);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

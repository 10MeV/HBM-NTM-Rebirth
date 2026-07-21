package com.hbm.ntm.entity.missile;

import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class MinerRocketEntity extends Entity {
    public static final int MODE_LANDING = 0;
    public static final int MODE_UNLOADING = 1;
    public static final int MODE_LIFTING = 2;

    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(MinerRocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SATELLITE_FREQUENCY =
            SynchedEntityData.defineId(MinerRocketEntity.class, EntityDataSerializers.INT);

    private int timer;

    public MinerRocketEntity(EntityType<? extends MinerRocketEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public MinerRocketEntity(Level level) {
        this(ModEntityTypes.MINER_ROCKET.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MODE, MODE_LANDING);
        entityData.define(SATELLITE_FREQUENCY, 0);
    }

    @Override
    public void tick() {
        // EntityMinerRocket#onUpdate never invokes Entity#onUpdate.  Its
        // complete movement contract is the manual setPositionAndRotation
        // below; entering the modern parent tick would add fluid pushing and
        // other base physics that legacy never applied to this rocket.
        // The old watcher branches assign motionY only for modes 0, 1 and 2.
        // An unrecognized persisted/watcher value does not become a landing
        // rocket: it keeps the previous Y motion while the legacy tick still
        // clears X/Z below.
        double motionY = getDeltaMovement().y;
        switch (mode()) {
            case MODE_LANDING -> motionY = -0.75D;
            case MODE_UNLOADING -> motionY = 0.0D;
            case MODE_LIFTING -> motionY = 1.0D;
            default -> {
                // Keep the source's no-assignment boundary for unknown modes.
            }
        }
        setDeltaMovement(0.0D, motionY, 0.0D);
        // EntityMinerRocket uses setPositionAndRotation(..., 0, 0) every
        // mode tick, so vertical travel never preserves a stale yaw or pitch.
        moveTo(getX(), getY() + motionY, getZ(), 0.0F, 0.0F);

        BlockPos dockPos = new BlockPos((int) (getX() - 0.5D), (int) (getY() - 0.5D),
                (int) (getZ() - 0.5D));
        BlockPos obstaclePos = new BlockPos((int) (getX() - 0.5D), (int) (getY() + 1.0D),
                (int) (getZ() - 0.5D));
        if (mode() == MODE_LANDING && level().getBlockState(dockPos).is(ModBlocks.SAT_DOCK.get())) {
            setMode(MODE_UNLOADING);
            // EntityMinerRocket clears motionY in the same landing tick that
            // switches its watcher to unloading.  Waiting for the following
            // tick to install the unloading velocity leaves a source-visible
            // -0.75 delta movement behind the docked rocket.
            setDeltaMovement(0.0D, 0.0D, 0.0D);
            setPos(getX(), (int) getY(), getZ());
        } else if (!level().getBlockState(obstaclePos).isAir() && !level().isClientSide && mode() != MODE_UNLOADING) {
            discard();
            ExplosionLarge.explodeFire(level(), getX() - 0.5D, getY(), getZ() - 0.5D,
                    10.0F, true, false, true, this);
        }

        if (mode() == MODE_UNLOADING) {
            if (!level().isClientSide && tickCount % 4 == 0) {
                ExplosionLarge.spawnShock(level(), getX(), getY(), getZ(),
                        1 + random.nextInt(3), 1.0D + random.nextGaussian());
            }
            timer++;
            if (timer > 100) {
                setMode(MODE_LIFTING);
            }
        }

        if (mode() != MODE_UNLOADING && !level().isClientSide && tickCount % 2 == 0) {
            ParticleUtil.spawnGasFlame(level(), getX(), getY() - 0.5D, getZ(), 0.0D, -1.0D, 0.0D);
        }

        if (mode() == MODE_LIFTING && getY() > 300.0D) {
            discard();
        }
    }

    public int mode() {
        return entityData.get(MODE);
    }

    public void setMode(int mode) {
        entityData.set(MODE, mode);
    }

    public int satelliteFrequency() {
        return entityData.get(SATELLITE_FREQUENCY);
    }

    public void setSatelliteFrequency(int frequency) {
        entityData.set(SATELLITE_FREQUENCY, frequency);
    }

    public int timer() {
        return timer;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setMode(tag.getInt("mode"));
        setSatelliteFrequency(tag.getInt("sat"));
        timer = tag.getInt("timer");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("mode", mode());
        tag.putInt("sat", satelliteFrequency());
        tag.putInt("timer", timer);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

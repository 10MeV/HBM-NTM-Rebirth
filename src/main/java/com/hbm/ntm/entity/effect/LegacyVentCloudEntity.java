package com.hbm.ntm.entity.effect;

import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmModelRenderDistances;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Shared 1.7.10 movement and server-effect contract for the vent cloud entities. */
public abstract class LegacyVentCloudEntity extends Entity {
    private static final EntityDataAccessor<Integer> MAX_AGE =
            SynchedEntityData.defineId(LegacyVentCloudEntity.class, EntityDataSerializers.INT);
    private final int minimumAge;
    private final int ageVariance;

    protected LegacyVentCloudEntity(EntityType<? extends LegacyVentCloudEntity> type, Level level,
            int minimumAge, int ageVariance) {
        super(type, level);
        this.minimumAge = minimumAge;
        this.ageVariance = ageVariance;
        setNoGravity(true);
        noPhysics = false;
    }

    protected LegacyVentCloudEntity(EntityType<? extends LegacyVentCloudEntity> type, Level level,
            double x, double y, double z, double motionX, double motionY, double motionZ,
            int minimumAge, int ageVariance) {
        this(type, level, minimumAge, ageVariance);
        setPos(x, y, z);
        setDeltaMovement(motionX, motionY, motionZ);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        if (getMaxAge() < minimumAge) {
            setMaxAge(minimumAge + random.nextInt(ageVariance + 1));
        }
        applyLegacyEffect();
        if (++tickCount >= getMaxAge()) {
            discard();
            return;
        }

        Vec3 motion = getDeltaMovement().scale(0.7599999785423279D);
        if (onGround()) {
            motion = new Vec3(motion.x * 0.699999988079071D, motion.y, motion.z * 0.699999988079071D);
        }
        BlockPos skyPos = BlockPos.containing(getX(), getY(), getZ());
        if (level().isRaining() && level().canSeeSky(skyPos)) {
            motion = motion.add(0.0D, -0.01D, 0.0D);
        }

        for (int i = 0; i < 4; i++) {
            Vec3 step = motion.scale(0.25D);
            setPos(getX() + step.x, getY() + step.y, getZ() + step.z);
            BlockPos pos = BlockPos.containing(getX(), getY(), getZ());
            if (handleSpecialBlock(pos)) {
                return;
            }
            BlockState state = level().getBlockState(pos);
            if (state.isCollisionShapeFullBlock(level(), pos)) {
                if (diesOnCollision()) {
                    discard();
                }
                setPos(getX() - step.x, getY() - step.y, getZ() - step.z);
                motion = Vec3.ZERO;
            }
        }
        setDeltaMovement(motion);
    }

    protected void applyLegacyEffect() {
        if (random.nextInt(50) != 0) {
            return;
        }
        if (isChlorine()) {
            ExplosionChaos.poison(level(), (int) getX(), (int) getY(), (int) getZ(), 2);
        } else if (!level().isClientSide) {
            if (isPink()) {
                ExplosionChaos.pc(level(), (int) getX(), (int) getY(), (int) getZ(), 2);
            } else {
                ExplosionChaos.c(level(), (int) getX(), (int) getY(), (int) getZ(), 2);
            }
        }
    }

    protected boolean handleSpecialBlock(BlockPos pos) {
        if (isPink() && level().getBlockState(pos).is(ModBlocks.RADIOREC.get())) {
            if (!level().isClientSide) {
                level().setBlock(pos, ModBlocks.BROADCASTER_PC.get().defaultBlockState()
                        .setValue(com.hbm.ntm.block.PinkCloudBroadcasterBlock.FACING,
                                level().getBlockState(pos).getValue(com.hbm.ntm.block.RadioReceiverBlock.FACING)), 2);
            }
            discard();
            return true;
        }
        return false;
    }

    private boolean diesOnCollision() {
        return !(!isChlorine() && level().isClientSide) && random.nextInt(5) != 0;
    }

    public int getMaxAge() {
        return entityData.get(MAX_AGE);
    }

    public void setMaxAge(int maxAge) {
        entityData.set(MAX_AGE, maxAge);
    }

    public abstract boolean isChlorine();

    public abstract boolean isPink();

    @Override
    protected void defineSynchedData() {
        entityData.define(MAX_AGE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("age");
        setMaxAge(tag.getInt("maxAge"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", tickCount);
        tag.putInt("maxAge", getMaxAge());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.api.entity.RadarDetectable;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.HimarsRocket;
import com.hbm.ntm.artillery.LegacyArtilleryImpactExecutor;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ArtilleryRocketEntity extends LegacyThrowableEntity implements RadarDetectable {
    private static final EntityDataAccessor<Integer> TYPE =
            SynchedEntityData.defineId(ArtilleryRocketEntity.class, EntityDataSerializers.INT);

    private Vec3 lastTargetPos = Vec3.ZERO;
    @Nullable
    private Entity targetEntity;
    @Nullable
    private UUID targetUuid;
    private boolean targeting = true;
    private boolean steering = true;
    private final double[][] targetMotion = new double[20][3];
    private long forcedChunk = Long.MIN_VALUE;
    private int turnProgress;
    private double syncPosX;
    private double syncPosY;
    private double syncPosZ;
    private double syncYaw;
    private double syncPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public ArtilleryRocketEntity(EntityType<? extends ArtilleryRocketEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public ArtilleryRocketEntity(Level level) {
        this(ModEntityTypes.ARTILLERY_ROCKET.get(), level);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    public ArtilleryRocketEntity setType(int type) {
        entityData.set(TYPE, type);
        return this;
    }

    public int typeIndex() {
        return entityData.get(TYPE);
    }

    public HimarsRocket ammoType() {
        int index = typeIndex();
        return index >= 0 && index < LegacyArtilleryAmmoCatalog.himarsRockets().size()
                ? LegacyArtilleryAmmoCatalog.himarsRockets().get(index)
                : LegacyArtilleryAmmoCatalog.AMMO_HIMARS_STANDARD;
    }

    public void shoot(Vec3 heading, float velocity, float inaccuracy) {
        Vec3 motion = heading == null || heading.lengthSqr() <= 1.0E-7D ? Vec3.ZERO : heading.normalize();
        if (inaccuracy > 0.0F) {
            motion = motion.add(random.nextGaussian() * 0.0075D * inaccuracy,
                    random.nextGaussian() * 0.0075D * inaccuracy,
                    random.nextGaussian() * 0.0075D * inaccuracy);
        }
        Vec3 launchMotion = motion.scale(velocity);
        setDeltaMovement(launchMotion);
        setInitialRotationFromMotion(launchMotion);
    }

    public ArtilleryRocketEntity setTarget(Entity target) {
        if (target != null) {
            targetEntity = target;
            targetUuid = target.getUUID();
            Vec3 center = entityTargetCenter(target);
            setTarget(center.x, center.y, center.z);
        }
        return this;
    }

    public ArtilleryRocketEntity setTarget(double x, double y, double z) {
        lastTargetPos = new Vec3(x, y, z);
        return this;
    }

    public Vec3 getLastTarget() {
        return lastTargetPos;
    }

    @Override
    public void tick() {
        Vec3 previousMotion = getDeltaMovement();
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (isRemoved()) {
            return;
        }
        updateRotationFromMotion(previousMotion);
        updateGuidance();
        forceCurrentChunk();
    }

    private void updateGuidance() {
        if (lastTargetPos == null) {
            lastTargetPos = position();
        }
        Vec3 motion = getDeltaMovement();
        Vec3 delta = lastTargetPos.subtract(position());
        double momentum = motion.length() * motionMultiplier();
        if (momentum <= 1.0E-7D) {
            return;
        }
        if (delta.length() <= momentum * 1.5D) {
            Entity liveTarget = targetEntity();
            if (liveTarget == null || !liveTarget.isAlive()) {
                targeting = false;
                steering = false;
            }
            Vec3 direct = delta.normalize().scale(momentum / motionMultiplier());
            setDeltaMovement(direct);
        } else {
            Entity liveTarget = targetEntity();
            if (targeting && liveTarget != null) {
                recalculatePredictiveTarget(liveTarget);
            }
            if (steering) {
                adjustBallisticCourse(25.0D, 15.0D);
            }
        }
    }

    @Override
    protected boolean clientUsesServerInterpolationOnly() {
        return true;
    }

    @Override
    protected void tickClientServerInterpolationOnly() {
        Vec3 before = position();
        if (turnProgress > 0) {
            double interpX = getX() + (syncPosX - getX()) / (double) turnProgress;
            double interpY = getY() + (syncPosY - getY()) / (double) turnProgress;
            double interpZ = getZ() + (syncPosZ - getZ()) / (double) turnProgress;
            double deltaYaw = Mth.wrapDegrees(syncYaw - (double) getYRot());
            setYRot((float) ((double) getYRot() + deltaYaw / (double) turnProgress));
            setXRot((float) ((double) getXRot() + (syncPitch - (double) getXRot()) / (double) turnProgress));
            turnProgress--;
            setPos(interpX, interpY, interpZ);
        } else {
            setPos(getX(), getY(), getZ());
        }
        spawnClientRocketFlame(before);
    }

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
        syncYaw = yaw;
        syncPitch = pitch;
        turnProgress = steps;
        setDeltaMovement(velocityX, velocityY, velocityZ);
    }

    @Nullable
    private Entity targetEntity() {
        if (targetEntity != null && targetEntity.isAlive()) {
            return targetEntity;
        }
        targetEntity = null;
        if (targetUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity loadedTarget = serverLevel.getEntity(targetUuid);
        if (loadedTarget != null && loadedTarget.isAlive()) {
            targetEntity = loadedTarget;
            return loadedTarget;
        }
        return null;
    }

    private void recalculatePredictiveTarget(Entity target) {
        Vec3 speed = getDeltaMovement();
        Vec3 delta = target.position().subtract(position());
        double eta = delta.length() - speed.length();
        double motionX = target.getDeltaMovement().x;
        double motionY = target.getDeltaMovement().y;
        double motionZ = target.getDeltaMovement().z;
        for (int i = 1; i < 20; i++) {
            targetMotion[i - 1] = targetMotion[i];
            motionX += targetMotion[i][0];
            motionY += targetMotion[i][1];
            motionZ += targetMotion[i][2];
        }
        targetMotion[19][0] = target.getDeltaMovement().x;
        targetMotion[19][1] = target.getDeltaMovement().y;
        targetMotion[19][2] = target.getDeltaMovement().z;
        if (eta <= 1.0D) {
            setTarget(target);
            return;
        }
        Vec3 center = entityTargetCenter(target);
        setTarget(center.x + (motionX / 20.0D) * eta,
                center.y + (motionY / 20.0D) * eta,
                center.z + (motionZ / 20.0D) * eta);
    }

    private static Vec3 entityTargetCenter(Entity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ());
    }

    private void adjustBallisticCourse(double speed, double maxTurn) {
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() <= 1.0E-7D || lastTargetPos == null) {
            return;
        }
        Vec3 direction = motion.normalize();
        double horizontalMomentum = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontalMomentum <= 1.0E-7D) {
            return;
        }
        double horizontalDelta = Math.sqrt(Mth.square(lastTargetPos.x - getX()) + Mth.square(lastTargetPos.z - getZ()));
        double stepsRequired = horizontalDelta / horizontalMomentum;
        if (!Double.isFinite(stepsRequired) || stepsRequired <= 0.0D) {
            return;
        }
        Vec3 target = lastTargetPos.subtract(position()).normalize();
        double rocketYaw = legacyYaw(direction);
        double rocketPitch = legacyPitch(direction);
        double targetYaw = legacyYaw(target);
        double targetPitch = legacyPitch(target);
        double turnSpeed = Math.min(maxTurn, 45.0D / stepsRequired);
        if (stepsRequired <= 1.0D) {
            turnSpeed = 180.0D;
        }
        double deltaYaw = ((targetYaw - rocketYaw) + 180.0D) % 360.0D - 180.0D;
        double deltaPitch = ((targetPitch - rocketPitch) + 180.0D) % 360.0D - 180.0D;
        double turnYaw = Math.min(Math.abs(deltaYaw), turnSpeed) * Math.signum(deltaYaw);
        double turnPitch = Math.min(Math.abs(deltaPitch), turnSpeed) * Math.signum(deltaPitch);
        Vec3 velocity = new Vec3(speed, 0.0D, 0.0D)
                .zRot((float) -Math.toRadians(rocketPitch + turnPitch))
                .yRot((float) Math.toRadians(rocketYaw + turnYaw + 90.0D));
        setDeltaMovement(velocity);
    }

    private static double legacyYaw(Vec3 vec) {
        boolean positiveZ = vec.z >= 0.0D;
        return Math.toDegrees(Math.atan(vec.x / vec.z)) + (positiveZ ? 180.0D : 0.0D);
    }

    private static double legacyPitch(Vec3 vec) {
        return Math.toDegrees(Math.atan(vec.y / Math.sqrt(vec.x * vec.x + vec.z * vec.z)));
    }

    private void spawnClientRocketFlame(Vec3 before) {
        Vec3 back = before.subtract(position());
        double velocity = back.length();
        if (velocity <= 1.0D) {
            return;
        }
        Vec3 direction = back.normalize();
        int offset = 6;
        for (int i = offset; i < velocity + offset; i++) {
            Vec3 pos = position().add(direction.scale(i));
            CompoundTag data = new CompoundTag();
            data.putString("type", ParticleUtil.TYPE_EX_KEROSENE);
            ParticleUtil.spawnAux(level(), pos.x, pos.y, pos.z, data, 150.0D);
        }
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide() || isRemoved()) {
            return;
        }
        BlockPos impactBlockPos = hit instanceof BlockHitResult blockHit
                ? blockHit.getBlockPos()
                : BlockPos.containing(hit.getLocation());
        Vec3 explosionCreatorHit = hit instanceof BlockHitResult
                ? Vec3.atCenterOf(impactBlockPos)
                : hit.getLocation();
        LegacyArtilleryImpactExecutor.applyImpact(level(), hit.getLocation(), getDeltaMovement(), this, ammoType(),
                explosionCreatorHit, impactBlockPos);
        discard();
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    protected double getGravityVelocity() {
        return steering ? 0.0D : 0.01D;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TYPE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lastTargetPos = new Vec3(tag.getDouble("targetX"), tag.getDouble("targetY"), tag.getDouble("targetZ"));
        setType(tag.getInt("type"));
        targetEntity = null;
        targetUuid = tag.hasUUID("targetUUID") ? tag.getUUID("targetUUID") : null;
        forcedChunk = Long.MIN_VALUE;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        Vec3 target = lastTargetPos == null ? position() : lastTargetPos;
        tag.putDouble("targetX", target.x);
        tag.putDouble("targetY", target.y);
        tag.putDouble("targetZ", target.z);
        tag.putInt("type", typeIndex());
        if (targetUuid != null) {
            tag.putUUID("targetUUID", targetUuid);
        }
    }

    @Override
    public String getRadarName() {
        return "Artillery Rocket";
    }

    @Override
    public int getBlipLevel() {
        return RadarDetectable.ARTY;
    }

    @Override
    public boolean canBeSeenBy(RadarContext radar) {
        return true;
    }

    @Override
    public boolean paramsApplicable(RadarScanParams params) {
        return params.scanShells();
    }

    @Override
    public boolean suppliesRedstone(RadarScanParams params) {
        return getDeltaMovement().y < 0.0D;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void remove(RemovalReason reason) {
        clearForcedChunk();
        super.remove(reason);
    }

    public void killAndClear() {
        discard();
        clearChunkLoader();
    }

    public void clearChunkLoader() {
        clearForcedChunk();
    }

    public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
        forceChunk(newChunkX, newChunkZ);
    }

    private void setInitialRotationFromMotion(Vec3 motion) {
        if (motion == null || motion.lengthSqr() <= 1.0E-7D) {
            return;
        }
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    private void updateRotationFromMotion(Vec3 motion) {
        if (motion == null || motion.lengthSqr() <= 1.0E-7D || onGround()) {
            return;
        }
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float targetYaw = (float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG);
        float targetPitch = (float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG);
        setYRot(yRotO + Mth.wrapDegrees(targetYaw - yRotO) * 0.2F);
        setXRot(xRotO + Mth.wrapDegrees(targetPitch - xRotO) * 0.2F);
    }

    private void forceCurrentChunk() {
        ChunkPos chunk = chunkPosition();
        forceChunk(chunk.x, chunk.z);
    }

    private void forceChunk(int chunkX, int chunkZ) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
        long packed = chunk.toLong();
        if (forcedChunk == packed) {
            return;
        }
        clearForcedChunk();
        ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunk.x, chunk.z, true, true);
        forcedChunk = packed;
    }

    private void clearForcedChunk() {
        if (forcedChunk == Long.MIN_VALUE || !(level() instanceof ServerLevel serverLevel)) {
            forcedChunk = Long.MIN_VALUE;
            return;
        }
        ChunkPos chunk = new ChunkPos(forcedChunk);
        ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunk.x, chunk.z, false, true);
        forcedChunk = Long.MIN_VALUE;
    }
}

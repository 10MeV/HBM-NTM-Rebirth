package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.api.entity.RadarDetectable;
import com.hbm.ntm.bullet.BulletBehaviorTag;
import com.hbm.ntm.bullet.BulletCollisionUtil;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletConfigSyncRegistry;
import com.hbm.ntm.bullet.BulletDamageUtil;
import com.hbm.ntm.bullet.BulletFlightVisualUtil;
import com.hbm.ntm.bullet.BulletHomingStateUtil;
import com.hbm.ntm.bullet.BulletKinematicsUtil;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.BulletNpcLaunchUtil;
import com.hbm.ntm.bullet.BulletPersistenceUtil;
import com.hbm.ntm.bullet.BulletPlink;
import com.hbm.ntm.bullet.BulletProjectileHitUtil;
import com.hbm.ntm.bullet.BulletProjectileTickUtil;
import com.hbm.ntm.bullet.BulletSpecialSpawnUtil;
import com.hbm.ntm.bullet.BulletStuckStateUtil;
import com.hbm.ntm.bullet.BulletSyncedState;
import com.hbm.ntm.bullet.BulletTauTrailUtil;
import com.hbm.ntm.bullet.Ni4NiCoinRicochetUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BulletProjectileEntity extends Entity implements RadarDetectable {
    private static final EntityDataAccessor<Integer> CONFIG_ID =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> STYLE =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> TRAIL =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> HOMING_TARGET =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> STUCK_IN =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IN_GROUND =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BEAM_LENGTH =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.defineId(BulletProjectileEntity.class, EntityDataSerializers.INT);

    private BulletConfig config;
    private UUID ownerUuid;
    private int ticksInAir;
    private int ticksInGround;
    public int throwableShake;
    private BlockPos stuckBlockPos;
    private BlockState stuckBlockState;
    private boolean hasTauTrailNodes;
    private boolean enteredPortal;
    private int ricochets;
    private float acceleration;
    private final List<BulletTauTrailUtil.TauTrailNode> tauTrailNodes = new ArrayList<>();
    private double prevTauRenderX;
    private double prevTauRenderY;
    private double prevTauRenderZ;
    private boolean hasTauRenderPosition;
    private double renderDistanceWeight = BulletKinematicsUtil.RENDER_DISTANCE_WEIGHT;
    private int turnProgress;
    private double syncPosX;
    private double syncPosY;
    private double syncPosZ;
    private double syncYaw;
    private double syncPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    public float overrideDamage;

    public BulletProjectileEntity(EntityType<? extends BulletProjectileEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public BulletProjectileEntity(Level level) {
        this(ModEntityTypes.BULLET_PROJECTILE.get(), level);
    }

    public static BulletProjectileEntity fromLaunchPlan(Level level, BulletLaunchUtil.LaunchPlan plan,
            @Nullable Entity shooter) {
        BulletProjectileEntity bullet = new BulletProjectileEntity(level);
        bullet.applyLaunchPlan(plan);
        bullet.setOwner(shooter);
        return bullet;
    }

    public static boolean spawn(Level level, BulletSpecialSpawnUtil.SpawnRequest request) {
        if (level == null || request == null || level.isClientSide()) {
            return false;
        }
        BulletLaunchUtil.LaunchPlan plan = request.launchPlan();
        if (plan == null || !plan.valid()) {
            return false;
        }
        BulletProjectileEntity bullet = fromLaunchPlan(level, plan, request.shooter());
        bullet.overrideDamage = request.overrideDamage();
        return level.addFreshEntity(bullet);
    }

    public static int spawnAll(Level level, List<BulletSpecialSpawnUtil.SpawnRequest> requests) {
        if (level == null || requests == null || requests.isEmpty() || level.isClientSide()) {
            return 0;
        }
        int spawned = 0;
        for (BulletSpecialSpawnUtil.SpawnRequest request : requests) {
            if (spawn(level, request)) {
                spawned++;
            }
        }
        return spawned;
    }

    public static int executeNpcAttack(Level level, BulletNpcLaunchUtil.NpcAttackRequest request,
            double soundX, double soundY, double soundZ) {
        if (level == null || request == null || level.isClientSide()) {
            return 0;
        }
        int spawned = spawnAll(level, request.spawnRequests());
        String sound = legacyNpcSound(request.legacySoundName());
        if (spawned > 0 && sound != null) {
            LegacySoundPlayer.playSoundEffect(level, soundX, soundY, soundZ, sound, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        return spawned;
    }

    @Nullable
    private static String legacyNpcSound(String legacySoundName) {
        if (legacySoundName == null) {
            return null;
        }
        return switch (legacySoundName) {
            case BulletNpcLaunchUtil.MASKMAN_MINIGUN_SOUND -> BulletNpcLaunchUtil.MASKMAN_MINIGUN_SOUND;
            case BulletNpcLaunchUtil.MASKMAN_ORB_SOUND -> BulletNpcLaunchUtil.MASKMAN_ORB_SOUND;
            case BulletNpcLaunchUtil.MASKMAN_MISSILE_SOUND -> BulletNpcLaunchUtil.MASKMAN_MISSILE_SOUND;
            default -> null;
        };
    }

    public void applyLaunchPlan(BulletLaunchUtil.LaunchPlan plan) {
        if (plan == null || !plan.valid()) {
            discard();
            return;
        }
        setConfig(plan.config());
        setPos(plan.position().x, plan.position().y, plan.position().z);
        setDeltaMovement(plan.motion());
        renderDistanceWeight = plan.renderDistanceWeight();
        if (plan.config() != null && plan.config().plink() == BulletPlink.ENERGY) {
            entityData.set(BEAM_LENGTH, (float) BulletProjectileTickUtil.LEGACY_BEAM_RANGE);
        }
        setYRot(plan.yaw());
        setXRot(plan.pitch());
        yRotO = plan.yaw();
        xRotO = plan.pitch();
    }

    @Override
    public void tick() {
        Vec3 previousPosition = position();
        super.tick();

        BulletConfig currentConfig = config();
        if (currentConfig == null) {
            discard();
            return;
        }

        if (throwableShake > 0) {
            throwableShake--;
        }
        if (level().isClientSide()) {
            tickClientServerInterpolationOnly(currentConfig, previousPosition);
            return;
        }
        if (inGround()) {
            if (BulletStuckStateUtil.sameLegacyStuckBlock(level(), stuckBlockPos, stuckBlockState)) {
                ticksInGround++;
                if (BulletStuckStateUtil.shouldDespawnInGround(ticksInGround)) {
                    discard();
                }
                return;
            }
            releaseFromGround();
        }

        int activeTicksInAir = ticksInAir + 1;
        BulletProjectileTickUtil.TickResult result = BulletProjectileTickUtil.applyEntityTick(currentConfig, this,
                getOwner(), homingTarget(), tickCount, activeTicksInAir, hasTauTrailNodes, previousPosition,
                random, overrideDamage, inGround(), acceleration);
        ticksInAir = activeTicksInAir;
        acceleration = result.acceleration();
        if (currentConfig.plink() == BulletPlink.ENERGY && result.beamLength() > 0.0D) {
            entityData.set(BEAM_LENGTH, (float) result.beamLength());
        }

        boolean stuckByHook = applyChargeHookStick(currentConfig, result);
        if (!stuckByHook) {
            setPos(result.nextPosition().x, result.nextPosition().y, result.nextPosition().z);
            setDeltaMovement(result.nextMotion());
            updateRotationFromMovement(position().subtract(previousPosition));
        }
        setHomingTarget(result.homingTarget());
        hasTauTrailNodes = hasTauTrailNodes || result.tauTrail().appended();
        appendTauTrailNode(result.tauTrail());
        enteredPortal |= result.enteredPortal();
        applyPortalState(result);
        applyEntityHitState(result.hit());
        boolean exceededRicochetLimit = applyRicochetState(currentConfig, result.hit());
        spawnRequestedProjectiles(result);
        boolean redirectedByNi4NiCoin = Ni4NiCoinRicochetUtil.apply(this, currentConfig, result, overrideDamage);

        if (result.discardProjectile() || exceededRicochetLimit || redirectedByNi4NiCoin) {
            discard();
        }
    }

    private void tickClientServerInterpolationOnly(BulletConfig currentConfig, Vec3 previousPosition) {
        Vec3 motion = getDeltaMovement();
        BulletTauTrailUtil.TauTrailAppend tauTrail =
                BulletTauTrailUtil.appendClientNode(currentConfig, true, hasTauTrailNodes, motion);
        hasTauTrailNodes = hasTauTrailNodes || tauTrail.appended();
        appendTauTrailNode(tauTrail);
        BulletFlightVisualUtil.spawnBlackPowderBurst(currentConfig, level(), previousPosition, motion, tickCount,
                random);

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

        Vec3 currentPosition = position();
        BulletFlightVisualUtil.spawnMeteorFlameParticles(currentConfig, level(), currentPosition, random);
        BulletFlightVisualUtil.spawnFlamethrowerTrail(currentConfig, level(), currentPosition);
        BulletFlightVisualUtil.spawnFireExtinguisherTrail(currentConfig, level(), currentPosition, motion, random);
        BulletFlightVisualUtil.spawnVanillaTrail(currentConfig, level(), previousPosition, currentPosition);
    }

    private boolean applyChargeHookStick(BulletConfig currentConfig, BulletProjectileTickUtil.TickResult result) {
        if (currentConfig == null || result == null
                || !currentConfig.hasBehavior(BulletBehaviorTag.CHARGE_HOOK_STICK)) {
            return false;
        }
        BulletProjectileHitUtil.HitApplication hit = result.hit();
        if (hit == null || hit.scan() == null || hit.scan().primaryHit() != BulletCollisionUtil.PrimaryHit.BLOCK
                || hit.scan().blockHit() == null) {
            return false;
        }
        BulletCollisionUtil.BlockCollision blockHit = hit.scan().blockHit();
        Vec3 motion = hit.resultingMotion().lengthSqr() > 1.0E-7D ? hit.resultingMotion() : getDeltaMovement();
        Vec3 offset = motion.lengthSqr() > 1.0E-7D ? motion.normalize().scale(-0.05D) : Vec3.ZERO;
        Vec3 stuckPosition = blockHit.location().add(offset);
        setPos(stuckPosition.x, stuckPosition.y, stuckPosition.z);
        getStuck(blockHit.blockPos(), blockHit.side());
        return true;
    }

    @Nullable
    public BulletConfig config() {
        if (config == null) {
            config = BulletConfigSyncRegistry.pullConfig(configId()).orElse(null);
        }
        return config;
    }

    public int configId() {
        return entityData.get(CONFIG_ID);
    }

    public byte styleId() {
        return entityData.get(STYLE);
    }

    public byte trailId() {
        return entityData.get(TRAIL);
    }

    public boolean enteredPortal() {
        return enteredPortal;
    }

    public boolean inGround() {
        return entityData.get(IN_GROUND);
    }

    public int getStuckIn() {
        return entityData.get(STUCK_IN);
    }

    public float beamLength() {
        return entityData.get(BEAM_LENGTH);
    }

    public void setStuckIn(int side) {
        entityData.set(STUCK_IN, (byte) side);
    }

    public void getStuck(BlockPos pos, @Nullable Direction side) {
        if (pos == null) {
            return;
        }
        stuckBlockPos = pos.immutable();
        stuckBlockState = level().getBlockState(stuckBlockPos);
        entityData.set(IN_GROUND, true);
        setDeltaMovement(Vec3.ZERO);
        setStuckIn(BulletStuckStateUtil.legacySide(side));
    }

    public void getStuck(int x, int y, int z, int side) {
        getStuck(new BlockPos(x, y, z), Direction.from3DDataValue(side));
    }

    public List<BulletTauTrailUtil.TauTrailNode> tauTrailNodes() {
        return Collections.unmodifiableList(tauTrailNodes);
    }

    public void updateTauTrailRenderPosition(double x, double y, double z) {
        if (!hasTauRenderPosition) {
            prevTauRenderX = x;
            prevTauRenderY = y;
            prevTauRenderZ = z;
            hasTauRenderPosition = true;
            return;
        }
        double deltaX = prevTauRenderX - x;
        double deltaY = prevTauRenderY - y;
        double deltaZ = prevTauRenderZ - z;
        if (deltaX != 0.0D || deltaY != 0.0D || deltaZ != 0.0D) {
            for (int i = 0; i < tauTrailNodes.size(); i++) {
                BulletTauTrailUtil.TauTrailNode node = tauTrailNodes.get(i);
                tauTrailNodes.set(i, new BulletTauTrailUtil.TauTrailNode(
                        node.offset().add(deltaX, deltaY, deltaZ), node.weight()));
            }
        }
        prevTauRenderX = x;
        prevTauRenderY = y;
        prevTauRenderZ = z;
    }

    public void setConfig(@Nullable BulletConfig config) {
        this.config = config;
        BulletSyncedState state = BulletConfigSyncRegistry.syncedState(config);
        entityData.set(CONFIG_ID, state.configId());
        entityData.set(STYLE, state.styleByte());
        entityData.set(TRAIL, state.trailByte());
        if (config != null && (config.plink() == BulletPlink.ENERGY
                || config.hasBehavior(BulletBehaviorTag.CHARGE_HOOK_STICK))) {
            noCulling = true;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (level() instanceof ServerLevel serverLevel) {
            return ownerUuid == null ? null : serverLevel.getEntity(ownerUuid);
        }
        int ownerId = entityData.get(OWNER_ID);
        return ownerId < 0 ? null : level().getEntity(ownerId);
    }

    public void setOwner(@Nullable Entity owner) {
        ownerUuid = owner == null ? null : owner.getUUID();
        entityData.set(OWNER_ID, owner == null ? -1 : owner.getId());
    }

    @Override
    public String getRadarName() {
        return "Artillery Shell";
    }

    @Override
    public int getBlipLevel() {
        return RadarDetectable.ARTY;
    }

    @Override
    public boolean canBeSeenBy(RadarContext radar) {
        BulletConfig currentConfig = config();
        return currentConfig != null && currentConfig.hasBehavior(BulletBehaviorTag.ARTILLERY_RADAR_TARGET);
    }

    @Override
    public boolean paramsApplicable(RadarScanParams params) {
        return params.scanShells();
    }

    @Override
    public boolean suppliesRedstone(RadarScanParams params) {
        return getDeltaMovement().y < 0.0D;
    }

    @Nullable
    private LivingEntity homingTarget() {
        int targetId = entityData.get(HOMING_TARGET);
        if (targetId == BulletHomingStateUtil.NO_TARGET_ID || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity target = serverLevel.getEntity(targetId);
        return target instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private void setHomingTarget(@Nullable LivingEntity target) {
        entityData.set(HOMING_TARGET, BulletHomingStateUtil.targetId(target));
    }

    public void setHomingTargetEntity(@Nullable LivingEntity target) {
        setHomingTarget(target);
    }

    private void applyEntityHitState(BulletProjectileHitUtil.HitApplication hit) {
        if (hit == null || hit.entityHits().isEmpty()) {
            return;
        }
        if (hit.overrideDamageChanged()) {
            overrideDamage = hit.nextOverrideDamage();
        }
        for (BulletProjectileHitUtil.EntityHitApplication entityHit : hit.entityHits()) {
            BulletDamageUtil.EntityHitResult result = entityHit.result();
            if (result.resetHomingTarget()) {
                entityData.set(HOMING_TARGET, BulletHomingStateUtil.NO_TARGET_ID);
            }
        }
    }

    private boolean applyRicochetState(BulletConfig currentConfig, BulletProjectileHitUtil.HitApplication hit) {
        if (currentConfig == null || hit == null || !hit.blockHit().ricocheted()) {
            return false;
        }
        ricochets++;
        return currentConfig.maxRicochetCount() > 0 && ricochets > currentConfig.maxRicochetCount();
    }

    private void applyPortalState(BulletProjectileTickUtil.TickResult result) {
        if (result == null || !result.enteredPortal() || result.hit().scan().blockHit() == null) {
            return;
        }
        handleInsidePortal(result.hit().scan().blockHit().blockPos());
    }

    private void spawnRequestedProjectiles(BulletProjectileTickUtil.TickResult result) {
        if (level().isClientSide() || result == null || result.spawnRequests().isEmpty()) {
            return;
        }
        spawnAll(level(), result.spawnRequests());
    }

    private void releaseFromGround() {
        entityData.set(IN_GROUND, false);
        setDeltaMovement(BulletStuckStateUtil.releasedMotion(getDeltaMovement(), random));
        ticksInGround = 0;
        ticksInAir = 0;
    }

    private void updateRotationFromMovement(Vec3 movement) {
        if (movement == null || movement.lengthSqr() <= 1.0E-7D || inGround() || onGround()) {
            return;
        }
        double horizontal = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        setYRot((float) (Mth.atan2(movement.x, movement.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(movement.y, horizontal) * Mth.RAD_TO_DEG));
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

    private void appendTauTrailNode(BulletTauTrailUtil.TauTrailAppend tauTrail) {
        if (tauTrail == null || tauTrail.node() == null) {
            return;
        }
        tauTrailNodes.add(tauTrail.node());
        if (tauTrail.requestIgnoreFrustum()) {
            noCulling = true;
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CONFIG_ID, BulletSyncedState.EMPTY.configId());
        entityData.define(STYLE, BulletSyncedState.EMPTY.styleByte());
        entityData.define(TRAIL, BulletSyncedState.EMPTY.trailByte());
        entityData.define(HOMING_TARGET, BulletHomingStateUtil.NO_TARGET_ID);
        entityData.define(STUCK_IN, (byte) BulletStuckStateUtil.LEGACY_DEFAULT_STUCK_SIDE);
        entityData.define(IN_GROUND, false);
        entityData.define(BEAM_LENGTH, 0.0F);
        entityData.define(OWNER_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return BulletPersistenceUtil.shouldSaveProjectile(config());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double edge = Math.max(BulletKinematicsUtil.ENTITY_SIZE, getBoundingBox().getSize());
        double range = edge * 64.0D * Math.max(1.0D, renderDistanceWeight);
        return distance < range * range;
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

    @Override
    public double getPassengersRidingOffset() {
        return BulletKinematicsUtil.ENTITY_SIZE * 0.5D;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

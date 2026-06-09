package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.api.entity.LegacyRadarDetectable;
import com.hbm.ntm.bullet.BulletBehaviorTag;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletConfigSyncRegistry;
import com.hbm.ntm.bullet.BulletDamageUtil;
import com.hbm.ntm.bullet.BulletHomingStateUtil;
import com.hbm.ntm.bullet.BulletKinematicsUtil;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.BulletNpcLaunchUtil;
import com.hbm.ntm.bullet.BulletPersistenceUtil;
import com.hbm.ntm.bullet.BulletProjectileHitUtil;
import com.hbm.ntm.bullet.BulletProjectileTickUtil;
import com.hbm.ntm.bullet.BulletSpecialSpawnUtil;
import com.hbm.ntm.bullet.BulletStuckStateUtil;
import com.hbm.ntm.bullet.BulletSyncedState;
import com.hbm.ntm.bullet.BulletTauTrailUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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

public class BulletProjectileEntity extends Entity implements LegacyRadarDetectable {
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

    private BulletConfig config;
    private UUID ownerUuid;
    private int ticksInAir;
    private int ticksInGround;
    public int throwableShake;
    private BlockPos stuckBlockPos;
    private BlockState stuckBlockState;
    private boolean hasTauTrailNodes;
    private boolean enteredPortal;
    private final List<BulletTauTrailUtil.TauTrailNode> tauTrailNodes = new ArrayList<>();
    private double prevTauRenderX;
    private double prevTauRenderY;
    private double prevTauRenderZ;
    private boolean hasTauRenderPosition;
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
        return level.addFreshEntity(fromLaunchPlan(level, plan, request.shooter()));
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
        SoundEvent sound = modernNpcSound(request.legacySoundName());
        if (spawned > 0 && sound != null) {
            level.playSound(null, soundX, soundY, soundZ, sound, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        return spawned;
    }

    @Nullable
    private static SoundEvent modernNpcSound(String legacySoundName) {
        if (legacySoundName == null) {
            return null;
        }
        return switch (legacySoundName) {
            case BulletNpcLaunchUtil.MASKMAN_MINIGUN_SOUND -> ModSounds.WEAPON_CAL_SHOOT.get();
            case BulletNpcLaunchUtil.MASKMAN_ORB_SOUND -> ModSounds.WEAPON_TESLA_SHOOT.get();
            case BulletNpcLaunchUtil.MASKMAN_MISSILE_SOUND -> ModSounds.WEAPON_HK_SHOOT.get();
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
                random, overrideDamage, inGround());
        ticksInAir = activeTicksInAir;

        setPos(result.nextPosition().x, result.nextPosition().y, result.nextPosition().z);
        setDeltaMovement(result.nextMotion());
        setHomingTarget(result.homingTarget());
        hasTauTrailNodes = hasTauTrailNodes || result.tauTrail().appended();
        appendTauTrailNode(result.tauTrail());
        enteredPortal |= result.enteredPortal();
        applyPortalState(result);
        applyEntityHitState(result.hit());
        spawnRequestedProjectiles(result);

        if (result.discardProjectile()) {
            discard();
        }
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
    }

    @Nullable
    public Entity getOwner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getEntity(ownerUuid);
    }

    public void setOwner(@Nullable Entity owner) {
        ownerUuid = owner == null ? null : owner.getUUID();
    }

    @Override
    public LegacyRadarDetectable.RadarTargetType getTargetType() {
        return LegacyRadarDetectable.RadarTargetType.ARTILLERY;
    }

    @Override
    public boolean canBeDetectedByLegacyRadar() {
        BulletConfig currentConfig = config();
        return currentConfig != null && currentConfig.hasBehavior(BulletBehaviorTag.ARTILLERY_RADAR_TARGET);
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

    private void applyEntityHitState(BulletProjectileHitUtil.HitApplication hit) {
        if (hit == null || hit.entityHits().isEmpty()) {
            return;
        }
        for (BulletProjectileHitUtil.EntityHitApplication entityHit : hit.entityHits()) {
            BulletDamageUtil.EntityHitResult result = entityHit.result();
            if (result.resetHomingTarget()) {
                entityData.set(HOMING_TARGET, BulletHomingStateUtil.NO_TARGET_ID);
            }
        }
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
    public double getPassengersRidingOffset() {
        return BulletKinematicsUtil.ENTITY_SIZE * 0.5D;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

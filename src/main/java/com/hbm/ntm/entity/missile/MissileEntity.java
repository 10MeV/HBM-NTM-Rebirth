package com.hbm.ntm.entity.missile;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.entity.LegacyMissileRadarDetectable;
import com.hbm.ntm.api.entity.LegacyMissileRadarProfile;
import com.hbm.ntm.entity.effect.BlackHoleEntity;
import com.hbm.ntm.entity.effect.EmpBlastEntity;
import com.hbm.ntm.entity.logic.EmpLogicEntity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.ExplosionNT;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorFire;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCross;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.item.missile.MissileItem;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class MissileEntity extends Entity implements LegacyMissileRadarDetectable, IEntityAdditionalSpawnData {
    /** EntityMissileBaseNT declares one shared {@code public int health = 50}. */
    public static final float LEGACY_BASE_HEALTH = 50.0F;
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HEALTH =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.FLOAT);
    // EntityMissileBaseNT data watcher 3.  Tier4 uses this legacy ForgeDirection
    // ordinal to rotate its three engine trails relative to the launcher.
    private static final EntityDataAccessor<Byte> LAUNCH_FACING =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.BYTE);

    // EntityMissileBaseNT persists horizontal trajectory endpoints as ints.  Keep the
    // carrier integer too: a target is a legacy block coordinate, never a Vec3 endpoint.
    private int startX;
    private int startZ;
    private int targetX;
    private int targetZ;
    private double velocity;
    private double decelY;
    private double accelXZ;
    private boolean cluster;
    private int turnProgress;
    private double syncPosX;
    private double syncPosY;
    private double syncPosZ;
    private double syncYaw;
    private double syncPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    // EntityThrowableNT's world-only constructor starts at 0.25 x 0.25.  The
    // legacy missile launch constructors enlarge that one flight instance to
    // 1.5 x 1.5, but EntityMissileBaseNT never serializes the size change.
    private boolean launchedCollisionSize;
    private long forcedChunk = Long.MIN_VALUE;
    // 1.20.1 queues Entity#discard removal until the current tick completes, unlike
    // the immediate legacy isDead marker used by EntityMissileBaseNT.
    private boolean destructionStarted;

    public MissileEntity(EntityType<? extends MissileEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noCulling = true;
        // Entity's superclass construction seeds its cached dimensions from the
        // EntityType before this class's launch-only state is available.  Reapply
        // the world-constructor default after construction, matching
        // EntityThrowableNT(World)'s immediate 0.25 x 0.25 setSize call.
        refreshDimensions();
    }

    public MissileEntity(EntityType<? extends MissileEntity> type, Level level, Variant variant) {
        this(type, level);
        setVariant(variant);
        setHealth(variant.health());
    }

    public void configureLaunch(double startX, double startY, double startZ, double targetX, double targetZ) {
        // EntityMissileBaseNT stores these trajectory endpoints as legacy ints.  The missile
        // itself starts at the centre of the launch block, but the horizontal flight vector
        // is calculated from the integer launch-pad coordinate, not from that centre point.
        this.startX = (int) startX;
        this.startZ = (int) startZ;
        this.targetX = (int) targetX;
        this.targetZ = (int) targetZ;
        setPos(startX, startY, startZ);
        setDeltaMovement(0.0D, 2.0D, 0.0D);
        double distance = Math.max(1.0D, Math.sqrt((this.targetX - this.startX) * (this.targetX - this.startX)
                + (this.targetZ - this.startZ) * (this.targetZ - this.startZ)));
        this.decelY = 2.0D / distance;
        this.accelXZ = 1.0D / distance;
        this.velocity = 0.0D;
        // The first rendered yaw in EntityMissileBaseNT used the real centred spawn
        // position.  The integer endpoints remain the trajectory contract; only this
        // initial visual orientation uses the physical launch position.
        setYRot((float) Math.toDegrees(Math.atan2(this.targetX - startX, this.targetZ - startZ)));
        yRotO = getYRot();
        xRotO = getXRot();
        launchedCollisionSize = true;
        refreshDimensions();
        forceCurrentChunk();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(launchedCollisionSize ? 1.5F : 0.25F,
                launchedCollisionSize ? 1.5F : 0.25F);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    /**
     * {@code EntityMissileBaseNT#canBeCollidedWith()} returned true.  Modern
     * projectile sweeps use this hook, so without it players and weapons cannot
     * select a flying missile to apply the legacy health/destruction contract.
     */
    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            tickClientInterpolation();
            // EntityMissileBaseNT invokes spawnContrail for every client flight
            // tick.  This remains true after EntityMissileCustom exhausts its
            // fuel: hasPropulsion only changes its server-side trajectory, not
            // whether the already-flying missile renders its legacy trail.
            spawnContrail();
            return;
        }

        Vec3 motion = getDeltaMovement();
        HitResult hit = traceNextBlockHit();
        if (hit.getType() != HitResult.Type.MISS) {
            // EntityThrowableNT handles a legacy Nether portal hit specially: it
            // enters the portal instead of forwarding the hit to onImpact.  Keep
            // that branch ahead of the generic missile detonation path.
            if (hit instanceof BlockHitResult blockHit
                    && level().getBlockState(blockHit.getBlockPos()).is(Blocks.NETHER_PORTAL)) {
                handleInsidePortal(blockHit.getBlockPos());
            } else {
                // EntityThrowableNT invokes EntityMissileBaseNT#onMissileImpact before it
                // advances the entity for this tick.  Almost every legacy warhead uses the
                // missile position rather than the ray-hit vector, while taint deliberately
                // reads the supplied hit result.  Do not move the entity onto the hit vector.
                onMissileImpact(hit);
                discard();
                return;
            }
        }

        // EntityMissileBaseNT overrides EntityThrowableNT#motionMult with velocity.
        // The accumulated motion vector and its per-tick multiplier are both required
        // for the legacy ballistic arc and therefore its horizontal landing coordinate.
        setPos(getX() + motion.x * velocity, getY() + motion.y * velocity, getZ() + motion.z * velocity);
        // EntityMissileBaseNT called loadNeighboringChunks after EntityThrowableNT
        // advanced the missile.  Updating before movement leaves the ticket one tick
        // behind at chunk crossings, which can unload the column used by the next
        // trajectory trace.
        forceCurrentChunk();
        updateFlight();
        motion = getDeltaMovement();
        if (cluster && motion.y < -1.5D) {
            explodeClusterInFlight();
            discard();
            return;
        }
        updateRotationFromMotion(motion);

        if (getY() < level().getMinBuildHeight() - 64.0D) {
            discard();
        }
    }

    protected HitResult traceNextBlockHit() {
        // EntityThrowableNT asks World#rayTraceBlocks for a zero-length segment
        // on a newly launched missile's first tick (velocity starts at zero).
        // That legacy query produces no hit at the launch-pad surface. Modern
        // Level#clip instead reports the block touched by its zero-length start
        // vector, which immediately destroys ordinary y + 1 launch-pad missiles.
        // Keep the source contract by deferring collision tracing until this
        // velocity-multiplied projectile has an actual segment to trace.
        if (velocity == 0.0D) {
            return BlockHitResult.miss(position(), Direction.UP, BlockPos.containing(position()));
        }
        Vec3 start = position();
        Vec3 end = start.add(getDeltaMovement().scale(velocity));
        return level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    protected void updateFlight() {
        Vec3 motion = getDeltaMovement();
        // EntityMissileBaseNT raises velocity before its propulsion check.  This remains
        // true after a custom missile exhausts fuel because EntityThrowableNT also uses
        // velocity as the movement multiplier for the ballistic descent.
        if (velocity < 4.0D) {
            velocity += Mth.clamp(tickCount / 60.0D * 0.05D, 0.0D, 0.05D);
        }
        if (hasPropulsion()) {
            // Preserve the legacy guard-before-add ordering.  A tick that begins below
            // four may step slightly above it; clamping it back to exactly four shifts
            // the long-range endpoint.  The shared guard above intentionally precedes
            // this propulsion branch, just as it does in the legacy base entity.
            double deltaX = targetX - startX;
            double deltaZ = targetZ - startZ;
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double normX = distance > 1.0E-7D ? deltaX / distance : 0.0D;
            double normZ = distance > 1.0E-7D ? deltaZ / distance : 0.0D;
            // EntityMissileBaseNT subtracts vertical velocity before choosing the
            // horizontal direction.  The sign at the apex is therefore the sign
            // after this decrement; using the previous-tick sign makes a missile
            // travel one extra horizontal thrust step away from the commanded X/Z.
            double vertical = motion.y - decelY * velocity;
            double factor = vertical > 0.0D ? accelXZ * velocity
                    : vertical < 0.0D ? -accelXZ * velocity : 0.0D;
            motion = new Vec3(
                    motion.x + normX * factor,
                    vertical,
                    motion.z + normZ * factor);
        } else {
            double vertical = motion.y;
            // Legacy code tests before subtracting.  It can therefore cross below
            // -1.5 once; clamping first changes the terminal trajectory.
            if (vertical > -1.5D) {
                vertical -= 0.05D;
            }
            motion = new Vec3(motion.x * 0.99D, vertical, motion.z * 0.99D);
        }
        setDeltaMovement(motion);
    }

    protected void updateRotationFromMotion(Vec3 motion) {
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        // EntityMissileBaseNT applies these two atan2 results every server tick,
        // including a zero-length motion vector.  atan2(0, 0) deliberately gives
        // the legacy -90° pitch instead of preserving a stale spawn/reload angle.
        float yaw = (float) Math.toDegrees(Math.atan2(targetX - getX(), targetZ - getZ()));
        float pitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontal)) - 90.0F;
        setYRot(yaw);
        setXRot(pitch);
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

    protected boolean hasPropulsion() {
        return true;
    }

    protected void onMissileImpact(HitResult hit) {
        Variant variant = variant();
        switch (variant.impact()) {
            case TEST -> impactTestMissile();
            case STANDARD -> {
                explodeLegacyStandard(variant, false);
            }
            case FIRE -> {
                explodeLegacyStandard(variant, true);
                if (variant.igniteAllRadius() > 0) {
                    // EntityMissileInferno first ignites every block in the inner
                    // radius, then runs the broader flammable-block pass.
                    ExplosionChaos.igniteAllBlocks(level(), (int) getX(), (int) getY(), (int) getZ(),
                            variant.igniteAllRadius());
                }
                if (variant.igniteRadius() > 0) {
                    if (variant == Variant.INCENDIARY_STRONG) {
                        // EntityMissileIncendiaryStrong added 0.5F and then used Java
                        // truncation.  This intentionally differs from floor for negatives.
                        ExplosionChaos.igniteFlammableBlocks(level(), (int) ((float) getX() + 0.5F),
                                (int) ((float) getY() + 0.5F), (int) ((float) getZ() + 0.5F),
                                variant.igniteRadius());
                    } else {
                        // EntityMissileInferno cast its position directly to int.
                        ExplosionChaos.igniteFlammableBlocks(level(), (int) getX(), (int) getY(), (int) getZ(),
                                variant.igniteRadius());
                    }
                }
            }
            case DECOY -> level().explode(this, getX(), getY(), getZ(), variant.explosionStrength(),
                    false, Level.ExplosionInteraction.NONE);
            case CLUSTER -> {
                level().explode(this, getX(), getY(), getZ(), variant.explosionStrength(),
                        true, Level.ExplosionInteraction.BLOCK);
                spawnClusterSubmunitions(variant.clusterCount());
            }
            case BUSTER -> {
                for (int i = 0; i < variant.busterDepth(); i++) {
                    level().explode(this, getX(), getY() - i, getZ(), variant.explosionStrength(),
                            true, Level.ExplosionInteraction.BLOCK);
                }
                ExplosionLarge.spawnParticles(level(), getX(), getY(), getZ(), variant.busterExtraCount());
                ExplosionLarge.spawnShrapnels(level(), getX(), getY(), getZ(), variant.busterExtraCount(),
                        1.0F, this);
                ExplosionLarge.spawnRubble(level(), getX(), getY(), getZ(), variant.busterExtraCount(), this);
            }
            case DRILL -> {
                for (int i = 0; i < variant.busterDepth(); i++) {
                    new ExplosionNT(level(), this, getX(), getY() - i, getZ(), variant.explosionStrength())
                            .addAllAttrib(ExplosionNT.ExAttrib.ERRODE)
                            .explode();
                }
                ExplosionLarge.spawnParticles(level(), getX(), getY(), getZ(), 25);
                ExplosionLarge.spawnShrapnels(level(), getX(), getY(), getZ(), variant.shrapnelCount(),
                        1.0F, this);
                ExplosionLarge.jolt(level(), getX(), getY(), getZ(), 10, 50, 1.0D);
            }
            case EMP_BLAST -> {
                ExplosionNukeGeneric.empBlast(level(), (int) getX(), (int) getY(), (int) getZ(), 50);
                level().addFreshEntity(EmpBlastEntity.create(level(), getX(), getY(), getZ(), 50));
            }
            case EMP_LOGIC -> {
                EmpLogicEntity emp = new EmpLogicEntity(ModEntityTypes.EMP_LOGIC.get(), level());
                emp.setPos(getX(), getY(), getZ());
                level().addFreshEntity(emp);
            }
            case NUKE_MICRO -> NuclearExplosionUtil.explodeFatman(level(), getX(), getY() + 0.5D, getZ());
            case SCHRABIDIUM -> NuclearExplosionUtil.spawnMissileAntiSchrabidium(level(), getX(), getY(), getZ());
            case BLACK_HOLE -> {
                level().explode(this, getX(), getY(), getZ(), 1.5F, true, Level.ExplosionInteraction.BLOCK);
                BlackHoleEntity blackHole = new BlackHoleEntity(level(), 1.5F);
                blackHole.setPos(getX(), getY(), getZ());
                level().addFreshEntity(blackHole);
            }
            case TAINT -> {
                level().explode(this, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                        5.0F, true, Level.ExplosionInteraction.BLOCK);
                BlockPos origin = hit instanceof BlockHitResult blockHit
                        ? blockHit.getBlockPos()
                        : BlockPos.containing(hit.getLocation());
                ExplosionChaos.taintBlocksAtLevel(level(), origin.getX(), origin.getY(), origin.getZ(), 5, 100, 0);
            }
            case NUCLEAR -> NuclearExplosionUtil.spawnMissileNuclear(level(), getX(), getY(), getZ());
            case MIRV -> NuclearExplosionUtil.spawnMissileMirv(level(), getX(), getY(), getZ());
            case VOLCANO -> {
                ExplosionLarge.explode(level(), getX(), getY(), getZ(), 10.0F, true, true, true, this);
                placeVolcanoCore();
            }
            case SHUTTLE -> {
                new ExplosionNT(level(), this, getX() + 0.5D, getY() + 0.5D, getZ() + 0.5D, 20.0F)
                        .overrideResolution(64)
                        .addAllAttrib(ExplosionNT.ExAttrib.NOSOUND, ExplosionNT.ExAttrib.NOPARTICLE)
                        .explode();
                ParticleUtil.spawnRbmkMush(level(), getX() + 0.5D, getY() + 1.0D, getZ() + 0.5D, 10.0F);
                float pitch = (1.0F + (level().random.nextFloat() - level().random.nextFloat()) * 0.2F) * 0.7F;
                LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "hbm:weapon.robin_explosion",
                        net.minecraft.sounds.SoundSource.BLOCKS, 4.0F, pitch);
            }
            case DOOMSDAY -> NuclearExplosionUtil.spawnMissileDoomsday(level(), getX(), getY(), getZ());
            case DOOMSDAY_RUSTED -> NuclearExplosionUtil.spawnMissileDoomsdayRusted(level(), getX(), getY(), getZ());
        }
    }

    /**
     * Legacy {@code EntityMissileTier0.EntityMissileTest}: turn the sphere around the impact
     * into graded sellafield slaked blocks, preserving the old normal-cube/air split.
     */
    private void impactTestMissile() {
        final int range = 50;
        final int rangeSquared = range * range;
        int originX = Mth.floor(getX());
        int originY = Mth.floor(getY());
        int originZ = Mth.floor(getZ());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockState slaked = ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();

        for (int offsetX = -range; offsetX <= range; offsetX++) {
            for (int offsetY = -range; offsetY <= range; offsetY++) {
                int y = originY + offsetY;
                if (y < level().getMinBuildHeight() || y >= level().getMaxBuildHeight()) {
                    continue;
                }
                for (int offsetZ = -range; offsetZ <= range; offsetZ++) {
                    int distanceSquared = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
                    if (distanceSquared > rangeSquared) {
                        continue;
                    }
                    cursor.set(originX + offsetX, y, originZ + offsetZ);
                    BlockState existing = level().getBlockState(cursor);
                    if (existing.isSolidRender(level(), cursor)) {
                        int levelValue = Mth.clamp((int) (12.0D
                                - (distanceSquared / (double) rangeSquared) * 13.0D), 0, 12);
                        if (!existing.is(ModBlocks.SELLAFIELD_SLAKED.get())
                                || existing.getValue(LegacySellafieldSlakedBlock.LEVEL) < levelValue) {
                            level().setBlock(cursor, slaked.setValue(LegacySellafieldSlakedBlock.LEVEL, levelValue), 3);
                        }
                    } else {
                        level().setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void explodeLegacyStandard(Variant variant, boolean fire) {
        new ExplosionVnt(level(), getX(), getY(), getZ(), variant.explosionStrength(), this)
                .setBlockAllocator(new BlockAllocatorStandard(legacyStandardResolution(variant)))
                .setBlockProcessor(new BlockProcessorStandard().setNoDrop()
                        .withBlockEffect(fire ? new BlockMutatorFire() : null))
                .setEntityProcessor(new EntityProcessorCross(7.5D).withRangeMod(2.0F))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .explode();
        spawnLegacyStandardVisual(variant);
    }

    private static int legacyStandardResolution(Variant variant) {
        return switch (variant) {
            case STRONG, INCENDIARY_STRONG -> 32;
            case BURST, INFERNO -> 48;
            default -> 24;
        };
    }

    private void spawnLegacyStandardVisual(Variant variant) {
        switch (variant) {
            case GENERIC, INCENDIARY -> ParticleUtil.spawnLegacyExplosionSmall(level(), getX(), getY(), getZ());
            case STRONG, INCENDIARY_STRONG, STEALTH -> ParticleUtil.spawnLegacyExplosionStandard(level(), getX(), getY(), getZ());
            case BURST, INFERNO -> ParticleUtil.spawnLegacyExplosionLarge(level(), getX(), getY(), getZ());
            default -> {
            }
        }
    }

    private void placeVolcanoCore() {
        int originX = Mth.floor(getX());
        int originY = Mth.floor(getY());
        int originZ = Mth.floor(getZ());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    cursor.set(originX + x, originY + y, originZ + z);
                    if (!level().isOutsideBuildHeight(cursor)) {
                        level().setBlock(cursor, ModBlocks.VOLCANIC_LAVA_BLOCK.get().defaultBlockState(), 3);
                    }
                }
            }
        }
        cursor.set(originX, originY, originZ);
        if (!level().isOutsideBuildHeight(cursor)) {
            level().setBlock(cursor, ModBlocks.VOLCANO_CORE.get().defaultBlockState(), 3);
        }
    }

    private void spawnClusterSubmunitions(int count) {
        ExplosionChaos.cluster(level(), getX(), getY(), getZ(), count,
                getYRot(), getXRot(),
                (float) Math.PI * 0.25F, (float) Math.PI * 0.25F, 1.0F, this);
    }

    private void explodeClusterInFlight() {
        Variant variant = variant();
        level().explode(this, getX(), getY(), getZ(), variant.explosionStrength(),
                true, Level.ExplosionInteraction.BLOCK);
        spawnClusterSubmunitions(variant.clusterCount());
    }

    protected double flightVelocity() {
        return velocity;
    }

    public void killMissile() {
        if (beginDestruction()) {
            // EntityMissileBaseNT#killMissile marks the missile dead before triggering
            // its destruction blast.  The standard VNT processor intentionally allows
            // self damage, so retaining this entity in the level during the blast lets
            // the blast kill it again and recurse through this method.
            discard();
            ExplosionLarge.explode(level(), getX(), getY(), getZ(), 5.0F, true, false, true, this);
            ExplosionLarge.spawnShrapnelShower(level(), getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z, 15, 0.075D, this);
            ExplosionLarge.spawnMissileDebris(level(), getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z, 0.25D,
                    variant().debris(), variant().rareDebrisDrop());
        }
    }

    /**
     * Modern equivalent of the old immediate {@code isDead} transition.  An
     * explosion can enumerate the entity before the deferred discard is flushed,
     * so subclasses must use this shared re-entry gate before creating effects.
     */
    protected final boolean beginDestruction() {
        if (level().isClientSide || destructionStarted) {
            return false;
        }
        destructionStarted = true;
        discard();
        return true;
    }

    public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
        forceChunk(newChunkX, newChunkZ);
    }

    public void clearChunkLoader() {
        clearForcedChunk();
    }

    @Override
    public void remove(RemovalReason reason) {
        clearForcedChunk();
        super.remove(reason);
    }

    protected void spawnContrail() {
        Variant variant = variant();
        if (variant.hasTier3Contrail()) {
            Vec3 thrust = new Vec3(0.0D, 0.0D, 0.5D);
            thrust = legacyRotateAroundY(thrust, (getYRot() + 90.0F) * Mth.DEG_TO_RAD);
            thrust = legacyRotateAroundX(thrust, getXRot() * Mth.DEG_TO_RAD);
            thrust = legacyRotateAroundY(thrust, -(getYRot() + 90.0F) * Mth.DEG_TO_RAD);

            // Keep EntityMissileTier3's original component order, including its
            // asymmetric third and fourth offsets.
            spawnContrailWithOffset(thrust.x, thrust.y, thrust.z);
            spawnContrailWithOffset(-thrust.z, thrust.y, thrust.x);
            spawnContrailWithOffset(-thrust.x, -thrust.z, -thrust.z);
            spawnContrailWithOffset(thrust.z, -thrust.z, -thrust.x);
            return;
        }
        if (variant.hasTier4Contrail()) {
            Vec3 thrust = new Vec3(0.0D, 0.0D, 1.0D);
            // The old entity's data watcher 3 carried the launch-pad
            // ForgeDirection ordinal: NORTH=2, SOUTH=3, WEST=4, EAST=5.
            switch (launchFacing()) {
                case 2 -> thrust = legacyRotateAroundY(thrust, -Mth.PI / 2.0F);
                case 4 -> thrust = legacyRotateAroundY(thrust, -Mth.PI);
                case 3 -> thrust = legacyRotateAroundY(thrust, -Mth.PI / 2.0F * 3.0F);
                default -> {
                    // Legacy EAST (5) and the default watcher value do not rotate.
                }
            }
            thrust = legacyRotateAroundY(thrust, (getYRot() + 90.0F) * Mth.DEG_TO_RAD);
            thrust = legacyRotateAroundX(thrust, getXRot() * Mth.DEG_TO_RAD);
            thrust = legacyRotateAroundY(thrust, -(getYRot() + 90.0F) * Mth.DEG_TO_RAD);

            spawnContrailWithOffset(thrust.x, thrust.y, thrust.z);
            spawnContrailWithOffset(0.0D, 0.0D, 0.0D);
            spawnContrailWithOffset(-thrust.x, -thrust.z, -thrust.z);
            return;
        }
        spawnContrailWithOffset(0.0D, 0.0D, 0.0D);
    }

    private static Vec3 legacyRotateAroundX(Vec3 vector, float radians) {
        float cos = Mth.cos(radians);
        float sin = Mth.sin(radians);
        return new Vec3(vector.x, vector.y * cos + vector.z * sin, vector.z * cos - vector.y * sin);
    }

    private static Vec3 legacyRotateAroundY(Vec3 vector, float radians) {
        float cos = Mth.cos(radians);
        float sin = Mth.sin(radians);
        return new Vec3(vector.x * cos + vector.z * sin, vector.y, vector.z * cos - vector.x * sin);
    }

    protected void spawnContrailWithOffset(double offsetX, double offsetY, double offsetZ) {
        Vec3 trail = new Vec3(xo - getX(), yo - getY(), zo - getZ());
        double len = trail.length();
        Vec3 direction = len > 1.0E-7D ? trail.normalize() : Vec3.ZERO;
        Vec3 thrust = legacyThrustVector();
        int count = Math.max(Math.min((int) len, 10), 1);
        for (int i = 0; i < count; i++) {
            double j = i - len;
            ParticleUtil.spawnMissileContrail(level(),
                    getX() - direction.x * j + offsetX,
                    getY() - direction.y * j + offsetY,
                    getZ() - direction.z * j + offsetZ,
                    -thrust.x,
                    -thrust.y,
                    -thrust.z,
                    contrailScale(),
                    60 + random.nextInt(20));
        }
    }

    protected float contrailScale() {
        return switch (variant()) {
            // EntityMissileTier0 and EntityMissileTier1 both override the base
            // contract to use a half-scale contrail.  Stealth is a base-only V2.
            case GENERIC, DECOY, INCENDIARY, CLUSTER, BUSTER,
                    EMP, TEST, MICRO, SCHRABIDIUM, BHOLE, TAINT -> 0.5F;
            default -> 1.0F;
        };
    }

    private Vec3 legacyThrustVector() {
        // EntityMissileBaseNT#spawnContraolWithOffset rotates its upward
        // thrust vector around Z first, then around Y.  Keep the original
        // Vec3 rotation order instead of substituting a differently-oriented
        // trigonometric basis.
        Vec3 thrust = new Vec3(0.0D, 1.0D, 0.0D);
        thrust = legacyRotateAroundZ(thrust, getXRot() * Mth.DEG_TO_RAD);
        return legacyRotateAroundY(thrust, (getYRot() + 90.0F) * Mth.DEG_TO_RAD);
    }

    private static Vec3 legacyRotateAroundZ(Vec3 vector, float radians) {
        float cos = Mth.cos(radians);
        float sin = Mth.sin(radians);
        return new Vec3(vector.x * cos + vector.y * sin, vector.y * cos - vector.x * sin, vector.z);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (!level().isClientSide) {
            setHealth(health() - amount);
            if (health() <= 0.0F) {
                killMissile();
            }
        }
        return true;
    }

    @Override
    public LegacyMissileRadarProfile radarProfile() {
        return variant().radarProfile();
    }

    @Override
    public double radarVerticalMotion() {
        return getDeltaMovement().y;
    }

    public Variant variant() {
        return Variant.byId(entityData.get(VARIANT));
    }

    public void setVariant(Variant variant) {
        entityData.set(VARIANT, variant.ordinal());
        cluster = variant.impact() == Impact.CLUSTER;
    }

    public float health() {
        return entityData.get(HEALTH);
    }

    public void setHealth(float health) {
        entityData.set(HEALTH, health);
    }

    public void setLaunchFacing(int legacyFacing) {
        entityData.set(LAUNCH_FACING, (byte) legacyFacing);
    }

    /**
     * Legacy EntityMissileBaseNT data watcher 3: the ForgeDirection ordinal copied
     * from a prebuilt missile's launch pad.  It is renderer-facing state, not part
     * of the horizontal ballistic endpoint contract.
     */
    public int launchFacing() {
        return entityData.get(LAUNCH_FACING);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(VARIANT, Variant.GENERIC.ordinal());
        entityData.define(HEALTH, LEGACY_BASE_HEALTH);
        entityData.define(LAUNCH_FACING, (byte) 5);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // EntityMissileBaseNT(World) delegates to EntityThrowableNT(World), which
        // restores its 0.25 x 0.25 default before legacy NBT is applied.  The
        // launch-only setSize(1.5F, 1.5F) is deliberately absent from that NBT.
        launchedCollisionSize = false;
        setDeltaMovement(tag.getDouble("moX"), tag.getDouble("moY"), tag.getDouble("moZ"));
        setPos(tag.getDouble("poX"), tag.getDouble("poY"), tag.getDouble("poZ"));
        decelY = tag.getDouble("decel");
        accelXZ = tag.getDouble("accel");
        targetX = tag.getInt("tX");
        targetZ = tag.getInt("tZ");
        startX = tag.getInt("sX");
        startZ = tag.getInt("sZ");
        velocity = tag.getDouble("veloc");
        if (tag.contains("variant")) {
            setVariant(Variant.byId(tag.getInt("variant")));
        }
        // The 1.7.10 cluster subclasses set isCluster only in their launch constructors.
        // EntityMissileBaseNT never serializes it, so a world-only reconstruction loses
        // the in-flight cluster trigger even though its subtype still has cluster impact.
        cluster = false;
        refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        Vec3 motion = getDeltaMovement();
        tag.putDouble("moX", motion.x);
        tag.putDouble("moY", motion.y);
        tag.putDouble("moZ", motion.z);
        tag.putDouble("poX", getX());
        tag.putDouble("poY", getY());
        tag.putDouble("poZ", getZ());
        tag.putDouble("decel", decelY);
        tag.putDouble("accel", accelXZ);
        tag.putInt("tX", targetX);
        tag.putInt("tZ", targetZ);
        tag.putInt("sX", startX);
        tag.putInt("sZ", startZ);
        tag.putDouble("veloc", velocity);
        tag.putInt("variant", variant().ordinal());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(startX);
        buffer.writeInt(startZ);
        buffer.writeInt(targetX);
        buffer.writeInt(targetZ);
        buffer.writeDouble(velocity);
        buffer.writeDouble(decelY);
        buffer.writeDouble(accelXZ);
        Vec3 motion = getDeltaMovement();
        buffer.writeDouble(motion.x);
        buffer.writeDouble(motion.y);
        buffer.writeDouble(motion.z);
        buffer.writeByte(launchFacing());
        buffer.writeBoolean(launchedCollisionSize);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        startX = additionalData.readInt();
        startZ = additionalData.readInt();
        targetX = additionalData.readInt();
        targetZ = additionalData.readInt();
        velocity = additionalData.readDouble();
        decelY = additionalData.readDouble();
        accelXZ = additionalData.readDouble();
        setDeltaMovement(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        setLaunchFacing(additionalData.readByte());
        launchedCollisionSize = additionalData.readBoolean();
        refreshDimensions();
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

    private void tickClientInterpolation() {
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

    public enum Variant {
        GENERIC(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, LEGACY_BASE_HEALTH,
                Impact.STANDARD, 15.0F, 24, 0, 0, 0, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, LEGACY_BASE_HEALTH,
                Impact.STANDARD, 30.0F, 32, 0, 0, 0, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        BURST(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, LEGACY_BASE_HEALTH,
                Impact.STANDARD, 50.0F, 48, 0, 0, 0, 0, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        DECOY(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER4, LEGACY_BASE_HEALTH,
                Impact.DECOY, 4.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        INCENDIARY(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, LEGACY_BASE_HEALTH,
                Impact.FIRE, 15.0F, 24, 0, 0, 0, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        CLUSTER(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, LEGACY_BASE_HEALTH,
                Impact.CLUSTER, 5.0F, 0, 0, 0, 25, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        BUSTER(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, LEGACY_BASE_HEALTH,
                Impact.BUSTER, 5.0F, 0, 0, 0, 0, 15, 5,
                "plate_titanium", 4, "thruster_small", 1),
        INCENDIARY_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, LEGACY_BASE_HEALTH,
                Impact.FIRE, 30.0F, 32, 25, 0, 0, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        CLUSTER_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, LEGACY_BASE_HEALTH,
                Impact.CLUSTER, 15.0F, 0, 0, 0, 50, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        BUSTER_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, LEGACY_BASE_HEALTH,
                Impact.BUSTER, 7.5F, 0, 0, 0, 0, 20, 8,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        INFERNO(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, LEGACY_BASE_HEALTH,
                Impact.FIRE, 50.0F, 48, 25, 10, 0, 0, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        RAIN(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, LEGACY_BASE_HEALTH,
                Impact.CLUSTER, 25.0F, 0, 0, 0, 100, 0, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        DRILL(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, LEGACY_BASE_HEALTH,
                Impact.DRILL, 10.0F, 12, 0, 0, 0, 30, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        STEALTH(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.STEALTH, LEGACY_BASE_HEALTH,
                Impact.STANDARD, 20.0F, 24, 0, 0, 0, 0, 0,
                "bolt_steel", 4),
        EMP(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, LEGACY_BASE_HEALTH,
                Impact.EMP_BLAST, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine_aluminium", 4, "plate_titanium", 4, "shell_aluminium", 2, "ducttape", 1),
        EMP_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, LEGACY_BASE_HEALTH,
                Impact.EMP_LOGIC, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        TEST(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, LEGACY_BASE_HEALTH,
                Impact.TEST, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine_aluminium", 4, "plate_titanium", 4, "shell_aluminium", 2, "ducttape", 1),
        MICRO(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, LEGACY_BASE_HEALTH,
                Impact.NUKE_MICRO, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine_aluminium", 4, "plate_titanium", 4, "shell_aluminium", 2, "ducttape", 1),
        SCHRABIDIUM(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, LEGACY_BASE_HEALTH,
                Impact.SCHRABIDIUM, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine_aluminium", 4, "plate_titanium", 4, "shell_aluminium", 2, "ducttape", 1),
        BHOLE(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, LEGACY_BASE_HEALTH,
                Impact.BLACK_HOLE, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine_aluminium", 4, "plate_titanium", 4, "shell_aluminium", 2, "ducttape", 1),
        TAINT(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, LEGACY_BASE_HEALTH,
                Impact.TAINT, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine_aluminium", 4, "plate_titanium", 4, "shell_aluminium", 2, "ducttape", 1),
        NUCLEAR(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, LEGACY_BASE_HEALTH,
                Impact.NUCLEAR, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 16, "plate_steel", 20, "plate_aluminium", 12, "thruster_large", 1),
        MIRV(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, LEGACY_BASE_HEALTH,
                Impact.MIRV, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 16, "plate_steel", 20, "plate_aluminium", 12, "thruster_large", 1),
        VOLCANO(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, LEGACY_BASE_HEALTH,
                Impact.VOLCANO, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 16, "plate_steel", 20, "plate_aluminium", 12, "thruster_large", 1),
        SHUTTLE(MissileItem.FormFactor.OTHER, LegacyMissileRadarProfile.SHUTTLE, LEGACY_BASE_HEALTH,
                Impact.SHUTTLE, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_steel", 8, "thruster_medium", 2, "canister_empty", 1, Items.GLASS_PANE, 2),
        DOOMSDAY(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, LEGACY_BASE_HEALTH,
                Impact.DOOMSDAY, 0.0F, 0, 0, 0, 0, 0, 0),
        DOOMSDAY_RUSTED(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, LEGACY_BASE_HEALTH,
                Impact.DOOMSDAY_RUSTED, 0.0F, 0, 0, 0, 0, 0, 0);

        private final MissileItem.FormFactor formFactor;
        private final LegacyMissileRadarProfile radarProfile;
        private final float health;
        private final Impact impact;
        private final float explosionStrength;
        private final int shrapnelCount;
        private final int igniteRadius;
        private final int igniteAllRadius;
        private final int clusterCount;
        private final int busterDepth;
        private final int busterExtraCount;
        private final List<ItemStack> debris;

        Variant(MissileItem.FormFactor formFactor, LegacyMissileRadarProfile radarProfile, float health,
                Impact impact, float explosionStrength, int shrapnelCount, int igniteRadius, int igniteAllRadius,
                int clusterCount, int busterDepth, int busterExtraCount, Object... debris) {
            this.formFactor = formFactor;
            this.radarProfile = radarProfile;
            this.health = health;
            this.impact = impact;
            this.explosionStrength = explosionStrength;
            this.shrapnelCount = shrapnelCount;
            this.igniteRadius = igniteRadius;
            this.igniteAllRadius = igniteAllRadius;
            this.clusterCount = clusterCount;
            this.busterDepth = busterDepth;
            this.busterExtraCount = busterExtraCount;
            this.debris = buildDebris(debris);
        }

        public static Variant byId(int id) {
            Variant[] values = values();
            return id >= 0 && id < values.length ? values[id] : GENERIC;
        }

        public MissileItem.FormFactor formFactor() {
            return formFactor;
        }

        public LegacyMissileRadarProfile radarProfile() {
            return radarProfile;
        }

        public float health() {
            return health;
        }

        public float explosionStrength() {
            return explosionStrength;
        }

        public Impact impact() {
            return impact;
        }

        private boolean hasTier3Contrail() {
            return this == BURST || this == INFERNO || this == RAIN || this == DRILL;
        }

        private boolean hasTier4Contrail() {
            return this == NUCLEAR || this == MIRV || this == VOLCANO
                    || this == DOOMSDAY || this == DOOMSDAY_RUSTED;
        }

        public int shrapnelCount() {
            return shrapnelCount;
        }

        public int igniteRadius() {
            return igniteRadius;
        }

        public int igniteAllRadius() {
            return igniteAllRadius;
        }

        public int clusterCount() {
            return clusterCount;
        }

        public int busterDepth() {
            return busterDepth;
        }

        public int busterExtraCount() {
            return busterExtraCount;
        }

        public List<ItemStack> debris() {
            return debris;
        }

        public ItemStack rareDebrisDrop() {
            return switch (this) {
                case GENERIC -> rareItem("warhead_generic_small");
                case DECOY -> rareItem("ingot_steel");
                case INCENDIARY -> rareItem("warhead_incendiary_small");
                case CLUSTER -> rareItem("warhead_cluster_small");
                case BUSTER -> rareItem("warhead_buster_small");
                case STRONG, EMP_STRONG -> rareItem("warhead_generic_medium");
                case INCENDIARY_STRONG -> rareItem("warhead_incendiary_medium");
                case CLUSTER_STRONG -> rareItem("warhead_cluster_medium");
                case BUSTER_STRONG -> rareItem("warhead_buster_medium");
                case BURST -> rareItem("warhead_generic_large");
                case INFERNO -> rareItem("warhead_incendiary_large");
                case RAIN -> rareItem("warhead_cluster_large");
                case DRILL -> rareItem("warhead_buster_large");
                case STEALTH -> rareItem("powder_ash_misc");
                case EMP -> new ItemStack(ModBlocks.EMP_BOMB.get());
                case MICRO -> rareItem("ammo_standard_nuke_high");
                case BHOLE -> rareItem("black_hole");
                case TAINT -> rareItem("powder_spark_mix");
                case NUCLEAR -> rareItem("warhead_nuclear");
                case MIRV -> rareItem("warhead_mirv");
                case VOLCANO -> rareItem("warhead_volcano");
                case SHUTTLE -> rareItem("missile_generic");
                default -> ItemStack.EMPTY;
            };
        }

        private static List<ItemStack> buildDebris(Object... entries) {
            List<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i + 1 < entries.length; i += 2) {
                Object entry = entries[i];
                Item item = null;
                if (entry instanceof String legacyName) {
                    RegistryObject<Item> legacyItem = ModItems.legacyItem(legacyName);
                    item = legacyItem == null ? null : legacyItem.get();
                } else if (entry instanceof Item directItem) {
                    item = directItem;
                }
                int count = (Integer) entries[i + 1];
                if (item != null) {
                    stacks.add(new ItemStack(item, count));
                }
            }
            return List.copyOf(stacks);
        }

        private static ItemStack rareItem(String legacyName) {
            RegistryObject<Item> item = ModItems.legacyItem(legacyName);
            return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
        }
    }

    public enum Impact {
        STANDARD,
        TEST,
        FIRE,
        DECOY,
        CLUSTER,
        BUSTER,
        DRILL,
        EMP_BLAST,
        EMP_LOGIC,
        NUKE_MICRO,
        SCHRABIDIUM,
        BLACK_HOLE,
        TAINT,
        NUCLEAR,
        MIRV,
        VOLCANO,
        SHUTTLE,
        DOOMSDAY,
        DOOMSDAY_RUSTED
    }
}

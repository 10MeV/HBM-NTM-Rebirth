package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.api.entity.RadarDetectable;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ArtyShell;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ClusterEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.DelegatedImpactEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ImpactProfile;
import com.hbm.ntm.artillery.LegacyArtilleryImpactExecutor;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ArtilleryShellEntity extends LegacyThrowableEntity implements RadarDetectable {
    private static final EntityDataAccessor<Integer> TYPE =
            SynchedEntityData.defineId(ArtilleryShellEntity.class, EntityDataSerializers.INT);

    private double targetX;
    private double targetY;
    private double targetZ;
    private boolean shouldWhistle;
    private boolean didWhistle;
    private ItemStack cargo = ItemStack.EMPTY;
    private BlockPos stuckBlockPos;
    private Block stuckBlock = Blocks.AIR;
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

    public ArtilleryShellEntity(EntityType<? extends ArtilleryShellEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public ArtilleryShellEntity(Level level) {
        this(ModEntityTypes.ARTILLERY_SHELL.get(), level);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    public ArtilleryShellEntity setType(int type) {
        entityData.set(TYPE, type);
        return this;
    }

    public int typeIndex() {
        return entityData.get(TYPE);
    }

    public ArtyShell ammoType() {
        int index = typeIndex();
        return index >= 0 && index < LegacyArtilleryAmmoCatalog.artyShells().size()
                ? LegacyArtilleryAmmoCatalog.artyShells().get(index)
                : LegacyArtilleryAmmoCatalog.AMMO_ARTY;
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

    public void setTarget(double x, double y, double z) {
        targetX = x;
        targetY = y;
        targetZ = z;
    }

    public double[] getTarget() {
        return new double[] { targetX, targetY, targetZ };
    }

    public double getTargetHeight() {
        return targetY;
    }

    public void setWhistle(boolean whistle) {
        shouldWhistle = whistle;
    }

    public boolean getWhistle() {
        return shouldWhistle;
    }

    public boolean shouldWhistle() {
        return shouldWhistle;
    }

    public boolean didWhistle() {
        return didWhistle;
    }

    public void setCargo(ItemStack stack) {
        cargo = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public ItemStack getCargo() {
        return cargo;
    }

    @Override
    public void tick() {
        Vec3 previousMotion = getDeltaMovement();
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        checkStuckBlock();
        if (isRemoved()) {
            return;
        }
        updateRotationFromMotion(previousMotion);
        handleWhistle();
        forceCurrentChunk();
        handleClusterUpdate();
    }

    private void handleWhistle() {
        if (didWhistle || !shouldWhistle) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        double deltaX = getX() - targetX;
        double deltaZ = getZ() - targetZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (speed * 18.0D > distance) {
            LegacySoundPlayer.playSoundEffectRandomPitch(level(), targetX, targetY, targetZ,
                    "hbm:turret.mortarWhistle", SoundSource.BLOCKS, 15.0F, 0.9F, 0.2F);
            didWhistle = true;
        }
    }

    private void handleClusterUpdate() {
        ImpactProfile profile = ammoType().impactProfile();
        if (profile == null) {
            return;
        }
        for (ClusterEffect cluster : profile.effects(ClusterEffect.class)) {
            if (trySplitCluster(cluster)) {
                return;
            }
        }
    }

    private boolean trySplitCluster(ClusterEffect cluster) {
        if (!shouldWhistle || getDeltaMovement().y > 0.0D || getTargetHeight() + cluster.splitHeight() < getY()) {
            return false;
        }
        ArtyShell childType = LegacyArtilleryAmmoCatalog.findArtyShell(cluster.childLegacyName());
        if (childType == null) {
            return false;
        }
        int childIndex = LegacyArtilleryAmmoCatalog.artyShells().indexOf(childType);
        Vec3 motion = getDeltaMovement();
        ParticleUtil.spawnArtilleryClusterSplitPlasmaBlast(level(), position());
        discard();
        for (int i = 0; i < cluster.amount(); i++) {
            ArtilleryShellEntity child = new ArtilleryShellEntity(level());
            child.setType(childIndex);
            child.setPos(getX(), getY(), getZ());
            child.setDeltaMovement(i == 0 ? motion : motion.add(
                    random.nextGaussian() * cluster.deviation(),
                    0.0D,
                    random.nextGaussian() * cluster.deviation()));
            child.setYRot(getYRot());
            child.setXRot(getXRot());
            child.yRotO = yRotO;
            child.xRotO = xRotO;
            child.setTarget(targetX, targetY, targetZ);
            child.setWhistle(shouldWhistle && !didWhistle);
            level().addFreshEntity(child);
        }
        return true;
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide() || isRemoved()) {
            return;
        }
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof ArtilleryShellEntity) {
            return;
        }
        ArtyShell shell = ammoType();
        ImpactProfile profile = shell.impactProfile();
        if (profile != null && !profile.effects(DelegatedImpactEffect.class).isEmpty()) {
            DelegatedImpactEffect delegated = profile.effects(DelegatedImpactEffect.class).get(0);
            ArtyShell delegatedShell = LegacyArtilleryAmmoCatalog.findArtyShell(delegated.delegatedLegacyName());
            if (delegatedShell != null) {
                shell = delegatedShell;
            }
        }
        if (profile != null && !profile.effects(LegacyArtilleryAmmoCatalog.CargoStickEffect.class).isEmpty()
                && cargoImpact(hit)) {
            return;
        }
        BlockPos impactBlockPos = hit instanceof BlockHitResult blockHit
                ? blockHit.getBlockPos()
                : BlockPos.containing(hit.getLocation());
        LegacyArtilleryImpactExecutor.applyImpact(level(), hit.getLocation(), getDeltaMovement(), this, shell, null,
                impactBlockPos);
        discard();
    }

    private boolean cargoImpact(HitResult hit) {
        if (hit instanceof BlockHitResult blockHit) {
            stickCargo(blockHit);
        }
        return true;
    }

    @Override
    protected boolean clientUsesServerInterpolationOnly() {
        return true;
    }

    @Override
    protected void tickClientServerInterpolationOnly() {
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

        if (new Vec3(syncPosX - getX(), syncPosY - getY(), syncPosZ - getZ()).length() < 0.2D) {
            ParticleUtil.spawnLegacyArtillerySmokeTrail(level(), getX(), getY(), getZ());
        }
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

    private void stickCargo(BlockHitResult hit) {
        inGround = true;
        ticksInGround = 0;
        stuckBlockPos = hit.getBlockPos().immutable();
        stuckBlock = level().getBlockState(stuckBlockPos).getBlock();
        setDeltaMovement(Vec3.ZERO);
        setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
    }

    private void checkStuckBlock() {
        if (!inGround || stuckBlockPos == null || level().getBlockState(stuckBlockPos).is(stuckBlock)) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        inGround = false;
        stuckBlockPos = null;
        stuckBlock = Blocks.AIR;
        setDeltaMovement(
                motion.x * random.nextFloat() * 0.2F,
                motion.y * random.nextFloat() * 0.2F,
                motion.z * random.nextFloat() * 0.2F);
        ticksInGround = 0;
        ticksInAir = 0;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            if (!cargo.isEmpty()) {
                HbmItemStackUtil.giveOrDrop(player, cargo, level(), getX(), getY(), getZ());
            }
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    protected double getGravityVelocity() {
        return 9.81D * 0.05D;
    }

    @Override
    protected int groundDespawn() {
        return cargo.isEmpty() ? 1200 : 0;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TYPE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setType(tag.getInt("type"));
        shouldWhistle = tag.getBoolean("shouldWhistle");
        didWhistle = tag.getBoolean("didWhistle");
        targetX = tag.getDouble("targetX");
        targetY = tag.getDouble("targetY");
        targetZ = tag.getDouble("targetZ");
        cargo = tag.contains("cargo") ? ItemStack.of(tag.getCompound("cargo")) : ItemStack.EMPTY;
        if (tag.contains("xTile")) {
            stuckBlockPos = new BlockPos(tag.getInt("xTile"), tag.getInt("yTile"), tag.getInt("zTile"));
            String blockName = tag.getString("stuckBlock");
            ResourceLocation blockId = ResourceLocation.tryParse(blockName);
            stuckBlock = blockId == null ? Blocks.AIR
                    : BuiltInRegistries.BLOCK.getOptional(blockId).orElse(Blocks.AIR);
        } else {
            stuckBlockPos = null;
            stuckBlock = Blocks.AIR;
        }
        forcedChunk = Long.MIN_VALUE;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("type", typeIndex());
        tag.putBoolean("shouldWhistle", shouldWhistle);
        tag.putBoolean("didWhistle", didWhistle);
        tag.putDouble("targetX", targetX);
        tag.putDouble("targetY", targetY);
        tag.putDouble("targetZ", targetZ);
        if (!cargo.isEmpty()) {
            tag.put("cargo", cargo.save(new CompoundTag()));
        }
        if (stuckBlockPos != null) {
            tag.putInt("xTile", stuckBlockPos.getX());
            tag.putInt("yTile", stuckBlockPos.getY());
            tag.putInt("zTile", stuckBlockPos.getZ());
            tag.putString("stuckBlock", BuiltInRegistries.BLOCK.getKey(stuckBlock).toString());
        }
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

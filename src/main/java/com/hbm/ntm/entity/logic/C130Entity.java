package com.hbm.ntm.entity.logic;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.item.ParachuteCrateEntity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.itempool.HbmItemPoolRegistry;
import com.hbm.ntm.particle.LegacyParticleCreators;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkHooks;

/** Source-backed EntityC130 / EntityPlaneBase transport plane for 26mm airdrop flares. */
public class C130Entity extends Entity {
    public static final int LIFETIME = 200;
    private static final EntityDataAccessor<Float> HEALTH =
            SynchedEntityData.defineId(C130Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> PAYLOAD =
            SynchedEntityData.defineId(C130Entity.class, EntityDataSerializers.BYTE);
    private long forcedChunk = Long.MIN_VALUE;

    public C130Entity(EntityType<? extends C130Entity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public C130Entity(Level level) {
        this(ModEntityTypes.C130.get(), level);
    }

    public static C130Entity create(Level level, double targetX, double targetY, double targetZ, Payload payload) {
        C130Entity plane = new C130Entity(level);
        Vec3 direction = new Vec3(level.random.nextDouble() - 0.5D, 0.0D,
                level.random.nextDouble() - 0.5D).normalize().scale(2.0D);
        plane.setPayload(payload);
        plane.setPos(targetX - direction.x * 100.0D, targetY + 100.0D, targetZ - direction.z * 100.0D);
        plane.setDeltaMovement(direction.x, 0.0D, direction.z);
        plane.updateRotationFromMotion();
        plane.forceCurrentChunk();
        return plane;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            startClientLoopSound();
            return;
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        updateRotationFromMotion();
        forceCurrentChunk();
        if (health() <= 0.0F) {
            tickCrash();
            return;
        }
        if (tickCount > LIFETIME) {
            discard();
            return;
        }
        if (tickCount == LIFETIME / 2) {
            dropPayload();
        }
    }

    private void dropPayload() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ParachuteCrateEntity crate = new ParachuteCrateEntity(serverLevel);
        Vec3 motion = getDeltaMovement();
        crate.setPos(getX() - motion.x * 7.0D, getY() - 10.0D, getZ() - motion.z * 7.0D);
        Vec3 origin = crate.position();
        if (payload() == Payload.SUPPLIES) {
            for (int i = 0; i < 5; i++) {
                crate.addItem(HbmItemPoolRegistry.getStack(serverLevel, HbmItemPoolIds.POOL_SUPPLIES, origin));
            }
        } else if (payload() == Payload.WEAPONS) {
            for (int i = 0, amount = 1 + random.nextInt(2); i < amount; i++) {
                crate.addItem(HbmItemPoolRegistry.getStack(serverLevel, HbmItemPoolIds.POOL_WEAPONS, origin));
            }
            for (int i = 0; i < 6; i++) {
                crate.addItem(HbmItemPoolRegistry.getStack(serverLevel, HbmItemPoolIds.POOL_AMMO, origin));
            }
        }
        level().addFreshEntity(crate);
    }

    private void tickCrash() {
        Vec3 motion = getDeltaMovement().add(0.0D, -0.025D, 0.0D);
        setDeltaMovement(motion);
        for (int i = 0; i < 10; i++) {
            ParticleUtil.spawnGasFlame(level(), getX() + random.nextGaussian() * 0.5D - motion.x * 2.0D,
                    getY() + random.nextGaussian() * 0.5D - motion.y * 2.0D,
                    getZ() + random.nextGaussian() * 0.5D - motion.z * 2.0D, 0.0D, 0.1D, 0.0D);
        }
        BlockPos pos = blockPosition();
        if (!level().getBlockState(pos).isAir() || getY() < level().getMinBuildHeight()) {
            ExplosionLarge.explode(level(), getX(), getY(), getZ(), 15.0F, true, false, false, this);
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "hbm:entity.planeCrash",
                    SoundSource.BLOCKS, 25.0F, 1.0F);
            discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide() || source.is(ModDamageSources.NUCLEAR_BLAST) || isRemoved() || health() <= 0.0F) {
            return false;
        }
        float health = health() - amount;
        entityData.set(HEALTH, health);
        if (health <= 0.0F) {
            LegacyParticleCreators.composeSmallExplosion(level(), getX(), getY(), getZ(), 25, 3.5F, 2.0F);
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "hbm:entity.planeShotDown",
                    SoundSource.BLOCKS, 25.0F, 1.0F);
        }
        return true;
    }

    @Override
    public boolean isPickable() {
        return health() > 0.0F;
    }

    private void startClientLoopSound() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.sound.LegacyMovingEntitySound.startForEntity(
                        com.hbm.ntm.sound.LegacySoundIds.resolveLocation("hbm:entity.bomberLoop"), this, "c130",
                        SoundSource.HOSTILE, 2.0F, 1.0F,
                        entity -> entity instanceof C130Entity plane && plane.health() > 0.0F));
    }

    private void updateRotationFromMotion() {
        Vec3 motion = getDeltaMovement();
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal <= 1.0E-7D && Math.abs(motion.y) <= 1.0E-7D) {
            return;
        }
        setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG - 90.0D));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    private void forceCurrentChunk() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        int chunkX = Mth.floor(getX() / 16.0D);
        int chunkZ = Mth.floor(getZ() / 16.0D);
        long packed = ChunkPos.asLong(chunkX, chunkZ);
        if (forcedChunk == packed) return;
        clearForcedChunk();
        ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunkX, chunkZ, true, true);
        forcedChunk = packed;
    }

    private void clearForcedChunk() {
        if (forcedChunk == Long.MIN_VALUE || !(level() instanceof ServerLevel serverLevel)) {
            forcedChunk = Long.MIN_VALUE;
            return;
        }
        ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, (int) (forcedChunk >> 32),
                (int) forcedChunk, false, true);
        forcedChunk = Long.MIN_VALUE;
    }

    public float health() { return entityData.get(HEALTH); }
    public Payload payload() { return Payload.byId(entityData.get(PAYLOAD)); }
    public void setPayload(Payload payload) { entityData.set(PAYLOAD, (byte) payload.ordinal()); }

    @Override protected void defineSynchedData() {
        entityData.define(HEALTH, 50.0F);
        entityData.define(PAYLOAD, (byte) Payload.SUPPLIES.ordinal());
    }
    @Override public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
    @Override public void remove(RemovalReason reason) { clearForcedChunk(); super.remove(reason); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(HEALTH, tag.getFloat("health"));
        entityData.set(PAYLOAD, tag.getByte("payload"));
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("health", health());
        tag.putByte("payload", (byte) payload().ordinal());
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public enum Payload {
        SUPPLIES, WEAPONS, A_FUCKING_FUEL_TRUCK;
        static Payload byId(byte id) { return id >= 0 && id < values().length ? values()[id] : SUPPLIES; }
    }
}

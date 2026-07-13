package com.hbm.ntm.entity.logic;

import com.hbm.config.GeneralConfig;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.particle.LegacyParticleCreators;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.entity.projectile.AirstrikeBombletEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Source-backed 1.7.10 EntityBomber contract for the public airstrike designator variants. */
public class AirstrikeBomberEntity extends Entity {
    public static final int TYPE_CARPET = 0;
    public static final int TYPE_NAPALM = 1;
    public static final int TYPE_CHLORINE = 2;
    public static final int TYPE_ORANGE = 3;
    public static final int TYPE_ATOMIC = 4;

    private static final EntityDataAccessor<Float> HEALTH =
            SynchedEntityData.defineId(AirstrikeBomberEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> STYLE =
            SynchedEntityData.defineId(AirstrikeBomberEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> BOMB_TYPE =
            SynchedEntityData.defineId(AirstrikeBomberEntity.class, EntityDataSerializers.INT);

    private int bombStart = 75;
    private int bombStop = 125;
    private int bombRate = 3;
    private int lifetime = 200;
    private long forcedChunk = Long.MIN_VALUE;

    public AirstrikeBomberEntity(EntityType<? extends AirstrikeBomberEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public AirstrikeBomberEntity(Level level) {
        this(ModEntityTypes.AIRSTRIKE_BOMBER.get(), level);
    }

    public static AirstrikeBomberEntity create(Level level, double x, double y, double z, int type) {
        AirstrikeBomberEntity bomber = new AirstrikeBomberEntity(level);
        bomber.configure(type);
        Vec3 direction = new Vec3(level.random.nextDouble() - 0.5D, 0.0D, level.random.nextDouble() - 0.5D).normalize();
        double speed = GeneralConfig.enableBomberShortMode ? 1.0D : 2.0D;
        direction = direction.scale(speed);
        bomber.setPos(x - direction.x * 100.0D, y + 50.0D, z - direction.z * 100.0D);
        bomber.setDeltaMovement(direction.x, 0.0D, direction.z);
        bomber.updateRotationFromMotion();
        bomber.forceCurrentChunk();
        return bomber;
    }

    private void configure(int requestedType) {
        int type = Mth.clamp(requestedType, TYPE_CARPET, TYPE_ATOMIC);
        entityData.set(BOMB_TYPE, type);
        entityData.set(STYLE, (byte) randomStyle());
        lifetime = 200;
        switch (type) {
            case TYPE_CARPET -> {
                bombStart = 50;
                bombStop = 100;
                bombRate = 2;
            }
            case TYPE_NAPALM -> {
                bombStart = 50;
                bombStop = 100;
                bombRate = 5;
            }
            case TYPE_CHLORINE -> {
                bombStart = 50;
                bombStop = 100;
                bombRate = 4;
            }
            case TYPE_ORANGE -> {
                bombStart = 75;
                bombStop = 125;
                bombRate = 1;
            }
            case TYPE_ATOMIC -> {
                bombStart = 60;
                bombStop = 70;
                bombRate = 65;
                int style = switch (random.nextInt(3)) {
                    case 0 -> 5;
                    case 1 -> 6;
                    default -> 7;
                };
                if (random.nextInt(100) == 0) {
                    style = 8;
                }
                entityData.set(STYLE, (byte) style);
            }
            default -> throw new IllegalStateException("Unhandled airstrike type " + type);
        }
    }

    private int randomStyle() {
        int style = switch (random.nextInt(7)) {
            case 0, 1 -> 1;
            case 2, 3 -> 2;
            case 4 -> 5;
            case 5 -> 6;
            default -> 7;
        };
        if (random.nextInt(100) == 0) {
            style = switch (random.nextInt(4)) {
                case 0 -> 0;
                case 1 -> 3;
                case 2 -> 4;
                default -> 8;
            };
        }
        return style;
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

        if (getHealth() <= 0.0F) {
            tickCrash();
            return;
        }
        if (tickCount > lifetime) {
            discard();
            return;
        }
        if (tickCount > bombStart && tickCount < bombStop && tickCount % bombRate == 0) {
            dropPayload();
        }
    }

    private void dropPayload() {
        if (bombType() == TYPE_ORANGE) {
            LegacySoundPlayer.playSoundEffectRandomPitch(level(), getX() + 0.5D, getY() + 0.5D, getZ() + 0.5D,
                    "random.fizz", SoundSource.BLOCKS, 5.0F, 2.6F, 0.8F);
            ExplosionChaos.spawnPoisonCloud(level(), getX(), getY() - 1.0D, getZ(), 10, 0.5D, 3);
            return;
        }

        LegacySoundPlayer.playSoundEffectRandomPitch(level(), getX() + 0.5D, getY() + 0.5D, getZ() + 0.5D,
                "hbm:entity.bombWhistle", SoundSource.BLOCKS, 10.0F, 0.9F, 0.2F);
        AirstrikeBombletEntity bomblet = new AirstrikeBombletEntity(level());
        bomblet.setPayloadType(bombType());
        bomblet.setPos(getX() + random.nextDouble() - 0.5D, getY() - random.nextDouble(),
                getZ() + random.nextDouble() - 0.5D);
        Vec3 motion = getDeltaMovement();
        if (bombType() == TYPE_CARPET) {
            bomblet.setDeltaMovement(motion.x + random.nextGaussian() * 0.15D, motion.y,
                    motion.z + random.nextGaussian() * 0.15D);
        } else {
            bomblet.setDeltaMovement(motion);
        }
        bomblet.updateRotationFromMotion();
        level().addFreshEntity(bomblet);
    }

    private void tickCrash() {
        setDeltaMovement(getDeltaMovement().add(0.0D, -0.025D, 0.0D));
        BlockPos pos = blockPosition();
        if (!level().getBlockState(pos).isAir() || getY() < level().getMinBuildHeight()) {
            ExplosionLarge.explode(level(), getX(), getY(), getZ(), 15.0F, true, false, false, this);
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "hbm:entity.planeCrash",
                    SoundSource.BLOCKS, 25.0F, 1.0F);
            discard();
        }
    }

    public int bombType() {
        return entityData.get(BOMB_TYPE);
    }

    public int style() {
        return entityData.get(STYLE);
    }

    public float getHealth() {
        return entityData.get(HEALTH);
    }

    @Override
    public boolean isPickable() {
        return getHealth() > 0.0F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide() || source.is(ModDamageSources.NUCLEAR_BLAST) || isRemoved() || getHealth() <= 0.0F) {
            return false;
        }
        float health = getHealth() - amount;
        entityData.set(HEALTH, health);
        if (health <= 0.0F) {
            LegacyParticleCreators.composeSmallExplosion(level(), getX(), getY(), getZ(), 25, 3.5F, 2.0F);
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "hbm:entity.planeShotDown",
                    SoundSource.BLOCKS, 25.0F, 1.0F);
        }
        return true;
    }

    private void startClientLoopSound() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.sound.LegacyMovingEntitySound.startForEntity(
                        com.hbm.ntm.sound.LegacySoundIds.resolveLocation(
                                bombType() <= TYPE_ATOMIC ? "hbm:entity.bomberSmallLoop" : "hbm:entity.bomberLoop"),
                        this, "airstrike_bomber", SoundSource.HOSTILE, 2.0F, 1.0F,
                        entity -> entity instanceof AirstrikeBomberEntity bomber && bomber.getHealth() > 0.0F));
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
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int chunkX = Mth.floor(getX() / 16.0D);
        int chunkZ = Mth.floor(getZ() / 16.0D);
        long packed = ChunkPos.asLong(chunkX, chunkZ);
        if (forcedChunk == packed) {
            return;
        }
        clearForcedChunk();
        ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunkX, chunkZ, true, true);
        forcedChunk = packed;
    }

    private void clearForcedChunk() {
        if (forcedChunk == Long.MIN_VALUE || !(level() instanceof ServerLevel serverLevel)) {
            forcedChunk = Long.MIN_VALUE;
            return;
        }
        int x = (int) (forcedChunk >> 32);
        int z = (int) forcedChunk;
        ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, x, z, false, true);
        forcedChunk = Long.MIN_VALUE;
    }

    @Override
    public void remove(RemovalReason reason) {
        clearForcedChunk();
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(HEALTH, 50.0F);
        entityData.define(STYLE, (byte) 0);
        entityData.define(BOMB_TYPE, TYPE_CARPET);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        bombStart = tag.getInt("bombStart");
        bombStop = tag.getInt("bombStop");
        bombRate = Math.max(1, tag.getInt("bombRate"));
        lifetime = tag.contains("timer") ? tag.getInt("timer") : 200;
        entityData.set(BOMB_TYPE, Mth.clamp(tag.getInt("type"), TYPE_CARPET, TYPE_ATOMIC));
        entityData.set(STYLE, tag.getByte("style"));
        entityData.set(HEALTH, tag.getFloat("health"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("bombStart", bombStart);
        tag.putInt("bombStop", bombStop);
        tag.putInt("bombRate", bombRate);
        tag.putInt("timer", lifetime);
        tag.putInt("type", bombType());
        tag.putByte("style", (byte) style());
        tag.putFloat("health", getHealth());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

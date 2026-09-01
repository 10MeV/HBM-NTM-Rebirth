package com.hbm.ntm.entity.effect;

import com.hbm.ntm.entity.logic.ExplosionChunkLoadingEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkHooks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/*
 * Toroidial Convection Simulation Explosion Effect
 * Tor                             Ex
 */
public class NukeTorexEntity extends ExplosionChunkLoadingEntity implements IEntityAdditionalSpawnData {
    private static final int MAX_WARM_START_CLOUDLETS = 60_000;
    private static final double VEC3_NORMALIZE_EPSILON = 1.0E-4D;

    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(NukeTorexEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> TYPE =
            SynchedEntityData.defineId(NukeTorexEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE =
            SynchedEntityData.defineId(NukeTorexEntity.class, EntityDataSerializers.INT);

    public double coreHeight = 3.0D;
    public double convectionHeight = 3.0D;
    public double torusWidth = 3.0D;
    public double rollerSize = 1.0D;
    public double heat = 1.0D;
    public double lastSpawnY = -1.0D;
    public final List<Cloudlet> cloudlets = new ArrayList<>();
    public int lastRenderSortTick = Integer.MIN_VALUE;
    public boolean didPlaySound;
    public boolean didShake;
    private boolean didSpawnWarpShockwave;
    private int clientSyncedAge = -1;

    public NukeTorexEntity(EntityType<? extends NukeTorexEntity> type, Level level) {
        super(type, level);
        noCulling = true;
        noPhysics = true;
        setNoGravity(true);
        setBoundingBox(getBoundingBox().inflate(0.0D, 50.0D, 0.0D));
    }

    public NukeTorexEntity(Level level) {
        this(ModEntityTypes.NUKE_TOREX.get(), level);
    }

    public static NukeTorexEntity createStandard(Level level, double x, double y, double z, float radius) {
        return create(level, x, y, z, radius, 0);
    }

    public static NukeTorexEntity createBalefire(Level level, double x, double y, double z, float radius) {
        return create(level, x, y, z, radius, 1);
    }

    public static NukeTorexEntity create(Level level, double x, double y, double z, float radius, int cloudType) {
        NukeTorexEntity torex = new NukeTorexEntity(level).setScale(legacyCloudScale(radius));
        torex.setType(cloudType);
        torex.setPos(x, y, z);
        return torex;
    }

    private static float legacyCloudScale(float radius) {
        return Mth.clamp((float) squirt(radius * 0.01D) * 1.5F, 0.5F, 5.0F);
    }

    private static double squirt(double x) {
        return Math.sqrt(x + 1.0D / ((x + 2.0D) * (x + 2.0D))) - 1.0D / (x + 2.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            forceCenterChunk();
            entityData.set(AGE, tickCount);
        } else {
            syncClientAge();
        }

        int maxAge = getMaxAge();

        if (level().isClientSide()) {
            clientVisualTick(true);
        }

        if (!level().isClientSide() && tickCount > maxAge) {
            discard();
        }
    }

    private void spawnCondensationBand(boolean upper, double simulationScale, double cloudScale, int lifetime,
            int radialSamples, int verticalSamples) {
        for (int i = 0; i < radialSamples; i++) {
            for (int j = 0; j < verticalSamples; j++) {
                float angle = (float) (Math.PI * 2.0D * random.nextDouble());
                double radial = upper
                        ? torusWidth + rollerSize * (3.0D + random.nextDouble() * 0.5D)
                        : torusWidth + rollerSize * (5.0D + random.nextDouble());
                float zAngle = (float) (Math.PI / 45.0D * j);
                double rotatedRadial = radial * Mth.cos(zAngle);
                double offsetX = rotatedRadial * Mth.cos(angle);
                double offsetZ = -rotatedRadial * Mth.sin(angle);
                double y = getY() + coreHeight + (upper ? 25.0D + j * cloudScale : -5.0D + j * simulationScale);
                Cloudlet cloud = new Cloudlet(getX() + offsetX, y, getZ() + offsetZ, angle, 0,
                        (int) ((20.0D + tickCount / 10.0D) * (1.0D + random.nextDouble() * 0.1D)),
                        TorexType.CONDENSATION);
                cloud.setScale(0.125F * (float) cloudScale, 3.0F * (float) cloudScale);
                cloudlets.add(cloud);
            }
        }
    }

    /**
     * 1.7.10 clamped the cloudlet's {@code getHeightValue() - 3} target to y=1,
     * which meant one block above that world's bottom.  It must remain bottom-relative
     * in a modern negative-Y dimension instead of becoming absolute Y=1.
     */
    private static int cloudletGroundY(Level level, int x, int z) {
        return Math.max(WorldUtil.legacyGetHeightValue(level, x, z) - 3, WorldUtil.bottomBlockY(level) + 1);
    }

    public NukeTorexEntity setScale(float scale) {
        if (!level().isClientSide()) {
            entityData.set(SCALE, scale);
        }
        coreHeight = coreHeight / 1.5D * scale;
        convectionHeight = convectionHeight / 1.5D * scale;
        torusWidth = torusWidth / 1.5D * scale;
        rollerSize = rollerSize / 1.5D * scale;
        return this;
    }

    public NukeTorexEntity setType(int type) {
        entityData.set(TYPE, type);
        return this;
    }

    public double getSimulationSpeed() {
        int lifetime = getMaxAge();
        int simSlow = lifetime / 4;
        int simStop = lifetime / 2;

        if (tickCount > simStop) {
            return 0.0D;
        }
        if (tickCount > simSlow) {
            return 1.0D - ((double) (tickCount - simSlow) / (double) (simStop - simSlow));
        }
        return 1.0D;
    }

    public float getCloudScale() {
        return entityData.get(SCALE);
    }

    public int getCloudType() {
        return entityData.get(TYPE);
    }

    public int getSyncedAge() {
        return entityData.get(AGE);
    }

    private void syncClientAge() {
        int syncedAge = entityData.get(AGE);
        if (syncedAge <= 0 || syncedAge == clientSyncedAge || syncedAge <= tickCount + 1) {
            clientSyncedAge = syncedAge;
            return;
        }

        xo = getX();
        yo = getY();
        zo = getZ();
        boolean shockwavePassedPlayer = hasShockwaveReachedClientPlayer(syncedAge);
        didPlaySound = shockwavePassedPlayer;
        didShake = shockwavePassedPlayer;
        if (cloudlets.isEmpty() || syncedAge > tickCount + 20) {
            warmStartClientSimulation(syncedAge);
        }
        tickCount = syncedAge;
        clientSyncedAge = syncedAge;
    }

    private void resetClientSimulationState(int visualAge) {
        int age = Math.max(0, visualAge);
        double simulationScale = 1.5D;
        double cloudScale = 1.5D;
        coreHeight = 3.0D / 1.5D * simulationScale;
        convectionHeight = 3.0D / 1.5D * simulationScale;
        torusWidth = 3.0D / 1.5D * simulationScale;
        rollerSize = 1.0D / 1.5D * simulationScale;
        coreHeight += 0.15D / simulationScale * age;
        torusWidth += 0.05D / simulationScale * age;
        rollerSize = torusWidth * 0.35D;
        convectionHeight = coreHeight + rollerSize;
        int maxHeat = (int) (50.0D * cloudScale);
        heat = maxHeat - Math.pow((maxHeat * age) / (double) Math.max(1, getMaxAge()), 1.0D);
        lastSpawnY = cloudletGroundY(level(), Mth.floor(getX()), Mth.floor(getZ()));
        lastRenderSortTick = Integer.MIN_VALUE;
    }

    private void clientVisualTick(boolean spawnSound) {
        clientVisualTick(spawnSound, false);
    }

    private void warmStartClientSimulation(int visualAge) {
        int age = Mth.clamp(visualAge, 0, Math.max(0, getMaxAge()));
        cloudlets.clear();
        resetClientSimulationState(0);
        random.setSeed(clientWarmStartSeed());

        for (int simulatedTick = 1; simulatedTick <= age; simulatedTick++) {
            tickCount = simulatedTick;
            clientVisualTick(false, true);
            if (cloudlets.size() > MAX_WARM_START_CLOUDLETS) {
                cloudlets.subList(0, cloudlets.size() - MAX_WARM_START_CLOUDLETS).clear();
            }
        }

        tickCount = age;
        clientSyncedAge = age;
        lastRenderSortTick = Integer.MIN_VALUE;
    }

    private void clientVisualTick(boolean spawnSound, boolean warmStart) {
        double simulationScale = 1.5D;
        double cloudScale = 1.5D;
        int maxAge = getMaxAge();
        double x = getX();
        double y = getY();
        double z = getZ();

        if (tickCount == 1) {
            setScale((float) simulationScale);
        }

        if (lastSpawnY == -1.0D) {
            lastSpawnY = y - 3.0D;
        }

        if (!warmStart && tickCount < 100) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hbm.ntm.client.NukeTorexClientEffects.updateFlash(level(), tickCount));
        }

        if (!warmStart && !didSpawnWarpShockwave) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hbm.ntm.client.NukeTorexClientEffects.spawnWarpShockwave(x, y, z, tickCount));
            didSpawnWarpShockwave = true;
        }

        int spawnTarget = cloudletGroundY(level(), Mth.floor(x), Mth.floor(z));
        double moveSpeed = 0.5D;
        if (Math.abs(spawnTarget - lastSpawnY) < moveSpeed) {
            lastSpawnY = spawnTarget;
        } else {
            lastSpawnY += moveSpeed * Math.signum(spawnTarget - lastSpawnY);
        }

        double range = (torusWidth - rollerSize) * 0.25D;
        double simSpeed = getSimulationSpeed();
        int toSpawn = (int) Math.ceil(10.0D * simSpeed * simSpeed);
        int lifetime = Math.min((tickCount * tickCount) + 200, maxAge - tickCount + 200);

        for (int i = 0; i < toSpawn; i++) {
            double cloudX = x + random.nextGaussian() * range;
            double cloudZ = z + random.nextGaussian() * range;
            Cloudlet cloud = new Cloudlet(cloudX, lastSpawnY, cloudZ,
                    (float) (random.nextDouble() * Math.PI * 2.0D), 0, lifetime);
            cloud.setScale(1.0F + tickCount * 0.005F * (float) cloudScale, 5.0F * (float) cloudScale);
            cloudlets.add(cloud);
        }

        if (tickCount < 150) {
            int cloudCount = tickCount * 5;
            int shockLife = Math.max(300 - tickCount * 20, 50);
            for (int i = 0; i < cloudCount; i++) {
                float rot = (float) (Math.PI * 2.0D * random.nextDouble());
                double radial = (tickCount * 1.5D + random.nextDouble()) * 1.5D;
                double offsetX = radial * Mth.cos(rot);
                double offsetZ = -radial * Mth.sin(rot);
                cloudlets.add(new Cloudlet(offsetX + x,
                        WorldUtil.legacyGetHeightValue(level(), (int) (offsetX + x) + 1, (int) (offsetZ + z)),
                        offsetZ + z, rot, 0, shockLife, TorexType.SHOCK)
                        .setScale(7.0F, 2.0F)
                        .setMotion(tickCount > 15 ? 0.75D : 0.0D));
            }

            if (spawnSound && !didPlaySound) {
                tryPlayClientSound(x, y, z);
            }
        }

        if (tickCount < 130.0D * simulationScale) {
            lifetime = (int) (lifetime * simulationScale);
            for (int i = 0; i < 2; i++) {
                Cloudlet cloud = new Cloudlet(x, y + coreHeight, z,
                        (float) (random.nextDouble() * Math.PI * 2.0D), 0, lifetime, TorexType.RING);
                cloud.setScale(1.0F + tickCount * 0.0025F * (float) (cloudScale * cloudScale),
                        3.0F * (float) (cloudScale * cloudScale));
                cloudlets.add(cloud);
            }
        }

        if (tickCount > 130.0D * simulationScale && tickCount < 600.0D * simulationScale) {
            spawnCondensationBand(false, simulationScale, cloudScale, lifetime, 20, 4);
        }
        if (tickCount > 200.0D * simulationScale && tickCount < 600.0D * simulationScale) {
            spawnCondensationBand(true, simulationScale, cloudScale, lifetime, 20, 4);
        }

        for (int i = cloudlets.size() - 1; i >= 0; i--) {
            Cloudlet cloud = cloudlets.get(i);
            cloud.update();
            if (cloud.isDead) {
                cloudlets.remove(i);
            }
        }

        coreHeight += 0.15D / simulationScale;
        torusWidth += 0.05D / simulationScale;
        rollerSize = torusWidth * 0.35D;
        convectionHeight = coreHeight + rollerSize;

        int maxHeat = (int) (50.0D * cloudScale);
        heat = maxHeat - Math.pow((maxHeat * tickCount) / (double) maxAge, 1.0D);
    }

    public double getGreying() {
        int lifetime = getMaxAge();
        int greying = lifetime * 3 / 4;
        if (tickCount > greying) {
            return 1.0D + ((double) (tickCount - greying) / (double) (lifetime - greying));
        }
        return 1.0D;
    }

    public float getAlpha() {
        int lifetime = getMaxAge();
        int fadeOut = lifetime * 3 / 4;
        if (tickCount > fadeOut) {
            float factor = (float) (tickCount - fadeOut) / (float) (lifetime - fadeOut);
            return 1.0F - factor;
        }
        return 1.0F;
    }

    public int getMaxAge() {
        return (int) (45.0D * 20.0D * getCloudScale());
    }

    private void tryPlayClientSound(double x, double y, double z) {
        Player player = localClientPlayer();
        if (player == null) {
            return;
        }

        double soundRange = (tickCount * 1.5D + 1.0D) * 1.5D;
        if (player.distanceToSqr(x, y, z) < soundRange * soundRange) {
            LegacySoundPlayer.playLegacyNuclearExplosionClient(level(), x, y, z, 10_000.0F, 1.0F);
            didPlaySound = true;
            applyClientShockwaveShake(player);
        }
    }

    private boolean hasShockwaveReachedClientPlayer(int age) {
        Player player = localClientPlayer();
        if (player == null) {
            return false;
        }
        double soundRange = (age * 1.5D + 1.0D) * 1.5D;
        return player.distanceToSqr(getX(), getY(), getZ()) < soundRange * soundRange;
    }

    public void applyClientShockwaveShake(Player player) {
        if (didShake || player == null) {
            return;
        }
        didShake = DistExecutor.unsafeRunForDist(
                () -> () -> com.hbm.ntm.client.NukeTorexClientEffects.applyShockwaveShake(player),
                () -> () -> false);
    }

    private static Player localClientPlayer() {
        return DistExecutor.unsafeRunForDist(
                () -> () -> com.hbm.ntm.client.NukeTorexClientEffects.localPlayer(),
                () -> () -> null);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SCALE, 1.0F);
        entityData.define(TYPE, 0);
        entityData.define(AGE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setScale(tag.contains("scale") ? tag.getFloat("scale") : 1.0F);
        setType(tag.getInt("type"));
        tickCount = Math.max(0, tag.getInt("ticksExisted"));
        entityData.set(AGE, tickCount);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("scale", getCloudScale());
        tag.putInt("type", getCloudType());
        tag.putInt("ticksExisted", tickCount);
    }

    @Override
    public boolean shouldBeSaved() {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(getCloudScale());
        buffer.writeVarInt(getCloudType());
        buffer.writeVarInt(tickCount);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        entityData.set(SCALE, buffer.readFloat());
        entityData.set(TYPE, buffer.readVarInt());
        int age = Math.max(0, buffer.readVarInt());
        entityData.set(AGE, age);
        tickCount = age;
        clientSyncedAge = age;
        random.setSeed(clientWarmStartSeed());
        if (age > 0) {
            boolean shockwavePassedPlayer = hasShockwaveReachedClientPlayer(age);
            didPlaySound = shockwavePassedPlayer;
            didShake = shockwavePassedPlayer;
            warmStartClientSimulation(age);
        }
    }

    private long clientWarmStartSeed() {
        return getUUID().getMostSignificantBits()
                ^ getUUID().getLeastSignificantBits()
                ^ (((long) getCloudType()) << 32)
                ^ (((long) Mth.floor(getX())) << 32)
                ^ (Mth.floor(getZ()) & 0xFFFFFFFFL);
    }

    public enum TorexType {
        STANDARD,
        SHOCK,
        RING,
        CONDENSATION
    }

    public class Cloudlet {
        public double posX;
        public double posY;
        public double posZ;
        public double prevPosX;
        public double prevPosY;
        public double prevPosZ;
        public double motionX;
        public double motionY;
        public double motionZ;
        public int age;
        public int cloudletLife;
        public float angle;
        public boolean isDead;
        public float rangeMod = 1.0F;
        public float colorMod = 1.0F;
        public double colorR;
        public double colorG;
        public double colorB;
        public double prevColorR;
        public double prevColorG;
        public double prevColorB;
        public double renderSortDistanceSq;
        public TorexType type;
        private float startingScale = 1.0F;
        private float growingScale = 5.0F;
        private double motionMult = 1.0D;
        private double computedMotionX;
        private double computedMotionY;
        private double computedMotionZ;

        public Cloudlet(double x, double y, double z, float angle, int age, int maxAge) {
            this(x, y, z, angle, age, maxAge, TorexType.STANDARD);
        }

        public Cloudlet(double x, double y, double z, float angle, int age, int maxAge, TorexType type) {
            posX = x;
            posY = y;
            posZ = z;
            prevPosX = x;
            prevPosY = y;
            prevPosZ = z;
            this.age = age;
            cloudletLife = maxAge;
            this.angle = angle;
            rangeMod = 0.3F + random.nextFloat() * 0.7F;
            colorMod = 0.8F + random.nextFloat() * 0.2F;
            this.type = type;
            updateColor();
            prevColorR = colorR;
            prevColorG = colorG;
            prevColorB = colorB;
        }

        private void update() {
            age++;
            if (age > cloudletLife) {
                isDead = true;
            }

            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;

            double simDeltaX = NukeTorexEntity.this.getX() - posX;
            double simDeltaZ = NukeTorexEntity.this.getZ() - posZ;
            double simPosX = NukeTorexEntity.this.getX() + Math.sqrt(simDeltaX * simDeltaX + simDeltaZ * simDeltaZ);
            double simPosZ = NukeTorexEntity.this.getZ();

            if (type == TorexType.STANDARD) {
                computeConvectionMotion(simPosX, simPosZ);
                double convectionX = computedMotionX;
                double convectionY = computedMotionY;
                double convectionZ = computedMotionZ;
                computeLiftMotion(simPosX);
                double factor = Mth.clamp((posY - NukeTorexEntity.this.getY()) / NukeTorexEntity.this.coreHeight, 0.0D, 1.0D);
                motionX = convectionX * factor + computedMotionX * (1.0D - factor);
                motionY = convectionY * factor + computedMotionY * (1.0D - factor);
                motionZ = convectionZ * factor + computedMotionZ * (1.0D - factor);
            } else if (type == TorexType.SHOCK) {
                double factor = Mth.clamp((posY - NukeTorexEntity.this.getY()) / NukeTorexEntity.this.coreHeight, 0.0D, 1.0D);
                motionX = Mth.cos(angle) * factor;
                motionY = 0.0D;
                motionZ = -Mth.sin(angle) * factor;
            } else if (type == TorexType.RING) {
                computeRingMotion(simPosX, simPosZ);
                motionX = computedMotionX;
                motionY = computedMotionY;
                motionZ = computedMotionZ;
            } else if (type == TorexType.CONDENSATION) {
                computeCondensationMotion();
                motionX = computedMotionX;
                motionY = computedMotionY;
                motionZ = computedMotionZ;
            }

            double mult = motionMult * getSimulationSpeed();
            posX += motionX * mult;
            posY += motionY * mult;
            posZ += motionZ * mult;

            updateColor();
        }

        private void computeCondensationMotion() {
            double speed = 0.00002D * NukeTorexEntity.this.tickCount;
            setComputedMotion((posX - NukeTorexEntity.this.getX()) * speed, 0.0D,
                    (posZ - NukeTorexEntity.this.getZ()) * speed);
        }

        private void computeRingMotion(double simPosX, double simPosZ) {
            if (simPosX > NukeTorexEntity.this.getX() + torusWidth * 2.0D) {
                setComputedMotion(0.0D, 0.0D, 0.0D);
                return;
            }

            double torusPosX = NukeTorexEntity.this.getX() + torusWidth;
            double torusPosY = NukeTorexEntity.this.getY() + coreHeight * 0.5D;
            double torusPosZ = NukeTorexEntity.this.getZ();
            double deltaX = torusPosX - simPosX;
            double deltaY = torusPosY - posY;
            double deltaZ = torusPosZ - simPosZ;
            double roller = rollerSize * rangeMod * 0.25D;
            double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / roller - 1.0D;
            if (Math.abs(dist) < 1.0E-6D) {
                setComputedMotion(0.0D, 0.0D, 0.0D);
                return;
            }

            double func = 1.0D - Math.pow(Math.E, -dist);
            float turn = (float) (func * Math.PI * 0.5D);
            double rotX = -deltaX / dist;
            double rotY = -deltaY / dist;
            double rotZ = -deltaZ / dist;
            double cos = Mth.cos(turn);
            double sin = Mth.sin(turn);
            double rotatedX = rotX * cos + rotY * sin;
            double rotatedY = rotY * cos - rotX * sin;
            setNormalizedMotion((torusPosX + rotatedX - simPosX) * 0.001D,
                    (torusPosY + rotatedY - posY) * 0.001D,
                    (torusPosZ + rotZ - simPosZ) * 0.001D);
            rotateComputedMotionAroundY();
        }

        private void computeConvectionMotion(double simPosX, double simPosZ) {
            double torusPosX = NukeTorexEntity.this.getX() + torusWidth;
            double torusPosY = NukeTorexEntity.this.getY() + coreHeight;
            double torusPosZ = NukeTorexEntity.this.getZ();
            double deltaX = torusPosX - simPosX;
            double deltaY = torusPosY - posY;
            double deltaZ = torusPosZ - simPosZ;
            double roller = rollerSize * rangeMod;
            double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / roller - 1.0D;
            if (Math.abs(dist) < 1.0E-6D) {
                setComputedMotion(0.0D, 0.0D, 0.0D);
                return;
            }

            double func = 1.0D - Math.pow(Math.E, -dist);
            float turn = (float) (func * Math.PI * 0.5D);
            double rotX = -deltaX / dist;
            double rotY = -deltaY / dist;
            double rotZ = -deltaZ / dist;
            double cos = Mth.cos(turn);
            double sin = Mth.sin(turn);
            double rotatedX = rotX * cos + rotY * sin;
            double rotatedY = rotY * cos - rotX * sin;
            setNormalizedMotion(torusPosX + rotatedX - simPosX,
                    torusPosY + rotatedY - posY,
                    torusPosZ + rotZ - simPosZ);
            rotateComputedMotionAroundY();
        }

        private void computeLiftMotion(double simPosX) {
            double scale = Mth.clamp(1.0D - (simPosX - (NukeTorexEntity.this.getX() + torusWidth)), 0.0D, 1.0D);
            setNormalizedMotion(NukeTorexEntity.this.getX() - posX,
                    (NukeTorexEntity.this.getY() + convectionHeight) - posY,
                    NukeTorexEntity.this.getZ() - posZ);
            computedMotionX *= scale;
            computedMotionY *= scale;
            computedMotionZ *= scale;
        }

        private void setComputedMotion(double x, double y, double z) {
            computedMotionX = x;
            computedMotionY = y;
            computedMotionZ = z;
        }

        private void setNormalizedMotion(double x, double y, double z) {
            double length = Math.sqrt(x * x + y * y + z * z);
            if (length < VEC3_NORMALIZE_EPSILON) {
                setComputedMotion(0.0D, 0.0D, 0.0D);
                return;
            }
            setComputedMotion(x / length, y / length, z / length);
        }

        private void rotateComputedMotionAroundY() {
            double cos = Mth.cos(angle);
            double sin = Mth.sin(angle);
            double x = computedMotionX;
            double z = computedMotionZ;
            computedMotionX = x * cos + z * sin;
            computedMotionZ = z * cos - x * sin;
        }

        private void updateColor() {
            prevColorR = colorR;
            prevColorG = colorG;
            prevColorB = colorB;

            double exX = NukeTorexEntity.this.getX();
            double exY = NukeTorexEntity.this.getY() + NukeTorexEntity.this.coreHeight;
            double exZ = NukeTorexEntity.this.getZ();
            double distX = exX - posX;
            double distY = exY - posY;
            double distZ = exZ - posZ;
            double distSq = (distX * distX + distY * distY + distZ * distZ) / NukeTorexEntity.this.heat;
            double col = 2.0D / Math.max(Math.sqrt(distSq), 1.0D);

            int cloudType = NukeTorexEntity.this.getCloudType();
            if (cloudType == 1) {
                colorR = Math.max(col, 0.25D);
                colorG = Math.max(col * 2.0D, 0.25D);
                colorB = Math.max(col * 0.5D, 0.25D);
            } else if (cloudType == 2) {
                Color hsb = Color.getHSBColor(angle / 2.0F / (float) Math.PI, 1.0F, 1.0F);
                if (type == TorexType.RING) {
                    colorR = Math.max(col, 0.25D);
                    colorG = Math.max(col, 0.25D);
                    colorB = Math.max(col, 0.25D);
                } else {
                    colorR = hsb.getRed() / 255.0D;
                    colorG = hsb.getGreen() / 255.0D;
                    colorB = hsb.getBlue() / 255.0D;
                }
            } else {
                colorR = Math.max(col * 2.0D, 0.25D);
                colorG = Math.max(col * 1.5D, 0.25D);
                colorB = Math.max(col * 0.5D, 0.25D);
            }
        }

        public double getInterpRenderX(float partialTick) {
            return interpRenderCoord(partialTick, prevPosX, posX, NukeTorexEntity.this.getX());
        }

        public double getInterpRenderY(float partialTick) {
            return interpRenderCoord(partialTick, prevPosY, posY, NukeTorexEntity.this.getY());
        }

        public double getInterpRenderZ(float partialTick) {
            return interpRenderCoord(partialTick, prevPosZ, posZ, NukeTorexEntity.this.getZ());
        }

        private double interpRenderCoord(float partialTick, double previous, double current, double origin) {
            double value = Mth.lerp(partialTick, previous, current);
            if (type == TorexType.SHOCK) {
                return value;
            }
            float scale = NukeTorexEntity.this.getCloudScale();
            return (value - origin) * scale + origin;
        }

        public double getInterpRenderRed(float partialTick) {
            return interpRenderColor(partialTick, 0);
        }

        public double getInterpRenderGreen(float partialTick) {
            return interpRenderColor(partialTick, 1);
        }

        public double getInterpRenderBlue(float partialTick) {
            return interpRenderColor(partialTick, 2);
        }

        private double interpRenderColor(float partialTick, int component) {
            if (type == TorexType.CONDENSATION) {
                return 1.0D;
            }

            double greying = NukeTorexEntity.this.getGreying();
            if (type == TorexType.RING) {
                greying += 1.0D;
            }
            return switch (component) {
                case 0 -> Mth.lerp(partialTick, prevColorR, colorR) * greying;
                case 1 -> Mth.lerp(partialTick, prevColorG, colorG) * greying;
                default -> Mth.lerp(partialTick, prevColorB, colorB) * greying;
            };
        }

        public float getAlpha() {
            float alpha = (1.0F - ((float) age / (float) cloudletLife)) * NukeTorexEntity.this.getAlpha();
            if (type == TorexType.CONDENSATION) {
                alpha *= 0.25F;
            }
            return Mth.clamp(alpha, 0.0F, 1.0F);
        }

        public float getScale() {
            float base = startingScale + ((float) age / (float) cloudletLife) * growingScale;
            if (type != TorexType.SHOCK) {
                base *= NukeTorexEntity.this.getCloudScale();
            }
            return base;
        }

        public Cloudlet setScale(float start, float grow) {
            startingScale = start;
            growingScale = grow;
            return this;
        }

        public Cloudlet setMotion(double mult) {
            motionMult = mult;
            return this;
        }
    }
}

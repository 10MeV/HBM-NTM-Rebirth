package com.hbm.ntm.entity.effect;

import com.hbm.ntm.client.NukeHudEffects;
import com.hbm.ntm.client.render.HbmRenderEffects;
import com.hbm.ntm.entity.logic.ExplosionChunkLoadingEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
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
                Vec3 vec = rotateY(rotateZ(new Vec3(radial, 0.0D, 0.0D), (float) (Math.PI / 45.0D * j)), angle);
                double y = getY() + coreHeight + (upper ? 25.0D + j * cloudScale : -5.0D + j * simulationScale);
                Cloudlet cloud = new Cloudlet(getX() + vec.x, y, getZ() + vec.z, angle, 0,
                        (int) ((20.0D + tickCount / 10.0D) * (1.0D + random.nextDouble() * 0.1D)),
                        TorexType.CONDENSATION);
                cloud.setScale(0.125F * (float) cloudScale, 3.0F * (float) cloudScale);
                cloudlets.add(cloud);
            }
        }
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
        lastSpawnY = Math.max(level().getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(getX()), Mth.floor(getZ())) - 3, 1);
        lastRenderSortTick = Integer.MIN_VALUE;
    }

    private void clientVisualTick(boolean spawnSound) {
        clientVisualTick(spawnSound, false);
    }

    @OnlyIn(Dist.CLIENT)
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
            int flashTime = level() instanceof ClientLevel clientLevel
                    ? Math.max(clientLevel.getSkyFlashTime(), 4)
                    : 4;
            level().setSkyFlashTime(flashTime);
            if (tickCount < 10) {
                NukeHudEffects.triggerFlash();
            }
        }

        if (!warmStart && !didSpawnWarpShockwave) {
            HbmRenderEffects.spawnTorexWarpShockwave(x, y, z, tickCount);
            didSpawnWarpShockwave = true;
        }

        int spawnTarget = Math.max(level().getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(x), Mth.floor(z)) - 3, 1);
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
                Vec3 vec = rotateY(new Vec3((tickCount * 1.5D + random.nextDouble()) * 1.5D, 0.0D, 0.0D), rot);
                cloudlets.add(new Cloudlet(vec.x + x,
                        level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) (vec.x + x) + 1, (int) (vec.z + z)),
                        vec.z + z, rot, 0, shockLife, TorexType.SHOCK)
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

    @OnlyIn(Dist.CLIENT)
    private void tryPlayClientSound(double x, double y, double z) {
        Player player = Minecraft.getInstance().player;
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

    @OnlyIn(Dist.CLIENT)
    private boolean hasShockwaveReachedClientPlayer(int age) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        double soundRange = (age * 1.5D + 1.0D) * 1.5D;
        return player.distanceToSqr(getX(), getY(), getZ()) < soundRange * soundRange;
    }

    @OnlyIn(Dist.CLIENT)
    public void applyClientShockwaveShake(Player player) {
        if (didShake || player == null) {
            return;
        }
        if (!NukeHudEffects.triggerShake()) {
            return;
        }
        player.animateHurt(0.0F);
        player.hurtTime = 15;
        player.hurtDuration = 15;
        didShake = true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
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

    private static Vec3 rotateY(Vec3 vec, float angle) {
        double cos = Mth.cos(angle);
        double sin = Mth.sin(angle);
        return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    private static Vec3 rotateZ(Vec3 vec, float angle) {
        double cos = Mth.cos(angle);
        double sin = Mth.sin(angle);
        return new Vec3(vec.x * cos + vec.y * sin, vec.y * cos - vec.x * sin, vec.z);
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
        public Vec3 color;
        public Vec3 prevColor;
        public double renderSortDistanceSq;
        public TorexType type;
        private float startingScale = 1.0F;
        private float growingScale = 5.0F;
        private double motionMult = 1.0D;

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
            prevColor = color;
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
                Vec3 convection = getConvectionMotion(simPosX, simPosZ);
                Vec3 lift = getLiftMotion(simPosX);
                double factor = Mth.clamp((posY - NukeTorexEntity.this.getY()) / NukeTorexEntity.this.coreHeight, 0.0D, 1.0D);
                motionX = convection.x * factor + lift.x * (1.0D - factor);
                motionY = convection.y * factor + lift.y * (1.0D - factor);
                motionZ = convection.z * factor + lift.z * (1.0D - factor);
            } else if (type == TorexType.SHOCK) {
                double factor = Mth.clamp((posY - NukeTorexEntity.this.getY()) / NukeTorexEntity.this.coreHeight, 0.0D, 1.0D);
                Vec3 motion = rotateY(new Vec3(1.0D, 0.0D, 0.0D), angle);
                motionX = motion.x * factor;
                motionY = motion.y * factor;
                motionZ = motion.z * factor;
            } else if (type == TorexType.RING) {
                Vec3 motion = getRingMotion(simPosX, simPosZ);
                motionX = motion.x;
                motionY = motion.y;
                motionZ = motion.z;
            } else if (type == TorexType.CONDENSATION) {
                Vec3 motion = getCondensationMotion();
                motionX = motion.x;
                motionY = motion.y;
                motionZ = motion.z;
            }

            double mult = motionMult * getSimulationSpeed();
            posX += motionX * mult;
            posY += motionY * mult;
            posZ += motionZ * mult;

            updateColor();
        }

        private Vec3 getCondensationMotion() {
            double speed = 0.00002D * NukeTorexEntity.this.tickCount;
            return new Vec3((posX - NukeTorexEntity.this.getX()) * speed, 0.0D,
                    (posZ - NukeTorexEntity.this.getZ()) * speed);
        }

        private Vec3 getRingMotion(double simPosX, double simPosZ) {
            if (simPosX > NukeTorexEntity.this.getX() + torusWidth * 2.0D) {
                return Vec3.ZERO;
            }

            Vec3 torusPos = new Vec3(NukeTorexEntity.this.getX() + torusWidth,
                    NukeTorexEntity.this.getY() + coreHeight * 0.5D, NukeTorexEntity.this.getZ());
            Vec3 delta = new Vec3(torusPos.x - simPosX, torusPos.y - posY, torusPos.z - simPosZ);
            double roller = rollerSize * rangeMod * 0.25D;
            double dist = delta.length() / roller - 1.0D;
            if (Math.abs(dist) < 1.0E-6D) {
                return Vec3.ZERO;
            }

            double func = 1.0D - Math.pow(Math.E, -dist);
            float turn = (float) (func * Math.PI * 0.5D);
            Vec3 rot = rotateZ(new Vec3(-delta.x / dist, -delta.y / dist, -delta.z / dist), turn);
            Vec3 motion = new Vec3(torusPos.x + rot.x - simPosX, torusPos.y + rot.y - posY,
                    torusPos.z + rot.z - simPosZ).scale(0.001D).normalize();
            return rotateY(motion, angle);
        }

        private Vec3 getConvectionMotion(double simPosX, double simPosZ) {
            Vec3 torusPos = new Vec3(NukeTorexEntity.this.getX() + torusWidth,
                    NukeTorexEntity.this.getY() + coreHeight, NukeTorexEntity.this.getZ());
            Vec3 delta = new Vec3(torusPos.x - simPosX, torusPos.y - posY, torusPos.z - simPosZ);
            double roller = rollerSize * rangeMod;
            double dist = delta.length() / roller - 1.0D;
            if (Math.abs(dist) < 1.0E-6D) {
                return Vec3.ZERO;
            }

            double func = 1.0D - Math.pow(Math.E, -dist);
            float turn = (float) (func * Math.PI * 0.5D);
            Vec3 rot = rotateZ(new Vec3(-delta.x / dist, -delta.y / dist, -delta.z / dist), turn);
            Vec3 motion = new Vec3(torusPos.x + rot.x - simPosX, torusPos.y + rot.y - posY,
                    torusPos.z + rot.z - simPosZ).normalize();
            return rotateY(motion, angle);
        }

        private Vec3 getLiftMotion(double simPosX) {
            double scale = Mth.clamp(1.0D - (simPosX - (NukeTorexEntity.this.getX() + torusWidth)), 0.0D, 1.0D);
            Vec3 motion = new Vec3(NukeTorexEntity.this.getX() - posX,
                    (NukeTorexEntity.this.getY() + convectionHeight) - posY,
                    NukeTorexEntity.this.getZ() - posZ).normalize();
            return motion.scale(scale);
        }

        private void updateColor() {
            prevColor = color;

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
                color = new Vec3(Math.max(col, 0.25D), Math.max(col * 2.0D, 0.25D), Math.max(col * 0.5D, 0.25D));
            } else if (cloudType == 2) {
                Color hsb = Color.getHSBColor(angle / 2.0F / (float) Math.PI, 1.0F, 1.0F);
                if (type == TorexType.RING) {
                    color = new Vec3(Math.max(col, 0.25D), Math.max(col, 0.25D), Math.max(col, 0.25D));
                } else {
                    color = new Vec3(hsb.getRed() / 255.0D, hsb.getGreen() / 255.0D, hsb.getBlue() / 255.0D);
                }
            } else {
                color = new Vec3(Math.max(col * 2.0D, 0.25D), Math.max(col * 1.5D, 0.25D), Math.max(col * 0.5D, 0.25D));
            }
        }

        public Vec3 getInterpPos(float partialTick) {
            float scale = NukeTorexEntity.this.getCloudScale();
            Vec3 base = new Vec3(Mth.lerp(partialTick, prevPosX, posX), Mth.lerp(partialTick, prevPosY, posY),
                    Mth.lerp(partialTick, prevPosZ, posZ));
            if (type != TorexType.SHOCK) {
                base = new Vec3((base.x - NukeTorexEntity.this.getX()) * scale + NukeTorexEntity.this.getX(),
                        (base.y - NukeTorexEntity.this.getY()) * scale + NukeTorexEntity.this.getY(),
                        (base.z - NukeTorexEntity.this.getZ()) * scale + NukeTorexEntity.this.getZ());
            }
            return base;
        }

        public Vec3 getInterpColor(float partialTick) {
            if (type == TorexType.CONDENSATION) {
                return new Vec3(1.0D, 1.0D, 1.0D);
            }

            double greying = NukeTorexEntity.this.getGreying();
            if (type == TorexType.RING) {
                greying += 1.0D;
            }
            Vec3 previous = prevColor == null ? color : prevColor;
            return new Vec3(Mth.lerp(partialTick, previous.x, color.x) * greying,
                    Mth.lerp(partialTick, previous.y, color.y) * greying,
                    Mth.lerp(partialTick, previous.z, color.z) * greying);
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

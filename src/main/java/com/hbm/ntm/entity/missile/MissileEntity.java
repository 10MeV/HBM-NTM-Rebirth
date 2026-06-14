package com.hbm.ntm.entity.missile;

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
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class MissileEntity extends Entity implements LegacyMissileRadarDetectable, IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HEALTH =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.FLOAT);

    private double startX;
    private double startZ;
    private double targetX;
    private double targetZ;
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

    public MissileEntity(EntityType<? extends MissileEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noCulling = true;
    }

    public MissileEntity(EntityType<? extends MissileEntity> type, Level level, Variant variant) {
        this(type, level);
        setVariant(variant);
        setHealth(variant.health());
    }

    public void configureLaunch(double startX, double startY, double startZ, double targetX, double targetZ) {
        this.startX = startX;
        this.startZ = startZ;
        this.targetX = targetX;
        this.targetZ = targetZ;
        setPos(startX, startY, startZ);
        setDeltaMovement(0.0D, 2.0D, 0.0D);
        double distance = Math.max(1.0D, Math.sqrt((targetX - startX) * (targetX - startX) + (targetZ - startZ) * (targetZ - startZ)));
        this.decelY = 2.0D / distance;
        this.accelXZ = 1.0D / distance;
        this.velocity = 0.0D;
        setYRot((float) (Mth.atan2(targetX - startX, targetZ - startZ) * Mth.RAD_TO_DEG));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            tickClientInterpolation();
            if (hasPropulsion()) {
                spawnContrail();
            }
            return;
        }

        Vec3 motion = getDeltaMovement();
        HitResult hit = traceNextBlockHit();
        if (hit.getType() != HitResult.Type.MISS) {
            setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
            onMissileImpact(hit);
            discard();
            return;
        }

        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
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
        Vec3 start = position();
        Vec3 end = start.add(getDeltaMovement());
        return level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    protected void updateFlight() {
        Vec3 motion = getDeltaMovement();
        if (hasPropulsion()) {
            velocity += Mth.clamp(tickCount / 60.0D * 0.05D, 0.0D, 0.05D);
            velocity = Math.min(velocity, 4.0D);
            double deltaX = targetX - startX;
            double deltaZ = targetZ - startZ;
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double normX = distance > 1.0E-7D ? deltaX / distance : 0.0D;
            double normZ = distance > 1.0E-7D ? deltaZ / distance : 0.0D;
            double factor = motion.y > 0.0D ? accelXZ * velocity : -accelXZ * velocity;
            motion = new Vec3(
                    motion.x + normX * factor,
                    motion.y - decelY * velocity,
                    motion.z + normZ * factor);
        } else {
            motion = new Vec3(motion.x * 0.99D, Math.max(motion.y - 0.05D, -1.5D), motion.z * 0.99D);
        }
        setDeltaMovement(motion);
    }

    protected void updateRotationFromMotion(Vec3 motion) {
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal > 1.0E-4D || Math.abs(motion.y) > 1.0E-4D) {
            float yaw = (float) (Mth.atan2(targetX - getX(), targetZ - getZ()) * Mth.RAD_TO_DEG);
            float pitch = (float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG) - 90.0F;
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
    }

    protected boolean hasPropulsion() {
        return true;
    }

    protected void onMissileImpact(HitResult hit) {
        Variant variant = variant();
        switch (variant.impact()) {
            case STANDARD -> {
                explodeLegacyStandard(variant, false);
            }
            case FIRE -> {
                explodeLegacyStandard(variant, true);
                if (variant.igniteRadius() > 0) {
                    ExplosionChaos.igniteFlammableBlocks(level(), Mth.floor(getX() + 0.5D),
                            Mth.floor(getY() + 0.5D), Mth.floor(getZ() + 0.5D), variant.igniteRadius());
                }
                if (variant.igniteAllRadius() > 0) {
                    ExplosionChaos.igniteAllBlocks(level(), Mth.floor(getX()),
                            Mth.floor(getY()), Mth.floor(getZ()), variant.igniteAllRadius());
                }
            }
            case DECOY -> level().explode(this, getX(), getY(), getZ(), variant.explosionStrength(),
                    false, Level.ExplosionInteraction.NONE);
            case CLUSTER -> {
                level().explode(this, getX(), getY(), getZ(), variant.explosionStrength(),
                        false, Level.ExplosionInteraction.BLOCK);
                spawnClusterSubmunitions(variant.clusterCount());
            }
            case BUSTER -> {
                for (int i = 0; i < variant.busterDepth(); i++) {
                    level().explode(this, getX(), getY() - i, getZ(), variant.explosionStrength(),
                            false, Level.ExplosionInteraction.BLOCK);
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
            case SCHRABIDIUM -> NuclearExplosionUtil.spawnAntiSchrabidium(level(), getX(), getY(), getZ());
            case BLACK_HOLE -> {
                level().explode(this, getX(), getY(), getZ(), 1.5F, false, Level.ExplosionInteraction.BLOCK);
                BlackHoleEntity blackHole = new BlackHoleEntity(level(), 1.5F);
                blackHole.setPos(getX(), getY(), getZ());
                level().addFreshEntity(blackHole);
            }
            case TAINT -> {
                level().explode(this, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                        5.0F, false, Level.ExplosionInteraction.BLOCK);
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
            case DOOMSDAY -> NuclearExplosionUtil.spawnMissileDoomsday(level(), getX(), getY(), getZ());
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
                getYRot() * Mth.DEG_TO_RAD, getXRot() * Mth.DEG_TO_RAD,
                (float) Math.PI * 0.25F, (float) Math.PI * 0.25F, 1.0F, this);
    }

    private void explodeClusterInFlight() {
        Variant variant = variant();
        level().explode(this, getX(), getY(), getZ(), variant.explosionStrength(),
                false, Level.ExplosionInteraction.BLOCK);
        spawnClusterSubmunitions(variant.clusterCount());
    }

    protected double flightVelocity() {
        return velocity;
    }

    public void killMissile() {
        if (!level().isClientSide) {
            ExplosionLarge.explode(level(), getX(), getY(), getZ(), 5.0F, true, false, true, this);
            ExplosionLarge.spawnShrapnelShower(level(), getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z, 15, 0.075D, this);
            ExplosionLarge.spawnMissileDebris(level(), getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z, 0.25D,
                    variant().debris(), variant().rareDebrisDrop());
            discard();
        }
    }

    protected void spawnContrail() {
        spawnContrailWithOffset(0.0D, 0.0D, 0.0D);
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
        return 1.0F;
    }

    private Vec3 legacyThrustVector() {
        double pitch = Math.toRadians(getXRot());
        double yaw = Math.toRadians(getYRot() + 90.0F);
        double sinPitch = Math.sin(pitch);
        double cosPitch = Math.cos(pitch);
        double x = -sinPitch * Math.sin(yaw);
        double y = cosPitch;
        double z = sinPitch * Math.cos(yaw);
        return new Vec3(x, y, z);
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

    @Override
    protected void defineSynchedData() {
        entityData.define(VARIANT, Variant.GENERIC.ordinal());
        entityData.define(HEALTH, Variant.GENERIC.health());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setDeltaMovement(tag.getDouble("moX"), tag.getDouble("moY"), tag.getDouble("moZ"));
        setPos(tag.getDouble("poX"), tag.getDouble("poY"), tag.getDouble("poZ"));
        decelY = tag.getDouble("decel");
        accelXZ = tag.getDouble("accel");
        targetX = tag.getDouble("tX");
        targetZ = tag.getDouble("tZ");
        startX = tag.getDouble("sX");
        startZ = tag.getDouble("sZ");
        velocity = tag.getDouble("veloc");
        cluster = tag.getBoolean("cluster");
        if (!tag.contains("cluster")) {
            cluster = variant().impact() == Impact.CLUSTER;
        }
        if (tag.contains("variant")) {
            setVariant(Variant.byId(tag.getInt("variant")));
        }
        if (tag.contains("health")) {
            setHealth(tag.getFloat("health"));
        }
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
        tag.putDouble("tX", targetX);
        tag.putDouble("tZ", targetZ);
        tag.putDouble("sX", startX);
        tag.putDouble("sZ", startZ);
        tag.putDouble("veloc", velocity);
        tag.putBoolean("cluster", cluster);
        tag.putInt("variant", variant().ordinal());
        tag.putFloat("health", health());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeDouble(startX);
        buffer.writeDouble(startZ);
        buffer.writeDouble(targetX);
        buffer.writeDouble(targetZ);
        buffer.writeDouble(velocity);
        buffer.writeDouble(decelY);
        buffer.writeDouble(accelXZ);
        Vec3 motion = getDeltaMovement();
        buffer.writeDouble(motion.x);
        buffer.writeDouble(motion.y);
        buffer.writeDouble(motion.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        startX = additionalData.readDouble();
        startZ = additionalData.readDouble();
        targetX = additionalData.readDouble();
        targetZ = additionalData.readDouble();
        velocity = additionalData.readDouble();
        decelY = additionalData.readDouble();
        accelXZ = additionalData.readDouble();
        setDeltaMovement(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
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

    public enum Variant {
        GENERIC(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, 25.0F,
                Impact.STANDARD, 15.0F, 24, 0, 0, 0, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, 30.0F,
                Impact.STANDARD, 30.0F, 32, 0, 0, 0, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        BURST(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, 35.0F,
                Impact.STANDARD, 50.0F, 48, 0, 0, 0, 0, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        DECOY(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER4, 25.0F,
                Impact.DECOY, 4.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        INCENDIARY(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, 25.0F,
                Impact.FIRE, 15.0F, 24, 0, 0, 0, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        CLUSTER(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, 25.0F,
                Impact.CLUSTER, 5.0F, 0, 0, 0, 25, 0, 0,
                "plate_titanium", 4, "thruster_small", 1),
        BUSTER(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.TIER1, 25.0F,
                Impact.BUSTER, 5.0F, 0, 0, 0, 0, 15, 5,
                "plate_titanium", 4, "thruster_small", 1),
        INCENDIARY_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, 30.0F,
                Impact.FIRE, 30.0F, 32, 25, 0, 0, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        CLUSTER_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, 30.0F,
                Impact.CLUSTER, 15.0F, 0, 0, 0, 50, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        BUSTER_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, 30.0F,
                Impact.BUSTER, 7.5F, 0, 0, 0, 0, 20, 8,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        INFERNO(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, 35.0F,
                Impact.FIRE, 50.0F, 48, 25, 10, 0, 0, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        RAIN(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, 35.0F,
                Impact.CLUSTER, 25.0F, 0, 0, 0, 100, 0, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        DRILL(MissileItem.FormFactor.HUGE, LegacyMissileRadarProfile.TIER3, 35.0F,
                Impact.DRILL, 10.0F, 12, 0, 0, 0, 30, 0,
                "plate_steel", 16, "plate_titanium", 10, "thruster_large", 1),
        STEALTH(MissileItem.FormFactor.V2, LegacyMissileRadarProfile.STEALTH, 25.0F,
                Impact.STANDARD, 20.0F, 24, 0, 0, 0, 0, 0,
                "bolt_steel", 4),
        EMP(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, 20.0F,
                Impact.EMP_BLAST, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine", 4, "plate_titanium", 4, "shell", 2, "ducttape", 1),
        EMP_STRONG(MissileItem.FormFactor.STRONG, LegacyMissileRadarProfile.TIER2, 30.0F,
                Impact.EMP_LOGIC, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_steel", 10, "plate_titanium", 6, "thruster_medium", 1),
        MICRO(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, 20.0F,
                Impact.NUKE_MICRO, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine", 4, "plate_titanium", 4, "shell", 2, "ducttape", 1),
        SCHRABIDIUM(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, 20.0F,
                Impact.SCHRABIDIUM, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine", 4, "plate_titanium", 4, "shell", 2, "ducttape", 1),
        BHOLE(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, 20.0F,
                Impact.BLACK_HOLE, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine", 4, "plate_titanium", 4, "shell", 2, "ducttape", 1),
        TAINT(MissileItem.FormFactor.MICRO, LegacyMissileRadarProfile.TIER0, 20.0F,
                Impact.TAINT, 0.0F, 0, 0, 0, 0, 0, 0,
                "wire_fine", 4, "plate_titanium", 4, "shell", 2, "ducttape", 1),
        NUCLEAR(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, 40.0F,
                Impact.NUCLEAR, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 16, "plate_steel", 20, "plate_aluminium", 12, "thruster_large", 1),
        MIRV(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, 40.0F,
                Impact.MIRV, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 16, "plate_steel", 20, "plate_aluminium", 12, "thruster_large", 1),
        VOLCANO(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, 40.0F,
                Impact.VOLCANO, 0.0F, 0, 0, 0, 0, 0, 0,
                "plate_titanium", 16, "plate_steel", 20, "plate_aluminium", 12, "thruster_large", 1),
        DOOMSDAY(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, 40.0F,
                Impact.DOOMSDAY, 0.0F, 0, 0, 0, 0, 0, 0),
        DOOMSDAY_RUSTED(MissileItem.FormFactor.ATLAS, LegacyMissileRadarProfile.TIER4, 40.0F,
                Impact.DOOMSDAY, 0.0F, 0, 0, 0, 0, 0, 0);

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
                case MICRO -> rareItem("ammo_standard_nuke_high");
                case BHOLE -> rareItem("black_hole");
                case TAINT -> rareItem("powder_spark_mix");
                case NUCLEAR -> rareItem("warhead_nuclear");
                case MIRV -> rareItem("warhead_mirv");
                case VOLCANO -> rareItem("warhead_volcano");
                default -> ItemStack.EMPTY;
            };
        }

        private static List<ItemStack> buildDebris(Object... entries) {
            List<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i + 1 < entries.length; i += 2) {
                RegistryObject<Item> item = ModItems.legacyItem((String) entries[i]);
                int count = (Integer) entries[i + 1];
                if (item != null) {
                    stacks.add(new ItemStack(item.get(), count));
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
        DOOMSDAY
    }
}

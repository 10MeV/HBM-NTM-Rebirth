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
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.item.missile.MissileItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class MissileEntity extends Entity implements LegacyMissileRadarDetectable {
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
        this.decelY = -0.01D;
        this.accelXZ = 0.15D / distance;
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hit = traceNextBlockHit();
        if (hit.getType() != HitResult.Type.MISS) {
            setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
            if (!level().isClientSide) {
                onMissileImpact(hit);
                discard();
            }
            return;
        }

        updateFlight();
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        updateRotationFromMotion(motion);

        if (level().isClientSide && hasPropulsion() && tickCount % 2 == 0) {
            spawnContrail();
        }
        if (!level().isClientSide && getY() < level().getMinBuildHeight() - 64.0D) {
            discard();
        }
    }

    private HitResult traceNextBlockHit() {
        Vec3 start = position();
        Vec3 end = start.add(getDeltaMovement());
        return level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    private void updateFlight() {
        Vec3 motion = getDeltaMovement();
        if (hasPropulsion()) {
            velocity += Mth.clamp(tickCount / 60.0D * 0.05D, 0.0D, 0.05D);
            velocity = Math.min(velocity, 4.0D);
            double deltaX = targetX - startX;
            double deltaZ = targetZ - startZ;
            double factor = motion.y > 0.0D ? accelXZ : -accelXZ;
            motion = new Vec3(
                    Mth.clamp(motion.x + deltaX * factor, -velocity, velocity),
                    motion.y + decelY,
                    Mth.clamp(motion.z + deltaZ * factor, -velocity, velocity));
        } else {
            motion = new Vec3(motion.x * 0.99D, Math.max(motion.y - 0.03D, -1.5D), motion.z * 0.99D);
        }
        setDeltaMovement(motion);
    }

    private void updateRotationFromMotion(Vec3 motion) {
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal > 1.0E-4D || Math.abs(motion.y) > 1.0E-4D) {
            setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
            setXRot((float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG));
            yRotO = getYRot();
            xRotO = getXRot();
        }
    }

    protected boolean hasPropulsion() {
        return true;
    }

    protected void onMissileImpact(HitResult hit) {
        Variant variant = variant();
        switch (variant.impact()) {
            case STANDARD -> {
                ExplosionLarge.explode(level(), getX(), getY(), getZ(), variant.explosionStrength(),
                        true, false, true, this);
                spawnImpactShrapnel(variant);
            }
            case FIRE -> {
                ExplosionLarge.explodeFire(level(), getX(), getY(), getZ(), variant.explosionStrength(),
                        true, false, true, this);
                if (variant.igniteRadius() > 0) {
                    ExplosionChaos.igniteFlammableBlocks(level(), Mth.floor(getX() + 0.5D),
                            Mth.floor(getY() + 0.5D), Mth.floor(getZ() + 0.5D), variant.igniteRadius());
                }
                if (variant.igniteAllRadius() > 0) {
                    ExplosionChaos.igniteAllBlocks(level(), Mth.floor(getX()),
                            Mth.floor(getY()), Mth.floor(getZ()), variant.igniteAllRadius());
                }
                spawnImpactShrapnel(variant);
            }
            case DECOY -> WeaponExplosionUtil.explodeStandard(level(), getX(), getY(), getZ(),
                    variant.explosionStrength(), this, false, false);
            case CLUSTER -> {
                WeaponExplosionUtil.explodeStandard(level(), getX(), getY(), getZ(),
                        variant.explosionStrength(), this, true, true);
                spawnClusterSubmunitions(variant.clusterCount());
            }
            case BUSTER -> {
                for (int i = 0; i < variant.busterDepth(); i++) {
                    WeaponExplosionUtil.explodeStandard(level(), getX(), getY() - i, getZ(),
                            variant.explosionStrength(), this, true, true);
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

    private void spawnImpactShrapnel(Variant variant) {
        if (variant.shrapnelCount() > 0) {
            ExplosionLarge.spawnShrapnelShower(level(), getX(), getY(), getZ(), variant.shrapnelCount(), 1.0F, this);
        }
    }

    private void spawnClusterSubmunitions(int count) {
        ExplosionChaos.cluster(level(), getX(), getY(), getZ(), count,
                getYRot() * Mth.DEG_TO_RAD, getXRot() * Mth.DEG_TO_RAD,
                (float) Math.PI * 0.25F, (float) Math.PI * 0.25F, 1.0F, this);
    }

    public void killMissile() {
        if (!level().isClientSide) {
            ExplosionLarge.explode(level(), getX(), getY(), getZ(), 5.0F, true, false, false, this);
            ExplosionLarge.spawnShrapnelShower(level(), getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z, 12, 0.35D, this);
            ExplosionLarge.spawnMissileDebris(level(), getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z, 0.35D,
                    variant().debris(), ItemStack.EMPTY);
            discard();
        }
    }

    private void spawnContrail() {
        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_MISSILE_CONTRAIL);
        ParticleUtil.spawnAux(level(), getX(), getY(), getZ(), data, 150.0D);
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

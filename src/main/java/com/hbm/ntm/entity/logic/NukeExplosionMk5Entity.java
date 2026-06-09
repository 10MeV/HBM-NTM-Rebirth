package com.hbm.ntm.entity.logic;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.ExplosionNukeRayBatched;
import com.hbm.ntm.explosion.ExplosionNukeRayParallelized;
import com.hbm.ntm.explosion.ExplosionRay;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.HbmBlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiConsumer;

public class NukeExplosionMk5Entity extends ExplosionChunkLoadingEntity {
    private int strength;
    private int speed;
    private int length;
    private boolean fallout = true;
    private int falloutAdd;
    private long explosionStart;
    private boolean initialized;
    private boolean expiredFromSave;
    private ExplosionNukeRayBatched explosion;

    public NukeExplosionMk5Entity(EntityType<? extends NukeExplosionMk5Entity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public NukeExplosionMk5Entity(Level level, int strength, int speed, int length) {
        this(ModEntityTypes.NUKE_EXPLOSION_MK5.get(), level);
        this.strength = strength;
        this.speed = speed;
        this.length = length;
    }

    public static NukeExplosionMk5Entity create(Level level, int radius, double x, double y, double z) {
        if (extendedLoggingEnabled() && !level.isClientSide()) {
            HbmNtm.LOGGER.info("[NUKE] Initialized explosion at {} / {} / {} with strength {}!", x, y, z, radius);
        }

        if (radius == 0) {
            radius = BombConfig.nukaRadius();
        }

        int strength = radius * 2;
        NukeExplosionMk5Entity entity = new NukeExplosionMk5Entity(level, strength, Math.max(1, 100000 / strength),
                strength / 2);
        entity.setPos(x, y, z);
        return entity;
    }

    public static NukeExplosionMk5Entity createNoFallout(Level level, int radius, double x, double y, double z) {
        return create(level, radius, x, y, z).setFallout(false);
    }

    public static NukeExplosionMk5Entity statFac(Level level, int radius, double x, double y, double z) {
        return create(level, radius, x, y, z);
    }

    public static NukeExplosionMk5Entity statFacNoRad(Level level, int radius, double x, double y, double z) {
        return createNoFallout(level, radius, x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        if (expiredFromSave) {
            discard();
            return;
        }

        if (strength == 0) {
            discard();
            return;
        }

        forceCenterChunk();

        if (fallout && explosion != null && tickCount < 10 && strength >= 75) {
            radiate(2_500_000.0F / (tickCount * 5 + 1), length * 2.0D);
        }

        ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), length * 2.0D);

        if (explosion == null) {
            explosionStart = System.currentTimeMillis();
            explosion = createExplosionWorker();
            initialized = true;
        }

        if (!explosion.isComplete()) {
            int budget = BombConfig.mk5BudgetMs();
            explosion.cacheChunksTick(budget);
            explosion.destructionTick(budget);
            return;
        }

        if (fallout) {
            level().addFreshEntity(FalloutRainEntity.create(level(), getX(), getY(), getZ(), falloutScale()));
        }
        if (extendedLoggingEnabled() && explosionStart != 0L) {
            HbmNtm.LOGGER.info("[NUKE] Explosion complete. Time elapsed: {}ms", System.currentTimeMillis() - explosionStart);
        }
        discard();
    }

    private void radiate(float rads, double range) {
        AABB bounds = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(range);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, bounds);
        Vec3 origin = position();

        for (LivingEntity entity : entities) {
            Vec3 offset = new Vec3(entity.getX() - getX(), entity.getEyeY() - getY(), entity.getZ() - getZ());
            double distance = offset.length();
            if (distance <= 0.0D) {
                continue;
            }

            Vec3 direction = offset.normalize();
            float resistance = 0.0F;
            for (int i = 1; i < distance; i++) {
                BlockPos sample = BlockPos.containing(origin.add(direction.scale(i)));
                resistance += HbmBlockStateUtil.explosionResistance(level().getBlockState(sample), level(), sample);
            }
            if (resistance < 1.0F) {
                resistance = 1.0F;
            }

            float entityRads = rads / resistance / (float) (distance * distance);
            RadiationUtil.contaminate(entity, HazardType.RADIATION, RadiationUtil.ContaminationType.RAD_BYPASS, entityRads);
        }
    }

    private int falloutScale() {
        return Math.max(1, (int) (length * 2.5D + falloutAdd) * BombConfig.falloutRangePercent() / 100);
    }

    public NukeExplosionMk5Entity setFallout(boolean fallout) {
        this.fallout = fallout;
        return this;
    }

    public NukeExplosionMk5Entity moreFallout(int fallout) {
        this.falloutAdd = fallout;
        return this;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (explosion != null) {
            explosion.cancel();
        }
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("ticksExisted");
        strength = tag.getInt("strength");
        speed = tag.getInt("speed");
        length = tag.getInt("length");
        fallout = !tag.contains("fallout") || tag.getBoolean("fallout");
        falloutAdd = tag.getInt("falloutAdd");
        initialized = tag.getBoolean("initialized");
        readChunkLoader(tag);
        expiredFromSave = shouldExpireFromSave(tag);
        if (initialized && !expiredFromSave && strength > 0) {
            explosion = createExplosionWorker();
            explosion.readFromNbt(tag, "expl_");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ticksExisted", tickCount);
        tag.putInt("strength", strength);
        tag.putInt("speed", speed);
        tag.putInt("length", length);
        tag.putBoolean("fallout", fallout);
        tag.putInt("falloutAdd", falloutAdd);
        tag.putBoolean("initialized", initialized || explosion != null);
        saveChunkLoader(tag);
        if (explosion != null) {
            explosion.saveToNbt(tag, "expl_");
        }
    }

    private ExplosionNukeRayBatched createExplosionWorker() {
        return createExplosionWorker(level(), getX(), getY(), getZ(), strength, speed, length, this::loadChunk);
    }

    private static ExplosionNukeRayBatched createExplosionWorker(Level level, double x, double y, double z, int strength,
            int speed, int length, BiConsumer<Integer, Integer> chunkLoader) {
        int algorithm = BombConfig.explosionAlgorithm();
        if (algorithm == 1 || algorithm == 2) {
            return new ExplosionNukeRayParallelized(level, x, y, z, strength, speed, length, chunkLoader);
        }
        return new ExplosionNukeRayBatched(level, (int) x, (int) y, (int) z, strength, speed, length, chunkLoader);
    }

    private static boolean extendedLoggingEnabled() {
        return HbmCommonConfig.extendedLoggingEnabled();
    }
}

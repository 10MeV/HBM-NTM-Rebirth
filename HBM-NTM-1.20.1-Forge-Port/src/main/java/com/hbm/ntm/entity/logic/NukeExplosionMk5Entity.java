package com.hbm.ntm.entity.logic;

import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.ExplosionNukeRayBatched;
import com.hbm.ntm.explosion.ExplosionRay;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NukeExplosionMk5Entity extends Entity {
    private static final int DEFAULT_CACHE_BUDGET_MS = 25;
    private static final int DEFAULT_FALLOUT_RANGE_PERCENT = 100;

    private int strength;
    private int speed;
    private int length;
    private boolean fallout = true;
    private int falloutAdd;
    private long explosionStart;
    private ExplosionRay explosion;

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
        if (radius == 0) {
            radius = 25;
        }

        int strength = radius * 2;
        NukeExplosionMk5Entity entity = new NukeExplosionMk5Entity(level, strength, Math.max(1, (int) Math.ceil(100000.0D / strength)),
                strength / 2);
        entity.setPos(x, y, z);
        return entity;
    }

    public static NukeExplosionMk5Entity createNoFallout(Level level, int radius, double x, double y, double z) {
        return create(level, radius, x, y, z).setFallout(false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        if (strength == 0) {
            discard();
            return;
        }

        if (fallout && explosion != null && tickCount < 10 && strength >= 75) {
            radiate(2_500_000.0F / (tickCount * 5 + 1), length * 2.0D);
        }

        ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), length * 2.0D);

        if (explosion == null) {
            explosionStart = System.currentTimeMillis();
            explosion = new ExplosionNukeRayBatched(level(), blockPosition().getX(), blockPosition().getY(), blockPosition().getZ(),
                    strength, speed, length);
        }

        if (!explosion.isComplete()) {
            explosion.cacheChunksTick(DEFAULT_CACHE_BUDGET_MS);
            explosion.destructionTick(DEFAULT_CACHE_BUDGET_MS);
            return;
        }

        if (fallout) {
            placeFalloutFootprint();
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
            if (distance <= 0.0D || distance > range) {
                continue;
            }

            Vec3 direction = offset.normalize();
            float resistance = 0.0F;
            for (int i = 1; i < distance; i++) {
                resistance += ExplosionNukeRayBatched.masqueradeResistance(level().getBlockState(blockPositionFrom(origin.add(direction.scale(i)))));
            }
            resistance = Math.max(resistance, 1.0F);

            float entityRads = rads / resistance / (float) (distance * distance);
            RadiationUtil.contaminate(entity, HazardType.RADIATION, RadiationUtil.ContaminationType.RAD_BYPASS, entityRads);
        }
    }

    private static net.minecraft.core.BlockPos blockPositionFrom(Vec3 position) {
        return net.minecraft.core.BlockPos.containing(position);
    }

    private void placeFalloutFootprint() {
        int radius = Math.max(1, (int) (length * 2.5D + falloutAdd) * DEFAULT_FALLOUT_RANGE_PERCENT / 100);
        ExplosionNukeGeneric.waste(level(), blockPosition().getX(), blockPosition().getY(), blockPosition().getZ(), Math.min(radius, length * 2));
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ticksExisted", tickCount);
        tag.putInt("strength", strength);
        tag.putInt("speed", speed);
        tag.putInt("length", length);
        tag.putBoolean("fallout", fallout);
        tag.putInt("falloutAdd", falloutAdd);
    }
}

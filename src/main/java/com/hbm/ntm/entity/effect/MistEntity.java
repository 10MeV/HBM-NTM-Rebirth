package com.hbm.ntm.entity.effect;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class MistEntity extends Entity {
    private static final EntityDataAccessor<Integer> FLUID_ID =
            SynchedEntityData.defineId(MistEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AREA_WIDTH =
            SynchedEntityData.defineId(MistEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AREA_HEIGHT =
            SynchedEntityData.defineId(MistEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_AGE =
            SynchedEntityData.defineId(MistEntity.class, EntityDataSerializers.INT);

    public MistEntity(EntityType<? extends MistEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setNoGravity(true);
    }

    public MistEntity(Level level) {
        this(ModEntityTypes.MIST.get(), level);
    }

    public static MistEntity create(Level level, double x, double y, double z, FluidType fluid,
            float width, float height, int duration) {
        MistEntity entity = new MistEntity(level);
        entity.setPos(x, y, z);
        entity.setFluidType(fluid);
        entity.setArea(width, height);
        entity.setDuration(duration);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            spawnClientParticles();
            return;
        }
        if (tickCount >= getMaxAge()) {
            discard();
            return;
        }

        FluidType type = getFluidType();
        releaseVentRadiation(type);

        float intensity = 1.0F - (float) tickCount / (float) getMaxAge();
        if (type.hasTrait(FlammableFluidTrait.class) && isOnFire()) {
            level().explode(this, getX(), getY() + getAreaHeight() / 2.0D, getZ(), intensity * 15.0F,
                    false, Level.ExplosionInteraction.BLOCK);
            discard();
            return;
        }

        List<Entity> affected = level().getEntities(this, area(), Entity::isAlive);
        for (Entity entity : affected) {
            affect(type, entity, intensity);
        }
    }

    public MistEntity setFluidType(FluidType fluid) {
        entityData.set(FLUID_ID, fluid == null ? HbmFluids.NONE.getId() : fluid.getId());
        return this;
    }

    public FluidType getFluidType() {
        return HbmFluids.fromId(entityData.get(FLUID_ID));
    }

    public MistEntity setArea(float width, float height) {
        entityData.set(AREA_WIDTH, Math.max(0.0F, width));
        entityData.set(AREA_HEIGHT, Math.max(0.0F, height));
        return this;
    }

    public float getAreaWidth() {
        return entityData.get(AREA_WIDTH);
    }

    public float getAreaHeight() {
        return entityData.get(AREA_HEIGHT);
    }

    public MistEntity setDuration(int duration) {
        entityData.set(MAX_AGE, Math.max(1, duration));
        return this;
    }

    public int getMaxAge() {
        return entityData.get(MAX_AGE);
    }

    public AABB area() {
        float width = getAreaWidth();
        float height = getAreaHeight();
        return new AABB(getX() - width / 2.0D, getY(), getZ() - width / 2.0D,
                getX() + width / 2.0D, getY() + height, getZ() + width / 2.0D);
    }

    private void releaseVentRadiation(FluidType type) {
        VentRadiationFluidTrait trait = type.getTrait(VentRadiationFluidTrait.class);
        if (trait != null) {
            ChunkRadiationManager.incrementRadiation(level(), blockPosition(), trait.getRadiationPerMb() * 2.0F);
        }
    }

    private void affect(FluidType type, Entity entity, float intensity) {
        type.affectEntity(entity, intensity);
        if (isExtinguishing(type)) {
            entity.clearFire();
        }
        if (type == HbmFluids.ENDERJUICE && entity instanceof LivingEntity living) {
            teleportRandomly(living);
        }
    }

    private boolean isExtinguishing(FluidType type) {
        return type.getTemperature() < 50 && !type.hasTrait(FlammableFluidTrait.class);
    }

    private void teleportRandomly(LivingEntity living) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double startX = living.getX();
        double startY = living.getY();
        double startZ = living.getZ();
        double targetX = getX() + (random.nextDouble() - 0.5D) * 64.0D;
        double targetY = Mth.clamp(getY() + random.nextInt(64) - 32.0D,
                level().getMinBuildHeight() + 1.0D, level().getMaxBuildHeight() - 1.0D);
        double targetZ = getZ() + (random.nextDouble() - 0.5D) * 64.0D;

        if (living.randomTeleport(targetX, targetY, targetZ, true)) {
            for (int i = 0; i < 32; i++) {
                double progress = i / 31.0D;
                double x = Mth.lerp(progress, startX, living.getX()) + (random.nextDouble() - 0.5D) * living.getBbWidth();
                double y = Mth.lerp(progress, startY, living.getY()) + random.nextDouble() * living.getBbHeight();
                double z = Mth.lerp(progress, startZ, living.getZ()) + (random.nextDouble() - 0.5D) * living.getBbWidth();
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, x, y, z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            level().playSound(null, startX, startY, startZ, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE, 1.0F, 1.0F);
            living.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }

    private void spawnClientParticles() {
        FluidType type = getFluidType();
        if (type == HbmFluids.NONE) {
            return;
        }

        int color = type.getColor();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        AABB area = area();
        for (int i = 0; i < 2; i++) {
            double x = area.minX + random.nextDouble() * area.getXsize();
            double y = area.minY + random.nextDouble() * area.getYsize();
            double z = area.minZ + random.nextDouble() * area.getZsize();
            ParticleUtil.spawnVanillaExtColoredCloud(level(), x, y, z, red, green, blue);
        }
    }

    public static SprayStyle getStyleFromType(FluidType type) {
        if (type == null) {
            return SprayStyle.NULL;
        }
        if (type.hasTrait(SimpleFluidTraits.Viscous.class)) {
            return SprayStyle.NULL;
        }
        if (type.hasTrait(SimpleFluidTraits.Gaseous.class)
                || type.hasTrait(SimpleFluidTraits.GaseousAtRoomTemperature.class)) {
            return SprayStyle.GAS;
        }
        if (type.hasTrait(SimpleFluidTraits.Liquid.class)) {
            return SprayStyle.MIST;
        }
        return SprayStyle.NULL;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(FLUID_ID, HbmFluids.NONE.getId());
        entityData.define(AREA_WIDTH, 0.0F);
        entityData.define(AREA_HEIGHT, 0.0F);
        entityData.define(MAX_AGE, 150);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setFluidType(HbmFluids.fromId(tag.getInt("type")));
        setArea(tag.getFloat("width"), tag.getFloat("height"));
        if (tag.contains("maxAge")) {
            setDuration(tag.getInt("maxAge"));
        }
        tickCount = tag.getInt("age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("type", getFluidType().getId());
        tag.putFloat("width", getAreaWidth());
        tag.putFloat("height", getAreaHeight());
        tag.putInt("maxAge", getMaxAge());
        tag.putInt("age", tickCount);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public enum SprayStyle {
        MIST,
        GAS,
        NULL
    }
}

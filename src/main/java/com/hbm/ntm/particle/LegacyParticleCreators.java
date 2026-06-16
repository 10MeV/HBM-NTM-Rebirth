package com.hbm.ntm.particle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Set;

public final class LegacyParticleCreators {
    public static final int META_FIRE = ParticleUtil.FLAMETHROWER_META_FIRE;
    public static final int META_BALEFIRE = ParticleUtil.FLAMETHROWER_META_BALEFIRE;
    public static final int META_DIGAMMA = ParticleUtil.FLAMETHROWER_META_DIGAMMA;
    public static final int META_OXY = ParticleUtil.FLAMETHROWER_META_OXY;
    public static final int META_BLACK = ParticleUtil.FLAMETHROWER_META_BLACK;

    private static final Set<String> CREATOR_TYPES = Set.of(
            ParticleUtil.TYPE_EXPLOSION_LARGE,
            ParticleUtil.TYPE_CASING,
            ParticleUtil.TYPE_FLAMETHROWER,
            ParticleUtil.TYPE_EXPLOSION_SMALL,
            ParticleUtil.TYPE_BLACK_POWDER,
            ParticleUtil.TYPE_ASHES,
            ParticleUtil.TYPE_SKELETON);

    public static Set<String> creatorTypes() {
        return CREATOR_TYPES;
    }

    public static boolean hasCreator(String type) {
        return type != null && CREATOR_TYPES.contains(type);
    }

    public static void sendPacket(Level level, double x, double y, double z, int range, CompoundTag data) {
        if (data == null) {
            return;
        }
        ParticleUtil.spawnAux(level, x, y, z, data, range);
    }

    public static void sendThreadedPacket(Level level, double x, double y, double z, int range, CompoundTag data) {
        if (data == null) {
            return;
        }
        ParticleUtil.spawnAuxThreaded(level, x, y, z, data, range);
    }

    public static void composeFlamethrower(Level level, double x, double y, double z, int meta) {
        ParticleUtil.spawnFlamethrower(level, x, y, z, meta);
    }

    public static void composeEffect(Level level, double x, double y, double z, int meta) {
        composeFlamethrower(level, x, y, z, meta);
    }

    public static void composeFlamethrowerClient(Level level, double x, double y, double z, int meta) {
        ParticleUtil.spawnFlamethrower(level, x, y, z, meta);
    }

    public static void composeEffectClient(Level level, double x, double y, double z, int meta) {
        composeFlamethrowerClient(level, x, y, z, meta);
    }

    public static void composeBlackPowder(Level level, double x, double y, double z,
            double headingX, double headingY, double headingZ, int cloudCount, float cloudScale,
            float cloudSpeedMult, int sparkCount, float sparkSpeedMult) {
        ParticleUtil.spawnBlackPowder(level, x, y, z, headingX, headingY, headingZ,
                cloudCount, cloudScale, cloudSpeedMult, sparkCount, sparkSpeedMult);
    }

    public static void composeEffect(Level level, double x, double y, double z,
            double headingX, double headingY, double headingZ, int cloudCount, float cloudScale,
            float cloudSpeedMult, int sparkCount, float sparkSpeedMult) {
        composeBlackPowder(level, x, y, z, headingX, headingY, headingZ, cloudCount, cloudScale,
                cloudSpeedMult, sparkCount, sparkSpeedMult);
    }

    public static void composeAshes(Entity entity, int ashesCount, float ashesScale) {
        ParticleUtil.spawnAshes(entity, ashesCount, ashesScale);
    }

    public static void composeEffect(Entity entity, int ashesCount, float ashesScale) {
        composeAshes(entity, ashesCount, ashesScale);
    }

    public static void composeAshes(Level level, Entity entity, int ashesCount, float ashesScale) {
        if (entity == null) {
            return;
        }
        ParticleUtil.spawnAshes(level, entity.getX(), entity.getY(), entity.getZ(), entity.getId(), ashesCount, ashesScale);
    }

    public static void composeEffect(Level level, Entity entity, int ashesCount, float ashesScale) {
        composeAshes(level, entity, ashesCount, ashesScale);
    }

    public static void composeExplosion(Level level, double x, double y, double z, int cloudCount,
            float cloudScale, float cloudSpeedMult, float waveScale, int debrisCount, int debrisSize,
            int debrisRetry, float debrisVelocity, float debrisHorizontalDeviation, float debrisVerticalOffset,
            float soundRange) {
        ParticleUtil.spawnExplosionLarge(level, x, y, z, cloudCount, cloudScale, cloudSpeedMult,
                waveScale, debrisCount, debrisSize, debrisRetry, debrisVelocity, debrisHorizontalDeviation,
                debrisVerticalOffset, soundRange);
    }

    public static void composeEffect(Level level, double x, double y, double z, int cloudCount,
            float cloudScale, float cloudSpeedMult, float waveScale, int debrisCount, int debrisSize,
            int debrisRetry, float debrisVelocity, float debrisHorizontalDeviation, float debrisVerticalOffset,
            float soundRange) {
        composeExplosion(level, x, y, z, cloudCount, cloudScale, cloudSpeedMult, waveScale, debrisCount,
                debrisSize, debrisRetry, debrisVelocity, debrisHorizontalDeviation, debrisVerticalOffset, soundRange);
    }

    public static void composeExplosionSmallPreset(Level level, double x, double y, double z) {
        ParticleUtil.spawnLegacyExplosionSmall(level, x, y, z);
    }

    public static void composeExplosionStandardPreset(Level level, double x, double y, double z) {
        ParticleUtil.spawnLegacyExplosionStandard(level, x, y, z);
    }

    public static void composeExplosionLargePreset(Level level, double x, double y, double z) {
        ParticleUtil.spawnLegacyExplosionLarge(level, x, y, z);
    }

    public static void composeSmallExplosion(Level level, double x, double y, double z,
            int cloudCount, float cloudScale, float cloudSpeedMult) {
        ParticleUtil.spawnExplosionSmall(level, x, y, z, cloudCount, cloudScale, cloudSpeedMult);
    }

    public static void composeEffect(Level level, double x, double y, double z,
            int cloudCount, float cloudScale, float cloudSpeedMult) {
        composeSmallExplosion(level, x, y, z, cloudCount, cloudScale, cloudSpeedMult);
    }

    public static void composeSmallExplosion(Level level, double x, double y, double z,
            int cloudCount, float cloudScale, float cloudSpeedMult, int debris) {
        ParticleUtil.spawnExplosionSmall(level, x, y, z, cloudCount, cloudScale, cloudSpeedMult, debris);
    }

    public static void composeCasing(Level level, double x, double y, double z, float yaw, float pitch,
            double frontMotion, double heightMotion, double sideMotion, double motionVariance,
            float momentumPitch, float momentumYaw, String casing, boolean smoking, int smokeLife,
            double smokeLift, int nodeLife) {
        ParticleUtil.spawnCasingFromView(level, x, y, z, yaw, pitch, frontMotion, heightMotion, sideMotion,
                motionVariance, momentumPitch, momentumYaw, casing, smoking, smokeLife, smokeLift, nodeLife);
    }

    public static void composeEffect(Level level, double x, double y, double z, float yaw, float pitch,
            double frontMotion, double heightMotion, double sideMotion, double motionVariance,
            float momentumPitch, float momentumYaw, String casing, boolean smoking, int smokeLife,
            double smokeLift, int nodeLife) {
        composeCasing(level, x, y, z, yaw, pitch, frontMotion, heightMotion, sideMotion, motionVariance,
                momentumPitch, momentumYaw, casing, smoking, smokeLife, smokeLift, nodeLife);
    }

    public static void composeCasing(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion,
            double motionVariance, String casing) {
        ParticleUtil.spawnCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion,
                sideMotion, motionVariance, casing);
    }

    public static void composeEffect(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion,
            double motionVariance, String casing) {
        composeCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion,
                sideMotion, motionVariance, casing);
    }

    public static void composeCasing(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion,
            double motionVariance, float momentumPitch, float momentumYaw, String casing) {
        ParticleUtil.spawnCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion,
                sideMotion, motionVariance, momentumPitch, momentumYaw, casing);
    }

    public static void composeEffect(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion,
            double motionVariance, float momentumPitch, float momentumYaw, String casing) {
        composeCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion,
                sideMotion, motionVariance, momentumPitch, momentumYaw, casing);
    }

    public static void composeCasing(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion,
            double motionVariance, float momentumPitch, float momentumYaw, String casing, boolean smoking,
            int smokeLife, double smokeLift, int nodeLife) {
        ParticleUtil.spawnCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion,
                sideMotion, motionVariance, momentumPitch, momentumYaw, casing, smoking, smokeLife, smokeLift, nodeLife);
    }

    public static void composeEffect(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion,
            double motionVariance, float momentumPitch, float momentumYaw, String casing, boolean smoking,
            int smokeLife, double smokeLift, int nodeLife) {
        composeCasing(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion,
                sideMotion, motionVariance, momentumPitch, momentumYaw, casing, smoking, smokeLife, smokeLift, nodeLife);
    }

    public static void composeSkeleton(Entity entity, float brightness) {
        ParticleUtil.spawnSkeleton(entity, brightness);
    }

    public static void composeEffect(Entity entity, float brightness) {
        composeSkeleton(entity, brightness);
    }

    public static void composeSkeletonGib(Entity entity, float force) {
        ParticleUtil.spawnSkeletonGib(entity, force);
    }

    public static void composeEffectGib(Entity entity, float force) {
        composeSkeletonGib(entity, force);
    }

    private LegacyParticleCreators() {
    }
}

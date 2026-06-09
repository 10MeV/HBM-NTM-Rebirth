package com.hbm.ntm.bullet;

import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BulletCasingEjectUtil {
    public static final double CASING_PACKET_RANGE = 50.0D;
    public static final float DEFAULT_MOMENTUM_PITCH = 5.0F;
    public static final float DEFAULT_MOMENTUM_YAW = 10.0F;

    public static CasingRequest legacyEjectorRequest(Vec3 position, int ejectorId, String casingName,
            float pitchRadians, float yawRadians, boolean crouched) {
        if (position == null || ejectorId < 0 || casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }
        return CasingRequest.legacyEjector(position, ejectorId, casingName, pitchRadians, yawRadians, crouched);
    }

    public static CasingRequest legacyEjectorFromShooter(LivingEntity shooter, int ejectorId, String casingName) {
        if (shooter == null) {
            return CasingRequest.NONE;
        }
        return legacyEjectorRequest(shooter.position(), ejectorId, casingName,
                shooter.getXRot() * Mth.DEG_TO_RAD, shooter.getYRot() * Mth.DEG_TO_RAD, shooter.isCrouching());
    }

    public static CasingRequest directRequest(Vec3 position, Vec3 motion, float yaw, float pitch,
            float momentumPitch, float momentumYaw, String casingName, boolean smoking, int smokeLife,
            double smokeLift, int nodeLife) {
        if (position == null || motion == null || casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }
        return CasingRequest.direct(position, motion, yaw, pitch, momentumPitch, momentumYaw, casingName,
                smoking, smokeLife, smokeLift, nodeLife);
    }

    public static CasingRequest directFromShooter(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance,
            String casingName, RandomSource random) {
        return directFromShooter(shooter, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion,
                sideMotion, motionVariance, DEFAULT_MOMENTUM_PITCH, DEFAULT_MOMENTUM_YAW, casingName,
                false, 0, 0.0D, 0, random);
    }

    public static CasingRequest directFromShooter(LivingEntity shooter, double frontOffset, double heightOffset,
            double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance,
            float momentumPitch, float momentumYaw, String casingName, boolean smoking, int smokeLife,
            double smokeLift, int nodeLife, RandomSource random) {
        if (shooter == null || casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }

        double adjustedHeightOffset = shooter.isCrouching() ? heightOffset - 0.075F : heightOffset;
        float pitch = shooter.getXRot();
        float yaw = shooter.getYRot();
        Vec3 offset = rotatePlayerRelative(new Vec3(sideOffset, adjustedHeightOffset, frontOffset), pitch, yaw);
        Vec3 position = new Vec3(shooter.getX() + offset.x,
                shooter.getY() + shooter.getEyeHeight() + offset.y,
                shooter.getZ() + offset.z);

        RandomSource roll = random == null ? shooter.getRandom() : random;
        Vec3 localMotion = rotatePlayerRelative(new Vec3(sideMotion, heightMotion, frontMotion), pitch, yaw);
        Vec3 shooterMotion = shooter.getDeltaMovement();
        double motionX = shooterMotion.x + localMotion.x + roll.nextGaussian() * motionVariance;
        double motionY = shooterMotion.y + localMotion.y + roll.nextGaussian() * motionVariance;
        double motionZ = shooterMotion.z + localMotion.z + roll.nextGaussian() * motionVariance;
        if (shooter instanceof Player player && player.getAbilities().flying) {
            motionY -= 0.04D;
        }
        Vec3 motion = new Vec3(motionX, motionY, motionZ);

        return directRequest(position, motion, yaw, pitch, momentumPitch, momentumYaw, casingName,
                smoking, smokeLife, smokeLift, nodeLife);
    }

    public static CasingRequest directAtPosition(Vec3 position, float yaw, float pitch, double frontMotion,
            double heightMotion, double sideMotion, double motionVariance, float momentumPitch, float momentumYaw,
            String casingName, boolean smoking, int smokeLife, double smokeLift, int nodeLife, RandomSource random) {
        if (position == null || casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }
        RandomSource roll = random == null ? RandomSource.create() : random;
        Vec3 localMotion = rotatePlayerRelative(new Vec3(sideMotion, heightMotion, frontMotion), pitch, yaw);
        Vec3 motion = new Vec3(
                localMotion.x + roll.nextGaussian() * motionVariance,
                localMotion.y + roll.nextGaussian() * motionVariance,
                localMotion.z + roll.nextGaussian() * motionVariance);
        return directRequest(position, motion, yaw, pitch, momentumPitch, momentumYaw, casingName,
                smoking, smokeLife, smokeLift, nodeLife);
    }

    public static boolean execute(Level level, CasingRequest request) {
        if (level == null || request == null || !request.valid()) {
            return false;
        }
        Vec3 pos = request.position();
        if (request.kind() == CasingKind.LEGACY_EJECTOR) {
            ParticleUtil.spawnLegacyCasing(level, pos.x, pos.y, pos.z, request.ejectorId(), request.casingName(),
                    request.pitch(), request.yaw(), request.crouched());
            return true;
        }
        ParticleUtil.spawnCasing(level, pos.x, pos.y, pos.z,
                request.motion().x, request.motion().y, request.motion().z,
                request.yaw(), request.pitch(), request.momentumPitch(), request.momentumYaw(),
                request.casingName(), request.smoking(), request.smokeLife(), request.smokeLift(),
                request.nodeLife());
        return true;
    }

    private static Vec3 rotatePlayerRelative(Vec3 vector, float pitchDegrees, float yawDegrees) {
        Vec3 pitchRotated = rotateX(vector, -pitchDegrees * Mth.DEG_TO_RAD);
        return pitchRotated.yRot(-yawDegrees * Mth.DEG_TO_RAD);
    }

    private static Vec3 rotateX(Vec3 vector, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double y = vector.y * cos - vector.z * sin;
        double z = vector.y * sin + vector.z * cos;
        return new Vec3(vector.x, y, z);
    }

    public enum CasingKind {
        NONE,
        LEGACY_EJECTOR,
        DIRECT
    }

    public record CasingRequest(CasingKind kind, Vec3 position, Vec3 motion, int ejectorId, String casingName,
            float pitch, float yaw, float momentumPitch, float momentumYaw, boolean crouched, boolean smoking,
            int smokeLife, double smokeLift, int nodeLife, boolean valid) {
        public static final CasingRequest NONE = new CasingRequest(CasingKind.NONE, Vec3.ZERO, Vec3.ZERO, -1, "",
                0.0F, 0.0F, DEFAULT_MOMENTUM_PITCH, DEFAULT_MOMENTUM_YAW, false, false, 0, 0.0D, 0, false);

        public static CasingRequest legacyEjector(Vec3 position, int ejectorId, String casingName,
                float pitchRadians, float yawRadians, boolean crouched) {
            return new CasingRequest(CasingKind.LEGACY_EJECTOR, position, Vec3.ZERO, ejectorId, casingName,
                    pitchRadians, yawRadians, DEFAULT_MOMENTUM_PITCH, DEFAULT_MOMENTUM_YAW, crouched, false,
                    0, 0.0D, 0, true);
        }

        public static CasingRequest direct(Vec3 position, Vec3 motion, float yaw, float pitch,
                float momentumPitch, float momentumYaw, String casingName, boolean smoking, int smokeLife,
                double smokeLift, int nodeLife) {
            return new CasingRequest(CasingKind.DIRECT, position, motion, -1, casingName, pitch, yaw,
                    momentumPitch, momentumYaw, false, smoking, smokeLife, smokeLift, nodeLife, true);
        }
    }

    private BulletCasingEjectUtil() {
    }
}

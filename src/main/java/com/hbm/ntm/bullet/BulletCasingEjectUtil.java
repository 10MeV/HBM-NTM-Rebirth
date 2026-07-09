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
        return CasingRequest.legacyEjector(position.x, position.y, position.z, ejectorId, casingName,
                pitchRadians, yawRadians, crouched);
    }

    public static CasingRequest legacyEjectorFromShooter(LivingEntity shooter, int ejectorId, String casingName) {
        if (shooter == null) {
            return CasingRequest.NONE;
        }
        if (ejectorId < 0 || casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }
        return CasingRequest.legacyEjector(shooter.getX(), shooter.getY(), shooter.getZ(), ejectorId, casingName,
                shooter.getXRot() * Mth.DEG_TO_RAD, shooter.getYRot() * Mth.DEG_TO_RAD, shooter.isCrouching());
    }

    public static CasingRequest directRequest(Vec3 position, Vec3 motion, float yaw, float pitch,
            float momentumPitch, float momentumYaw, String casingName, boolean smoking, int smokeLife,
            double smokeLift, int nodeLife) {
        if (position == null || motion == null || casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }
        return directRequest(position.x, position.y, position.z, motion.x, motion.y, motion.z, yaw, pitch,
                momentumPitch, momentumYaw, casingName, smoking, smokeLife, smokeLift, nodeLife);
    }

    public static CasingRequest directRequest(double x, double y, double z, double motionX, double motionY,
            double motionZ, float yaw, float pitch, float momentumPitch, float momentumYaw, String casingName,
            boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
        if (casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }
        return CasingRequest.direct(x, y, z, motionX, motionY, motionZ, yaw, pitch, momentumPitch, momentumYaw,
                casingName, smoking, smokeLife, smokeLift, nodeLife);
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
        double pitchRadians = -pitch * Mth.DEG_TO_RAD;
        double pitchCos = Math.cos(pitchRadians);
        double pitchSin = Math.sin(pitchRadians);
        double yawRadians = -yaw * Mth.DEG_TO_RAD;
        double yawCos = Math.cos(yawRadians);
        double yawSin = Math.sin(yawRadians);

        double offsetY = adjustedHeightOffset * pitchCos - frontOffset * pitchSin;
        double offsetZPitch = adjustedHeightOffset * pitchSin + frontOffset * pitchCos;
        double x = shooter.getX() + sideOffset * yawCos + offsetZPitch * yawSin;
        double y = shooter.getY() + shooter.getEyeHeight() + offsetY;
        double z = shooter.getZ() + offsetZPitch * yawCos - sideOffset * yawSin;

        RandomSource roll = random == null ? shooter.getRandom() : random;
        double motionYLocal = heightMotion * pitchCos - frontMotion * pitchSin;
        double motionZPitch = heightMotion * pitchSin + frontMotion * pitchCos;
        double motionXLocal = sideMotion * yawCos + motionZPitch * yawSin;
        double motionZLocal = motionZPitch * yawCos - sideMotion * yawSin;
        Vec3 shooterMotion = shooter.getDeltaMovement();
        double motionX = shooterMotion.x + motionXLocal + roll.nextGaussian() * motionVariance;
        double motionY = shooterMotion.y + motionYLocal + roll.nextGaussian() * motionVariance;
        double motionZ = shooterMotion.z + motionZLocal + roll.nextGaussian() * motionVariance;
        if (shooter instanceof Player player && player.getAbilities().flying) {
            motionY -= 0.04D;
        }

        return directRequest(x, y, z, motionX, motionY, motionZ, yaw, pitch, momentumPitch, momentumYaw, casingName,
                smoking, smokeLife, smokeLift, nodeLife);
    }

    public static CasingRequest directAtPosition(Vec3 position, float yaw, float pitch, double frontMotion,
            double heightMotion, double sideMotion, double motionVariance, float momentumPitch, float momentumYaw,
            String casingName, boolean smoking, int smokeLife, double smokeLift, int nodeLife, RandomSource random) {
        if (position == null || casingName == null || casingName.isBlank()) {
            return CasingRequest.NONE;
        }
        RandomSource roll = random == null ? RandomSource.create() : random;
        double pitchRadians = -pitch * Mth.DEG_TO_RAD;
        double pitchCos = Math.cos(pitchRadians);
        double pitchSin = Math.sin(pitchRadians);
        double motionYLocal = heightMotion * pitchCos - frontMotion * pitchSin;
        double motionZPitch = heightMotion * pitchSin + frontMotion * pitchCos;

        double yawRadians = -yaw * Mth.DEG_TO_RAD;
        double yawCos = Math.cos(yawRadians);
        double yawSin = Math.sin(yawRadians);
        double motionX = sideMotion * yawCos + motionZPitch * yawSin + roll.nextGaussian() * motionVariance;
        double motionY = motionYLocal + roll.nextGaussian() * motionVariance;
        double motionZ = motionZPitch * yawCos - sideMotion * yawSin + roll.nextGaussian() * motionVariance;
        return directRequest(position.x, position.y, position.z, motionX, motionY, motionZ, yaw, pitch,
                momentumPitch, momentumYaw, casingName,
                smoking, smokeLife, smokeLift, nodeLife);
    }

    public static boolean execute(Level level, CasingRequest request) {
        if (level == null || request == null || !request.valid()) {
            return false;
        }
        if (request.kind() == CasingKind.LEGACY_EJECTOR) {
            ParticleUtil.spawnLegacyCasing(level, request.x(), request.y(), request.z(), request.ejectorId(),
                    request.casingName(), request.pitch(), request.yaw(), request.crouched());
            return true;
        }
        ParticleUtil.spawnCasing(level, request.x(), request.y(), request.z(),
                request.motionX(), request.motionY(), request.motionZ(),
                request.yaw(), request.pitch(), request.momentumPitch(), request.momentumYaw(),
                request.casingName(), request.smoking(), request.smokeLife(), request.smokeLift(),
                request.nodeLife());
        return true;
    }

    public enum CasingKind {
        NONE,
        LEGACY_EJECTOR,
        DIRECT
    }

    public record CasingRequest(CasingKind kind, double x, double y, double z, double motionX, double motionY,
            double motionZ, int ejectorId, String casingName, float pitch, float yaw, float momentumPitch,
            float momentumYaw, boolean crouched, boolean smoking, int smokeLife, double smokeLift, int nodeLife,
            boolean valid) {
        public static final CasingRequest NONE = new CasingRequest(CasingKind.NONE, 0.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 0.0D, -1, "", 0.0F, 0.0F, DEFAULT_MOMENTUM_PITCH, DEFAULT_MOMENTUM_YAW,
                false, false, 0, 0.0D, 0, false);

        public static CasingRequest legacyEjector(Vec3 position, int ejectorId, String casingName,
                float pitchRadians, float yawRadians, boolean crouched) {
            return position == null
                    ? NONE
                    : legacyEjector(position.x, position.y, position.z, ejectorId, casingName, pitchRadians,
                            yawRadians, crouched);
        }

        public static CasingRequest legacyEjector(double x, double y, double z, int ejectorId, String casingName,
                float pitchRadians, float yawRadians, boolean crouched) {
            return new CasingRequest(CasingKind.LEGACY_EJECTOR, x, y, z, 0.0D, 0.0D, 0.0D, ejectorId, casingName,
                    pitchRadians, yawRadians, DEFAULT_MOMENTUM_PITCH, DEFAULT_MOMENTUM_YAW, crouched, false,
                    0, 0.0D, 0, true);
        }

        public static CasingRequest direct(Vec3 position, Vec3 motion, float yaw, float pitch,
                float momentumPitch, float momentumYaw, String casingName, boolean smoking, int smokeLife,
                double smokeLift, int nodeLife) {
            return position == null || motion == null
                    ? NONE
                    : direct(position.x, position.y, position.z, motion.x, motion.y, motion.z, yaw, pitch,
                            momentumPitch, momentumYaw, casingName, smoking, smokeLife, smokeLift, nodeLife);
        }

        public static CasingRequest direct(double x, double y, double z, double motionX, double motionY,
                double motionZ, float yaw, float pitch, float momentumPitch, float momentumYaw, String casingName,
                boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
            return new CasingRequest(CasingKind.DIRECT, x, y, z, motionX, motionY, motionZ, -1, casingName,
                    pitch, yaw, momentumPitch, momentumYaw, false, smoking, smokeLife, smokeLift, nodeLife, true);
        }
    }

    private BulletCasingEjectUtil() {
    }
}

package com.hbm.ntm.bullet;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class BulletRuntimeUtil {
    public static DamageRoll rollDamage(BulletConfig config, Entity victim, Vec3 hitLocation, RandomSource random,
            float overrideDamage) {
        float damage = rollBaseDamage(config, random, overrideDamage);
        boolean headshot = isHeadshot(config, victim, hitLocation);
        return new DamageRoll(damage, headshot ? damage * config.headshotMultiplier() : damage, headshot);
    }

    public static float rollBaseDamage(BulletConfig config, RandomSource random, float overrideDamage) {
        if (overrideDamage != 0.0F) {
            return overrideDamage;
        }
        float min = config.damageMin();
        float max = config.damageMax();
        if (max <= min) {
            return min;
        }
        return random.nextFloat() * (max - min) + min;
    }

    public static boolean isHeadshot(BulletConfig config, Entity victim, Vec3 hitLocation) {
        if (config.headshotMultiplier() <= 1.0F || !(victim instanceof LivingEntity living) || hitLocation == null) {
            return false;
        }
        double head = living.getBbHeight() - living.getEyeHeight();
        return living.isAlive() && hitLocation.y > living.getY() + living.getBbHeight() - head * 2.0D;
    }

    public static RicochetRoll rollRicochet(BulletConfig config, Vec3 motion, Direction side, RandomSource random) {
        boolean highChance = random.nextInt(100) < config.higherBoundRicochetChance();
        if (!config.ricochets() || !highChance || side == null || motion == null || motion.lengthSqr() == 0.0D) {
            return new RicochetRoll(false, motion, highChance, false, 0.0D);
        }

        Vec3 face = new Vec3(side.getStepX(), side.getStepY(), side.getStepZ());
        double angle = Math.abs(crossAngleDegrees(motion, face) - 90.0D);
        Vec3 reflected = reflect(motion, side).scale(config.bounceModifier());
        return new RicochetRoll(true, reflected, true, false, angle);
    }

    private static Vec3 reflect(Vec3 motion, Direction side) {
        Direction.Axis axis = side.getAxis();
        return switch (axis) {
            case X -> new Vec3(-motion.x, motion.y, motion.z);
            case Y -> new Vec3(motion.x, -motion.y, motion.z);
            case Z -> new Vec3(motion.x, motion.y, -motion.z);
        };
    }

    private static double crossAngleDegrees(Vec3 first, Vec3 second) {
        double firstLength = first.length();
        double secondLength = second.length();
        if (firstLength == 0.0D || secondLength == 0.0D) {
            return 0.0D;
        }
        double cosine = first.dot(second) / (firstLength * secondLength);
        double angle = Math.acos(Math.max(-1.0D, Math.min(1.0D, cosine))) * 180.0D / Math.PI;
        return angle >= 180.0D ? angle - 180.0D : angle;
    }

    public record DamageRoll(float baseDamage, float finalDamage, boolean headshot) {
    }

    public record RicochetRoll(boolean ricochet, Vec3 motion, boolean highChance, boolean lowerChance, double surfaceAngle) {
    }

    private BulletRuntimeUtil() {
    }
}

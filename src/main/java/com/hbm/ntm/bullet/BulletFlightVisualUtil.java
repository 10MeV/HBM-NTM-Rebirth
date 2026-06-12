package com.hbm.ntm.bullet;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BulletFlightVisualUtil {
    public static int spawnClientTickVisuals(BulletConfig config, Level level, Vec3 previousPosition,
            Vec3 currentPosition, Vec3 motion, int ticksExisted, RandomSource random) {
        return spawnBlackPowderBurst(config, level, currentPosition, motion, ticksExisted, random)
                + spawnFlamethrowerTrail(config, level, currentPosition)
                + spawnFireExtinguisherTrail(config, level, currentPosition, motion, random)
                + spawnVanillaTrail(config, level, previousPosition, currentPosition);
    }

    public static int spawnVanillaTrail(BulletConfig config, Level level, Vec3 previousPosition, Vec3 currentPosition) {
        if (config == null || level == null || !level.isClientSide() || previousPosition == null
                || currentPosition == null || config.vanillaParticle().isEmpty()) {
            return 0;
        }

        Vec3 delta = currentPosition.subtract(previousPosition);
        double distance = Math.max(delta.length(), 0.1D);
        Vec3 direction = delta.lengthSqr() == 0.0D ? Vec3.ZERO : delta.normalize();

        int spawned = 0;
        for (double offset = 0.0D; offset < distance; offset += 0.5D) {
            Vec3 particle = currentPosition.subtract(direction.scale(offset));
            ParticleUtil.spawnVanillaExt(level, particle.x, particle.y, particle.z, config.vanillaParticle(),
                    0.0D, 0.0D, 0.0D);
            spawned++;
        }
        return spawned;
    }

    public static int spawnBlackPowderBurst(BulletConfig config, Level level, Vec3 position, Vec3 motion,
            int ticksExisted, RandomSource random) {
        if (config == null || level == null || !level.isClientSide() || !config.blackPowder()
                || ticksExisted != 1 || position == null || motion == null) {
            return 0;
        }

        RandomSource roll = random == null ? level.random : random;
        for (int i = 0; i < 15; i++) {
            double modifier = roll.nextDouble();
            ParticleUtil.spawnVanillaExt(level, position.x, position.y, position.z, ParticleUtil.VANILLA_SMOKE,
                    (motion.x + roll.nextGaussian() * 0.05D) * modifier,
                    (motion.y + roll.nextGaussian() * 0.05D) * modifier,
                    (motion.z + roll.nextGaussian() * 0.05D) * modifier);
        }

        Vec3 flame = position.add(motion.scale(0.5D));
        ParticleUtil.spawnVanillaExt(level, flame.x, flame.y, flame.z, ParticleUtil.VANILLA_FLAME,
                0.0D, 0.0D, 0.0D);
        return 16;
    }

    public static int spawnMeteorFlameParticles(BulletConfig config, Level level, Vec3 position,
            RandomSource random) {
        if (config == null || level == null || !level.isClientSide()
                || !config.hasBehavior(BulletBehaviorTag.MASKMAN_METEOR_FLAME_PARTICLES) || position == null) {
            return 0;
        }

        RandomSource roll = random == null ? level.random : random;
        for (int i = 0; i < 5; i++) {
            ParticleUtil.spawnVanillaExt(level,
                    position.x + roll.nextDouble() * 0.5D - 0.25D,
                    position.y + roll.nextDouble() * 0.5D - 0.25D,
                    position.z + roll.nextDouble() * 0.5D - 0.25D,
                    ParticleUtil.VANILLA_FLAME, 0.0D, 0.0D, 0.0D);
        }
        return 5;
    }

    public static int spawnFlamethrowerTrail(BulletConfig config, Level level, Vec3 position) {
        if (config == null || level == null || !level.isClientSide() || position == null) {
            return 0;
        }
        if (config.hasBehavior(BulletBehaviorTag.BALEFIRE_VISUAL)) {
            ParticleUtil.spawnLegacyFlameEffectClient(level, position.x, position.y - 0.125D, position.z,
                    ParticleUtil.FLAMETHROWER_META_BALEFIRE);
            return 1;
        }
        if (config.hasBehavior(BulletBehaviorTag.FLAME_VISUAL)) {
            ParticleUtil.spawnLegacyFlameEffectClient(level, position.x, position.y - 0.125D, position.z,
                    ParticleUtil.FLAMETHROWER_META_FIRE);
            return 1;
        }
        return 0;
    }

    public static int spawnFireExtinguisherTrail(BulletConfig config, Level level, Vec3 position, Vec3 motion,
            RandomSource random) {
        if (config == null || level == null || !level.isClientSide() || position == null || motion == null) {
            return 0;
        }
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_WATER_VISUAL)) {
            spawnFireExtinguisherDust(level, position, motion, random, Blocks.WATER.defaultBlockState(), 0.05D);
            return 1;
        }
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_FOAM_VISUAL)) {
            spawnFireExtinguisherDust(level, position, motion, random,
                    ModBlocks.legacyBlock("block_foam").get().defaultBlockState(),
                    0.1D);
            return 1;
        }
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_SAND_VISUAL)) {
            spawnFireExtinguisherDust(level, position, motion, random, ModBlocks.SAND_BORON.get().defaultBlockState(),
                    0.1D);
            return 1;
        }
        return 0;
    }

    private static void spawnFireExtinguisherDust(Level level, Vec3 position, Vec3 motion, RandomSource random,
            BlockState state, double deviation) {
        RandomSource roll = random == null ? level.random : random;
        ParticleUtil.spawnVanillaExtBlockDust(level, position.x, position.y, position.z,
                motion.x + roll.nextGaussian() * deviation,
                motion.y - 0.2D + roll.nextGaussian() * deviation,
                motion.z + roll.nextGaussian() * deviation,
                state);
    }

    private BulletFlightVisualUtil() {
    }
}

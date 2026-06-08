package com.hbm.ntm.bullet;

import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BulletFlightVisualUtil {
    public static int spawnClientTickVisuals(BulletConfig config, Level level, Vec3 previousPosition,
            Vec3 currentPosition, Vec3 motion, int ticksExisted, RandomSource random) {
        return spawnBlackPowderBurst(config, level, currentPosition, motion, ticksExisted, random)
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

    private BulletFlightVisualUtil() {
    }
}

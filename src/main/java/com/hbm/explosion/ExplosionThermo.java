package com.hbm.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for thermo-explosion terrain and entity effects.
 *
 * <p>The modern implementation remains the sole owner of block mutation and
 * entity-effect runtime state.</p>
 */
@Deprecated(forRemoval = false)
public final class ExplosionThermo {
    private ExplosionThermo() {
    }

    public static void freeze(Level level, int x, int y, int z, int bombStartStrength) {
        com.hbm.ntm.explosion.ExplosionThermo.freeze(level, x, y, z, bombStartStrength);
    }

    public static void snow(Level level, int x, int y, int z, int bound) {
        com.hbm.ntm.explosion.ExplosionThermo.snow(level, x, y, z, bound);
    }

    public static void scorch(Level level, int x, int y, int z, int bombStartStrength) {
        com.hbm.ntm.explosion.ExplosionThermo.scorch(level, x, y, z, bombStartStrength);
    }

    public static void scorchLight(Level level, int x, int y, int z, int bombStartStrength) {
        com.hbm.ntm.explosion.ExplosionThermo.scorchLight(level, x, y, z, bombStartStrength);
    }

    public static void freezeDest(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.ExplosionThermo.freezeDest(level, x, y, z);
    }

    public static void freezeDest(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.ExplosionThermo.freezeDest(level, pos);
    }

    public static void scorchDest(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.ExplosionThermo.scorchDest(level, x, y, z);
    }

    public static void scorchDest(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.ExplosionThermo.scorchDest(level, pos);
    }

    public static void scorchDestLight(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.ExplosionThermo.scorchDestLight(level, x, y, z);
    }

    public static void scorchDestLight(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.ExplosionThermo.scorchDestLight(level, pos);
    }

    public static void freezer(Level level, int x, int y, int z, int bombStartStrength) {
        com.hbm.ntm.explosion.ExplosionThermo.freezer(level, x, y, z, bombStartStrength);
    }

    public static void setEntitiesOnFire(Level level, double x, double y, double z, int radius) {
        com.hbm.ntm.explosion.ExplosionThermo.setEntitiesOnFire(level, x, y, z, radius);
    }
}

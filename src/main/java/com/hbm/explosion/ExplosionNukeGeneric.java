package com.hbm.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for generic nuke damage and mutation helpers.
 */
@Deprecated(forRemoval = false)
public class ExplosionNukeGeneric {
    public static void empBlast(Level level, int x, int y, int z, int bombStartStrength) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.empBlast(level, x, y, z, bombStartStrength);
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.dealDamage(level, x, y, z, radius);
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius, float maxDamage) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.dealDamage(level, x, y, z, radius, maxDamage);
    }

    public static void vapor(Level level, int x, int y, int z, int bombStartStrength) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.vapor(level, x, y, z, bombStartStrength);
    }

    public static int destruction(Level level, int x, int y, int z) {
        return com.hbm.ntm.explosion.ExplosionNukeGeneric.destruction(level, x, y, z);
    }

    public static int destruction(Level level, BlockPos pos) {
        return com.hbm.ntm.explosion.ExplosionNukeGeneric.destruction(level, pos);
    }

    public static int vaporDest(Level level, int x, int y, int z) {
        return com.hbm.ntm.explosion.ExplosionNukeGeneric.vaporDest(level, x, y, z);
    }

    public static int vaporDest(Level level, BlockPos pos) {
        return com.hbm.ntm.explosion.ExplosionNukeGeneric.vaporDest(level, pos);
    }

    public static void waste(Level level, int x, int y, int z, int radius) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.waste(level, x, y, z, radius);
    }

    public static void wasteDest(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.wasteDest(level, x, y, z);
    }

    public static void wasteDest(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.wasteDest(level, pos);
    }

    public static void wasteNoSchrab(Level level, int x, int y, int z, int radius) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.wasteNoSchrab(level, x, y, z, radius);
    }

    public static void wasteDestNoSchrab(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.wasteDestNoSchrab(level, x, y, z);
    }

    public static void wasteDestNoSchrab(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.wasteDestNoSchrab(level, pos);
    }

    public static void emp(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.emp(level, x, y, z);
    }

    public static void emp(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.emp(level, pos);
    }

    public static void solinium(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.solinium(level, x, y, z);
    }

    public static void solinium(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.ExplosionNukeGeneric.solinium(level, pos);
    }
}

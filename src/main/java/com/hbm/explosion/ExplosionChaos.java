package com.hbm.explosion;

import net.minecraft.world.level.Level;

/**
 * Legacy-package facade for the shared modern chaos-explosion helpers.
 */
@Deprecated
public final class ExplosionChaos {
    public ExplosionChaos() {
    }

    public static void hardenVirus(Level level, int x, int y, int z, int bombStartStrength) {
        com.hbm.ntm.explosion.ExplosionChaos.hardenVirus(level, x, y, z, bombStartStrength);
    }

    public static void igniteFlammableBlocks(Level level, int x, int y, int z, int bound) {
        com.hbm.ntm.explosion.ExplosionChaos.igniteFlammableBlocks(level, x, y, z, bound);
    }

    public static void igniteAllBlocks(Level level, int x, int y, int z, int bound) {
        com.hbm.ntm.explosion.ExplosionChaos.igniteAllBlocks(level, x, y, z, bound);
    }

    public static void spawnPoisonCloud(Level level, double x, double y, double z, int count, double speed, int type) {
        com.hbm.ntm.explosion.ExplosionChaos.spawnPoisonCloud(level, x, y, z, count, speed, type);
    }

    public static void spawnVolley(Level level, double x, double y, double z, int count, double speed) {
        com.hbm.ntm.explosion.ExplosionChaos.spawnVolley(level, x, y, z, count, speed);
    }

    public static void cluster(Level level, double x, double y, double z, int count, float yaw, float pitch,
            float yawRand, float pitchRand, float speed) {
        com.hbm.ntm.explosion.ExplosionChaos.cluster(level, x, y, z, count, yaw, pitch, yawRand, pitchRand, speed);
    }

    public static void poison(Level level, double x, double y, double z, double range) {
        com.hbm.ntm.explosion.ExplosionChaos.poison(level, x, y, z, range);
    }

    public static void pc(Level level, double x, double y, double z, double range) {
        com.hbm.ntm.explosion.ExplosionChaos.pc(level, x, y, z, range);
    }

    public static void c(Level level, double x, double y, double z, double range) {
        com.hbm.ntm.explosion.ExplosionChaos.c(level, x, y, z, range);
    }

    public static void floater(Level level, int x, int y, int z, int radius, int height) {
        com.hbm.ntm.explosion.ExplosionChaos.floater(level, x, y, z, radius, height);
    }

    public static void move(Level level, int x, int y, int z, int radius, int offsetX, int offsetY, int offsetZ) {
        com.hbm.ntm.explosion.ExplosionChaos.move(level, x, y, z, radius, offsetX, offsetY, offsetZ);
    }
}

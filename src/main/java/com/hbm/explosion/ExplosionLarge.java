package com.hbm.explosion;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Legacy-package facade for the single modern large-explosion helper runtime.
 */
@Deprecated
public final class ExplosionLarge {
    public static void spawnParticles(Level level, double x, double y, double z, int count) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnParticles(level, x, y, z, count);
    }

    public static void spawnParticlesRadial(Level level, double x, double y, double z, int count) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnParticlesRadial(level, x, y, z, count);
    }

    public static void spawnFoam(Level level, double x, double y, double z, int count) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnFoam(level, x, y, z, count);
    }

    public static void spawnShock(Level level, double x, double y, double z, int count, double strength) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnShock(level, x, y, z, count, strength);
    }

    public static void spawnBurst(Level level, double x, double y, double z, int count, double strength) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnBurst(level, x, y, z, count, strength);
    }

    public static void spawnRubble(Level level, double x, double y, double z, int count) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnRubble(level, x, y, z, count);
    }

    public static void spawnRubble(Level level, double x, double y, double z, int count, @Nullable Entity source) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnRubble(level, x, y, z, count, source);
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnShrapnels(level, x, y, z, count);
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count, float motion) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnShrapnels(level, x, y, z, count, motion);
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count, float motion,
            @Nullable Entity source) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnShrapnels(level, x, y, z, count, motion, source);
    }

    public static void spawnTracers(Level level, double x, double y, double z, int count) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnTracers(level, x, y, z, count);
    }

    public static void spawnTracers(Level level, double x, double y, double z, int count, float motion) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnTracers(level, x, y, z, count, motion);
    }

    public static void spawnTracers(Level level, double x, double y, double z, int count, float motion,
            @Nullable Entity source) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnTracers(level, x, y, z, count, motion, source);
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z, double motionX, double motionY,
            double motionZ, int count, double deviation) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnShrapnelShower(level, x, y, z, motionX, motionY, motionZ, count, deviation);
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z, double motionX, double motionY,
            double motionZ, int count, double deviation, @Nullable Entity source) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnShrapnelShower(level, x, y, z, motionX, motionY, motionZ, count, deviation, source);
    }

    public static void spawnMissileDebris(Level level, double x, double y, double z, double motionX, double motionY,
            double motionZ, double deviation, List<ItemStack> debris, @Nullable ItemStack rareDrop) {
        com.hbm.ntm.explosion.ExplosionLarge.spawnMissileDebris(level, x, y, z, motionX, motionY, motionZ, deviation, debris, rareDrop);
    }

    public static void explode(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel) {
        com.hbm.ntm.explosion.ExplosionLarge.explode(level, x, y, z, strength, cloud, rubble, shrapnel);
    }

    public static void explode(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel, @Nullable Entity source) {
        com.hbm.ntm.explosion.ExplosionLarge.explode(level, x, y, z, strength, cloud, rubble, shrapnel, source);
    }

    public static void explodeFire(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel) {
        com.hbm.ntm.explosion.ExplosionLarge.explodeFire(level, x, y, z, strength, cloud, rubble, shrapnel);
    }

    public static void explodeFire(Level level, double x, double y, double z, float strength, boolean cloud, boolean rubble,
            boolean shrapnel, @Nullable Entity source) {
        com.hbm.ntm.explosion.ExplosionLarge.explodeFire(level, x, y, z, strength, cloud, rubble, shrapnel, source);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, float depth) {
        com.hbm.ntm.explosion.ExplosionLarge.buster(level, x, y, z, direction, strength, depth);
    }

    public static void buster(Level level, double x, double y, double z, Vec3 direction, float strength, float depth,
            @Nullable Entity source) {
        com.hbm.ntm.explosion.ExplosionLarge.buster(level, x, y, z, direction, strength, depth, source);
    }

    public static void jolt(Level level, double x, double y, double z, double strength, int count, double velocity) {
        com.hbm.ntm.explosion.ExplosionLarge.jolt(level, x, y, z, strength, count, velocity);
    }

    public static int cloudFunction(int strength) {
        return com.hbm.ntm.explosion.ExplosionLarge.cloudFunction(strength);
    }

    public static int rubbleFunction(int strength) {
        return com.hbm.ntm.explosion.ExplosionLarge.rubbleFunction(strength);
    }

    public static int shrapnelFunction(int strength) {
        return com.hbm.ntm.explosion.ExplosionLarge.shrapnelFunction(strength);
    }

    private ExplosionLarge() {
    }
}

package com.hbm.ntm.explosion;

import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacyBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class CustomMissileExplosion {
    public enum WarheadType {
        HE,
        INC,
        BUSTER,
        CLUSTER,
        NUCLEAR,
        TX,
        N2,
        BALEFIRE,
        SCHRAB,
        TAINT,
        CLOUD,
        TURBINE,
        CUSTOM0,
        CUSTOM1,
        CUSTOM2,
        CUSTOM3,
        CUSTOM4,
        CUSTOM5,
        CUSTOM6,
        CUSTOM7,
        CUSTOM8,
        CUSTOM9
    }

    public static boolean explode(Level level, double x, double y, double z, Vec3 motion, float strength, WarheadType type,
            @Nullable Entity source) {
        if (level == null || level.isClientSide() || type == null) {
            return false;
        }

        switch (type) {
            case HE -> {
                ExplosionLarge.explode(level, x, y, z, strength, true, false, true, source);
                ExplosionLarge.jolt(level, x, y, z, strength, (int) (strength * 50.0F), 0.25D);
                return true;
            }
            case INC -> {
                ExplosionLarge.explodeFire(level, x, y, z, strength, true, false, true, source);
                ExplosionLarge.jolt(level, x, y, z, strength * 1.5D, (int) (strength * 50.0F), 0.25D);
                return true;
            }
            case BUSTER -> {
                ExplosionLarge.buster(level, x, y, z, motion, strength, strength * 4.0F, source);
                return true;
            }
            case NUCLEAR, TX -> {
                NuclearExplosionUtil.spawnNuclear(level, (int) strength, x, y, z);
                return true;
            }
            case BALEFIRE -> {
                WeaponExplosionUtil.spawnBalefire(level, x, y, z, (int) strength);
                return true;
            }
            case N2 -> {
                NuclearExplosionUtil.spawnNuclearNoFallout(level, (int) strength, x, y, z);
                return true;
            }
            case CLOUD -> {
                level.levelEvent(2002, net.minecraft.core.BlockPos.containing(Math.round(x), Math.round(y), Math.round(z)), 0);
                ExplosionChaos.spawnPoisonCloud(level, x - motion.x, y - motion.y, z - motion.z, 750, 2.5D, 2);
                return true;
            }
            case TURBINE -> {
                ExplosionLarge.explode(level, x, y, z, 10.0F, true, false, true, source);
                spawnTurbineBlades(level, x - motion.x, y - motion.y, z - motion.z, (int) strength);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static boolean explode(Level level, double x, double y, double z, double motionX, double motionY, double motionZ,
            float strength, WarheadType type, @Nullable Entity source) {
        return explode(level, x, y, z, new Vec3(motionX, motionY, motionZ), strength, type, source);
    }

    private CustomMissileExplosion() {
    }

    private static void spawnTurbineBlades(Level level, double x, double y, double z, int count) {
        if (count <= 0) {
            return;
        }
        Vec3 origin = new Vec3(x, y, z);
        for (int i = 0; i < count; i++) {
            double angle = 2.0D * Math.PI * i / count;
            Vec3 motion = legacyRotateY(0.5D, 0.0D, 0.0D, angle);
            Vec3 position = origin.add(0.0D, level.random.nextGaussian(), 0.0D);
            BulletLaunchUtil.LaunchPlan base = BulletLaunchUtil.directedLaunchPlan(
                    LegacyBulletConfigs.TURBINE, position, motion, 1.0F, 0.0F, level.random);
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.withMotion(base, motion);
            level.addFreshEntity(BulletProjectileEntity.fromLaunchPlan(level, plan, null));
        }
    }

    private static Vec3 legacyRotateY(double x, double y, double z, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(x * cos + z * sin, y, z * cos - x * sin);
    }
}

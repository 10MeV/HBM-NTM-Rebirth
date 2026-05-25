package com.hbm.ntm.explosion;

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
}

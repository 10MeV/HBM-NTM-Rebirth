package com.hbm.explosion;

import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for explosion radiation exposure.
 */
@Deprecated(forRemoval = false)
public class ExplosionHurtUtil {
    public static void doRadiation(Level level, double x, double y, double z, float outer, float inner, double radius) {
        com.hbm.ntm.explosion.ExplosionHurtUtil.doRadiation(level, x, y, z, outer, inner, radius);
    }
}

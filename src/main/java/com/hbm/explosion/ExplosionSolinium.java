package com.hbm.explosion;

import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the solinium column worker.
 */
@Deprecated(forRemoval = false)
public class ExplosionSolinium extends com.hbm.ntm.explosion.ExplosionSolinium {
    public ExplosionSolinium(int x, int y, int z, Level level, int radius, float coefficient, float coefficient2) {
        super(x, y, z, level, radius, coefficient, coefficient2);
    }
}

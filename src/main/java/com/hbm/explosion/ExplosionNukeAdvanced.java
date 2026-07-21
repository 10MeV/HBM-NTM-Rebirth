package com.hbm.explosion;

import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the MK3 column worker.
 */
@Deprecated(forRemoval = false)
public class ExplosionNukeAdvanced extends com.hbm.ntm.explosion.ExplosionNukeAdvanced {
    public ExplosionNukeAdvanced(int x, int y, int z, Level level, int radius, float coefficient, int type) {
        super(x, y, z, level, radius, coefficient, type);
    }
}

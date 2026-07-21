package com.hbm.explosion;

import net.minecraft.world.level.Level;

/** Legacy-package bridge for the one modern Fleija column worker. */
public class ExplosionFleija extends com.hbm.ntm.explosion.ExplosionFleija {
    public ExplosionFleija(int x, int y, int z, Level level, int radius, float coefficient, float coefficient2) {
        super(x, y, z, level, radius, coefficient, coefficient2);
    }
}

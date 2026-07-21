package com.hbm.explosion;

import net.minecraft.world.level.Level;

/** Legacy-package bridge for the one modern balefire terrain worker. */
public class ExplosionBalefire extends com.hbm.ntm.explosion.ExplosionBalefire {
    public ExplosionBalefire(int x, int y, int z, Level level, int radius) {
        super(x, y, z, level, radius);
    }
}

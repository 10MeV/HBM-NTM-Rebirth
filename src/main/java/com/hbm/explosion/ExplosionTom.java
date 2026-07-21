package com.hbm.explosion;

import net.minecraft.world.level.Level;

/** Legacy-package bridge for the one modern TOM crater worker. */
public class ExplosionTom extends com.hbm.ntm.explosion.ExplosionTom {
    public ExplosionTom(int x, int y, int z, Level level, int radius) {
        super(x, y, z, level, radius);
    }
}

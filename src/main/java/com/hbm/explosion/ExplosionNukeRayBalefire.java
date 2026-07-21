package com.hbm.explosion;

import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the balefire nuke-ray variant.
 */
@Deprecated(forRemoval = false)
public class ExplosionNukeRayBalefire extends com.hbm.ntm.explosion.ExplosionNukeRayBalefire {
    public ExplosionNukeRayBalefire(Level level, int x, int y, int z, int strength, int speed, int length) {
        super(level, x, y, z, strength, speed, length);
    }
}

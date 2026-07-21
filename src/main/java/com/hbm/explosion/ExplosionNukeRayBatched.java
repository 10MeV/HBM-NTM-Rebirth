package com.hbm.explosion;

import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;

/**
 * Legacy 1.7.10 package bridge for the active MK5 batched ray worker.
 */
@Deprecated(forRemoval = false)
public class ExplosionNukeRayBatched extends com.hbm.ntm.explosion.ExplosionNukeRayBatched
        implements com.hbm.interfaces.IExplosionRay {
    public ExplosionNukeRayBatched(Level level, int x, int y, int z, int strength, int speed, int length) {
        super(level, x, y, z, strength, speed, length);
    }

    public ExplosionNukeRayBatched(Level level, int x, int y, int z, int strength, int speed, int length,
            BiConsumer<Integer, Integer> chunkLoader) {
        super(level, x, y, z, strength, speed, length, chunkLoader);
    }
}

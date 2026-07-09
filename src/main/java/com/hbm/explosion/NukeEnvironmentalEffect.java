package com.hbm.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for nuke fallout terrain mutation helpers.
 */
@Deprecated(forRemoval = false)
public class NukeEnvironmentalEffect {
    public static void applyStandardAOE(Level level, int x, int y, int z, int radius, int jaggedness) {
        com.hbm.ntm.explosion.NukeEnvironmentalEffect.applyStandardAOE(level, x, y, z, radius, jaggedness);
    }

    public static void applyStandardEffect(Level level, int x, int y, int z) {
        com.hbm.ntm.explosion.NukeEnvironmentalEffect.applyStandardEffect(level, x, y, z);
    }

    public static void applyStandardEffect(Level level, BlockPos pos) {
        com.hbm.ntm.explosion.NukeEnvironmentalEffect.applyStandardEffect(level, pos);
    }
}

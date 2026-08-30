package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Server-side callback used by the legacy redstone fan when its air stream meets a block.
 *
 * <p>Mirrors 1.7.10 {@code IBlowable#applyFan(World, x, y, z, ForgeDirection, dist)} without
 * creating a second, modern-only wind system.</p>
 */
public interface Blowable {
    void applyFan(Level level, BlockPos pos, Direction direction, int distance);
}

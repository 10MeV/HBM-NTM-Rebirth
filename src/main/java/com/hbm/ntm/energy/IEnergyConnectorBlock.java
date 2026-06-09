package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy-name bridge for block-level Energy MK2 connection checks.
 */
@Deprecated(forRemoval = false)
public interface IEnergyConnectorBlock extends HbmEnergyConnectorBlock {
    default boolean canConnect(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return side != null;
    }

    @Override
    default boolean canConnectEnergy(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return canConnect(level, pos, side);
    }
}

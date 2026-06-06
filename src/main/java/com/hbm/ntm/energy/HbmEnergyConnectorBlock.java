package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public interface HbmEnergyConnectorBlock {
    /**
     * Side is the side of this block, matching the 1.7.10 IEnergyConnectorBlock contract.
     */
    boolean canConnectEnergy(BlockGetter level, BlockPos pos, @Nullable Direction side);
}

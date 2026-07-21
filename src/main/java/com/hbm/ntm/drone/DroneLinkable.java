package com.hbm.ntm.drone;

import net.minecraft.core.BlockPos;

/** A block entity which can be chained by the legacy drone linker. */
public interface DroneLinkable {
    BlockPos dronePoint();

    void setNextDroneTarget(BlockPos target);
}

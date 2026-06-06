package com.hbm.ntm.energy;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface HbmEnergyConnector {
    /**
     * Side is the side of this connector, matching the 1.7.10 IEnergyConnectorMK2 contract.
     */
    default boolean canConnectEnergy(@Nullable Direction side) {
        return side != null;
    }
}

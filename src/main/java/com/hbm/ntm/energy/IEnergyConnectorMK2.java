package com.hbm.ntm.energy;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy-name bridge for the Energy MK2 connector API.
 */
@Deprecated(forRemoval = false)
public interface IEnergyConnectorMK2 extends HbmEnergyConnector {
    default boolean canConnect(@Nullable Direction side) {
        return side != null;
    }

    @Override
    default boolean canConnectEnergy(@Nullable Direction side) {
        return canConnect(side);
    }
}

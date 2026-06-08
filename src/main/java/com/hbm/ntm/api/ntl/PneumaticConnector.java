package com.hbm.ntm.api.ntl;

import net.minecraft.core.Direction;

public interface PneumaticConnector {
    default boolean canConnectPneumatic(Direction side) {
        return side != null;
    }
}

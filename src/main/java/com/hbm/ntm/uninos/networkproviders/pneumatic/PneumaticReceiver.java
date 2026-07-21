package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public record PneumaticReceiver(
        IItemHandler handler,
        Direction pipeDirection,
        PneumaticEndpoint endpoint,
        @Nullable PneumaticItemAccess access) {

    public PneumaticReceiver {
        // A legacy non-TileEntity IInventory has no sortable position. Keep
        // that fact instead of manufacturing the endpoint tube position.
        access = access == null ? new PneumaticItemAccess(handler, null) : access;
    }
}

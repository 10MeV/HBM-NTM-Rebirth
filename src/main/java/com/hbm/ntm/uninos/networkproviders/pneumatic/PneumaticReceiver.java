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
        access = access == null ? new PneumaticItemAccess(handler, endpoint.getPneumaticPos()) : access;
    }
}

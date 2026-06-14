package com.hbm.packet;

import com.hbm.ntm.network.LegacyNetworkDispatcher;
import com.hbm.ntm.network.ModMessages;

/**
 * Legacy package facade for old PacketDispatcher.wrapper call sites.
 */
public final class PacketDispatcher {
    public static final LegacyNetworkDispatcher wrapper = ModMessages.wrapper();

    public static LegacyNetworkDispatcher wrapper() {
        return wrapper;
    }

    private PacketDispatcher() {
    }
}

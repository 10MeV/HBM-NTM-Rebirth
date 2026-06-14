package com.hbm.ntm.network;

/**
 * Marker for old package-name packet facades that should be sent as a modern
 * registered packet type.
 */
public interface LegacyPacketAdapter {
    Object toModernPacket();
}

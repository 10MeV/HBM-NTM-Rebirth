package com.hbm.ntm.network;

/**
 * Modern equivalent of the legacy ThreadedPacket precompile step.
 */
public interface HbmPreparablePacket {
    /**
     * Returns a thread-safe packet instance for deferred S2C sending.
     *
     * <p>Legacy 1.7.10 packets used PrecompiledPacket/ThreadedPacket to freeze
     * a ByteBuf before entering the packet thread. Modern packets should return
     * either an immutable copy or this instance when the packet is already safe.
     */
    Object prepareForThreadedSend();
}

package com.hbm.ntm.network;

/**
 * Modern equivalent of the legacy ThreadedPacket precompile step.
 */
public interface HbmPreparablePacket {
    Object prepareForThreadedSend();
}

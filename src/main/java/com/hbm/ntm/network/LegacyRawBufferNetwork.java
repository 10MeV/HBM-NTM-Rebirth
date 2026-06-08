package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Diagnostics for the old NetworkHandler ByteBuf dispatch path.
 */
public final class LegacyRawBufferNetwork {
    private static final AtomicLong BLOCKED_RAW_BUFFER_SENDS = new AtomicLong();
    private static volatile String lastBlockedRawBufferSend = "";

    private LegacyRawBufferNetwork() {
    }

    public static void rejectAllAround(ByteBuf message, PacketDistributor.TargetPoint point) {
        String reason = "sendToAllAround(ByteBuf, TargetPoint)"
                + " bytes=" + readableBytes(message)
                + " target=" + (point == null ? "null" : "near");
        recordBlocked(reason);
    }

    public static long blockedRawBufferSendCount() {
        return BLOCKED_RAW_BUFFER_SENDS.get();
    }

    public static String lastBlockedRawBufferSend() {
        return lastBlockedRawBufferSend;
    }

    public static String summary() {
        return "legacyRawBuffer=NetworkHandler ByteBuf facade"
                + " blocked=" + blockedRawBufferSendCount()
                + (lastBlockedRawBufferSend.isBlank() ? "" : " lastBlocked=\"" + lastBlockedRawBufferSend + "\"")
                + " note=raw ByteBuf dispatch is unsupported; migrate to registered packets or tile binary helpers";
    }

    public static void reset() {
        BLOCKED_RAW_BUFFER_SENDS.set(0L);
        lastBlockedRawBufferSend = "";
    }

    private static void recordBlocked(String reason) {
        BLOCKED_RAW_BUFFER_SENDS.incrementAndGet();
        lastBlockedRawBufferSend = reason;
        HbmNtm.LOGGER.warn("Blocked legacy HBM raw ByteBuf network send: {}.", reason);
    }

    private static int readableBytes(ByteBuf message) {
        return message == null ? -1 : message.readableBytes();
    }
}

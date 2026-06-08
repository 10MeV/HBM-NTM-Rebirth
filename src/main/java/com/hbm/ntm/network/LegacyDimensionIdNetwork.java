package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Diagnostics for legacy 1.7.10 integer dimension-id network targets.
 */
public final class LegacyDimensionIdNetwork {
    private static final AtomicLong BLOCKED_DIMENSION_ID_SENDS = new AtomicLong();
    private static final AtomicLong BLOCKED_SEND_TO_DIMENSION = new AtomicLong();
    private static final AtomicLong BLOCKED_NEAR_DIMENSION = new AtomicLong();
    private static final AtomicLong BLOCKED_PACKET_THREADING_NEAR_DIMENSION = new AtomicLong();
    private static volatile String lastBlockedDimensionIdSend = "";

    private LegacyDimensionIdNetwork() {
    }

    public static void rejectSendToDimension(Object message, int dimensionId, boolean threaded) {
        recordBlocked("sendToDimension"
                + (threaded ? "Threaded" : "")
                + "(message, int)"
                + " dimensionId=" + dimensionId
                + " message=" + messageType(message));
        BLOCKED_SEND_TO_DIMENSION.incrementAndGet();
    }

    public static void rejectAllAround(Object message, int dimensionId,
                                       double x, double y, double z, double range, boolean threaded) {
        recordBlocked("sendToAllAround"
                + (threaded ? "Threaded" : "")
                + "(message, int, x, y, z, range)"
                + " dimensionId=" + dimensionId
                + " pos=" + x + "," + y + "," + z
                + " range=" + range
                + " message=" + messageType(message));
        BLOCKED_NEAR_DIMENSION.incrementAndGet();
    }

    public static void rejectAllAround(Object message, LegacyTargetPoint point, boolean threaded) {
        if (point == null) {
            recordBlocked("sendToAllAround"
                    + (threaded ? "Threaded" : "")
                    + "(message, LegacyTargetPoint)"
                    + " target=null"
                    + " message=" + messageType(message));
            BLOCKED_NEAR_DIMENSION.incrementAndGet();
            return;
        }
        rejectAllAround(message, point.legacyDimensionIdOrZero(), point.x(), point.y(), point.z(), point.range(), threaded);
    }

    public static void rejectPacketThreadingAllAround(Object message, int dimensionId,
                                                      double x, double y, double z, double range) {
        recordBlocked("PacketThreading.createAllAroundThreadedPacket(message, int, x, y, z, range)"
                + " dimensionId=" + dimensionId
                + " pos=" + x + "," + y + "," + z
                + " range=" + range
                + " message=" + messageType(message));
        BLOCKED_PACKET_THREADING_NEAR_DIMENSION.incrementAndGet();
    }

    public static void rejectPacketThreadingAllAround(Object message, LegacyTargetPoint point) {
        if (point == null) {
            recordBlocked("PacketThreading.createAllAroundThreadedPacket(message, LegacyTargetPoint)"
                    + " target=null"
                    + " message=" + messageType(message));
            BLOCKED_PACKET_THREADING_NEAR_DIMENSION.incrementAndGet();
            return;
        }
        rejectPacketThreadingAllAround(message, point.legacyDimensionIdOrZero(),
                point.x(), point.y(), point.z(), point.range());
    }

    public static long blockedDimensionIdSendCount() {
        return BLOCKED_DIMENSION_ID_SENDS.get();
    }

    public static String lastBlockedDimensionIdSend() {
        return lastBlockedDimensionIdSend;
    }

    public static String summary() {
        return "legacyDimensionId=NetworkHandler int dimension facade"
                + " blocked=" + blockedDimensionIdSendCount()
                + " sendToDimension=" + BLOCKED_SEND_TO_DIMENSION.get()
                + " near=" + BLOCKED_NEAR_DIMENSION.get()
                + " packetThreadingNear=" + BLOCKED_PACKET_THREADING_NEAR_DIMENSION.get()
                + (lastBlockedDimensionIdSend.isBlank() ? "" : " lastBlocked=\"" + lastBlockedDimensionIdSend + "\"")
                + " note=int dimension ids are unsupported; pass ServerLevel or ResourceKey<Level>";
    }

    public static void reset() {
        BLOCKED_DIMENSION_ID_SENDS.set(0L);
        BLOCKED_SEND_TO_DIMENSION.set(0L);
        BLOCKED_NEAR_DIMENSION.set(0L);
        BLOCKED_PACKET_THREADING_NEAR_DIMENSION.set(0L);
        lastBlockedDimensionIdSend = "";
    }

    private static void recordBlocked(String reason) {
        BLOCKED_DIMENSION_ID_SENDS.incrementAndGet();
        lastBlockedDimensionIdSend = reason;
        HbmNtm.LOGGER.warn("Blocked legacy HBM integer dimension-id network send: {}.", reason);
    }

    private static String messageType(Object message) {
        return message == null ? "null" : message.getClass().getName();
    }
}

package com.hbm.ntm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Compatibility facade for 1.7.10-style PacketThreading call sites.
 */
public final class LegacyPacketThreading {
    public static final String THREAD_PREFIX = "NTM-Packet-Thread-";

    private static final AtomicLong LEGACY_ALL_AROUND_CALLS = new AtomicLong();
    private static final AtomicLong LEGACY_SEND_TO_CALLS = new AtomicLong();
    private static final AtomicLong LEGACY_WAIT_CALLS = new AtomicLong();
    private static final AtomicLong LEGACY_CLEAR_CALLS = new AtomicLong();
    private static final AtomicLong LEGACY_CURRENT_TICK_TOTAL = new AtomicLong();
    private static final AtomicLong LEGACY_LAST_TICK_TOTAL = new AtomicLong();
    private static final AtomicLong LEGACY_LAST_WAIT_MILLIS = new AtomicLong();

    private LegacyPacketThreading() {
    }

    public static void createAllAroundThreadedPacket(Object message, PacketDistributor.TargetPoint point) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        ThreadedPacketDispatcher.sendToAllAround(message, point);
    }

    public static void createAllAroundThreadedPacket(Object message, ServerLevel level,
                                                     double x, double y, double z, double range) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        ThreadedPacketDispatcher.sendToAllAround(message, level, x, y, z, range);
    }

    public static void createAllAroundThreadedPacket(Object message, ServerLevel level, BlockPos pos, double range) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        ThreadedPacketDispatcher.sendToAllAround(message, level, pos, range);
    }

    public static void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                                     double x, double y, double z, double range) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        ThreadedPacketDispatcher.sendToAllAround(message, dimension, x, y, z, range);
    }

    public static void createAllAroundThreadedPacket(Object message, int dimensionId,
                                                     double x, double y, double z, double range) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        LegacyDimensionIdNetwork.rejectPacketThreadingAllAround(message, dimensionId, x, y, z, range);
    }

    public static void createAllAroundThreadedPacket(Object message, LegacyTargetPoint point) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        if (point != null && point.hasModernDimension()) {
            ThreadedPacketDispatcher.sendToAllAround(message, point.toModernTargetPoint());
            return;
        }
        LegacyDimensionIdNetwork.rejectPacketThreadingAllAround(message, point);
    }

    public static void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                                     BlockPos pos, double range) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        ThreadedPacketDispatcher.sendToAllAround(message, dimension, pos, range);
    }

    public static void createAllAroundThreadedPacket(Object message, Entity entity, double range) {
        LEGACY_ALL_AROUND_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        ThreadedPacketDispatcher.sendToAllAround(message, entity, range);
    }

    public static void createSendToThreadedPacket(Object message, ServerPlayer player) {
        LEGACY_SEND_TO_CALLS.incrementAndGet();
        LEGACY_CURRENT_TICK_TOTAL.incrementAndGet();
        ThreadedPacketDispatcher.sendToPlayer(message, player);
    }

    public static void waitUntilThreadFinished() {
        LEGACY_WAIT_CALLS.incrementAndGet();
        ThreadedPacketDispatcher.flush();
        ThreadedPacketDispatcher.Snapshot snapshot = ThreadedPacketDispatcher.snapshot();
        LEGACY_LAST_TICK_TOTAL.set(LEGACY_CURRENT_TICK_TOTAL.getAndSet(0L));
        LEGACY_LAST_WAIT_MILLIS.set(snapshot.lastFlushWaitMillis());
    }

    public static int clearThreadPoolTasks() {
        LEGACY_CLEAR_CALLS.incrementAndGet();
        return ThreadedPacketDispatcher.clearPending("Legacy PacketThreading.clearThreadPoolTasks facade.");
    }

    public static boolean isTriggered() {
        return ThreadedPacketDispatcher.isFallbackToMainThread();
    }

    public static String threadPrefix() {
        return ThreadedPacketDispatcher.threadPrefix();
    }

    public static long legacyAllAroundCallCount() {
        return LEGACY_ALL_AROUND_CALLS.get();
    }

    public static long legacySendToCallCount() {
        return LEGACY_SEND_TO_CALLS.get();
    }

    public static long legacyWaitCallCount() {
        return LEGACY_WAIT_CALLS.get();
    }

    public static long legacyClearCallCount() {
        return LEGACY_CLEAR_CALLS.get();
    }

    public static void resetLegacyCounters() {
        LEGACY_ALL_AROUND_CALLS.set(0L);
        LEGACY_SEND_TO_CALLS.set(0L);
        LEGACY_WAIT_CALLS.set(0L);
        LEGACY_CLEAR_CALLS.set(0L);
        LEGACY_CURRENT_TICK_TOTAL.set(0L);
        LEGACY_LAST_TICK_TOTAL.set(0L);
        LEGACY_LAST_WAIT_MILLIS.set(0L);
    }

    public static int legacyHelperCount() {
        return 12;
    }

    public static String compatibilitySummary() {
        LegacyCommandSnapshot commandSnapshot = legacyCommandSnapshot();
        ThreadedPacketDispatcher.Snapshot dispatcherSnapshot = ThreadedPacketDispatcher.snapshot();
        return "legacyPacketThreading=PacketThreading facade"
                + " helpers=" + legacyHelperCount()
                + " allAroundCalls=" + legacyAllAroundCallCount()
                + " sendToCalls=" + legacySendToCallCount()
                + " waitCalls=" + legacyWaitCallCount()
                + " clearCalls=" + legacyClearCallCount()
                + " prepared=" + dispatcherSnapshot.totalPrepared()
                + " preparable=" + dispatcherSnapshot.preparableMessages()
                + " preparedCopies=" + dispatcherSnapshot.preparedCopyInstance()
                + " currentTickTotal=" + commandSnapshot.currentTickTotal()
                + " lastTickTotal=" + commandSnapshot.lastTickTotal()
                + " remaining=" + commandSnapshot.remaining()
                + " lastWaitMs=" + commandSnapshot.lastWaitMillis()
                + " triggered=" + isTriggered()
                + " threadPrefix=" + threadPrefix();
    }

    public static LegacyCommandSnapshot legacyCommandSnapshot() {
        ThreadedPacketDispatcher.Snapshot snapshot = ThreadedPacketDispatcher.snapshot();
        long currentTotal = LEGACY_CURRENT_TICK_TOTAL.get();
        int remaining = snapshot.pending() + snapshot.executorQueueSize();
        double remainingPercent = currentTotal == 0L ? 0.0D : remaining * 100.0D / currentTotal;
        return new LegacyCommandSnapshot(
                currentTotal,
                LEGACY_LAST_TICK_TOTAL.get(),
                remaining,
                remainingPercent,
                LEGACY_LAST_WAIT_MILLIS.get(),
                snapshot.consecutiveClears(),
                isTriggered(),
                ThreadedPacketDispatcher.isEnabled());
    }

    public static String legacyCommandInfoSummary() {
        LegacyCommandSnapshot snapshot = legacyCommandSnapshot();
        return "legacyCommandInfo"
                + " active=" + snapshot.active()
                + " triggered=" + snapshot.triggered()
                + " currentTickTotal=" + snapshot.currentTickTotal()
                + " lastTickTotal=" + snapshot.lastTickTotal()
                + " remaining=" + snapshot.remaining()
                + " remainingPercent=" + Math.round(snapshot.remainingPercent() * 100.0D) / 100.0D
                + " lastWaitMs=" + snapshot.lastWaitMillis()
                + " clearCount=" + snapshot.clearCount();
    }

    public record LegacyCommandSnapshot(
            long currentTickTotal,
            long lastTickTotal,
            int remaining,
            double remainingPercent,
            long lastWaitMillis,
            int clearCount,
            boolean triggered,
            boolean active) {
    }
}

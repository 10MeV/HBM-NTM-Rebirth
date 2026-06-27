package com.hbm.ntm.util;

import java.util.concurrent.atomic.AtomicLong;

public final class HbmMachinePerformanceCounters {
    private static volatile boolean enabled;

    private static final AtomicLong fluidNetworkTicks = new AtomicLong();
    private static final AtomicLong fluidNodeRefreshes = new AtomicLong();
    private static final AtomicLong fluidSubscriptionRefreshes = new AtomicLong();
    private static final AtomicLong fluidPortChecks = new AtomicLong();
    private static final AtomicLong fluidPortSubscriptions = new AtomicLong();
    private static final AtomicLong fluidPortTransfers = new AtomicLong();
    private static final AtomicLong energyPortChecks = new AtomicLong();
    private static final AtomicLong energyPortSubscriptions = new AtomicLong();
    private static final AtomicLong tileCacheHits = new AtomicLong();
    private static final AtomicLong tileCacheMisses = new AtomicLong();
    private static final AtomicLong tileCacheInvalidations = new AtomicLong();
    private static final AtomicLong networkPackBytes = new AtomicLong();
    private static final AtomicLong networkPackSent = new AtomicLong();
    private static final AtomicLong networkPackSkipped = new AtomicLong();
    private static final AtomicLong blockUpdates = new AtomicLong();

    private HbmMachinePerformanceCounters() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        HbmMachinePerformanceCounters.enabled = enabled;
    }

    public static void reset() {
        fluidNetworkTicks.set(0L);
        fluidNodeRefreshes.set(0L);
        fluidSubscriptionRefreshes.set(0L);
        fluidPortChecks.set(0L);
        fluidPortSubscriptions.set(0L);
        fluidPortTransfers.set(0L);
        energyPortChecks.set(0L);
        energyPortSubscriptions.set(0L);
        tileCacheHits.set(0L);
        tileCacheMisses.set(0L);
        tileCacheInvalidations.set(0L);
        networkPackBytes.set(0L);
        networkPackSent.set(0L);
        networkPackSkipped.set(0L);
        blockUpdates.set(0L);
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                enabled,
                fluidNetworkTicks.get(),
                fluidNodeRefreshes.get(),
                fluidSubscriptionRefreshes.get(),
                fluidPortChecks.get(),
                fluidPortSubscriptions.get(),
                fluidPortTransfers.get(),
                energyPortChecks.get(),
                energyPortSubscriptions.get(),
                tileCacheHits.get(),
                tileCacheMisses.get(),
                tileCacheInvalidations.get(),
                networkPackBytes.get(),
                networkPackSent.get(),
                networkPackSkipped.get(),
                blockUpdates.get());
    }

    public static void fluidNetworkTick() {
        if (enabled) fluidNetworkTicks.incrementAndGet();
    }

    public static void fluidNodeRefresh() {
        if (enabled) fluidNodeRefreshes.incrementAndGet();
    }

    public static void fluidSubscriptionRefresh() {
        if (enabled) fluidSubscriptionRefreshes.incrementAndGet();
    }

    public static void fluidPortCheck() {
        if (enabled) fluidPortChecks.incrementAndGet();
    }

    public static void fluidPortSubscription(boolean subscribed) {
        if (enabled && subscribed) fluidPortSubscriptions.incrementAndGet();
    }

    public static void fluidPortTransfer(long amount) {
        if (enabled && amount > 0L) fluidPortTransfers.incrementAndGet();
    }

    public static void energyPortCheck() {
        if (enabled) energyPortChecks.incrementAndGet();
    }

    public static void energyPortSubscription(boolean subscribed) {
        if (enabled && subscribed) energyPortSubscriptions.incrementAndGet();
    }

    public static void tileCacheHit() {
        if (enabled) tileCacheHits.incrementAndGet();
    }

    public static void tileCacheMiss() {
        if (enabled) tileCacheMisses.incrementAndGet();
    }

    public static void tileCacheInvalidation() {
        if (enabled) tileCacheInvalidations.incrementAndGet();
    }

    public static void networkPack(int bytes, boolean sent) {
        if (!enabled) {
            return;
        }
        if (sent) {
            networkPackSent.incrementAndGet();
            networkPackBytes.addAndGet(Math.max(0, bytes));
        } else {
            networkPackSkipped.incrementAndGet();
        }
    }

    public static void blockUpdate() {
        if (enabled) blockUpdates.incrementAndGet();
    }

    public record Snapshot(
            boolean enabled,
            long fluidNetworkTicks,
            long fluidNodeRefreshes,
            long fluidSubscriptionRefreshes,
            long fluidPortChecks,
            long fluidPortSubscriptions,
            long fluidPortTransfers,
            long energyPortChecks,
            long energyPortSubscriptions,
            long tileCacheHits,
            long tileCacheMisses,
            long tileCacheInvalidations,
            long networkPackBytes,
            long networkPackSent,
            long networkPackSkipped,
            long blockUpdates) {
    }
}

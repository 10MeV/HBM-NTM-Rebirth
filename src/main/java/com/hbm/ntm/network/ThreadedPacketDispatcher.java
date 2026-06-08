package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.NetworkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class ThreadedPacketDispatcher {
    private static final String THREAD_PREFIX = "NTM-Packet-Thread-";
    private static final int WAIT_TIMEOUT_MILLIS = 50;
    private static final int MAX_PENDING_OPERATIONS = 4096;
    private static final int MAX_CONSECUTIVE_CLEARS_BEFORE_FALLBACK = 5;
    private static final AtomicInteger THREAD_IDS = new AtomicInteger();
    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, new PacketThreadFactory());
    private static final List<CompletableFuture<Void>> PENDING = new ArrayList<>();
    private static final AtomicLong TOTAL_QUEUED = new AtomicLong();
    private static final AtomicLong TOTAL_SENT = new AtomicLong();
    private static final AtomicLong TOTAL_FAILED = new AtomicLong();
    private static final AtomicLong TOTAL_DISCARDED = new AtomicLong();
    private static final AtomicLong DISCARDED_INVALID_TARGET = new AtomicLong();
    private static final AtomicLong DISCARDED_INVALID_MESSAGE = new AtomicLong();
    private static final AtomicLong DISCARDED_PREPARE_NULL = new AtomicLong();
    private static final AtomicLong DISCARDED_PREPARE_EXCEPTION = new AtomicLong();
    private static final AtomicLong DISCARDED_QUEUE_CLEAR = new AtomicLong();
    private static final AtomicLong DISCARDED_DISABLE_CLEAR = new AtomicLong();
    private static final AtomicLong DISCARDED_MANUAL_CLEAR = new AtomicLong();
    private static final AtomicLong TOTAL_PREPARED = new AtomicLong();
    private static final AtomicLong TOTAL_PREPARE_FAILED = new AtomicLong();
    private static final AtomicLong PREPARABLE_MESSAGES = new AtomicLong();
    private static final AtomicLong NON_PREPARABLE_MESSAGES = new AtomicLong();
    private static final AtomicLong PREPARED_SAME_INSTANCE = new AtomicLong();
    private static final AtomicLong PREPARED_COPY_INSTANCE = new AtomicLong();
    private static final AtomicLong MANUAL_CLEARS = new AtomicLong();
    private static int lastFlushQueued;
    private static int lastFlushCompleted;
    private static int lastFlushDiscarded;
    private static long lastFlushWaitMillis;
    private static long lastObservedWaitMillis;
    private static int consecutiveClears;
    private static boolean fallbackToMainThread;
    private static boolean enabled = true;
    private static volatile String lastFailureMessage = "";

    public static synchronized void sendToAllAround(Object message, ServerLevel level, double x, double y, double z, double range) {
        if (!validateTarget(message, "threaded-near-level:null", level != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, level, x, y, z, range));
        }
    }

    public static synchronized void sendToAllAround(Object message, ServerLevel level, BlockPos pos, double range) {
        if (!validateTarget(message, "threaded-near-level:null", level != null)
                || !validateTarget(message, "threaded-near-pos:null", pos != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, level, pos, range));
        }
    }

    public static synchronized void sendToAllAround(Object message, ResourceKey<Level> dimension,
                                                    double x, double y, double z, double range) {
        if (!validateTarget(message, "threaded-near-dimensionKey:null", dimension != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, dimension, x, y, z, range));
        }
    }

    public static synchronized void sendToAllAround(Object message, ResourceKey<Level> dimension,
                                                    BlockPos pos, double range) {
        if (!validateTarget(message, "threaded-near-dimensionKey:null", dimension != null)
                || !validateTarget(message, "threaded-near-pos:null", pos != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, dimension, pos, range));
        }
    }

    public static synchronized void sendToAllAround(Object message, Entity entity, double range) {
        if (!validateTarget(message, "threaded-near-entity:null", entity != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, entity, range));
        }
    }

    public static synchronized void sendToAllAround(Object message, PacketDistributor.TargetPoint point) {
        if (!validateTarget(message, "threaded-near-point:null", point != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, point));
        }
    }

    public static synchronized void sendToAllAround(Object message, LegacyTargetPoint point) {
        if (!validateTarget(message, "threaded-near-legacyTargetPoint:null", point != null)) {
            return;
        }
        if (!point.hasModernDimension()) {
            LegacyDimensionIdNetwork.rejectAllAround(message, point, true);
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, point));
        }
    }

    public static synchronized void sendToPlayer(Object message, ServerPlayer player) {
        if (!validateTarget(message, "threaded-player:null", player != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToPlayer(prepared, player));
        }
    }

    public static synchronized void sendToEntityTrackers(Object message, Entity entity) {
        if (!validateTarget(message, "threaded-entityTrackers:null", entity != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToEntityTrackers(prepared, entity));
        }
    }

    public static synchronized void sendToEntityAndSelf(Object message, Entity entity) {
        if (!validateTarget(message, "threaded-entityAndSelf:null", entity != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToEntityAndSelf(prepared, entity));
        }
    }

    public static synchronized void sendToDimension(Object message, ServerLevel level) {
        if (!validateTarget(message, "threaded-dimension:null", level != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToDimension(prepared, level));
        }
    }

    public static synchronized void sendToDimension(Object message, ResourceKey<Level> dimension) {
        if (!validateTarget(message, "threaded-dimensionKey:null", dimension != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToDimension(prepared, dimension));
        }
    }

    public static synchronized void sendToAll(Object message) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAll(prepared));
        }
    }

    public static synchronized void sendToTrackingChunk(Object message, BlockEntity blockEntity) {
        if (!validateTarget(message, "threaded-trackingChunk:blockEntity:null", blockEntity != null)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToTrackingChunk(prepared, blockEntity));
        }
    }

    public static synchronized void sendToTrackingChunk(Object message, Level level, BlockPos pos) {
        if (!validateTarget(message, "threaded-trackingChunk:level:null", level != null)
                || !validateTarget(message, "threaded-trackingChunk:pos:null", pos != null)
                || !validateTarget(message, "threaded-trackingChunk:clientLevel", !level.isClientSide)) {
            return;
        }
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToTrackingChunk(prepared, level, pos));
        }
    }

    private static boolean validateTarget(Object message, String target, boolean valid) {
        if (ModMessages.validateTargetForSend(message, target, valid)) {
            return true;
        }
        DISCARDED_INVALID_TARGET.incrementAndGet();
        TOTAL_DISCARDED.incrementAndGet();
        lastFailureMessage = "Invalid threaded packet target: " + target;
        return false;
    }

    private static Object prepareMessage(Object message) {
        if (!ModMessages.validateMessageForSend(message, "threaded-queue", "S2C")) {
            DISCARDED_INVALID_MESSAGE.incrementAndGet();
            TOTAL_DISCARDED.incrementAndGet();
            lastFailureMessage = "Unregistered threaded packet message.";
            return null;
        }
        try {
            boolean preparableMessage = message instanceof HbmPreparablePacket;
            Object prepared;
            if (preparableMessage) {
                PREPARABLE_MESSAGES.incrementAndGet();
                prepared = ((HbmPreparablePacket) message).prepareForThreadedSend();
            } else {
                NON_PREPARABLE_MESSAGES.incrementAndGet();
                prepared = message;
            }
            if (prepared == null) {
                TOTAL_PREPARE_FAILED.incrementAndGet();
                DISCARDED_PREPARE_NULL.incrementAndGet();
                TOTAL_DISCARDED.incrementAndGet();
                lastFailureMessage = "Threaded packet prepare returned null.";
                return null;
            }
            if (prepared == message) {
                PREPARED_SAME_INSTANCE.incrementAndGet();
            } else {
                PREPARED_COPY_INSTANCE.incrementAndGet();
            }
            if (!ModMessages.validateMessageForSend(prepared, "threaded-queue-prepared", "S2C")) {
                DISCARDED_INVALID_MESSAGE.incrementAndGet();
                TOTAL_DISCARDED.incrementAndGet();
                lastFailureMessage = "Unregistered prepared threaded packet message.";
                return null;
            }
            TOTAL_PREPARED.incrementAndGet();
            return prepared;
        } catch (Exception exception) {
            TOTAL_PREPARE_FAILED.incrementAndGet();
            DISCARDED_PREPARE_EXCEPTION.incrementAndGet();
            TOTAL_DISCARDED.incrementAndGet();
            lastFailureMessage = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            HbmNtm.LOGGER.warn("Threaded packet preparation failed.", exception);
            return null;
        }
    }

    private static void enqueue(Runnable sendOperation) {
        TOTAL_QUEUED.incrementAndGet();
        if (!isEnabled() || isFallbackToMainThread()) {
            runSendOperation(sendOperation);
            return;
        }
        if (PENDING.size() >= maxPendingOperations()) {
            discardPending("Threaded packet queue exceeded " + maxPendingOperations() + " pending operations.");
            runSendOperation(sendOperation);
            return;
        }
        PENDING.add(CompletableFuture.runAsync(() -> runSendOperation(sendOperation), EXECUTOR));
    }

    public static synchronized void flush() {
        if (PENDING.isEmpty()) {
            lastFlushQueued = 0;
            lastFlushCompleted = 0;
            lastFlushDiscarded = 0;
            lastFlushWaitMillis = 0L;
            return;
        }

        List<CompletableFuture<Void>> tasks = List.copyOf(PENDING);
        PENDING.clear();
        long startNanos = System.nanoTime();
        lastObservedWaitMillis = 0L;
        int completed = 0;
        int discarded = 0;
        for (int index = 0; index < tasks.size(); index++) {
            CompletableFuture<Void> task = tasks.get(index);
            try {
                lastObservedWaitMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                task.get(waitTimeoutMillis(), TimeUnit.MILLISECONDS);
                completed++;
            } catch (TimeoutException exception) {
                discarded = cancelRemaining(tasks, index);
                registerClear("Threaded packet operation exceeded " + waitTimeoutMillis() + " ms wait budget.", discarded, exception);
                break;
            } catch (Exception exception) {
                completed++;
                TOTAL_FAILED.incrementAndGet();
                lastFailureMessage = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
                HbmNtm.LOGGER.warn("Threaded packet operation failed during flush.", exception);
            }
        }
        int queuedExecutorTasks = EXECUTOR.getQueue().size();
        if (queuedExecutorTasks > 0) {
            EXECUTOR.getQueue().clear();
            discarded += queuedExecutorTasks;
            registerClear("Threaded packet executor still had " + queuedExecutorTasks + " queued operations after flush.", queuedExecutorTasks, null);
        } else if (discarded == 0) {
            consecutiveClears = 0;
        }
        DISCARDED_QUEUE_CLEAR.addAndGet(discarded);
        TOTAL_DISCARDED.addAndGet(discarded);
        lastFlushQueued = tasks.size();
        lastFlushCompleted = completed;
        lastFlushDiscarded = discarded;
        lastFlushWaitMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        lastObservedWaitMillis = lastFlushWaitMillis;
    }

    public static synchronized boolean isFallbackToMainThread() {
        return fallbackToMainThread && !isErrorBypassEnabled();
    }

    public static synchronized boolean isEnabled() {
        return enabled && isConfiguredEnabled();
    }

    public static synchronized boolean setEnabled(boolean enabled) {
        ThreadedPacketDispatcher.enabled = enabled;
        if (!enabled) {
            int discarded = clearPendingOperations();
            if (discarded > 0) {
                DISCARDED_DISABLE_CLEAR.addAndGet(discarded);
                TOTAL_DISCARDED.addAndGet(discarded);
                lastFlushDiscarded = discarded;
                lastFailureMessage = "Packet threading disabled with pending operations.";
            }
        }
        return ThreadedPacketDispatcher.enabled;
    }

    public static synchronized boolean toggleEnabled() {
        return setEnabled(!enabled);
    }

    public static long waitTimeoutMillis() {
        return NetworkConfig.packetThreadingWaitTimeoutMs(WAIT_TIMEOUT_MILLIS);
    }

    public static int maxPendingOperations() {
        return NetworkConfig.packetThreadingMaxPending(MAX_PENDING_OPERATIONS);
    }

    public static int fallbackClearThreshold() {
        return NetworkConfig.packetThreadingFallbackClearThreshold(MAX_CONSECUTIVE_CLEARS_BEFORE_FALLBACK);
    }

    public static String threadPrefix() {
        return THREAD_PREFIX;
    }

    public static boolean isConfiguredEnabled() {
        return NetworkConfig.packetThreadingEnabled();
    }

    public static boolean isErrorBypassEnabled() {
        return NetworkConfig.packetThreadingErrorBypass();
    }

    public static synchronized int clearPending(String reason) {
        int discarded = clearPendingOperations();
        MANUAL_CLEARS.incrementAndGet();
        DISCARDED_MANUAL_CLEAR.addAndGet(discarded);
        TOTAL_DISCARDED.addAndGet(discarded);
        lastFlushDiscarded = discarded;
        if (discarded > 0) {
            lastFailureMessage = reason == null || reason.isBlank()
                    ? "Manual packet threading queue clear."
                    : reason;
            HbmNtm.LOGGER.warn("Manually cleared {} threaded packet operations. Reason: {}", discarded, lastFailureMessage);
        }
        return discarded;
    }

    public static synchronized Snapshot snapshot() {
        return new Snapshot(
                TOTAL_QUEUED.get(),
                TOTAL_SENT.get(),
                TOTAL_FAILED.get(),
                TOTAL_DISCARDED.get(),
                DISCARDED_INVALID_TARGET.get(),
                DISCARDED_INVALID_MESSAGE.get(),
                DISCARDED_PREPARE_NULL.get(),
                DISCARDED_PREPARE_EXCEPTION.get(),
                DISCARDED_QUEUE_CLEAR.get(),
                DISCARDED_DISABLE_CLEAR.get(),
                DISCARDED_MANUAL_CLEAR.get(),
                TOTAL_PREPARED.get(),
                TOTAL_PREPARE_FAILED.get(),
                PREPARABLE_MESSAGES.get(),
                NON_PREPARABLE_MESSAGES.get(),
                PREPARED_SAME_INSTANCE.get(),
                PREPARED_COPY_INSTANCE.get(),
                MANUAL_CLEARS.get(),
                PENDING.size(),
                EXECUTOR.getPoolSize(),
                EXECUTOR.getCorePoolSize(),
                EXECUTOR.getMaximumPoolSize(),
                EXECUTOR.getActiveCount(),
                EXECUTOR.getQueue().size(),
                EXECUTOR.getCompletedTaskCount(),
                lastFlushQueued,
                lastFlushCompleted,
                lastFlushDiscarded,
                lastFlushWaitMillis,
                lastObservedWaitMillis,
                consecutiveClears,
                isFallbackToMainThread(),
                isErrorBypassEnabled(),
                isConfiguredEnabled(),
                isEnabled(),
                lastFailureMessage);
    }

    public static List<ThreadSnapshot> threadSnapshots() {
        List<ThreadSnapshot> snapshots = new ArrayList<>();
        for (ThreadInfo thread : ManagementFactory.getThreadMXBean().dumpAllThreads(false, false)) {
            if (thread.getThreadName().startsWith(THREAD_PREFIX)) {
                snapshots.add(new ThreadSnapshot(
                        thread.getThreadName(),
                        thread.getThreadId(),
                        thread.getThreadState().name(),
                        thread.getLockOwnerName() == null ? "" : thread.getLockOwnerName()));
            }
        }
        return List.copyOf(snapshots);
    }

    public static synchronized void resetState() {
        PENDING.forEach(task -> task.cancel(false));
        PENDING.clear();
        EXECUTOR.getQueue().clear();
        TOTAL_QUEUED.set(0L);
        TOTAL_SENT.set(0L);
        TOTAL_FAILED.set(0L);
        TOTAL_DISCARDED.set(0L);
        DISCARDED_INVALID_TARGET.set(0L);
        DISCARDED_INVALID_MESSAGE.set(0L);
        DISCARDED_PREPARE_NULL.set(0L);
        DISCARDED_PREPARE_EXCEPTION.set(0L);
        DISCARDED_QUEUE_CLEAR.set(0L);
        DISCARDED_DISABLE_CLEAR.set(0L);
        DISCARDED_MANUAL_CLEAR.set(0L);
        TOTAL_PREPARED.set(0L);
        TOTAL_PREPARE_FAILED.set(0L);
        PREPARABLE_MESSAGES.set(0L);
        NON_PREPARABLE_MESSAGES.set(0L);
        PREPARED_SAME_INSTANCE.set(0L);
        PREPARED_COPY_INSTANCE.set(0L);
        MANUAL_CLEARS.set(0L);
        lastFlushQueued = 0;
        lastFlushCompleted = 0;
        lastFlushDiscarded = 0;
        lastFlushWaitMillis = 0L;
        lastObservedWaitMillis = 0L;
        consecutiveClears = 0;
        fallbackToMainThread = false;
        enabled = true;
        lastFailureMessage = "";
    }

    private static void runSendOperation(Runnable sendOperation) {
        try {
            sendOperation.run();
            TOTAL_SENT.incrementAndGet();
        } catch (Exception exception) {
            TOTAL_FAILED.incrementAndGet();
            lastFailureMessage = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            HbmNtm.LOGGER.warn("Threaded packet operation failed.", exception);
        }
    }

    private static int cancelRemaining(List<CompletableFuture<Void>> tasks, int firstIndex) {
        int discarded = 0;
        for (int index = firstIndex; index < tasks.size(); index++) {
            CompletableFuture<Void> task = tasks.get(index);
            if (!task.isDone()) {
                task.cancel(false);
                discarded++;
            }
        }
        EXECUTOR.getQueue().clear();
        return discarded;
    }

    private static int clearPendingOperations() {
        int queuedExecutorTasks = EXECUTOR.getQueue().size();
        for (CompletableFuture<Void> task : PENDING) {
            if (!task.isDone()) {
                task.cancel(false);
            }
        }
        PENDING.clear();
        EXECUTOR.getQueue().clear();
        return queuedExecutorTasks;
    }

    private static void discardPending(String reason) {
        int discarded = clearPendingOperations();
        DISCARDED_QUEUE_CLEAR.addAndGet(discarded);
        TOTAL_DISCARDED.addAndGet(discarded);
        lastFlushDiscarded = discarded;
        registerClear(reason, discarded, null);
    }

    private static void registerClear(String reason, int discarded, Exception exception) {
        consecutiveClears++;
        lastFailureMessage = reason;
        if (!isErrorBypassEnabled() && consecutiveClears > fallbackClearThreshold() && !fallbackToMainThread) {
            fallbackToMainThread = true;
            HbmNtm.LOGGER.error("Threaded packet dispatcher switched to main-thread fallback after {} clears. Last reason: {}",
                    consecutiveClears, reason, exception);
            return;
        }
        HbmNtm.LOGGER.warn("Discarded {} threaded packet operations. Reason: {}", discarded, reason, exception);
    }

    private ThreadedPacketDispatcher() {
    }

    private static final class PacketThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, THREAD_PREFIX + THREAD_IDS.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }

    public record Snapshot(
            long totalQueued,
            long totalSent,
            long totalFailed,
            long totalDiscarded,
            long discardedInvalidTarget,
            long discardedInvalidMessage,
            long discardedPrepareNull,
            long discardedPrepareException,
            long discardedQueueClear,
            long discardedDisableClear,
            long discardedManualClear,
            long totalPrepared,
            long totalPrepareFailed,
            long preparableMessages,
            long nonPreparableMessages,
            long preparedSameInstance,
            long preparedCopyInstance,
            long manualClears,
            int pending,
            int threadPoolSize,
            int corePoolSize,
            int maximumPoolSize,
            int activeThreadCount,
            int executorQueueSize,
            long completedTaskCount,
            int lastFlushQueued,
            int lastFlushCompleted,
            int lastFlushDiscarded,
            long lastFlushWaitMillis,
            long lastObservedWaitMillis,
            int consecutiveClears,
            boolean fallbackToMainThread,
            boolean errorBypass,
            boolean configuredEnabled,
            boolean enabled,
            String lastFailureMessage) {

        public long reasonedDiscardTotal() {
            return discardedInvalidTarget
                    + discardedInvalidMessage
                    + discardedPrepareNull
                    + discardedPrepareException
                    + discardedQueueClear
                    + discardedDisableClear
                    + discardedManualClear;
        }

        public long preparedInstanceTotal() {
            return preparedSameInstance + preparedCopyInstance;
        }
    }

    public record ThreadSnapshot(String name, long id, String state, String lockOwner) {
    }
}

package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.BlockPos;
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
    private static final long WAIT_TIMEOUT_MILLIS = 50L;
    private static final int MAX_PENDING_OPERATIONS = 4096;
    private static final int MAX_CONSECUTIVE_CLEARS_BEFORE_FALLBACK = 5;
    private static final AtomicInteger THREAD_IDS = new AtomicInteger();
    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, new PacketThreadFactory());
    private static final List<CompletableFuture<Void>> PENDING = new ArrayList<>();
    private static final AtomicLong TOTAL_QUEUED = new AtomicLong();
    private static final AtomicLong TOTAL_SENT = new AtomicLong();
    private static final AtomicLong TOTAL_FAILED = new AtomicLong();
    private static final AtomicLong TOTAL_DISCARDED = new AtomicLong();
    private static final AtomicLong TOTAL_PREPARED = new AtomicLong();
    private static final AtomicLong TOTAL_PREPARE_FAILED = new AtomicLong();
    private static int lastFlushQueued;
    private static int lastFlushCompleted;
    private static int lastFlushDiscarded;
    private static long lastFlushWaitMillis;
    private static int consecutiveClears;
    private static boolean fallbackToMainThread;
    private static boolean enabled = true;
    private static volatile String lastFailureMessage = "";

    public static synchronized void sendToAllAround(Object message, ServerLevel level, double x, double y, double z, double range) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, level, x, y, z, range));
        }
    }

    public static synchronized void sendToAllAround(Object message, Entity entity, double range) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, entity, range));
        }
    }

    public static synchronized void sendToAllAround(Object message, PacketDistributor.TargetPoint point) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAllAround(prepared, point));
        }
    }

    public static synchronized void sendToPlayer(Object message, ServerPlayer player) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToPlayer(prepared, player));
        }
    }

    public static synchronized void sendToEntityTrackers(Object message, Entity entity) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToEntityTrackers(prepared, entity));
        }
    }

    public static synchronized void sendToEntityAndSelf(Object message, Entity entity) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToEntityAndSelf(prepared, entity));
        }
    }

    public static synchronized void sendToDimension(Object message, ServerLevel level) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToDimension(prepared, level));
        }
    }

    public static synchronized void sendToAll(Object message) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToAll(prepared));
        }
    }

    public static synchronized void sendToTrackingChunk(Object message, BlockEntity blockEntity) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToTrackingChunk(prepared, blockEntity));
        }
    }

    public static synchronized void sendToTrackingChunk(Object message, Level level, BlockPos pos) {
        Object prepared = prepareMessage(message);
        if (prepared != null) {
            enqueue(() -> ModMessages.sendToTrackingChunk(prepared, level, pos));
        }
    }

    private static Object prepareMessage(Object message) {
        try {
            Object prepared = message instanceof HbmPreparablePacket preparable
                    ? preparable.prepareForThreadedSend()
                    : message;
            if (prepared == null) {
                TOTAL_PREPARE_FAILED.incrementAndGet();
                TOTAL_DISCARDED.incrementAndGet();
                lastFailureMessage = "Threaded packet prepare returned null.";
                return null;
            }
            TOTAL_PREPARED.incrementAndGet();
            return prepared;
        } catch (Exception exception) {
            TOTAL_PREPARE_FAILED.incrementAndGet();
            TOTAL_DISCARDED.incrementAndGet();
            lastFailureMessage = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            HbmNtm.LOGGER.warn("Threaded packet preparation failed.", exception);
            return null;
        }
    }

    private static void enqueue(Runnable sendOperation) {
        TOTAL_QUEUED.incrementAndGet();
        if (!enabled || fallbackToMainThread) {
            runSendOperation(sendOperation);
            return;
        }
        if (PENDING.size() >= MAX_PENDING_OPERATIONS) {
            discardPending("Threaded packet queue exceeded " + MAX_PENDING_OPERATIONS + " pending operations.");
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
        int completed = 0;
        int discarded = 0;
        for (int index = 0; index < tasks.size(); index++) {
            CompletableFuture<Void> task = tasks.get(index);
            try {
                task.get(WAIT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                completed++;
            } catch (TimeoutException exception) {
                discarded = cancelRemaining(tasks, index);
                registerClear("Threaded packet operation exceeded " + WAIT_TIMEOUT_MILLIS + " ms wait budget.", discarded, exception);
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
        TOTAL_DISCARDED.addAndGet(discarded);
        lastFlushQueued = tasks.size();
        lastFlushCompleted = completed;
        lastFlushDiscarded = discarded;
        lastFlushWaitMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    public static synchronized boolean isFallbackToMainThread() {
        return fallbackToMainThread;
    }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    public static synchronized boolean setEnabled(boolean enabled) {
        ThreadedPacketDispatcher.enabled = enabled;
        if (!enabled) {
            PENDING.forEach(task -> task.cancel(false));
            PENDING.clear();
            EXECUTOR.getQueue().clear();
        }
        return ThreadedPacketDispatcher.enabled;
    }

    public static synchronized boolean toggleEnabled() {
        return setEnabled(!enabled);
    }

    public static synchronized Snapshot snapshot() {
        return new Snapshot(
                TOTAL_QUEUED.get(),
                TOTAL_SENT.get(),
                TOTAL_FAILED.get(),
                TOTAL_DISCARDED.get(),
                TOTAL_PREPARED.get(),
                TOTAL_PREPARE_FAILED.get(),
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
                consecutiveClears,
                fallbackToMainThread,
                enabled,
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
        TOTAL_PREPARED.set(0L);
        TOTAL_PREPARE_FAILED.set(0L);
        lastFlushQueued = 0;
        lastFlushCompleted = 0;
        lastFlushDiscarded = 0;
        lastFlushWaitMillis = 0L;
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

    private static void discardPending(String reason) {
        int discarded = PENDING.size();
        PENDING.forEach(task -> task.cancel(false));
        PENDING.clear();
        EXECUTOR.getQueue().clear();
        TOTAL_DISCARDED.addAndGet(discarded);
        lastFlushDiscarded = discarded;
        registerClear(reason, discarded, null);
    }

    private static void registerClear(String reason, int discarded, Exception exception) {
        consecutiveClears++;
        lastFailureMessage = reason;
        if (consecutiveClears > MAX_CONSECUTIVE_CLEARS_BEFORE_FALLBACK && !fallbackToMainThread) {
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
            long totalPrepared,
            long totalPrepareFailed,
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
            int consecutiveClears,
            boolean fallbackToMainThread,
            boolean enabled,
            String lastFailureMessage) {
    }

    public record ThreadSnapshot(String name, long id, String state, String lockOwner) {
    }
}

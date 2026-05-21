package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadedPacketDispatcher {
    private static final String THREAD_PREFIX = "NTM-Packet-Thread-";
    private static final long WAIT_TIMEOUT_MILLIS = 50L;
    private static final AtomicInteger THREAD_IDS = new AtomicInteger();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new PacketThreadFactory());
    private static final List<CompletableFuture<Void>> PENDING = new ArrayList<>();

    public static synchronized void sendToAllAround(Object message, ServerLevel level, double x, double y, double z, double range) {
        enqueue(() -> ModMessages.sendToAllAround(message, level, x, y, z, range));
    }

    public static synchronized void sendToAllAround(Object message, PacketDistributor.TargetPoint point) {
        enqueue(() -> ModMessages.sendToAllAround(message, point));
    }

    public static synchronized void sendToPlayer(Object message, ServerPlayer player) {
        enqueue(() -> ModMessages.sendToPlayer(message, player));
    }

    private static void enqueue(Runnable sendOperation) {
        PENDING.add(CompletableFuture.runAsync(sendOperation, EXECUTOR));
    }

    public static synchronized void flush() {
        if (PENDING.isEmpty()) {
            return;
        }

        List<CompletableFuture<Void>> tasks = List.copyOf(PENDING);
        PENDING.clear();
        for (CompletableFuture<Void> task : tasks) {
            try {
                task.get(WAIT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (Exception exception) {
                HbmNtm.LOGGER.warn("Discarded delayed threaded packet operation after {} ms.", WAIT_TIMEOUT_MILLIS, exception);
            }
        }
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
}

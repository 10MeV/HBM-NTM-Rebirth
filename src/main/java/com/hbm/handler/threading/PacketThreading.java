package com.hbm.handler.threading;

import com.hbm.ntm.network.LegacyPacketThreading;
import com.hbm.ntm.network.LegacyTargetPoint;
import com.hbm.ntm.network.ThreadedPacketDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * Legacy package facade for 1.7.10 com.hbm.handler.threading.PacketThreading.
 */
public final class PacketThreading {
    public static final String threadPrefix = LegacyPacketThreading.THREAD_PREFIX;
    public static int totalCnt;
    public static long nanoTimeWaited;
    public static int clearCnt;
    public static boolean hasTriggered;

    public static void init() {
        resetLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, PacketDistributor.TargetPoint point) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, point);
        refreshLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, ServerLevel level,
                                                     double x, double y, double z, double range) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, level, x, y, z, range);
        refreshLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, ServerLevel level, BlockPos pos, double range) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, level, pos, range);
        refreshLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                                     double x, double y, double z, double range) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimension, x, y, z, range);
        refreshLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, int dimensionId,
                                                     double x, double y, double z, double range) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimensionId, x, y, z, range);
        refreshLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, LegacyTargetPoint point) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, point);
        refreshLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                                     BlockPos pos, double range) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimension, pos, range);
        refreshLegacyMirror();
    }

    public static void createAllAroundThreadedPacket(Object message, Entity entity, double range) {
        totalCnt++;
        LegacyPacketThreading.createAllAroundThreadedPacket(message, entity, range);
        refreshLegacyMirror();
    }

    public static void createSendToThreadedPacket(Object message, ServerPlayer player) {
        totalCnt++;
        LegacyPacketThreading.createSendToThreadedPacket(message, player);
        refreshLegacyMirror();
    }

    public static void waitUntilThreadFinished() {
        LegacyPacketThreading.waitUntilThreadFinished();
        totalCnt = 0;
        refreshLegacyMirror();
    }

    public static int clearThreadPoolTasks() {
        int cleared = LegacyPacketThreading.clearThreadPoolTasks();
        clearCnt++;
        refreshLegacyMirror();
        return cleared;
    }

    public static boolean isTriggered() {
        refreshLegacyMirror();
        return hasTriggered;
    }

    public static void resetLegacyMirror() {
        totalCnt = 0;
        nanoTimeWaited = 0L;
        clearCnt = 0;
        hasTriggered = false;
    }

    private static void refreshLegacyMirror() {
        ThreadedPacketDispatcher.Snapshot snapshot = ThreadedPacketDispatcher.snapshot();
        nanoTimeWaited = snapshot.lastObservedWaitMillis() * 1_000_000L;
        clearCnt = snapshot.consecutiveClears();
        hasTriggered = LegacyPacketThreading.isTriggered();
    }

    private PacketThreading() {
    }
}

package com.hbm.ntm.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Compatibility facade for 1.7.10-style PacketDispatcher.wrapper send calls.
 */
public final class LegacyNetworkDispatcher {
    public static final LegacyNetworkDispatcher WRAPPER = new LegacyNetworkDispatcher();

    private static final String COMPATIBILITY_NOTE = "Modern facade for legacy PacketDispatcher.wrapper send helpers.";
    private static final AtomicLong LEGACY_FLUSH_CALLS = new AtomicLong();

    private LegacyNetworkDispatcher() {
    }

    public void sendToServer(Object message) {
        ModMessages.sendToServer(message);
    }

    public void sendTo(Object message, ServerPlayer player) {
        ModMessages.sendToPlayer(message, player);
    }

    public void sendToThreaded(Object message, ServerPlayer player) {
        ThreadedPacketDispatcher.sendToPlayer(message, player);
    }

    public void sendToAll(Object message) {
        ModMessages.sendToAll(message);
    }

    public void sendToAllThreaded(Object message) {
        ThreadedPacketDispatcher.sendToAll(message);
    }

    public void sendToDimension(Object message, ServerLevel level) {
        ModMessages.sendToDimension(message, level);
    }

    public void sendToDimension(Object message, ResourceKey<Level> dimension) {
        ModMessages.sendToDimension(message, dimension);
    }

    public void sendToDimensionThreaded(Object message, ServerLevel level) {
        ThreadedPacketDispatcher.sendToDimension(message, level);
    }

    public void sendToDimensionThreaded(Object message, ResourceKey<Level> dimension) {
        ThreadedPacketDispatcher.sendToDimension(message, dimension);
    }

    public void sendToAllAround(Object message, ServerLevel level, double x, double y, double z, double range) {
        ModMessages.sendToAllAround(message, level, x, y, z, range);
    }

    public void sendToAllAroundThreaded(Object message, ServerLevel level, double x, double y, double z, double range) {
        ThreadedPacketDispatcher.sendToAllAround(message, level, x, y, z, range);
    }

    public void sendToAllAround(Object message, ServerLevel level, BlockPos pos, double range) {
        ModMessages.sendToAllAround(message, level, pos, range);
    }

    public void sendToAllAroundThreaded(Object message, ServerLevel level, BlockPos pos, double range) {
        ThreadedPacketDispatcher.sendToAllAround(message, level, pos, range);
    }

    public void sendToAllAround(Object message, ResourceKey<Level> dimension, double x, double y, double z, double range) {
        ModMessages.sendToAllAround(message, dimension, x, y, z, range);
    }

    public void sendToAllAroundThreaded(Object message, ResourceKey<Level> dimension,
                                        double x, double y, double z, double range) {
        ThreadedPacketDispatcher.sendToAllAround(message, dimension, x, y, z, range);
    }

    public void sendToAllAround(Object message, ResourceKey<Level> dimension, BlockPos pos, double range) {
        ModMessages.sendToAllAround(message, dimension, pos, range);
    }

    public void sendToAllAroundThreaded(Object message, ResourceKey<Level> dimension, BlockPos pos, double range) {
        ThreadedPacketDispatcher.sendToAllAround(message, dimension, pos, range);
    }

    public void sendToAllAround(Object message, Entity entity, double range) {
        ModMessages.sendToAllAround(message, entity, range);
    }

    public void sendToAllAroundThreaded(Object message, Entity entity, double range) {
        ThreadedPacketDispatcher.sendToAllAround(message, entity, range);
    }

    public void sendToAllAround(Object message, PacketDistributor.TargetPoint point) {
        ModMessages.sendToAllAround(message, point);
    }

    public void sendToAllAround(ByteBuf message, PacketDistributor.TargetPoint point) {
        LegacyRawBufferNetwork.rejectAllAround(message, point);
    }

    public void sendToAllAroundThreaded(Object message, PacketDistributor.TargetPoint point) {
        ThreadedPacketDispatcher.sendToAllAround(message, point);
    }

    public void createAllAroundThreadedPacket(Object message, PacketDistributor.TargetPoint point) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, point);
    }

    public void createAllAroundThreadedPacket(Object message, ServerLevel level,
                                              double x, double y, double z, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, level, x, y, z, range);
    }

    public void createAllAroundThreadedPacket(Object message, ServerLevel level, BlockPos pos, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, level, pos, range);
    }

    public void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                              double x, double y, double z, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimension, x, y, z, range);
    }

    public void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                              BlockPos pos, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimension, pos, range);
    }

    public void createAllAroundThreadedPacket(Object message, Entity entity, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, entity, range);
    }

    public void createSendToThreadedPacket(Object message, ServerPlayer player) {
        LegacyPacketThreading.createSendToThreadedPacket(message, player);
    }

    public void sendToTrackingEntity(Object message, Entity entity) {
        ModMessages.sendToEntityTrackers(message, entity);
    }

    public void sendToTrackingEntityThreaded(Object message, Entity entity) {
        ThreadedPacketDispatcher.sendToEntityTrackers(message, entity);
    }

    public void sendToTrackingEntityAndSelf(Object message, Entity entity) {
        ModMessages.sendToEntityAndSelf(message, entity);
    }

    public void sendToTrackingEntityAndSelfThreaded(Object message, Entity entity) {
        ThreadedPacketDispatcher.sendToEntityAndSelf(message, entity);
    }

    public void sendToTrackingChunk(Object message, BlockEntity blockEntity) {
        ModMessages.sendToTrackingChunk(message, blockEntity);
    }

    public void sendToTrackingChunkThreaded(Object message, BlockEntity blockEntity) {
        ThreadedPacketDispatcher.sendToTrackingChunk(message, blockEntity);
    }

    public void sendToTrackingChunk(Object message, Level level, BlockPos pos) {
        ModMessages.sendToTrackingChunk(message, level, pos);
    }

    public void sendToTrackingChunkThreaded(Object message, Level level, BlockPos pos) {
        ThreadedPacketDispatcher.sendToTrackingChunk(message, level, pos);
    }

    public void flush() {
        flushLegacyNetwork();
    }

    public void waitUntilThreadFinished() {
        LegacyPacketThreading.waitUntilThreadFinished();
    }

    public int clearThreadPoolTasks() {
        return LegacyPacketThreading.clearThreadPoolTasks();
    }

    public boolean isTriggered() {
        return LegacyPacketThreading.isTriggered();
    }

    public static void flushLegacyNetwork() {
        LEGACY_FLUSH_CALLS.incrementAndGet();
        ThreadedPacketDispatcher.flush();
    }

    public static long legacyFlushCallCount() {
        return LEGACY_FLUSH_CALLS.get();
    }

    public static void resetLegacyCounters() {
        LEGACY_FLUSH_CALLS.set(0L);
        LegacyRawBufferNetwork.reset();
    }

    public static String compatibilityNote() {
        return COMPATIBILITY_NOTE;
    }

    public static String flushCompatibilitySummary() {
        return "legacyFlush=NetworkHandler.flush facade"
                + " calls=" + legacyFlushCallCount()
                + " note=flushes threaded dispatcher only; Forge SimpleChannel has no manual flush";
    }

    public static int directSendHelperCount() {
        return 16;
    }

    public static int threadedSendHelperCount() {
        return 14;
    }

    public static int packetThreadingHelperCount() {
        return LegacyPacketThreading.legacyHelperCount();
    }
}

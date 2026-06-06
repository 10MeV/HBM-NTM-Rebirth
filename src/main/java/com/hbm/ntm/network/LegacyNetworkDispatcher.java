package com.hbm.ntm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

/**
 * Compatibility facade for 1.7.10-style PacketDispatcher.wrapper send calls.
 */
public final class LegacyNetworkDispatcher {
    public static final LegacyNetworkDispatcher WRAPPER = new LegacyNetworkDispatcher();

    private static final String COMPATIBILITY_NOTE = "Modern facade for legacy PacketDispatcher.wrapper send helpers.";

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

    public void sendToDimensionThreaded(Object message, ServerLevel level) {
        ThreadedPacketDispatcher.sendToDimension(message, level);
    }

    public void sendToAllAround(Object message, ServerLevel level, double x, double y, double z, double range) {
        ModMessages.sendToAllAround(message, level, x, y, z, range);
    }

    public void sendToAllAroundThreaded(Object message, ServerLevel level, double x, double y, double z, double range) {
        ThreadedPacketDispatcher.sendToAllAround(message, level, x, y, z, range);
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

    public void sendToAllAroundThreaded(Object message, PacketDistributor.TargetPoint point) {
        ThreadedPacketDispatcher.sendToAllAround(message, point);
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

    public static String compatibilityNote() {
        return COMPATIBILITY_NOTE;
    }

    public static int directSendHelperCount() {
        return 11;
    }

    public static int threadedSendHelperCount() {
        return 10;
    }
}

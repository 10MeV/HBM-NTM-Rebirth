package com.hbm.packet.threading;

import com.hbm.ntm.network.LegacyPacketThreading;
import com.hbm.ntm.network.LegacyTargetPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * Legacy package facade for 1.7.10 PacketThreading call sites.
 */
public final class PacketThreading {
    public static final String threadPrefix = LegacyPacketThreading.THREAD_PREFIX;

    public static void createAllAroundThreadedPacket(Object message, PacketDistributor.TargetPoint point) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, point);
    }

    public static void createAllAroundThreadedPacket(Object message, ServerLevel level,
                                                     double x, double y, double z, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, level, x, y, z, range);
    }

    public static void createAllAroundThreadedPacket(Object message, ServerLevel level, BlockPos pos, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, level, pos, range);
    }

    public static void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                                     double x, double y, double z, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimension, x, y, z, range);
    }

    public static void createAllAroundThreadedPacket(Object message, int dimensionId,
                                                     double x, double y, double z, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimensionId, x, y, z, range);
    }

    public static void createAllAroundThreadedPacket(Object message, LegacyTargetPoint point) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, point);
    }

    public static void createAllAroundThreadedPacket(Object message, ResourceKey<Level> dimension,
                                                     BlockPos pos, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, dimension, pos, range);
    }

    public static void createAllAroundThreadedPacket(Object message, Entity entity, double range) {
        LegacyPacketThreading.createAllAroundThreadedPacket(message, entity, range);
    }

    public static void createSendToThreadedPacket(Object message, ServerPlayer player) {
        LegacyPacketThreading.createSendToThreadedPacket(message, player);
    }

    public static void waitUntilThreadFinished() {
        LegacyPacketThreading.waitUntilThreadFinished();
    }

    public static int clearThreadPoolTasks() {
        return LegacyPacketThreading.clearThreadPoolTasks();
    }

    public static boolean isTriggered() {
        return LegacyPacketThreading.isTriggered();
    }

    private PacketThreading() {
    }
}

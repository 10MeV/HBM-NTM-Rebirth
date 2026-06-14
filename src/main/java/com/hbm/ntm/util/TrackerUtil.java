package com.hbm.ntm.util;

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Legacy-name entity tracking facade.
 */
@Deprecated(forRemoval = false)
public final class TrackerUtil {
    private TrackerUtil() {
    }

    public static void sendTeleport(Level level, Entity entity) {
        if (entity == null) {
            return;
        }
        Level actualLevel = level != null ? level : entity.level();
        if (actualLevel instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(entity, new ClientboundTeleportEntityPacket(entity));
        }
    }

    public static void sendTeleport(Entity entity) {
        sendTeleport(entity == null ? null : entity.level(), entity);
    }

    public static void setTrackingRange(Level level, Entity entity, int range) {
        // Modern Forge tracking range is configured on EntityType registration, not mutable per tracker entry.
    }

    public static boolean canChangeTrackingRangeAtRuntime() {
        return false;
    }
}

package com.hbm.ntm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative cooldown for client requests to resend state which the
 * client could not apply yet. The client-side cooldown is only an optimisation
 * and cannot be trusted for a C2S packet.
 */
public final class ServerResyncRequestRateLimiter {
    public static final long REQUEST_COOLDOWN_TICKS = 20L;
    private static final int MAX_TRACKED_TARGETS_PER_PLAYER = 256;
    private static final Map<UUID, RequestWindow> WINDOWS = new HashMap<>();

    public static boolean tryAcquireTile(ServerPlayer player, BlockPos pos, long gameTime) {
        return tryAcquire(player, new RequestKey(RequestType.TILE, pos.immutable(), 0, null), gameTime);
    }

    public static boolean tryAcquireEntity(ServerPlayer player, int entityId, long gameTime) {
        return tryAcquire(player, new RequestKey(RequestType.ENTITY, BlockPos.ZERO, entityId, null), gameTime);
    }

    public static boolean tryAcquireTileBinary(ServerPlayer player, BlockPos pos, ResourceLocation channel, long gameTime) {
        return tryAcquire(player, new RequestKey(RequestType.TILE_BINARY, pos.immutable(), 0, channel), gameTime);
    }

    public static void clearPlayer(UUID playerId) {
        WINDOWS.remove(playerId);
    }

    public static void clearAll() {
        WINDOWS.clear();
    }

    private static boolean tryAcquire(ServerPlayer player, RequestKey key, long gameTime) {
        RequestWindow window = WINDOWS.computeIfAbsent(player.getUUID(), ignored -> new RequestWindow());
        return window.tryAcquire(key, gameTime);
    }

    private ServerResyncRequestRateLimiter() {
    }

    private enum RequestType {
        TILE,
        ENTITY,
        TILE_BINARY
    }

    private record RequestKey(RequestType type, BlockPos pos, int entityId, ResourceLocation channel) {
    }

    private static final class RequestWindow {
        private final Map<RequestKey, Long> lastRequests = new HashMap<>();
        private long lastPrunedGameTime = Long.MIN_VALUE;

        private boolean tryAcquire(RequestKey key, long gameTime) {
            pruneExpired(gameTime);
            Long lastRequest = lastRequests.get(key);
            if (lastRequest != null && gameTime - lastRequest < REQUEST_COOLDOWN_TICKS) {
                return false;
            }
            if (lastRequest == null && lastRequests.size() >= MAX_TRACKED_TARGETS_PER_PLAYER) {
                return false;
            }
            lastRequests.put(key, gameTime);
            return true;
        }

        private void pruneExpired(long gameTime) {
            if (lastPrunedGameTime == gameTime) {
                return;
            }
            lastPrunedGameTime = gameTime;
            Iterator<Map.Entry<RequestKey, Long>> iterator = lastRequests.entrySet().iterator();
            while (iterator.hasNext()) {
                if (gameTime - iterator.next().getValue() >= REQUEST_COOLDOWN_TICKS) {
                    iterator.remove();
                }
            }
        }
    }
}

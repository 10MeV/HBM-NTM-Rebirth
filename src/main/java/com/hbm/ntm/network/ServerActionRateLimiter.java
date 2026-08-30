package com.hbm.ntm.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side admission control for C2S control packets.
 *
 * <p>This runs before a packet handler can enqueue its receiver on the server
 * thread.  It deliberately complements, rather than replaces, the more
 * specific gameplay cooldowns (for example resync request cooldowns).</p>
 */
public final class ServerActionRateLimiter {
    private static final int GLOBAL_BURST = 96;
    private static final int GLOBAL_TOKENS_PER_SECOND = 48;
    private static final int PACKET_BURST = 24;
    private static final int PACKET_TOKENS_PER_SECOND = 12;
    private static final Map<UUID, PlayerWindow> WINDOWS = new HashMap<>();

    public static synchronized boolean tryAcquire(ServerPlayer player, Class<?> packetType) {
        if (player == null || packetType == null) {
            return false;
        }
        PlayerWindow window = WINDOWS.computeIfAbsent(player.getUUID(), ignored -> new PlayerWindow());
        return window.tryAcquire(packetType.getName(), System.nanoTime());
    }

    public static synchronized void clearPlayer(UUID playerId) {
        WINDOWS.remove(playerId);
    }

    public static synchronized void clearAll() {
        WINDOWS.clear();
    }

    private ServerActionRateLimiter() {
    }

    private static final class PlayerWindow {
        private final TokenBucket total = new TokenBucket(GLOBAL_BURST, GLOBAL_TOKENS_PER_SECOND);
        private final Map<String, TokenBucket> byPacket = new HashMap<>();

        private boolean tryAcquire(String packetName, long nowNanos) {
            TokenBucket packet = byPacket.computeIfAbsent(packetName,
                    ignored -> new TokenBucket(PACKET_BURST, PACKET_TOKENS_PER_SECOND));
            // Refill both buckets before deciding, but only debit the global
            // quota once the per-packet quota has accepted the action.
            packet.refill(nowNanos);
            total.refill(nowNanos);
            if (!packet.hasToken() || !total.hasToken()) {
                return false;
            }
            packet.consume();
            total.consume();
            return true;
        }
    }

    private static final class TokenBucket {
        private final int capacity;
        private final int tokensPerSecond;
        private double tokens;
        private long lastRefillNanos = System.nanoTime();

        private TokenBucket(int capacity, int tokensPerSecond) {
            this.capacity = capacity;
            this.tokensPerSecond = tokensPerSecond;
            this.tokens = capacity;
        }

        private void refill(long nowNanos) {
            long elapsed = Math.max(0L, nowNanos - lastRefillNanos);
            if (elapsed == 0L) {
                return;
            }
            tokens = Math.min(capacity, tokens + elapsed * (tokensPerSecond / 1_000_000_000.0D));
            lastRefillNanos = nowNanos;
        }

        private boolean hasToken() {
            return tokens >= 1.0D;
        }

        private void consume() {
            tokens -= 1.0D;
        }
    }
}

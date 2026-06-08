package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RTTYSystem {
    public static final String TEST_CHANNEL = "2012-08-06";

    private static final Map<ChannelKey, RTTYChannel> BROADCAST = new HashMap<>();
    private static final Map<ChannelKey, Object> NEW_MESSAGES = new HashMap<>();

    public static void broadcast(Level level, String channelName, Object signal) {
        if (level == null || channelName == null || channelName.isEmpty()) {
            return;
        }
        ChannelKey identifier = new ChannelKey(level.dimension(), channelName);
        Long incoming = parseLong(signal);
        if (incoming != null && NEW_MESSAGES.containsKey(identifier)) {
            Long existing = parseLong(NEW_MESSAGES.get(identifier));
            if (existing != null) {
                NEW_MESSAGES.put(identifier, Long.toString(incoming + existing));
                return;
            }
        }
        NEW_MESSAGES.put(identifier, signal);
    }

    @Nullable
    public static RTTYChannel listen(Level level, String channelName) {
        if (level == null || channelName == null || channelName.isEmpty()) {
            return null;
        }
        return BROADCAST.get(new ChannelKey(level.dimension(), channelName));
    }

    public static void updateBroadcastQueue(MinecraftServer server) {
        for (Map.Entry<ChannelKey, Object> entry : NEW_MESSAGES.entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey().dimension());
            long time = level == null ? -1L : level.getGameTime();
            BROADCAST.put(entry.getKey(), new RTTYChannel(time, entry.getValue()));
        }

        for (ServerLevel level : server.getAllLevels()) {
            BROADCAST.put(new ChannelKey(level.dimension(), TEST_CHANNEL),
                    new RTTYChannel(level.getGameTime(), getTestSender(level.getGameTime())));
        }
        NEW_MESSAGES.clear();
    }

    public static void clear(Level level) {
        if (level == null) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        BROADCAST.keySet().removeIf(key -> key.dimension().equals(dimension));
        NEW_MESSAGES.keySet().removeIf(key -> key.dimension().equals(dimension));
    }

    public static void clearAll() {
        BROADCAST.clear();
        NEW_MESSAGES.clear();
    }

    public static List<String> channels(Level level) {
        if (level == null) {
            return List.of();
        }
        ResourceKey<Level> dimension = level.dimension();
        List<String> channels = new ArrayList<>();
        for (ChannelKey key : BROADCAST.keySet()) {
            if (key.dimension().equals(dimension)) {
                channels.add(key.channel());
            }
        }
        channels.sort(Comparator.naturalOrder());
        return channels;
    }

    public static Diagnostics diagnostics(Level level) {
        if (level == null) {
            return new Diagnostics(0, 0, 0, 0);
        }
        ResourceKey<Level> dimension = level.dimension();
        int levelBroadcast = 0;
        int levelQueued = 0;
        for (ChannelKey key : BROADCAST.keySet()) {
            if (key.dimension().equals(dimension)) {
                levelBroadcast++;
            }
        }
        for (ChannelKey key : NEW_MESSAGES.keySet()) {
            if (key.dimension().equals(dimension)) {
                levelQueued++;
            }
        }
        return new Diagnostics(BROADCAST.size(), NEW_MESSAGES.size(), levelBroadcast, levelQueued);
    }

    public static Object getTestSender(long timeStamp) {
        return "test:" + Math.floorMod(timeStamp, 640L);
    }

    @Nullable
    private static Long parseLong(Object signal) {
        if (signal == null) {
            return null;
        }
        try {
            return Long.parseLong(signal.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record ChannelKey(ResourceKey<Level> dimension, String channel) {
    }

    public record RTTYChannel(long timeStamp, Object signal) {
        public String signalString() {
            return String.valueOf(signal);
        }
    }

    public enum RTTYSpecialSignal {
        BEGIN_TTY,
        STOP_TTY,
        PRINT_BUFFER
    }

    public record Diagnostics(int totalBroadcastChannels, int totalQueuedMessages, int levelBroadcastChannels,
            int levelQueuedMessages) {
        public String summary() {
            return "broadcast=" + totalBroadcastChannels
                    + " queued=" + totalQueuedMessages
                    + " levelBroadcast=" + levelBroadcastChannels
                    + " levelQueued=" + levelQueuedMessages;
        }
    }

    private RTTYSystem() {
    }
}

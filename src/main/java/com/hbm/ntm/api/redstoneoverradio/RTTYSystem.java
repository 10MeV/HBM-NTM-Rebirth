package com.hbm.ntm.api.redstoneoverradio;

import com.hbm.ntm.util.NoteBuilder;
import com.hbm.ntm.util.NoteBuilder.Instrument;
import com.hbm.ntm.util.NoteBuilder.Note;
import com.hbm.ntm.util.NoteBuilder.Octave;
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
        int tempo = 4;
        int time = (int) Math.floorMod(timeStamp, tempo * 160L);

        Instrument flute = Instrument.PIANO;
        Instrument accordion = Instrument.BASSGUITAR;

        if (time == tempo * 0) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 2) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
        if (time == tempo * 4) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 6) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 8) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 12) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).end();
        if (time == tempo * 14) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
        if (time == tempo * 16) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();

        if (time == tempo * 18) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 20) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 24) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 26) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
        if (time == tempo * 28) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 30) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 32) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 36) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).end();
        if (time == tempo * 38) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();
        if (time == tempo * 40) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 42) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).end();
        if (time == tempo * 44) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).end();

        if (time == tempo * 48) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 50) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 52) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(flute, Note.D, Octave.MID).end();
        if (time == tempo * 54) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 56) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 58) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 60) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).add(flute, Note.D, Octave.MID).end();

        if (time == tempo * 64) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 66) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
        if (time == tempo * 67) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();
        if (time == tempo * 68) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 69) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();

        if (time == tempo * 70) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 71) return NoteBuilder.start().add(flute, Note.B, Octave.MID).end();
        if (time == tempo * 72) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.LOW).add(flute, Note.A, Octave.MID).end();

        if (time == tempo * 76) return NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 78) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 80) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 81) return NoteBuilder.start().add(flute, Note.G, Octave.MID).end();

        if (time == tempo * 82) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 84) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 88) return NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 90) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 92) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 93) return NoteBuilder.start().add(accordion, Note.B, Octave.MID).add(flute, Note.G, Octave.MID).end();

        if (time == tempo * 94) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(flute, Note.E, Octave.LOW).end();
        if (time == tempo * 96) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).end();

        if (time == tempo * 100) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 101) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 102) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.MID).end();
        if (time == tempo * 104) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.B, Octave.MID).end();

        if (time == tempo * 106) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 107) return NoteBuilder.start().add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 108) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.D, Octave.MID).end();

        if (time == tempo * 112) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 114) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).end();
        if (time == tempo * 115) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();
        if (time == tempo * 116) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.LOW).add(accordion, Note.C, Octave.MID).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 117) return NoteBuilder.start().add(flute, Note.F, Octave.MID).end();

        if (time == tempo * 118) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.E, Octave.MID).end();
        if (time == tempo * 119) return NoteBuilder.start().add(flute, Note.C, Octave.MID).end();
        if (time == tempo * 120) return NoteBuilder.start().add(accordion, Note.E, Octave.LOW).add(accordion, Note.G, Octave.LOW).add(accordion, Note.B, Octave.MID).add(flute, Note.A, Octave.MID).end();

        if (time == tempo * 124) return NoteBuilder.start().add(accordion, Note.G, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 126) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 128) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).add(flute, Note.F, Octave.LOW).end();
        if (time == tempo * 129) return NoteBuilder.start().add(flute, Note.G, Octave.MID).end();

        if (time == tempo * 130) return NoteBuilder.start().add(accordion, Note.F, Octave.LOW).add(flute, Note.A, Octave.MID).end();
        if (time == tempo * 132) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(accordion, Note.E, Octave.LOW).add(accordion, Note.A, Octave.MID).add(accordion, Note.G, Octave.LOW).end();
        if (time == tempo * 134) return NoteBuilder.start().add(flute, Note.A, Octave.MID).end();

        if (time == tempo * 136) return NoteBuilder.start().add(accordion, Note.C, Octave.LOW).add(flute, Note.D, Octave.LOW).end();
        if (time == tempo * 138) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).end();
        if (time == tempo * 140) return NoteBuilder.start().add(accordion, Note.D, Octave.LOW).add(accordion, Note.F, Octave.LOW).add(accordion, Note.A, Octave.MID).end();

        return "";
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

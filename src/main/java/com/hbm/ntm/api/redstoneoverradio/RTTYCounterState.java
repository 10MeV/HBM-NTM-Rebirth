package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class RTTYCounterState {
    public static final int SLOT_COUNT = 3;

    private static final String TAG_POLLING = "p";
    private static final String TAG_CHANNEL_PREFIX = "c";
    private static final String TAG_LAST_COUNT_PREFIX = "l";

    private boolean polling;
    private final String[] channels = new String[SLOT_COUNT];
    private final int[] lastCounts = new int[SLOT_COUNT];

    public RTTYCounterState() {
        clear();
    }

    public boolean polling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }

    public String channel(int slot) {
        return valid(slot) ? channels[slot] : "";
    }

    public void setChannel(int slot, String channel) {
        if (valid(slot)) {
            channels[slot] = RTTYDeviceState.clean(channel);
        }
    }

    public int lastCount(int slot) {
        return valid(slot) ? lastCounts[slot] : 0;
    }

    public void setLastCount(int slot, int count) {
        if (valid(slot)) {
            lastCounts[slot] = Math.max(0, count);
        }
    }

    public void clear() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            channels[i] = "";
            lastCounts[i] = 0;
        }
    }

    public CounterDecision evaluateCount(int slot, int count) {
        if (!valid(slot) || channels[slot].isEmpty()) {
            return new CounterDecision(false, false, "", Math.max(0, count));
        }
        int normalized = Math.max(0, count);
        boolean changed = lastCounts[slot] != normalized;
        boolean shouldBroadcast = polling || changed;
        lastCounts[slot] = normalized;
        return new CounterDecision(changed, shouldBroadcast, channels[slot], normalized);
    }

    public boolean broadcastCount(Level level, int slot, int count) {
        CounterDecision decision = evaluateCount(slot, count);
        if (!decision.shouldBroadcast()) {
            return false;
        }
        RTTYSystem.broadcast(level, decision.channel(), decision.count());
        return true;
    }

    public int broadcastCounts(Level level, int[] counts) {
        int sent = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            int count = counts != null && i < counts.length ? counts[i] : 0;
            if (broadcastCount(level, i, count)) {
                sent++;
            }
        }
        return sent;
    }

    public void save(CompoundTag tag) {
        tag.putBoolean(TAG_POLLING, polling);
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!channels[i].isEmpty()) {
                tag.putString(TAG_CHANNEL_PREFIX + i, channels[i]);
            }
            tag.putInt(TAG_LAST_COUNT_PREFIX + i, lastCounts[i]);
        }
    }

    public void load(CompoundTag tag) {
        polling = tag.getBoolean(TAG_POLLING);
        for (int i = 0; i < SLOT_COUNT; i++) {
            channels[i] = tag.getString(TAG_CHANNEL_PREFIX + i);
            lastCounts[i] = Math.max(0, tag.getInt(TAG_LAST_COUNT_PREFIX + i));
        }
    }

    public boolean applyControl(CompoundTag tag) {
        boolean changed = false;
        if (tag.contains(TAG_POLLING, Tag.TAG_BYTE)) {
            boolean value = tag.getBoolean(TAG_POLLING);
            changed |= polling != value;
            polling = value;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            String key = TAG_CHANNEL_PREFIX + i;
            if (tag.contains(key, Tag.TAG_STRING)) {
                String value = RTTYDeviceState.clean(tag.getString(key));
                changed |= !channels[i].equals(value);
                channels[i] = value;
            }
        }
        return changed;
    }

    private static boolean valid(int slot) {
        return slot >= 0 && slot < SLOT_COUNT;
    }

    public record CounterDecision(boolean countChanged, boolean shouldBroadcast, String channel, int count) {
    }
}

package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class RTTYReaderState {
    public static final int SLOT_COUNT = 8;

    private static final String TAG_POLLING = "p";
    private static final String TAG_CHANNEL_PREFIX = "c";
    private static final String TAG_NAME_PREFIX = "n";
    private static final String TAG_PREVIOUS_PREFIX = "p";

    private boolean polling;
    private final String[] channels = new String[SLOT_COUNT];
    private final String[] names = new String[SLOT_COUNT];
    private final String[] previous = new String[SLOT_COUNT];

    public RTTYReaderState() {
        clear();
    }

    public boolean polling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }

    public String channel(int slot) {
        return value(channels, slot);
    }

    public String name(int slot) {
        return value(names, slot);
    }

    public String previous(int slot) {
        return value(previous, slot);
    }

    public void setSlot(int slot, String channel, String name) {
        if (valid(slot)) {
            channels[slot] = RTTYDeviceState.clean(channel);
            names[slot] = RTTYDeviceState.clean(name);
        }
    }

    public void clear() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            channels[i] = "";
            names[i] = "";
            previous[i] = "";
        }
    }

    public int broadcastChangedValues(Level level, RORValueProvider provider) {
        int sent = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (broadcastSlot(level, provider, i)) {
                sent++;
            }
        }
        return sent;
    }

    public boolean broadcastSlot(Level level, RORValueProvider provider, int slot) {
        if (!valid(slot) || channels[slot].isEmpty() || names[slot].isEmpty()) {
            return false;
        }
        String value = RORRemoteBridge.readValue(provider, names[slot]);
        if (value == null) {
            return false;
        }
        if (polling || !value.equals(previous[slot])) {
            RTTYSystem.broadcast(level, channels[slot], value);
            previous[slot] = value;
            return true;
        }
        return false;
    }

    public void save(CompoundTag tag) {
        tag.putBoolean(TAG_POLLING, polling);
        for (int i = 0; i < SLOT_COUNT; i++) {
            tag.putString(TAG_CHANNEL_PREFIX + i, channels[i]);
            tag.putString(TAG_NAME_PREFIX + i, names[i]);
            tag.putString(TAG_PREVIOUS_PREFIX + i, previous[i]);
        }
    }

    public void load(CompoundTag tag) {
        polling = tag.getBoolean(TAG_POLLING);
        for (int i = 0; i < SLOT_COUNT; i++) {
            channels[i] = tag.getString(TAG_CHANNEL_PREFIX + i);
            names[i] = tag.getString(TAG_NAME_PREFIX + i);
            previous[i] = tag.getString(TAG_PREVIOUS_PREFIX + i);
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
            String channelKey = TAG_CHANNEL_PREFIX + i;
            if (tag.contains(channelKey, Tag.TAG_STRING)) {
                String value = RTTYDeviceState.clean(tag.getString(channelKey));
                changed |= !channels[i].equals(value);
                channels[i] = value;
            }
            String nameKey = TAG_NAME_PREFIX + i;
            if (tag.contains(nameKey, Tag.TAG_STRING)) {
                String value = RTTYDeviceState.clean(tag.getString(nameKey));
                changed |= !names[i].equals(value);
                names[i] = value;
            }
        }
        return changed;
    }

    private static boolean valid(int slot) {
        return slot >= 0 && slot < SLOT_COUNT;
    }

    private static String value(String[] values, int slot) {
        return valid(slot) ? values[slot] : "";
    }
}

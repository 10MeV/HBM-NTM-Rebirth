package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

public class RTTYLogicState {
    private static final String TAG_POLLING = "p";
    private static final String TAG_DESCENDING = "d";
    private static final String TAG_LAST_STATE = "l";
    private static final String TAG_LAST_UPDATE = "u";
    private static final String TAG_CHANNEL = "c";
    private static final String TAG_MAPPING_PREFIX = "m";
    private static final String TAG_CONDITION_PREFIX = "c";

    private String channel = "";
    private int lastState;
    private long lastUpdate;
    private boolean polling;
    private boolean descending;
    private final String[] mapping = new String[RTTYSignalMapper.REDSTONE_LEVELS];
    private final int[] conditions = new int[RTTYSignalMapper.REDSTONE_LEVELS];

    public RTTYLogicState() {
        clearMapping();
    }

    public String channel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = RTTYDeviceState.clean(channel);
    }

    public int lastState() {
        return lastState;
    }

    public long lastUpdate() {
        return lastUpdate;
    }

    public boolean polling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }

    public boolean descending() {
        return descending;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public String mapping(int index) {
        return RTTYSignalMapper.mappingValue(mapping, index);
    }

    public int condition(int index) {
        if (index < 0 || index >= conditions.length) {
            return 0;
        }
        return conditions[index];
    }

    public String[] mappingCopy() {
        return RTTYSignalMapper.copyMapping(mapping);
    }

    public int[] conditionsCopy() {
        return conditions.clone();
    }

    public void setRule(int index, String mappedValue, int condition) {
        if (index >= 0 && index < mapping.length) {
            mapping[index] = RTTYDeviceState.clean(mappedValue);
            conditions[index] = Mth.clamp(condition, 0, 9);
        }
    }

    public void clearMapping() {
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = "";
            conditions[i] = 0;
        }
    }

    public LogicReceiveResult receiveLogicSignal(RTTYSystem.RTTYChannel rttyChannel, long gameTime) {
        if (!RTTYSignalMapper.shouldReceive(rttyChannel, lastUpdate, polling)) {
            return LogicReceiveResult.none();
        }

        String signal = rttyChannel.signalString();
        lastUpdate = gameTime;
        if (RTTYSignalMapper.isStalePollingSignal(rttyChannel, lastUpdate, polling)) {
            signal = "0";
        }
        int nextState = RTTYLogicEvaluator.evaluate(signal, mapping, conditions, descending);
        return new LogicReceiveResult(true, nextState, signal);
    }

    public boolean applyLastState(int nextState) {
        int redstone = Mth.clamp(nextState, 0, 15);
        if (lastState == redstone) {
            return false;
        }
        lastState = redstone;
        return true;
    }

    public void save(CompoundTag tag) {
        tag.putBoolean(TAG_POLLING, polling);
        tag.putBoolean(TAG_DESCENDING, descending);
        tag.putInt(TAG_LAST_STATE, lastState);
        tag.putLong(TAG_LAST_UPDATE, lastUpdate);
        if (!channel.isEmpty()) {
            tag.putString(TAG_CHANNEL, channel);
        }
        for (int i = 0; i < mapping.length; i++) {
            if (!mapping[i].isEmpty()) {
                tag.putString(TAG_MAPPING_PREFIX + i, mapping[i]);
            }
            if (conditions[i] > 0) {
                tag.putInt(TAG_CONDITION_PREFIX + i, conditions[i]);
            }
        }
    }

    public void load(CompoundTag tag) {
        polling = tag.getBoolean(TAG_POLLING);
        descending = tag.getBoolean(TAG_DESCENDING);
        lastState = Mth.clamp(tag.getInt(TAG_LAST_STATE), 0, 15);
        lastUpdate = tag.getLong(TAG_LAST_UPDATE);
        channel = tag.getString(TAG_CHANNEL);
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = tag.getString(TAG_MAPPING_PREFIX + i);
            conditions[i] = Mth.clamp(tag.getInt(TAG_CONDITION_PREFIX + i), 0, 9);
        }
    }

    public boolean applyControl(CompoundTag tag) {
        boolean changed = false;
        if (tag.contains(TAG_POLLING, Tag.TAG_BYTE)) {
            boolean value = tag.getBoolean(TAG_POLLING);
            changed |= polling != value;
            polling = value;
        }
        if (tag.contains(TAG_DESCENDING, Tag.TAG_BYTE)) {
            boolean value = tag.getBoolean(TAG_DESCENDING);
            changed |= descending != value;
            descending = value;
        }
        if (tag.contains(TAG_CHANNEL, Tag.TAG_STRING)) {
            String value = RTTYDeviceState.clean(tag.getString(TAG_CHANNEL));
            changed |= !channel.equals(value);
            channel = value;
        }
        for (int i = 0; i < mapping.length; i++) {
            String mappingKey = TAG_MAPPING_PREFIX + i;
            if (tag.contains(mappingKey, Tag.TAG_STRING)) {
                String value = RTTYDeviceState.clean(tag.getString(mappingKey));
                changed |= !mapping[i].equals(value);
                mapping[i] = value;
            }
            String conditionKey = TAG_CONDITION_PREFIX + i;
            if (tag.contains(conditionKey, Tag.TAG_INT)) {
                int value = Mth.clamp(tag.getInt(conditionKey), 0, 9);
                changed |= conditions[i] != value;
                conditions[i] = value;
            }
        }
        return changed;
    }

    public record LogicReceiveResult(boolean received, int redstoneLevel, String signal) {
        private static final LogicReceiveResult NONE = new LogicReceiveResult(false, 0, "");

        public static LogicReceiveResult none() {
            return NONE;
        }
    }
}

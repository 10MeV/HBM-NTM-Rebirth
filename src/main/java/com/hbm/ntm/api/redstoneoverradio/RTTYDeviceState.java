package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

public class RTTYDeviceState {
    public static final String SELF_DESTRUCT_SIGNAL = "selfdestruct";

    private static final String TAG_POLLING = "p";
    private static final String TAG_CUSTOM_MAP = "m";
    private static final String TAG_LAST_STATE = "l";
    private static final String TAG_LAST_UPDATE = "u";
    private static final String TAG_CHANNEL = "c";
    private static final String TAG_MAPPING_PREFIX = "m";

    private String channel = "";
    private int lastState;
    private long lastUpdate;
    private boolean polling;
    private boolean customMap;
    private final String[] mapping = new String[RTTYSignalMapper.REDSTONE_LEVELS];

    public RTTYDeviceState() {
        clearMapping();
    }

    public String channel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = clean(channel);
    }

    public int lastState() {
        return lastState;
    }

    public void setLastState(int lastState) {
        this.lastState = Mth.clamp(lastState, 0, 15);
    }

    public long lastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean polling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }

    public boolean customMap() {
        return customMap;
    }

    public void setCustomMap(boolean customMap) {
        this.customMap = customMap;
    }

    public String mapping(int index) {
        return RTTYSignalMapper.mappingValue(mapping, index);
    }

    public String[] mappingCopy() {
        return RTTYSignalMapper.copyMapping(mapping);
    }

    public void setMapping(int index, String value) {
        if (index >= 0 && index < mapping.length) {
            mapping[index] = clean(value);
        }
    }

    public void setMapping(String[] mapping) {
        for (int i = 0; i < this.mapping.length; i++) {
            setMapping(i, RTTYSignalMapper.mappingValue(mapping, i));
        }
    }

    public void clearMapping() {
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = "";
        }
    }

    public BroadcastDecision evaluateBroadcastInput(int input) {
        int redstone = Mth.clamp(input, 0, 15);
        boolean changed = redstone != lastState;
        if (changed) {
            lastState = redstone;
        }
        boolean shouldSend = polling || changed;
        if (!shouldSend || channel.isEmpty()) {
            return new BroadcastDecision(changed, false, channel, "");
        }
        String signal = RTTYSignalMapper.redstoneToSignal(redstone, customMap, mapping);
        return new BroadcastDecision(changed, !signal.isEmpty(), channel, signal);
    }

    public RedstoneReceiveResult receiveRedstoneSignal(RTTYSystem.RTTYChannel rttyChannel, long gameTime) {
        if (!RTTYSignalMapper.shouldReceive(rttyChannel, lastUpdate, polling)) {
            return RedstoneReceiveResult.none();
        }

        String signal = rttyChannel.signalString();
        lastUpdate = gameTime;
        if (isSelfDestructSignal(signal)) {
            return new RedstoneReceiveResult(true, true, lastState, signal);
        }

        int nextState = RTTYSignalMapper.signalToRedstone(signal, customMap, mapping);
        if (RTTYSignalMapper.isStalePollingSignal(rttyChannel, lastUpdate, polling)) {
            nextState = 0;
        }
        return new RedstoneReceiveResult(true, false, nextState, signal);
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
        tag.putBoolean(TAG_CUSTOM_MAP, customMap);
        tag.putInt(TAG_LAST_STATE, lastState);
        tag.putLong(TAG_LAST_UPDATE, lastUpdate);
        if (!channel.isEmpty()) {
            tag.putString(TAG_CHANNEL, channel);
        }
        saveMapping(tag);
    }

    public void load(CompoundTag tag) {
        polling = tag.getBoolean(TAG_POLLING);
        customMap = tag.getBoolean(TAG_CUSTOM_MAP);
        lastState = Mth.clamp(tag.getInt(TAG_LAST_STATE), 0, 15);
        lastUpdate = tag.getLong(TAG_LAST_UPDATE);
        channel = tag.getString(TAG_CHANNEL);
        loadMapping(tag);
    }

    public CompoundTag saveControlTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_POLLING, polling);
        tag.putBoolean(TAG_CUSTOM_MAP, customMap);
        tag.putString(TAG_CHANNEL, channel);
        saveMapping(tag);
        return tag;
    }

    public boolean applyControl(CompoundTag tag) {
        boolean changed = false;
        if (tag.contains(TAG_POLLING, Tag.TAG_BYTE)) {
            boolean value = tag.getBoolean(TAG_POLLING);
            changed |= polling != value;
            polling = value;
        }
        if (tag.contains(TAG_CUSTOM_MAP, Tag.TAG_BYTE)) {
            boolean value = tag.getBoolean(TAG_CUSTOM_MAP);
            changed |= customMap != value;
            customMap = value;
        }
        if (tag.contains(TAG_CHANNEL, Tag.TAG_STRING)) {
            String value = clean(tag.getString(TAG_CHANNEL));
            changed |= !channel.equals(value);
            channel = value;
        }
        for (int i = 0; i < mapping.length; i++) {
            String key = TAG_MAPPING_PREFIX + i;
            if (tag.contains(key, Tag.TAG_STRING)) {
                String value = clean(tag.getString(key));
                changed |= !mapping[i].equals(value);
                mapping[i] = value;
            }
        }
        return changed;
    }

    public static boolean isSelfDestructSignal(String signal) {
        return SELF_DESTRUCT_SIGNAL.equals(signal);
    }

    protected void saveMapping(CompoundTag tag) {
        for (int i = 0; i < mapping.length; i++) {
            if (!mapping[i].isEmpty()) {
                tag.putString(TAG_MAPPING_PREFIX + i, mapping[i]);
            }
        }
    }

    protected void loadMapping(CompoundTag tag) {
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = tag.getString(TAG_MAPPING_PREFIX + i);
        }
    }

    protected static String clean(String value) {
        return value == null ? "" : value;
    }

    public record BroadcastDecision(boolean stateChanged, boolean shouldBroadcast, String channel, String signal) {
    }

    public record RedstoneReceiveResult(boolean received, boolean selfDestruct, int redstoneLevel, String signal) {
        private static final RedstoneReceiveResult NONE = new RedstoneReceiveResult(false, false, 0, "");

        public static RedstoneReceiveResult none() {
            return NONE;
        }
    }
}

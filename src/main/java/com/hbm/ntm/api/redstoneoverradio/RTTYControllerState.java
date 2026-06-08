package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class RTTYControllerState {
    private static final String TAG_POLLING = "p";
    private static final String TAG_CHANNEL = "c";
    private static final String TAG_PREVIOUS = "prev";

    private String channel = "";
    private String previous = "";
    private boolean polling = true;

    public String channel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = RTTYDeviceState.clean(channel);
    }

    public String previous() {
        return previous;
    }

    public boolean polling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }

    public ControllerRunResult runFromChannel(RORInteractive interactive, RTTYSystem.RTTYChannel rttyChannel,
            long currentGameTime) {
        if (rttyChannel == null) {
            return ControllerRunResult.none();
        }
        String received = rttyChannel.signalString();
        if (RTTYDeviceState.isSelfDestructSignal(received)) {
            return new ControllerRunResult(false, true, received, null, null);
        }
        if (!RORRemoteBridge.shouldRunControllerCommand(rttyChannel, currentGameTime, polling, received, previous)) {
            return ControllerRunResult.none();
        }
        previous = received;
        if (received == null || received.isEmpty()) {
            return ControllerRunResult.none();
        }
        try {
            return new ControllerRunResult(true, false, received, RORRemoteBridge.runCommand(interactive, received), null);
        } catch (RORFunctionException ex) {
            return new ControllerRunResult(false, false, received, null, ex.getMessage());
        }
    }

    public void save(CompoundTag tag) {
        tag.putBoolean(TAG_POLLING, polling);
        tag.putString(TAG_CHANNEL, channel);
        if (!previous.isEmpty()) {
            tag.putString(TAG_PREVIOUS, previous);
        }
    }

    public void load(CompoundTag tag) {
        if (tag.contains(TAG_POLLING, Tag.TAG_BYTE)) {
            polling = tag.getBoolean(TAG_POLLING);
        }
        channel = tag.getString(TAG_CHANNEL);
        previous = tag.getString(TAG_PREVIOUS);
    }

    public boolean applyControl(CompoundTag tag) {
        boolean changed = false;
        if (tag.contains(TAG_POLLING, Tag.TAG_BYTE)) {
            boolean value = tag.getBoolean(TAG_POLLING);
            changed |= polling != value;
            polling = value;
        }
        if (tag.contains(TAG_CHANNEL, Tag.TAG_STRING)) {
            String value = RTTYDeviceState.clean(tag.getString(TAG_CHANNEL));
            changed |= !channel.equals(value);
            channel = value;
        }
        return changed;
    }

    public record ControllerRunResult(boolean ran, boolean selfDestruct, String command, String result,
            String exceptionMessage) {
        private static final ControllerRunResult NONE = new ControllerRunResult(false, false, "", null, null);

        public static ControllerRunResult none() {
            return NONE;
        }
    }
}

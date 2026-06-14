package com.hbm.ntm.api.entity;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

public final class RadarClientControlProfile {
    private RadarClientControlProfile() {
    }

    public static boolean accepts(CompoundTag tag, int linkSlotCount) {
        return RadarControl.isControlTag(tag) || RadarLaunchCommand.validFromTag(tag, linkSlotCount).isPresent();
    }

    public static Request parse(CompoundTag tag, RadarControlState state, int linkSlotCount) {
        RadarControlState safeState = state != null
                ? state
                : RadarControlState.of(RadarDetectable.RadarScanParams.DEFAULT, true, false);
        List<RadarControl> controls = RadarControl.controlsFromTag(tag);
        RadarControlState.Application application = controls.isEmpty()
                ? new RadarControlState.Application(safeState, false)
                : safeState.apply(controls);
        RadarLaunchCommand command = RadarLaunchCommand.validFromTag(tag, linkSlotCount).orElse(null);
        return new Request(application, !controls.isEmpty(), command);
    }

    public record Request(RadarControlState.Application controlApplication, boolean hasControls,
                          RadarLaunchCommand launchCommand) {
        public boolean changedControls() {
            return hasControls;
        }

        public boolean hasLaunchCommand() {
            return launchCommand != null;
        }
    }
}

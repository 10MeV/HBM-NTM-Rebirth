package com.hbm.ntm.api.entity;

import org.jetbrains.annotations.Nullable;

public final class RadarScreenActionProfile {
    private RadarScreenActionProfile() {
    }

    public static Action mainClick(int leftPos, int topPos, double mouseX, double mouseY) {
        for (RadarControlPanel.Button button : RadarControlPanel.buttons()) {
            if (RadarGuiHitProfile.hitsArea(leftPos, topPos, button.mainX(), button.mainY(),
                    RadarControlPanel.BUTTON_SIZE, RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
                return Action.control(button.control());
            }
        }
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarControlPanel.mainToggleViewX(),
                RadarControlPanel.MAIN_TOGGLE_VIEW_Y, RadarControlPanel.BUTTON_SIZE,
                RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
            return Action.view(RadarScreenViewProfile.SLOTS);
        }
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarControlPanel.mainClearMapX(),
                RadarControlPanel.MAIN_CLEAR_MAP_Y, RadarControlPanel.BUTTON_SIZE,
                RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
            return Action.control(RadarControl.CLEAR_MAP);
        }
        return Action.consume();
    }

    public static Action slotClick(int leftPos, int topPos, double mouseX, double mouseY) {
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarGuiLayout.SLOT_TOGGLE_VIEW_X,
                RadarGuiLayout.SLOT_TOGGLE_VIEW_Y, RadarGuiLayout.SLOT_TOGGLE_VIEW_SIZE,
                RadarGuiLayout.SLOT_TOGGLE_VIEW_SIZE, mouseX, mouseY)) {
            return Action.view(RadarScreenViewProfile.MAIN);
        }
        for (RadarControlPanel.Button button : RadarControlPanel.buttons()) {
            if (RadarGuiHitProfile.hitsArea(leftPos, topPos, button.slotX(), button.slotY(),
                    RadarControlPanel.BUTTON_SIZE, RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
                return Action.control(button.control());
            }
        }
        return Action.none();
    }

    public record Action(Type type, @Nullable RadarControl control, @Nullable RadarScreenViewProfile view) {
        private static Action control(RadarControl control) {
            return new Action(Type.CONTROL, control, null);
        }

        private static Action view(RadarScreenViewProfile view) {
            return new Action(Type.VIEW, null, view);
        }

        private static Action consume() {
            return new Action(Type.CONSUME, null, null);
        }

        private static Action none() {
            return new Action(Type.NONE, null, null);
        }
    }

    public enum Type {
        NONE,
        CONSUME,
        CONTROL,
        VIEW
    }
}

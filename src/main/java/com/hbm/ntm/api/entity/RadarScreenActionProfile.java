package com.hbm.ntm.api.entity;

import org.jetbrains.annotations.Nullable;

public final class RadarScreenActionProfile {
    public static final int KEY_ESCAPE = 256;

    private RadarScreenActionProfile() {
    }

    public static Action mainKey(int keyCode, boolean inventoryKey, int leftPos, int topPos,
            int mouseX, int mouseY) {
        if (keyCode == KEY_ESCAPE || inventoryKey) {
            return Action.close();
        }
        int linkSlot = RadarLaunchKeyProfile.linkSlotForKey(keyCode);
        if (linkSlot >= 0 && RadarGuiHitProfile.hitsRadarArea(leftPos, topPos, mouseX, mouseY)) {
            return Action.launch(linkSlot);
        }
        return Action.consume();
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

    public record Action(Type type, @Nullable RadarControl control, @Nullable RadarScreenViewProfile view,
            int linkSlot) {
        private static Action control(RadarControl control) {
            return new Action(Type.CONTROL, control, null, -1);
        }

        private static Action view(RadarScreenViewProfile view) {
            return new Action(Type.VIEW, null, view, -1);
        }

        private static Action close() {
            return new Action(Type.CLOSE, null, null, -1);
        }

        private static Action launch(int linkSlot) {
            return new Action(Type.LAUNCH, null, null, linkSlot);
        }

        private static Action consume() {
            return new Action(Type.CONSUME, null, null, -1);
        }

        private static Action none() {
            return new Action(Type.NONE, null, null, -1);
        }
    }

    public enum Type {
        NONE,
        CONSUME,
        CLOSE,
        LAUNCH,
        CONTROL,
        VIEW
    }
}

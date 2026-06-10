package com.hbm.ntm.api.entity;

import org.jetbrains.annotations.Nullable;

public final class RadarScreenHoverProfile {
    private RadarScreenHoverProfile() {
    }

    public static Hover mainChrome(int leftPos, int topPos, double mouseX, double mouseY) {
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarGuiLayout.MAIN_ENERGY_X,
                RadarGuiLayout.MAIN_ENERGY_Y, RadarGuiLayout.MAIN_ENERGY_WIDTH,
                RadarGuiLayout.MAIN_ENERGY_HEIGHT, mouseX, mouseY)) {
            return Hover.energy();
        }
        for (RadarControlPanel.Button button : RadarControlPanel.buttons()) {
            if (RadarGuiHitProfile.hitsArea(leftPos, topPos, button.mainX(), button.mainY(),
                    RadarControlPanel.BUTTON_SIZE, RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
                return Hover.control(button);
            }
        }
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarControlPanel.mainToggleViewX(),
                RadarControlPanel.MAIN_TOGGLE_VIEW_Y, RadarControlPanel.BUTTON_SIZE,
                RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
            return Hover.toggleView();
        }
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarControlPanel.mainClearMapX(),
                RadarControlPanel.MAIN_CLEAR_MAP_Y, RadarControlPanel.BUTTON_SIZE,
                RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
            return Hover.clearMap();
        }
        return Hover.none();
    }

    public static Hover slots(int leftPos, int topPos, double mouseX, double mouseY) {
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarGuiLayout.SLOT_ENERGY_X,
                RadarGuiLayout.SLOT_ENERGY_Y, RadarGuiLayout.SLOT_ENERGY_WIDTH,
                RadarGuiLayout.SLOT_ENERGY_HEIGHT, mouseX, mouseY)) {
            return Hover.energy();
        }
        for (RadarControlPanel.Button button : RadarControlPanel.buttons()) {
            if (RadarGuiHitProfile.hitsArea(leftPos, topPos, button.slotX(), button.slotY(),
                    RadarControlPanel.BUTTON_SIZE, RadarControlPanel.BUTTON_SIZE, mouseX, mouseY)) {
                return Hover.control(button);
            }
        }
        if (RadarGuiHitProfile.hitsArea(leftPos, topPos, RadarGuiLayout.SLOT_TOGGLE_VIEW_X,
                RadarGuiLayout.SLOT_TOGGLE_VIEW_Y, RadarGuiLayout.SLOT_TOGGLE_VIEW_SIZE,
                RadarGuiLayout.SLOT_TOGGLE_VIEW_SIZE, mouseX, mouseY)) {
            return Hover.toggleView();
        }
        return Hover.none();
    }

    public record Hover(Type type, @Nullable RadarControlPanel.Button button) {
        private static Hover energy() {
            return new Hover(Type.ENERGY, null);
        }

        private static Hover control(RadarControlPanel.Button button) {
            return new Hover(Type.CONTROL, button);
        }

        private static Hover toggleView() {
            return new Hover(Type.TOGGLE_VIEW, null);
        }

        private static Hover clearMap() {
            return new Hover(Type.CLEAR_MAP, null);
        }

        private static Hover none() {
            return new Hover(Type.NONE, null);
        }
    }

    public enum Type {
        NONE,
        ENERGY,
        CONTROL,
        TOGGLE_VIEW,
        CLEAR_MAP
    }
}

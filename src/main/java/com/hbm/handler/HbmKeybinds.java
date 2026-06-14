package com.hbm.handler;

import com.hbm.ntm.network.HbmKeybind;

/**
 * Legacy keybind namespace facade. Client key registration stays in the modern
 * input system; this class only preserves the old enum shape for migrated code.
 */
public final class HbmKeybinds {
    public static final String category = "hbm.key";

    public enum EnumKeybind {
        JETPACK,
        TOGGLE_JETPACK,
        TOGGLE_MAGNET,
        TOGGLE_HEAD,
        DASH,
        TRAIN,
        CRANE_UP,
        CRANE_DOWN,
        CRANE_LEFT,
        CRANE_RIGHT,
        CRANE_LOAD,
        ABILITY_CYCLE,
        ABILITY_ALT,
        TOOL_ALT,
        TOOL_CTRL,
        GUN_PRIMARY,
        GUN_SECONDARY,
        GUN_TERTIARY,
        RELOAD;

        public HbmKeybind toModern() {
            return HbmKeybind.values()[ordinal()];
        }

        public static EnumKeybind fromModern(HbmKeybind keybind) {
            if (keybind == null) {
                return null;
            }
            return values()[keybind.ordinal()];
        }
    }

    public static HbmKeybind toModern(EnumKeybind keybind) {
        return keybind == null ? null : keybind.toModern();
    }

    public static EnumKeybind fromModern(HbmKeybind keybind) {
        return EnumKeybind.fromModern(keybind);
    }

    private HbmKeybinds() {
    }
}

package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.KeybindPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public final class HbmClientKeybinds {
    private static final String CATEGORY = "key.categories." + HbmNtm.MOD_ID;
    private static final Map<HbmKeybind, KeyMapping> MAPPINGS = new EnumMap<>(HbmKeybind.class);
    private static final Map<HbmKeybind, Boolean> STATES = new EnumMap<>(HbmKeybind.class);

    static {
        key(HbmKeybind.JETPACK, "jetpack", GLFW.GLFW_KEY_C);
        key(HbmKeybind.TOGGLE_JETPACK, "toggle_jetpack", GLFW.GLFW_KEY_X);
        key(HbmKeybind.TOGGLE_MAGNET, "toggle_magnet", GLFW.GLFW_KEY_Z);
        key(HbmKeybind.TOGGLE_HEAD, "toggle_head", GLFW.GLFW_KEY_V);
        key(HbmKeybind.DASH, "dash", GLFW.GLFW_KEY_LEFT_SHIFT);
        key(HbmKeybind.TRAIN, "train", GLFW.GLFW_KEY_R);
        key(HbmKeybind.CRANE_UP, "crane_up", GLFW.GLFW_KEY_UP);
        key(HbmKeybind.CRANE_DOWN, "crane_down", GLFW.GLFW_KEY_DOWN);
        key(HbmKeybind.CRANE_LEFT, "crane_left", GLFW.GLFW_KEY_LEFT);
        key(HbmKeybind.CRANE_RIGHT, "crane_right", GLFW.GLFW_KEY_RIGHT);
        key(HbmKeybind.CRANE_LOAD, "crane_load", GLFW.GLFW_KEY_ENTER);
        mouse(HbmKeybind.ABILITY_CYCLE, "ability_cycle", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        key(HbmKeybind.ABILITY_ALT, "ability_alt", GLFW.GLFW_KEY_LEFT_ALT);
        key(HbmKeybind.TOOL_ALT, "tool_alt", GLFW.GLFW_KEY_LEFT_ALT);
        key(HbmKeybind.TOOL_CTRL, "tool_ctrl", GLFW.GLFW_KEY_LEFT_CONTROL);
        mouse(HbmKeybind.GUN_PRIMARY, "gun_primary", GLFW.GLFW_MOUSE_BUTTON_LEFT);
        mouse(HbmKeybind.GUN_SECONDARY, "gun_secondary", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        mouse(HbmKeybind.GUN_TERTIARY, "gun_tertiary", GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        key(HbmKeybind.RELOAD, "reload", GLFW.GLFW_KEY_R);
    }

    public static void register(RegisterKeyMappingsEvent event) {
        MAPPINGS.values().forEach(event::register);
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            clearPressedStates();
            return;
        }

        for (Map.Entry<HbmKeybind, KeyMapping> entry : MAPPINGS.entrySet()) {
            HbmKeybind keybind = entry.getKey();
            boolean pressed = entry.getValue().isDown();
            boolean previous = STATES.getOrDefault(keybind, false);
            if (pressed != previous) {
                STATES.put(keybind, pressed);
                ModMessages.sendToServer(new KeybindPacket(keybind, pressed));
            }
        }
    }

    private static void clearPressedStates() {
        for (Map.Entry<HbmKeybind, Boolean> entry : STATES.entrySet()) {
            if (entry.getValue()) {
                ModMessages.sendToServer(new KeybindPacket(entry.getKey(), false));
            }
        }
        STATES.clear();
    }

    private static void key(HbmKeybind keybind, String name, int keyCode) {
        MAPPINGS.put(keybind, new KeyMapping("key." + HbmNtm.MOD_ID + "." + name,
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, keyCode, CATEGORY));
    }

    private static void mouse(HbmKeybind keybind, String name, int button) {
        MAPPINGS.put(keybind, new KeyMapping("key." + HbmNtm.MOD_ID + "." + name,
                KeyConflictContext.IN_GAME, InputConstants.Type.MOUSE, button, CATEGORY));
    }

    private HbmClientKeybinds() {
    }
}

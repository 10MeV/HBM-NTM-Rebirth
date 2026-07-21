package com.hbm.ntm.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public final class ClientTooltipState {
    private ClientTooltipState() {
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    /** Legacy ItemDrone intentionally recognized only the left Shift key. */
    public static boolean hasLeftShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT);
    }
}

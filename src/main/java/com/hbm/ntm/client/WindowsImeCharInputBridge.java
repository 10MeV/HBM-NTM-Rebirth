package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.sun.jna.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCharModsCallbackI;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Windows fallback for IME commits that reach GLFW's ordinary Unicode callback
 * but do not reach Minecraft 1.20.1's deprecated char-with-modifiers callback.
 *
 * <p>Minecraft registers only {@code glfwSetCharModsCallback}; GLFW documents the
 * ordinary char callback as the regular Unicode-text path.  The bridge observes both
 * callbacks, delegates the original Minecraft callback unchanged, and only replays a
 * pending Han character when its matching char-mods event was absent for the tick.
 * That keeps the normal path and other keyboard input free of duplicate events.</p>
 */
public final class WindowsImeCharInputBridge {
    private static final GLFWCharCallbackI CHAR_CALLBACK = WindowsImeCharInputBridge::onChar;
    private static final GLFWCharModsCallbackI CHAR_MODS_CALLBACK = WindowsImeCharInputBridge::onCharMods;

    private static final ArrayDeque<Integer> pendingHanCodePoints = new ArrayDeque<>();
    private static final Map<Integer, Integer> observedCharMods = new HashMap<>();

    private static long windowHandle;
    private static GLFWCharCallback originalCharCallback;
    private static GLFWCharModsCallback originalCharModsCallback;
    private static boolean installed;

    private WindowsImeCharInputBridge() {
    }

    public static void tick(Minecraft minecraft) {
        if (!Platform.isWindows()) {
            return;
        }

        long currentWindow = minecraft.getWindow().getWindow();
        if (!installed || windowHandle != currentWindow) {
            install(currentWindow);
        }
        if (!installed) {
            return;
        }

        while (!pendingHanCodePoints.isEmpty()) {
            int codePoint = pendingHanCodePoints.removeFirst();
            if (consumeObservedCharMods(codePoint)) {
                continue;
            }
            dispatchFallback(minecraft, codePoint);
        }
        observedCharMods.clear();
    }

    private static void install(long window) {
        GLFWCharModsCallback previousCharMods = GLFW.glfwSetCharModsCallback(window, CHAR_MODS_CALLBACK);
        if (previousCharMods == null) {
            GLFW.glfwSetCharModsCallback(window, null);
            HbmNtm.LOGGER.warn("Windows IME bridge could not preserve Minecraft's char-mods callback.");
            return;
        }

        GLFWCharCallback previousChar = GLFW.glfwSetCharCallback(window, CHAR_CALLBACK);
        windowHandle = window;
        originalCharModsCallback = previousCharMods;
        originalCharCallback = previousChar;
        pendingHanCodePoints.clear();
        observedCharMods.clear();
        installed = true;
        HbmNtm.LOGGER.info("Windows IME character fallback enabled.");
    }

    private static void onCharMods(long window, int codePoint, int modifiers) {
        GLFWCharModsCallback callback = originalCharModsCallback;
        if (window == windowHandle && isHan(codePoint)) {
            observedCharMods.merge(codePoint, 1, Integer::sum);
        }
        if (callback != null) {
            callback.invoke(window, codePoint, modifiers);
        }
    }

    private static void onChar(long window, int codePoint) {
        GLFWCharCallback callback = originalCharCallback;
        if (callback != null) {
            callback.invoke(window, codePoint);
            return;
        }
        if (window == windowHandle && isHan(codePoint)) {
            pendingHanCodePoints.addLast(codePoint);
        }
    }

    private static boolean consumeObservedCharMods(int codePoint) {
        Integer count = observedCharMods.get(codePoint);
        if (count == null || count == 0) {
            return false;
        }
        if (count == 1) {
            observedCharMods.remove(codePoint);
        } else {
            observedCharMods.put(codePoint, count - 1);
        }
        return true;
    }

    private static void dispatchFallback(Minecraft minecraft, int codePoint) {
        Screen screen = minecraft.screen;
        if (screen == null || minecraft.getOverlay() != null) {
            return;
        }
        for (char character : Character.toChars(codePoint)) {
            Screen.wrapScreenError(() -> {
                if (ForgeHooksClient.onScreenCharTypedPre(screen, character, 0)) {
                    return;
                }
                if (screen.charTyped(character, 0)) {
                    return;
                }
                ForgeHooksClient.onScreenCharTypedPost(screen, character, 0);
            }, "charTyped event handler", screen.getClass().getCanonicalName());
        }
    }

    private static boolean isHan(int codePoint) {
        return Character.isValidCodePoint(codePoint)
                && Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN;
    }
}

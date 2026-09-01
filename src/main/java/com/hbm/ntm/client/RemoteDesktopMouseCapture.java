package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Locale;

/** Stable relative mouse capture for Windows RDP sessions. */
public final class RemoteDesktopMouseCapture {
    private static final int SM_REMOTESESSION = 0x1000;
    private static final int CURSOR_SIZE = 16;
    private static final GLFWCursorPosCallbackI CALLBACK = RemoteDesktopMouseCapture::onCursorPosition;

    private static boolean detectionComplete;
    private static boolean remoteSession;
    private static boolean callbackInstalled;
    private static boolean captureActive;
    private static boolean centerWarpPending;
    private static boolean hasPhysicalSample;
    private static long windowHandle;
    private static long blankCursor;
    private static int windowWidth;
    private static int windowHeight;
    private static double centerX;
    private static double centerY;
    private static double physicalX;
    private static double physicalY;
    private static double virtualX;
    private static double virtualY;
    private static GLFWCursorPosCallback originalCallback;

    private RemoteDesktopMouseCapture() {
    }

    public static void tick(Minecraft minecraft) {
        detectRemoteSession();
        if (!remoteSession) {
            return;
        }

        long currentWindow = minecraft.getWindow().getWindow();
        if (!callbackInstalled || windowHandle != currentWindow) {
            install(currentWindow);
        }
        if (!callbackInstalled) {
            return;
        }

        boolean shouldCapture = minecraft.screen == null
                && minecraft.isWindowActive()
                && minecraft.mouseHandler.isMouseGrabbed();
        if (shouldCapture) {
            activate(minecraft);
        } else if (captureActive) {
            deactivate(minecraft);
        }
    }

    /**
     * A menu action may occur while a screen is being replaced by the client.
     * Stop the RDP-only relative capture before that transition so a pending
     * world-input centre warp cannot become visible as a GUI cursor jump.
     */
    public static void suspendForScreen(Minecraft minecraft) {
        if (captureActive) {
            deactivate(minecraft);
        }
    }

    private static void detectRemoteSession() {
        if (detectionComplete) {
            return;
        }
        detectionComplete = true;
        if (!Platform.isWindows()) {
            return;
        }

        try {
            remoteSession = User32.INSTANCE.GetSystemMetrics(SM_REMOTESESSION) != 0;
        } catch (LinkageError | RuntimeException exception) {
            String sessionName = System.getenv("SESSIONNAME");
            remoteSession = sessionName != null
                    && sessionName.toUpperCase(Locale.ROOT).startsWith("RDP-");
            HbmNtm.LOGGER.debug("Win32 RDP session query failed; SESSIONNAME fallback result is {}.",
                    remoteSession, exception);
        }

        if (remoteSession) {
            HbmNtm.LOGGER.info("Windows RDP session detected; enabling stable relative mouse capture.");
        }
    }

    private static void install(long window) {
        windowHandle = window;
        originalCallback = GLFW.glfwSetCursorPosCallback(window, CALLBACK);
        if (originalCallback == null) {
            HbmNtm.LOGGER.warn("RDP mouse capture could not preserve Minecraft's cursor callback.");
            return;
        }

        blankCursor = createBlankCursor();
        if (blankCursor == MemoryUtil.NULL) {
            GLFW.glfwSetCursorPosCallback(window, originalCallback);
            originalCallback = null;
            HbmNtm.LOGGER.warn("RDP mouse capture could not create a transparent cursor.");
            return;
        }
        callbackInstalled = true;
    }

    private static long createBlankCursor() {
        ByteBuffer pixels = MemoryUtil.memCalloc(CURSOR_SIZE * CURSOR_SIZE * 4);
        GLFWImage image = GLFWImage.malloc();
        try {
            pixels.put(3, (byte) 1);
            image.set(CURSOR_SIZE, CURSOR_SIZE, pixels);
            return GLFW.glfwCreateCursor(image, 0, 0);
        } finally {
            image.free();
            MemoryUtil.memFree((Buffer) pixels);
        }
    }

    private static void activate(Minecraft minecraft) {
        updateWindowCenter();
        if (windowWidth <= 0 || windowHeight <= 0) {
            return;
        }

        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        if (GLFW.glfwRawMouseMotionSupported()) {
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
        }
        GLFW.glfwSetCursor(windowHandle, blankCursor);

        if (!captureActive) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer x = stack.mallocDouble(1);
                DoubleBuffer y = stack.mallocDouble(1);
                GLFW.glfwGetCursorPos(windowHandle, x, y);
                physicalX = x.get(0);
                physicalY = y.get(0);
                virtualX = physicalX;
                virtualY = physicalY;
            }
            captureActive = true;
            HbmNtm.LOGGER.info("RDP stable relative mouse capture active at logical window size {}x{}.",
                    windowWidth, windowHeight);
            hasPhysicalSample = false;
            requestCenterWarp();
        }
    }

    private static void updateWindowCenter() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(windowHandle, width, height);
            windowWidth = width.get(0);
            windowHeight = height.get(0);
            centerX = windowWidth * 0.5D;
            centerY = windowHeight * 0.5D;
        }
    }

    private static void deactivate(Minecraft minecraft) {
        captureActive = false;
        centerWarpPending = false;
        hasPhysicalSample = false;
        GLFW.glfwSetCursor(windowHandle, MemoryUtil.NULL);
        if (GLFW.glfwRawMouseMotionSupported()) {
            boolean configured = minecraft.options.rawMouseInput().get();
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_RAW_MOUSE_MOTION,
                    configured ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        }
    }

    private static void onCursorPosition(long window, double x, double y) {
        if (originalCallback == null) {
            return;
        }
        if (!captureActive || window != windowHandle) {
            originalCallback.invoke(window, x, y);
            return;
        }

        boolean centerEvent = isAtCenter(x, y);
        if (centerEvent) {
            centerWarpPending = false;
            return;
        }

        // RDP reports absolute positions from the controlling machine. A server-side
        // cursor warp produces a center callback but does not reset that remote
        // absolute coordinate stream, so only consecutive non-center samples form
        // a valid relative delta.
        if (!hasPhysicalSample) {
            physicalX = x;
            physicalY = y;
            hasPhysicalSample = true;
            requestCenterWarp();
            return;
        }

        double deltaX = x - physicalX;
        double deltaY = y - physicalY;
        physicalX = x;
        physicalY = y;

        // A real event after centering cannot jump farther than half the window.
        if (Math.abs(deltaX) < windowWidth * 0.5D && Math.abs(deltaY) < windowHeight * 0.5D) {
            virtualX += deltaX;
            virtualY += deltaY;
            originalCallback.invoke(window, virtualX, virtualY);
        }
        requestCenterWarp();
    }

    private static boolean isAtCenter(double x, double y) {
        return Math.abs(x - centerX) <= 1.0D && Math.abs(y - centerY) <= 1.0D;
    }

    private static void requestCenterWarp() {
        if (centerWarpPending) {
            return;
        }
        centerWarpPending = true;
        GLFW.glfwSetCursorPos(windowHandle, centerX, centerY);
    }

}

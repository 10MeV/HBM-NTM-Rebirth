package com.hbm.ntm.client.render;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.ARBDrawInstanced;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLCapabilities;

public final class HbmInstancedGlCompat {
    private static final int REQUIRED_VERTEX_ATTRIBS = 14;

    private HbmInstancedGlCompat() {
    }

    public static boolean isInstancingUnavailable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof InstancingUnavailableException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public static boolean supportsDrawArraysInstancing() {
        try {
            GLCapabilities capabilities = currentCapabilities();
            if (capabilities == null) {
                return false;
            }
            boolean hasDivisor = capabilities.glVertexAttribDivisor != 0L
                    || capabilities.glVertexAttribDivisorARB != 0L;
            boolean hasDrawArraysInstanced = capabilities.glDrawArraysInstanced != 0L
                    || capabilities.glDrawArraysInstancedARB != 0L;
            return hasDivisor && hasDrawArraysInstanced && maxVertexAttribs() >= REQUIRED_VERTEX_ATTRIBS;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int requiredVertexAttribs() {
        return REQUIRED_VERTEX_ATTRIBS;
    }

    public static int maxVertexAttribs() {
        try {
            if (GLFW.glfwGetCurrentContext() == 0L) {
                return 0;
            }
            return GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static GLCapabilities currentCapabilities() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return null;
        }
        GLCapabilities capabilities;
        try {
            capabilities = GL.getCapabilities();
        } catch (IllegalStateException exception) {
            capabilities = null;
        }
        if (capabilities != null) {
            return capabilities;
        }
        try {
            GL.createCapabilities();
        } catch (Throwable ignored) {
            return null;
        }
        try {
            return GL.getCapabilities();
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    public static void vertexAttribDivisor(int index, int divisor) {
        GLCapabilities capabilities = currentCapabilities();
        if (capabilities != null && capabilities.glVertexAttribDivisor != 0L) {
            GL33.glVertexAttribDivisor(index, divisor);
        } else if (capabilities != null
                && (capabilities.glVertexAttribDivisorARB != 0L || capabilities.GL_ARB_instanced_arrays)) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        } else {
            throw new InstancingUnavailableException("Vertex attrib divisor unavailable in current GL context");
        }
    }

    public static void drawArraysInstanced(int mode, int first, int count, int instanceCount) {
        GLCapabilities capabilities = currentCapabilities();
        if (capabilities != null && capabilities.glDrawArraysInstanced != 0L) {
            GL31.glDrawArraysInstanced(mode, first, count, instanceCount);
        } else if (capabilities != null
                && (capabilities.glDrawArraysInstancedARB != 0L || capabilities.GL_ARB_draw_instanced)) {
            ARBDrawInstanced.glDrawArraysInstancedARB(mode, first, count, instanceCount);
        } else {
            throw new InstancingUnavailableException("Draw arrays instanced unavailable in current GL context");
        }
    }

    public static final class InstancingUnavailableException extends RuntimeException {
        private InstancingUnavailableException(String message) {
            super(message);
        }
    }
}

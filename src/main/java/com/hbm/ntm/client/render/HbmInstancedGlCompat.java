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
    private static long cachedInstancingContext;
    private static boolean cachedInstancingSupportResolved;
    private static boolean cachedDrawArraysInstancingSupport;
    private static long cachedMaxVertexAttribsContext;
    private static int cachedMaxVertexAttribs = -1;
    private static long cachedInstancingDispatchContext;
    private static boolean cachedInstancingDispatchResolved;
    private static boolean cachedCoreVertexAttribDivisor;
    private static boolean cachedArbVertexAttribDivisor;
    private static boolean cachedCoreDrawArraysInstanced;
    private static boolean cachedArbDrawArraysInstanced;

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
            long context = GLFW.glfwGetCurrentContext();
            if (context == 0L) {
                return false;
            }
            if (cachedInstancingSupportResolved && cachedInstancingContext == context) {
                return cachedDrawArraysInstancingSupport;
            }
            if (!resolveInstancingDispatchCapabilities()) {
                return false;
            }
            boolean hasDivisor = cachedCoreVertexAttribDivisor || cachedArbVertexAttribDivisor;
            boolean hasDrawArraysInstanced = cachedCoreDrawArraysInstanced || cachedArbDrawArraysInstanced;
            boolean supported = hasDivisor && hasDrawArraysInstanced && maxVertexAttribs() >= REQUIRED_VERTEX_ATTRIBS;
            cachedInstancingContext = context;
            cachedDrawArraysInstancingSupport = supported;
            cachedInstancingSupportResolved = true;
            return supported;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int requiredVertexAttribs() {
        return REQUIRED_VERTEX_ATTRIBS;
    }

    public static int maxVertexAttribs() {
        try {
            long context = GLFW.glfwGetCurrentContext();
            if (context == 0L) {
                return 0;
            }
            if (cachedMaxVertexAttribsContext == context && cachedMaxVertexAttribs >= 0) {
                return cachedMaxVertexAttribs;
            }
            int maxAttribs = GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS);
            cachedMaxVertexAttribsContext = context;
            cachedMaxVertexAttribs = maxAttribs;
            return maxAttribs;
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

    private static boolean resolveInstancingDispatchCapabilities() {
        try {
            long context = GLFW.glfwGetCurrentContext();
            if (context == 0L) {
                return false;
            }
            if (cachedInstancingDispatchResolved && cachedInstancingDispatchContext == context) {
                return true;
            }
            GLCapabilities capabilities = currentCapabilities();
            if (capabilities == null) {
                return false;
            }
            cachedCoreVertexAttribDivisor = capabilities.glVertexAttribDivisor != 0L;
            cachedArbVertexAttribDivisor = capabilities.glVertexAttribDivisorARB != 0L
                    || capabilities.GL_ARB_instanced_arrays;
            cachedCoreDrawArraysInstanced = capabilities.glDrawArraysInstanced != 0L;
            cachedArbDrawArraysInstanced = capabilities.glDrawArraysInstancedARB != 0L
                    || capabilities.GL_ARB_draw_instanced;
            cachedInstancingDispatchContext = context;
            cachedInstancingDispatchResolved = true;
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void vertexAttribDivisor(int index, int divisor) {
        boolean resolved = resolveInstancingDispatchCapabilities();
        if (resolved && cachedCoreVertexAttribDivisor) {
            GL33.glVertexAttribDivisor(index, divisor);
        } else if (resolved && cachedArbVertexAttribDivisor) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        } else {
            throw new InstancingUnavailableException("Vertex attrib divisor unavailable in current GL context");
        }
    }

    public static void drawArraysInstanced(int mode, int first, int count, int instanceCount) {
        boolean resolved = resolveInstancingDispatchCapabilities();
        if (resolved && cachedCoreDrawArraysInstanced) {
            GL31.glDrawArraysInstanced(mode, first, count, instanceCount);
        } else if (resolved && cachedArbDrawArraysInstanced) {
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

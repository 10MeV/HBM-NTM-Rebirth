package com.hbm.ntm.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

/**
 * Small helper for core-profile VAO rules around optimized HBM draw paths.
 */
public final class HbmGlVaoSafety {
    private static int dummyVao;

    private HbmGlVaoSafety() {
    }

    public static int currentBinding() {
        return GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
    }

    public static void bindVertexArray(int vao) {
        GlStateManager._glBindVertexArray(vao);
    }

    public static void withAttribEditVao(Runnable work) {
        int previousVao = currentBinding();
        int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        boolean useDummy = previousVao == 0;
        try {
            if (useDummy) {
                bindVertexArray(dummyVao());
            }
            work.run();
        } finally {
            bindVertexArray(previousVao);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
        }
    }

    private static int dummyVao() {
        if (dummyVao == 0) {
            dummyVao = GL30.glGenVertexArrays();
        }
        return dummyVao;
    }
}

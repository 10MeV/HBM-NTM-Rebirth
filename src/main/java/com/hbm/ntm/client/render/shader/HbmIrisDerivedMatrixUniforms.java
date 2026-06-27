package com.hbm.ntm.client.render.shader;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

/**
 * Resolves Iris/Oculus matrix uniform locations with type validation.
 */
public final class HbmIrisDerivedMatrixUniforms {
    private static final int GL_FLOAT_MAT3 = 0x8B5B;
    private static final int GL_FLOAT_MAT4 = 0x8B5C;

    private HbmIrisDerivedMatrixUniforms() {
    }

    public static Locations resolve(ShaderInstance shader) {
        if (shader == null || shader.getId() <= 0) {
            return Locations.NONE;
        }
        int programId = shader.getId();
        int modelView = firstMat4(programId, "ModelViewMat", "iris_ModelViewMat");
        if (modelView < 0) {
            Uniform uniform = shader.getUniform("ModelViewMat");
            if (uniform != null && isMat4(programId, uniform.getLocation())) {
                modelView = uniform.getLocation();
            }
        }
        return new Locations(
                modelView,
                firstMat4(programId, "ModelViewMatInverse", "iris_ModelViewMatInverse"),
                firstMat3(programId, "NormalMat", "iris_NormalMat"));
    }

    private static int firstMat4(int programId, String... names) {
        for (String name : names) {
            int location = GL20.glGetUniformLocation(programId, name);
            if (isMat4(programId, location)) {
                return location;
            }
        }
        return -1;
    }

    private static int firstMat3(int programId, String... names) {
        for (String name : names) {
            int location = GL20.glGetUniformLocation(programId, name);
            if (isMat3(programId, location)) {
                return location;
            }
        }
        return -1;
    }

    private static boolean isMat4(int programId, int location) {
        return location >= 0 && uniformType(programId, location) == GL_FLOAT_MAT4;
    }

    private static boolean isMat3(int programId, int location) {
        return location >= 0 && uniformType(programId, location) == GL_FLOAT_MAT3;
    }

    private static int uniformType(int programId, int location) {
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            return -1;
        }
        int count = GL20.glGetProgrami(programId, GL20.GL_ACTIVE_UNIFORMS);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var size = stack.mallocInt(1);
            var type = stack.mallocInt(1);
            for (int i = 0; i < count; i++) {
                String name = GL20.glGetActiveUniform(programId, i, size, type);
                if (name != null && GL20.glGetUniformLocation(programId, name) == location) {
                    return type.get(0);
                }
            }
        }
        return -1;
    }

    public record Locations(int modelView, int modelViewInverse, int normalMat) {
        public static final Locations NONE = new Locations(-1, -1, -1);
    }
}

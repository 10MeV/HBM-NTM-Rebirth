package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Per-frame guard for the dynamic lightmap upload used by optimized OBJ draws.
 */
@OnlyIn(Dist.CLIENT)
public final class HbmRenderFrameLight {
    private static int frameSerial;
    private static int lastUpdatedFrame = -1;
    private static long invalidSamplerBindings;
    private static long currentFrameInvalidSamplerBindings;
    private static long lastFrameInvalidSamplerBindings;
    private static int lastInvalidSamplerLogFrame = -120;
    private static ShaderInstance cachedSamplerShader;
    private static int cachedSamplerProgram = -1;
    private static long cachedSamplerPipelineGeneration = -1L;
    private static Uniform sampler0Uniform;
    private static Uniform sampler1Uniform;
    private static Uniform sampler2Uniform;
    private static ShaderInstance uploadedSamplerUniformShader;
    private static int uploadedSamplerUniformProgram = -1;
    private static long uploadedSamplerUniformPipelineGeneration = -1L;
    private static int cachedSamplerTextureFrame = -1;
    private static ResourceLocation cachedSamplerBaseTexture;
    private static int cachedSamplerBase = -1;
    private static int cachedSamplerOverlay = -1;
    private static int cachedSamplerLightmap = -1;
    private static Integer cachedSamplerBaseObject;
    private static Integer cachedSamplerOverlayObject;
    private static Integer cachedSamplerLightmapObject;
    private static ShaderInstance preparedSamplerShader;
    private static int preparedSamplerProgram = -1;
    private static long preparedSamplerPipelineGeneration = -1L;
    private static int preparedSamplerBase = -1;
    private static int preparedSamplerOverlay = -1;
    private static int preparedSamplerLightmap = -1;

    private HbmRenderFrameLight() {
    }

    public static void beginFrame() {
        lastFrameInvalidSamplerBindings = currentFrameInvalidSamplerBindings;
        currentFrameInvalidSamplerBindings = 0L;
        frameSerial++;
    }

    public static long invalidSamplerBindings() {
        return invalidSamplerBindings;
    }

    public static long currentFrameInvalidSamplerBindings() {
        return currentFrameInvalidSamplerBindings;
    }

    public static long lastFrameInvalidSamplerBindings() {
        return lastFrameInvalidSamplerBindings;
    }

    public static void invalidateCaches() {
        lastUpdatedFrame = -1;
        cachedSamplerShader = null;
        cachedSamplerProgram = -1;
        cachedSamplerPipelineGeneration = -1L;
        sampler0Uniform = null;
        sampler1Uniform = null;
        sampler2Uniform = null;
        uploadedSamplerUniformShader = null;
        uploadedSamplerUniformProgram = -1;
        uploadedSamplerUniformPipelineGeneration = -1L;
        cachedSamplerTextureFrame = -1;
        cachedSamplerBaseTexture = null;
        cachedSamplerBase = -1;
        cachedSamplerOverlay = -1;
        cachedSamplerLightmap = -1;
        cachedSamplerBaseObject = null;
        cachedSamplerOverlayObject = null;
        cachedSamplerLightmapObject = null;
        clearPreparedSamplerMapCache();
    }

    public static void ensureLightTextureUpdated() {
        if (lastUpdatedFrame == frameSerial) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer != null) {
            minecraft.gameRenderer.lightTexture().updateLightTexture(minecraft.getFrameTime());
        }
        lastUpdatedFrame = frameSerial;
    }

    public static void prepareBlockLitSamplers(ShaderInstance shader) {
        prepareBlockLitSamplers(shader, TextureAtlas.LOCATION_BLOCKS);
    }

    public static void prepareBlockLitSamplers(ShaderInstance shader, ResourceLocation baseTexture) {
        if (shader == null) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        ensureLightTextureUpdated();
        Minecraft minecraft = Minecraft.getInstance();
        ensureSamplerTextureIds(minecraft, baseTexture);
        int base = cachedSamplerBase;
        int overlay = cachedSamplerOverlay;
        int lightmap = cachedSamplerLightmap;
        if (base > 0 && overlay > 0 && lightmap > 0
                && samplerMapAlreadyPrepared(shader, base, overlay, lightmap)) {
            return;
        }
        if (base > 0) {
            shader.setSampler("Sampler0", cachedSamplerBaseObject(base));
        }
        if (overlay > 0) {
            shader.setSampler("Sampler1", cachedSamplerOverlayObject(overlay));
        }
        if (lightmap > 0) {
            shader.setSampler("Sampler2", cachedSamplerLightmapObject(lightmap));
        }
        if (base > 0 && overlay > 0 && lightmap > 0) {
            rememberPreparedSamplerMap(shader, base, overlay, lightmap);
        } else {
            clearPreparedSamplerMapCache();
        }
    }

    public static void bindBlockLitSamplerTextures(ShaderInstance shader) {
        bindBlockLitSamplerTextures(shader, TextureAtlas.LOCATION_BLOCKS);
    }

    public static void bindBlockLitSamplerTextures(ShaderInstance shader, ResourceLocation baseTexture) {
        if (shader == null) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        Minecraft minecraft = Minecraft.getInstance();
        ensureSamplerTextureIds(minecraft, baseTexture);
        int base = cachedSamplerBase;
        int overlay = cachedSamplerOverlay;
        int lightmap = cachedSamplerLightmap;
        if (base <= 0 || overlay <= 0 || lightmap <= 0) {
            recordInvalidSamplerBinding(base, overlay, lightmap);
            return;
        }
        bindSamplerTexture(0, base);
        bindSamplerTexture(1, overlay);
        bindSamplerTexture(2, lightmap);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        updateSamplerUniformCache(shader);
        uploadSamplerUniformsIfNeeded(shader);
    }

    private static void recordInvalidSamplerBinding(int base, int overlay, int lightmap) {
        invalidSamplerBindings++;
        currentFrameInvalidSamplerBindings++;
        if (frameSerial - lastInvalidSamplerLogFrame >= 120) {
            lastInvalidSamplerLogFrame = frameSerial;
            HbmNtm.LOGGER.warn(
                    "HBM block-lit sampler bind skipped: invalid GL texture id (base={}, overlay={}, lightmap={})",
                    base, overlay, lightmap);
        }
    }

    private static void setupBlockLitSamplerTextures(Minecraft minecraft, ResourceLocation baseTexture) {
        if (minecraft.gameRenderer == null) {
            return;
        }
        ResourceLocation texture = baseTexture != null ? baseTexture : TextureAtlas.LOCATION_BLOCKS;
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        minecraft.getTextureManager().bindForSetup(texture);

        RenderSystem.activeTexture(GL13.GL_TEXTURE1);
        minecraft.gameRenderer.overlayTexture().setupOverlayColor();

        RenderSystem.activeTexture(GL13.GL_TEXTURE2);
        minecraft.gameRenderer.lightTexture().turnOnLightLayer();

        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        minecraft.getTextureManager().bindForSetup(texture);
    }

    private static void ensureSamplerTextureIds(Minecraft minecraft, ResourceLocation baseTexture) {
        ResourceLocation texture = baseTexture != null ? baseTexture : TextureAtlas.LOCATION_BLOCKS;
        boolean cachedThisFrame = cachedSamplerTextureFrame == frameSerial;
        int base = cachedThisFrame && texture.equals(cachedSamplerBaseTexture)
                ? cachedSamplerBase
                : resolveTextureManagerGlId(minecraft, texture);
        int overlay = cachedThisFrame ? cachedSamplerOverlay : -1;
        int lightmap = cachedThisFrame ? cachedSamplerLightmap : -1;
        if (base <= 0 || overlay <= 0 || lightmap <= 0) {
            setupBlockLitSamplerTextures(minecraft, texture);
            base = resolveTextureGlId(minecraft, texture);
            overlay = resolveSamplerGlId(1);
            lightmap = resolveSamplerGlId(2);
            rememberSamplerTextureIds(texture, base, overlay, lightmap);
        } else {
            cachedSamplerBaseTexture = texture;
            cachedSamplerBase = base;
        }
    }

    private static void rememberSamplerTextureIds(ResourceLocation baseTexture, int base, int overlay, int lightmap) {
        ResourceLocation texture = baseTexture != null ? baseTexture : TextureAtlas.LOCATION_BLOCKS;
        cachedSamplerTextureFrame = frameSerial;
        cachedSamplerBaseTexture = texture;
        cachedSamplerBase = base;
        cachedSamplerOverlay = overlay;
        cachedSamplerLightmap = lightmap;
    }

    private static int resolveTextureManagerGlId(Minecraft minecraft, ResourceLocation textureLocation) {
        AbstractTexture abstractTexture = minecraft.getTextureManager().getTexture(textureLocation);
        return abstractTexture != null && abstractTexture.getId() > 0 ? abstractTexture.getId() : -1;
    }

    private static int resolveTextureGlId(Minecraft minecraft, ResourceLocation textureLocation) {
        ResourceLocation texture = textureLocation != null ? textureLocation : TextureAtlas.LOCATION_BLOCKS;
        AbstractTexture abstractTexture = minecraft.getTextureManager().getTexture(texture);
        if (abstractTexture != null && abstractTexture.getId() > 0) {
            return abstractTexture.getId();
        }
        return RenderSystem.getShaderTexture(0);
    }

    private static int resolveSamplerGlId(int unit) {
        int texture = RenderSystem.getShaderTexture(unit);
        if (texture > 0) {
            return texture;
        }
        RenderSystem.activeTexture(GL13.GL_TEXTURE0 + unit);
        int bound = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        return bound > 0 ? bound : -1;
    }

    private static void bindSamplerTexture(int unit, int textureId) {
        if (textureId <= 0) {
            return;
        }
        RenderSystem.setShaderTexture(unit, textureId);
    }

    private static void updateSamplerUniformCache(ShaderInstance shader) {
        int program = shader.getId();
        long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
        if (cachedSamplerShader == shader
                && cachedSamplerProgram == program
                && cachedSamplerPipelineGeneration == generation) {
            return;
        }
        cachedSamplerShader = shader;
        cachedSamplerProgram = program;
        cachedSamplerPipelineGeneration = generation;
        sampler0Uniform = shader.getUniform("Sampler0");
        sampler1Uniform = shader.getUniform("Sampler1");
        sampler2Uniform = shader.getUniform("Sampler2");
    }

    private static void setSamplerUniform(Uniform uniform, int unit) {
        if (uniform != null) {
            uniform.set(unit);
            uniform.upload();
        }
    }

    private static Integer cachedSamplerBaseObject(int textureId) {
        if (cachedSamplerBaseObject == null || cachedSamplerBaseObject.intValue() != textureId) {
            cachedSamplerBaseObject = Integer.valueOf(textureId);
        }
        return cachedSamplerBaseObject;
    }

    private static Integer cachedSamplerOverlayObject(int textureId) {
        if (cachedSamplerOverlayObject == null || cachedSamplerOverlayObject.intValue() != textureId) {
            cachedSamplerOverlayObject = Integer.valueOf(textureId);
        }
        return cachedSamplerOverlayObject;
    }

    private static Integer cachedSamplerLightmapObject(int textureId) {
        if (cachedSamplerLightmapObject == null || cachedSamplerLightmapObject.intValue() != textureId) {
            cachedSamplerLightmapObject = Integer.valueOf(textureId);
        }
        return cachedSamplerLightmapObject;
    }

    private static boolean samplerMapAlreadyPrepared(ShaderInstance shader, int base, int overlay, int lightmap) {
        int program = shader.getId();
        long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
        return preparedSamplerShader == shader
                && preparedSamplerProgram == program
                && preparedSamplerPipelineGeneration == generation
                && preparedSamplerBase == base
                && preparedSamplerOverlay == overlay
                && preparedSamplerLightmap == lightmap;
    }

    private static void rememberPreparedSamplerMap(ShaderInstance shader, int base, int overlay, int lightmap) {
        preparedSamplerShader = shader;
        preparedSamplerProgram = shader.getId();
        preparedSamplerPipelineGeneration = HbmShaderCompatibilityDetector.pipelineGeneration();
        preparedSamplerBase = base;
        preparedSamplerOverlay = overlay;
        preparedSamplerLightmap = lightmap;
    }

    private static void clearPreparedSamplerMapCache() {
        preparedSamplerShader = null;
        preparedSamplerProgram = -1;
        preparedSamplerPipelineGeneration = -1L;
        preparedSamplerBase = -1;
        preparedSamplerOverlay = -1;
        preparedSamplerLightmap = -1;
    }

    private static void uploadSamplerUniformsIfNeeded(ShaderInstance shader) {
        int program = shader.getId();
        long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
        if (uploadedSamplerUniformShader == shader
                && uploadedSamplerUniformProgram == program
                && uploadedSamplerUniformPipelineGeneration == generation) {
            return;
        }
        setSamplerUniform(sampler0Uniform, 0);
        setSamplerUniform(sampler1Uniform, 1);
        setSamplerUniform(sampler2Uniform, 2);
        uploadedSamplerUniformShader = shader;
        uploadedSamplerUniformProgram = program;
        uploadedSamplerUniformPipelineGeneration = generation;
    }

}

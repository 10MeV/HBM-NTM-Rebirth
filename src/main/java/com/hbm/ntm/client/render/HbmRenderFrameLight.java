package com.hbm.ntm.client.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
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

    private HbmRenderFrameLight() {
    }

    public static void beginFrame() {
        frameSerial++;
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
        if (shader == null) {
            return;
        }
        ensureLightTextureUpdated();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer != null) {
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            minecraft.getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
            minecraft.gameRenderer.overlayTexture().setupOverlayColor();
            minecraft.gameRenderer.lightTexture().turnOnLightLayer();
        }
        int atlas = resolveBlockAtlasGlId(minecraft);
        int overlay = RenderSystem.getShaderTexture(1);
        int lightmap = RenderSystem.getShaderTexture(2);
        if (atlas > 0) {
            shader.setSampler("Sampler0", Integer.valueOf(atlas));
        }
        if (overlay > 0) {
            shader.setSampler("Sampler1", Integer.valueOf(overlay));
        }
        if (lightmap > 0) {
            shader.setSampler("Sampler2", Integer.valueOf(lightmap));
        }
    }

    public static void bindBlockLitSamplerTextures(ShaderInstance shader) {
        if (shader == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int atlas = resolveBlockAtlasGlId(minecraft);
        int overlay = RenderSystem.getShaderTexture(1);
        int lightmap = RenderSystem.getShaderTexture(2);
        bindSamplerTexture(0, atlas);
        bindSamplerTexture(1, overlay);
        bindSamplerTexture(2, lightmap);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        setSamplerUniform(shader, "Sampler0", 0);
        setSamplerUniform(shader, "Sampler1", 1);
        setSamplerUniform(shader, "Sampler2", 2);
    }

    private static int resolveBlockAtlasGlId(Minecraft minecraft) {
        AbstractTexture atlas = minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        if (atlas != null && atlas.getId() > 0) {
            return atlas.getId();
        }
        return RenderSystem.getShaderTexture(0);
    }

    private static void bindSamplerTexture(int unit, int textureId) {
        if (textureId <= 0) {
            return;
        }
        RenderSystem.activeTexture(GL13.GL_TEXTURE0 + unit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    private static void setSamplerUniform(ShaderInstance shader, String name, int unit) {
        Uniform uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(unit);
            uniform.upload();
        }
    }
}

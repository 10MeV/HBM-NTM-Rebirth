package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum LegacyTexturedRenderMode {
    CUTOUT_NO_CULL,
    TRANSLUCENT,
    TRANSLUCENT_NO_DEPTH_WRITE,
    TRANSLUCENT_DEPTH_WRITE,
    ADDITIVE_NO_DEPTH_WRITE,
    ADDITIVE_DEPTH_WRITE,
    GLINT_NO_DEPTH_WRITE,
    GLINT_EQUAL_DEPTH;

    private static final RenderStateShard.TransparencyStateShard NORMAL_ALPHA_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_legacy_textured_alpha_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                GlStateManager.SourceFactor.ONE,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
    private static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_legacy_textured_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
    private static final RenderStateShard.TransparencyStateShard GLINT_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("hbm_legacy_textured_glint_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
    private static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST =
            new RenderStateShard.DepthTestStateShard("hbm_legacy_lequal_depth_test", 515);
    private static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST =
            new RenderStateShard.DepthTestStateShard("hbm_legacy_equal_depth_test", 514);
    private static final Map<Key, RenderType> CACHE = new ConcurrentHashMap<>();

    public RenderType renderType(ResourceLocation texture) {
        return switch (this) {
            case CUTOUT_NO_CULL -> RenderType.entityCutoutNoCull(texture);
            case TRANSLUCENT -> RenderType.entityTranslucent(texture);
            case TRANSLUCENT_NO_DEPTH_WRITE, TRANSLUCENT_DEPTH_WRITE, ADDITIVE_NO_DEPTH_WRITE, ADDITIVE_DEPTH_WRITE,
                    GLINT_NO_DEPTH_WRITE, GLINT_EQUAL_DEPTH ->
                    CACHE.computeIfAbsent(new Key(this, texture), Key::create);
        };
    }

    public boolean translucent() {
        return this != CUTOUT_NO_CULL;
    }

    public RenderModeStatePlan statePlan() {
        return switch (this) {
            case CUTOUT_NO_CULL -> new RenderModeStatePlan(false, BlendFunction.NONE, true, DepthTest.LEQUAL,
                    false, true, true, true);
            case TRANSLUCENT -> new RenderModeStatePlan(true, BlendFunction.NORMAL_ALPHA, true, DepthTest.LEQUAL,
                    true, true, true, true);
            case TRANSLUCENT_NO_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.NORMAL_ALPHA, false, DepthTest.LEQUAL,
                    false, true, true, true);
            case TRANSLUCENT_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.NORMAL_ALPHA, true, DepthTest.LEQUAL,
                    false, true, true, true);
            case ADDITIVE_NO_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.ADDITIVE, false, DepthTest.LEQUAL,
                    false, true, true, true);
            case ADDITIVE_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.ADDITIVE, true, DepthTest.LEQUAL,
                    false, true, true, true);
            case GLINT_NO_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.GLINT, false, DepthTest.LEQUAL,
                    false, true, true, true);
            case GLINT_EQUAL_DEPTH -> new RenderModeStatePlan(true, BlendFunction.GLINT, false, DepthTest.EQUAL,
                    false, true, true, true);
        };
    }

    public LegacyTexturedRenderMode withAlpha(int alpha) {
        return alpha < 255 && this == CUTOUT_NO_CULL ? TRANSLUCENT : this;
    }

    private static RenderType createCustom(String name, ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite) {
        return createCustom(name, texture, transparency, depthWrite, LEQUAL_DEPTH_TEST);
    }

    private static RenderType createCustom(String name, ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite,
            RenderStateShard.DepthTestStateShard depthTest) {
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                false, true, RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setDepthTestState(depthTest)
                        .setCullState(new RenderStateShard.CullStateShard(false))
                        .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                        .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, depthWrite))
                        .createCompositeState(false));
    }

    private record Key(LegacyTexturedRenderMode mode, ResourceLocation texture) {
        private RenderType create() {
            String name = "hbm_legacy_textured_" + mode.name().toLowerCase() + "_" + texture.toString()
                    .replace(':', '_').replace('/', '_').replace('.', '_');
            return switch (mode) {
                case TRANSLUCENT_NO_DEPTH_WRITE -> createCustom(name, texture, NORMAL_ALPHA_TRANSPARENCY, false);
                case TRANSLUCENT_DEPTH_WRITE -> createCustom(name, texture, NORMAL_ALPHA_TRANSPARENCY, true);
                case ADDITIVE_NO_DEPTH_WRITE -> createCustom(name, texture, ADDITIVE_TRANSPARENCY, false);
                case ADDITIVE_DEPTH_WRITE -> createCustom(name, texture, ADDITIVE_TRANSPARENCY, true);
                case GLINT_NO_DEPTH_WRITE -> createCustom(name, texture, GLINT_TRANSPARENCY, false);
                case GLINT_EQUAL_DEPTH -> createCustom(name, texture, GLINT_TRANSPARENCY, false, EQUAL_DEPTH_TEST);
                case CUTOUT_NO_CULL, TRANSLUCENT -> mode.renderType(texture);
            };
        }
    }

    public enum BlendFunction {
        NONE,
        NORMAL_ALPHA,
        ADDITIVE,
        GLINT
    }

    public enum DepthTest {
        LEQUAL,
        EQUAL
    }

    public record RenderModeStatePlan(boolean blendEnabled, BlendFunction blendFunction, boolean depthWrite,
                                      DepthTest depthTest, boolean vanillaEntityTranslucent,
                                      boolean cullDisabled, boolean lightmapEnabled, boolean overlayEnabled) {
    }
}

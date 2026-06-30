package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.hbm.ntm.client.render.HbmOptimizedRenderShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum LegacyTexturedRenderMode {
    CUTOUT_NO_CULL,
    CUTOUT_DOUBLE_SIDED,
    CUTOUT_REVERSED_CULL,
    CUTOUT_CULL,
    TRANSLUCENT,
    TRANSLUCENT_NO_DEPTH_WRITE,
    TRANSLUCENT_DEPTH_WRITE,
    ADDITIVE_NO_DEPTH_WRITE,
    ADDITIVE_CULL_NO_DEPTH_WRITE,
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
    private static final RenderStateShard.TexturingStateShard FRONT_FACE_CW =
            new RenderStateShard.TexturingStateShard("hbm_legacy_front_face_cw",
                    () -> GL11.glFrontFace(GL11.GL_CW),
                    () -> GL11.glFrontFace(GL11.GL_CCW));
    private static final RenderStateShard.ShaderStateShard BLOCK_LIT_SHADER =
            new RenderStateShard.ShaderStateShard(HbmOptimizedRenderShaders::blockLitStaticShader);
    private static final int LEGACY_OBJ_BUFFER_SIZE = 1_048_576;
    private static final Map<Key, RenderType> CACHE = new ConcurrentHashMap<>();

    public RenderType renderType(ResourceLocation texture) {
        return CACHE.computeIfAbsent(new Key(this, texture, VertexFormat.Mode.QUADS, useWorldBlockLitShader()), Key::create);
    }

    public RenderType renderType(ResourceLocation texture, VertexFormat.Mode drawMode) {
        if (drawMode == VertexFormat.Mode.QUADS) {
            return renderType(texture);
        }
        return CACHE.computeIfAbsent(new Key(this, texture, drawMode, useWorldBlockLitShader()), Key::create);
    }

    public static void clearCachedRenderTypes() {
        CACHE.clear();
    }

    public boolean translucent() {
        return this != CUTOUT_NO_CULL && this != CUTOUT_DOUBLE_SIDED
                && this != CUTOUT_REVERSED_CULL && this != CUTOUT_CULL;
    }

    public RenderModeStatePlan statePlan() {
        return switch (this) {
            case CUTOUT_NO_CULL -> new RenderModeStatePlan(false, BlendFunction.NONE, true, DepthTest.LEQUAL,
                    false, true, true, true);
            case CUTOUT_DOUBLE_SIDED -> new RenderModeStatePlan(false, BlendFunction.NONE, true, DepthTest.LEQUAL,
                    false, true, true, true);
            case CUTOUT_REVERSED_CULL -> new RenderModeStatePlan(false, BlendFunction.NONE, true, DepthTest.LEQUAL,
                    false, false, true, true);
            case CUTOUT_CULL -> new RenderModeStatePlan(false, BlendFunction.NONE, true, DepthTest.LEQUAL,
                    false, false, true, true);
            case TRANSLUCENT -> new RenderModeStatePlan(true, BlendFunction.NORMAL_ALPHA, true, DepthTest.LEQUAL,
                    true, true, true, true);
            case TRANSLUCENT_NO_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.NORMAL_ALPHA, false, DepthTest.LEQUAL,
                    false, true, true, true);
            case TRANSLUCENT_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.NORMAL_ALPHA, true, DepthTest.LEQUAL,
                    false, true, true, true);
            case ADDITIVE_NO_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.ADDITIVE, false, DepthTest.LEQUAL,
                    false, true, true, true);
            case ADDITIVE_CULL_NO_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.ADDITIVE, false, DepthTest.LEQUAL,
                    false, false, true, true);
            case ADDITIVE_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.ADDITIVE, true, DepthTest.LEQUAL,
                    false, true, true, true);
            case GLINT_NO_DEPTH_WRITE -> new RenderModeStatePlan(true, BlendFunction.GLINT, false, DepthTest.LEQUAL,
                    false, true, true, true);
            case GLINT_EQUAL_DEPTH -> new RenderModeStatePlan(true, BlendFunction.GLINT, false, DepthTest.EQUAL,
                    false, true, true, true);
        };
    }

    public LegacyTexturedRenderMode withAlpha(int alpha) {
        return this;
    }

    private static RenderType createCustom(String name, ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite) {
        return createCustom(name, texture, transparency, depthWrite, LEQUAL_DEPTH_TEST, false, VertexFormat.Mode.QUADS,
                useWorldBlockLitShader());
    }

    private static RenderType createCustom(String name, ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite,
            RenderStateShard.DepthTestStateShard depthTest) {
        return createCustom(name, texture, transparency, depthWrite, depthTest, false, VertexFormat.Mode.QUADS,
                useWorldBlockLitShader());
    }

    private static RenderType createCustom(String name, ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency, boolean depthWrite,
            RenderStateShard.DepthTestStateShard depthTest, boolean cull, VertexFormat.Mode drawMode,
            boolean blockLitShader) {
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, drawMode, LEGACY_OBJ_BUFFER_SIZE,
                false, true, RenderType.CompositeState.builder()
                        .setShaderState(!blockLitShader || glintTransparency(transparency)
                                ? new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader)
                                : BLOCK_LIT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setDepthTestState(depthTest)
                        .setCullState(new RenderStateShard.CullStateShard(cull))
                        .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                        .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, depthWrite))
                        .createCompositeState(false));
    }

    private static RenderType createCutout(String name, ResourceLocation texture, boolean cull,
            boolean reversedFrontFace, VertexFormat.Mode drawMode, boolean blockLitShader) {
        RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                .setShaderState(blockLitShader
                        ? BLOCK_LIT_SHADER
                        : new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityCutoutShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(new RenderStateShard.CullStateShard(cull))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true));
        if (reversedFrontFace) {
            builder.setTexturingState(FRONT_FACE_CW);
        }
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, drawMode, LEGACY_OBJ_BUFFER_SIZE,
                true, false, builder.createCompositeState(false));
    }

    private static boolean glintTransparency(RenderStateShard.TransparencyStateShard transparency) {
        return transparency == GLINT_TRANSPARENCY;
    }

    private static boolean useWorldBlockLitShader() {
        // Keep legacy OBJ rendering on vanilla entity shaders until block_lit_static
        // has a separate visual validation pass; mismatched shader state makes large
        // OBJ batches render transparent or corrupted.
        return false;
    }

    private record Key(LegacyTexturedRenderMode mode, ResourceLocation texture, VertexFormat.Mode drawMode,
                       boolean blockLitShader) {
        private RenderType create() {
            String name = "hbm_legacy_textured_" + mode.name().toLowerCase() + "_" + texture.toString()
                    .replace(':', '_').replace('/', '_').replace('.', '_') + "_" + drawMode.name().toLowerCase()
                    + (blockLitShader ? "_block_lit" : "_entity_lit");
            return switch (mode) {
                case CUTOUT_NO_CULL -> createCutout(name, texture, false, false, drawMode, blockLitShader);
                case CUTOUT_DOUBLE_SIDED -> createCutout(name, texture, false, false, drawMode, blockLitShader);
                case CUTOUT_REVERSED_CULL -> createCutout(name, texture, true, true, drawMode, blockLitShader);
                case CUTOUT_CULL -> createCutout(name, texture, true, false, drawMode, blockLitShader);
                case TRANSLUCENT -> createCustom(name, texture, NORMAL_ALPHA_TRANSPARENCY, true, LEQUAL_DEPTH_TEST, false, drawMode, blockLitShader);
                case TRANSLUCENT_NO_DEPTH_WRITE -> createCustom(name, texture, NORMAL_ALPHA_TRANSPARENCY, false, LEQUAL_DEPTH_TEST, false, drawMode, blockLitShader);
                case TRANSLUCENT_DEPTH_WRITE -> createCustom(name, texture, NORMAL_ALPHA_TRANSPARENCY, true, LEQUAL_DEPTH_TEST, false, drawMode, blockLitShader);
                case ADDITIVE_NO_DEPTH_WRITE -> createCustom(name, texture, ADDITIVE_TRANSPARENCY, false, LEQUAL_DEPTH_TEST, false, drawMode, blockLitShader);
                case ADDITIVE_CULL_NO_DEPTH_WRITE -> createCustom(name, texture, ADDITIVE_TRANSPARENCY, false, LEQUAL_DEPTH_TEST, true, drawMode, blockLitShader);
                case ADDITIVE_DEPTH_WRITE -> createCustom(name, texture, ADDITIVE_TRANSPARENCY, true, LEQUAL_DEPTH_TEST, false, drawMode, blockLitShader);
                case GLINT_NO_DEPTH_WRITE -> createCustom(name, texture, GLINT_TRANSPARENCY, false, LEQUAL_DEPTH_TEST, false, drawMode, false);
                case GLINT_EQUAL_DEPTH -> createCustom(name, texture, GLINT_TRANSPARENCY, false, EQUAL_DEPTH_TEST, false, drawMode, false);
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

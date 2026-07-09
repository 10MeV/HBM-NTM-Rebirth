package com.hbm.ntm.client.render.shader;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.HbmGlVaoSafety;
import com.hbm.ntm.client.render.HbmRenderFrameLight;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Small persistent Iris/Oculus draw batch for raw companion meshes.
 *
 * <p>The active batch is scoped by the caller-provided render state key and
 * shader instance. It keeps the RenderType state, Iris phase guard, neutral
 * block-entity id, and ExtendedShader apply active across compatible mesh
 * draws, then closes at backend flush/frame boundaries.
 */
public final class HbmIrisRenderBatch {
    private static ActiveBatch activeBatch;
    private static long beginCalls;
    private static long reuseHits;
    private static long endCalls;
    private static long drawCalls;
    private static long shadowDrawCalls;
    private static long applyFailures;
    private static long shaderRestoreAttempts;
    private static long shaderRestoreSuccesses;
    private static long shaderRestoreFailures;
    private static long currentFrameBeginCalls;
    private static long currentFrameReuseHits;
    private static long currentFrameEndCalls;
    private static long currentFrameDrawCalls;
    private static long currentFrameShadowDrawCalls;
    private static long currentFrameApplyFailures;
    private static long currentFrameShaderRestoreAttempts;
    private static long currentFrameShaderRestoreSuccesses;
    private static long currentFrameShaderRestoreFailures;
    private static long lastFrameBeginCalls;
    private static long lastFrameReuseHits;
    private static long lastFrameEndCalls;
    private static long lastFrameDrawCalls;
    private static long lastFrameShadowDrawCalls;
    private static long lastFrameApplyFailures;
    private static long lastFrameShaderRestoreAttempts;
    private static long lastFrameShaderRestoreSuccesses;
    private static long lastFrameShaderRestoreFailures;
    private static volatile String currentFrameApplyFailureReason = "none";
    private static volatile String lastFrameApplyFailureReason = "none";
    private static int cachedShaderProgram = -1;
    private static ShaderInstance cachedShaderInstance;
    private static long cachedPipelineGeneration = -1L;
    private static HbmIrisDerivedMatrixUniforms.Locations matrixLocations =
            HbmIrisDerivedMatrixUniforms.Locations.NONE;
    private static ShaderInstance cachedCommonUniformShader;
    private static int cachedCommonUniformProgram = -1;
    private static long cachedCommonUniformPipelineGeneration = -1L;
    private static Uniform fogStartUniform;
    private static Uniform fogEndUniform;
    private static Uniform fogColorUniform;
    private static ShaderInstance cachedCommonUniformValueShader;
    private static int cachedCommonUniformValueProgram = -1;
    private static long cachedCommonUniformValuePipelineGeneration = -1L;
    private static final Matrix4f LAST_PROJECTION_MATRIX = new Matrix4f();
    private static boolean projectionUniformValid;
    private static boolean fogStartUniformValid;
    private static boolean fogEndUniformValid;
    private static boolean fogColorUniformValid;
    private static float lastFogStart;
    private static float lastFogEnd;
    private static int lastFogColorRed = Integer.MIN_VALUE;
    private static int lastFogColorGreen = Integer.MIN_VALUE;
    private static int lastFogColorBlue = Integer.MIN_VALUE;
    private static int lastFogColorAlpha = Integer.MIN_VALUE;
    private static final Matrix4f MATRIX_INVERSE = new Matrix4f();
    private static final Matrix3f NORMAL_MATRIX = new Matrix3f();
    private static final float[] MODEL_VIEW_FLOATS = new float[16];
    private static final float[] INVERSE_FLOATS = new float[16];
    private static final float[] NORMAL_FLOATS = new float[9];
    private static final float[] LAST_MODEL_VIEW_FLOATS = new float[16];
    private static boolean lastModelViewValid;
    private static int lastConstantColorLocation = -1;
    private static int lastConstantColorRed = Integer.MIN_VALUE;
    private static int lastConstantColorGreen = Integer.MIN_VALUE;
    private static int lastConstantColorBlue = Integer.MIN_VALUE;
    private static int lastConstantColorAlpha = Integer.MIN_VALUE;
    private static int lastConstantOverlayLocation = -1;
    private static int lastConstantOverlayU = Integer.MIN_VALUE;
    private static int lastConstantOverlayV = Integer.MIN_VALUE;
    private static int lastConstantLightmapLocation = -1;
    private static int lastConstantLightmapBlock = Integer.MIN_VALUE;
    private static int lastConstantLightmapSky = Integer.MIN_VALUE;
    private static boolean activeShaderProgramKnownCurrent;

    private HbmIrisRenderBatch() {
    }

    public static boolean begin(Object stateKey, RenderType renderType, ShaderInstance shader) {
        return begin(stateKey, renderType, shader, null);
    }

    public static boolean begin(Object stateKey, RenderType renderType, ShaderInstance shader,
            Matrix4f projectionMatrix) {
        return begin(stateKey, renderType, shader, projectionMatrix, false);
    }

    public static boolean begin(Object stateKey, RenderType renderType, ShaderInstance shader,
            Matrix4f projectionMatrix, boolean shadowPass) {
        return begin(stateKey, renderType, shader, projectionMatrix, shadowPass, TextureAtlas.LOCATION_BLOCKS);
    }

    public static boolean begin(Object stateKey, RenderType renderType, ShaderInstance shader,
            Matrix4f projectionMatrix, boolean shadowPass, ResourceLocation baseTexture) {
        if (renderType == null || shader == null) {
            return false;
        }
        ResourceLocation resolvedBaseTexture = baseTexture != null ? baseTexture : TextureAtlas.LOCATION_BLOCKS;
        Matrix4f projectionSource = projectionMatrix != null ? projectionMatrix : RenderSystem.getProjectionMatrix();
        long pipelineGeneration = HbmShaderCompatibilityDetector.pipelineGeneration();
        ActiveBatch active = activeBatch;
        if (active != null && active.matches(stateKey, shader, projectionSource, pipelineGeneration, shadowPass,
                resolvedBaseTexture)) {
            HbmIrisExtendedShaderAccess.setCurrentRenderedBlockEntity(0);
            reuseHits++;
            currentFrameReuseHits++;
            return true;
        }
        endActiveBatch();
        Matrix4f resolvedProjection = projectionSource;
        int previousVao = HbmGlVaoSafety.currentBinding();
        int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        boolean previousCullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean previousDepthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean previousDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int previousDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean previousBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int previousBlendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int previousBlendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int previousBlendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int previousBlendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        int previousBlockEntityId = HbmIrisExtendedShaderAccess.setCurrentRenderedBlockEntity(0);
        HbmIrisPhaseGuard phaseGuard = HbmIrisPhaseGuard.pushBlockEntities();
        boolean renderStateSet = false;
        try {
            renderType.setupRenderState();
            renderStateSet = true;
            HbmRenderFrameLight.ensureLightTextureUpdated();
            setupIrisSamplersAndUniforms(shader, resolvedProjection, resolvedBaseTexture);
            RenderSystem.setShader(() -> shader);
            if (!HbmIrisShaderApply.tryApply(shader)) {
                recordApplyFailure();
                activeShaderProgramKnownCurrent = false;
                return false;
            }
            HbmRenderFrameLight.bindBlockLitSamplerTextures(shader, resolvedBaseTexture);
            activeShaderProgramKnownCurrent = true;
            resolveMatrixLocations(shader);
            resetDrawAttributeCache();
            resetDrawMatrixCache();
            activeBatch = REUSABLE_ACTIVE_BATCH.activate(stateKey, renderType, shader, phaseGuard,
                    previousBlockEntityId, previousVao, previousArrayBuffer, previousActiveTexture,
                    previousCullEnabled, previousDepthTestEnabled, previousDepthMask, previousDepthFunc,
                    previousBlendEnabled, previousBlendSrcRgb, previousBlendDstRgb,
                    previousBlendSrcAlpha, previousBlendDstAlpha, resolvedProjection, pipelineGeneration,
                    shadowPass, resolvedBaseTexture);
            beginCalls++;
            currentFrameBeginCalls++;
            return true;
        } finally {
            if (activeBatch == null) {
                activeShaderProgramKnownCurrent = false;
                tryRestoreState("begin rollback", renderStateSet ? renderType : null, phaseGuard,
                        previousBlockEntityId, previousVao, previousArrayBuffer, previousActiveTexture,
                        previousCullEnabled, previousDepthTestEnabled, previousDepthMask, previousDepthFunc,
                        previousBlendEnabled, previousBlendSrcRgb, previousBlendDstRgb,
                        previousBlendSrcAlpha, previousBlendDstAlpha);
            }
        }
    }

    public static void recordDraw(boolean shadowPass) {
        drawCalls++;
        currentFrameDrawCalls++;
        if (shadowPass) {
            shadowDrawCalls++;
            currentFrameShadowDrawCalls++;
        }
    }

    public static boolean isActive() {
        return activeBatch != null;
    }

    public static boolean isActiveShadowPass() {
        ActiveBatch active = activeBatch;
        return active != null && active.shadowPass();
    }

    public static boolean prepareCompanionDraw() {
        ActiveBatch active = activeBatch;
        if (active == null) {
            return true;
        }
        // Iris updates the captured BE id before each BER call; keep companion draws neutral.
        HbmIrisExtendedShaderAccess.setCurrentRenderedBlockEntity(0);
        if (activeShaderBindingDrifted(active)) {
            recordShaderRestoreAttempt();
            try {
                RenderSystem.setShader(active::shader);
                setupIrisSamplersAndUniforms(active.shader(), active.projectionMatrix(), active.baseTexture());
                if (!HbmIrisShaderApply.tryApply(active.shader())) {
                    recordApplyFailure();
                    recordShaderRestoreFailure();
                    activeShaderProgramKnownCurrent = false;
                    HbmNtm.LOGGER.warn("HBM Iris/Oculus companion shader restore failed before draw");
                    return false;
                }
                activeShaderProgramKnownCurrent = true;
                resolveMatrixLocations(active.shader());
                recordShaderRestoreSuccess();
            } catch (Throwable throwable) {
                HbmIrisShaderApply.rememberFailure(throwable);
                recordApplyFailure();
                recordShaderRestoreFailure();
                activeShaderProgramKnownCurrent = false;
                HbmNtm.LOGGER.warn("HBM Iris/Oculus companion shader restore threw before draw: {}",
                        throwable.toString());
                return false;
            } finally {
                resetDrawAttributeCache();
                resetDrawMatrixCache();
            }
        }
        // Persistent batches can span vanilla/Iris work that mutates texture units.
        HbmRenderFrameLight.bindBlockLitSamplerTextures(active.shader(), active.baseTexture());
        return true;
    }

    private static boolean activeShaderBindingDrifted(ActiveBatch active) {
        if (active.pipelineGeneration() != HbmShaderCompatibilityDetector.pipelineGeneration()) {
            activeShaderProgramKnownCurrent = false;
            return true;
        }
        if (RenderSystem.getShader() != active.shader()) {
            activeShaderProgramKnownCurrent = false;
            return true;
        }
        int activeProgram = active.shader().getId();
        if (activeProgram <= 0) {
            activeShaderProgramKnownCurrent = false;
            return true;
        }
        if (activeShaderProgramKnownCurrent) {
            return false;
        }
        boolean current = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) == activeProgram;
        activeShaderProgramKnownCurrent = current;
        return !current;
    }

    public static void endActiveBatch() {
        ActiveBatch active = activeBatch;
        if (active == null) {
            return;
        }
        activeBatch = null;
        activeShaderProgramKnownCurrent = false;
        try {
            try {
                active.shader().clear();
            } catch (Throwable throwable) {
                HbmNtm.LOGGER.debug("HBM Iris/Oculus shader clear failed: {}", throwable.toString());
            }
        } finally {
            tryRestoreState("batch close", active.renderType(), active.phaseGuard(), active.previousBlockEntityId(),
                    active.previousVao(), active.previousArrayBuffer(), active.previousActiveTexture(),
                    active.previousCullEnabled(), active.previousDepthTestEnabled(), active.previousDepthMask(), active.previousDepthFunc(),
                    active.previousBlendEnabled(), active.previousBlendSrcRgb(), active.previousBlendDstRgb(),
                    active.previousBlendSrcAlpha(), active.previousBlendDstAlpha());
        }
        active.clearAfterClose();
        endCalls++;
        currentFrameEndCalls++;
    }

    /**
     * Modernized parity hook for closing persistent Iris/Oculus companion batches
     * at world-render stage boundaries.
     */
    public static void closePersistentIfActive() {
        endActiveBatch();
    }

    /**
     * Runs a short vanilla draw while a persistent Iris/Oculus companion batch is open.
     *
     * <p>Some vanilla/item overlays expect VAO 0 and their own shader program. Re-apply
     * the active ExtendedShader afterward so the next companion draw keeps using the
     * current shader-pack program instead of the temporary vanilla shader.
     */
    public static void runVanillaOverlay(Runnable draw) {
        ActiveBatch active = activeBatch;
        if (active == null || active.shader() == null) {
            draw.run();
            return;
        }
        long activeGeneration = active.generation();
        int previousVao = HbmGlVaoSafety.currentBinding();
        int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        boolean previousCullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean previousDepthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean previousDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int previousDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean previousBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int previousBlendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int previousBlendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int previousBlendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int previousBlendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        try {
            HbmGlVaoSafety.bindVertexArray(0);
            activeShaderProgramKnownCurrent = false;
            RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
            draw.run();
        } finally {
            Throwable failure = null;
            if (activeBatch == active && active.generation() == activeGeneration && active.shader() != null) {
                recordShaderRestoreAttempt();
                try {
                    RenderSystem.setShader(active::shader);
                    setupIrisSamplersAndUniforms(active.shader(), active.projectionMatrix(), active.baseTexture());
                    boolean restored = HbmIrisShaderApply.tryApply(active.shader());
                    if (restored && activeBatch == active && active.generation() == activeGeneration) {
                        HbmRenderFrameLight.bindBlockLitSamplerTextures(active.shader(), active.baseTexture());
                        activeShaderProgramKnownCurrent = true;
                        resolveMatrixLocations(active.shader());
                        recordShaderRestoreSuccess();
                    } else {
                        recordApplyFailure();
                        recordShaderRestoreFailure();
                        activeShaderProgramKnownCurrent = false;
                        HbmNtm.LOGGER.warn("HBM Iris/Oculus vanilla overlay shader restore failed");
                    }
                } catch (Throwable throwable) {
                    HbmIrisShaderApply.rememberFailure(throwable);
                    recordApplyFailure();
                    recordShaderRestoreFailure();
                    activeShaderProgramKnownCurrent = false;
                    failure = rememberRestoreFailure(failure, throwable);
                } finally {
                    resetDrawAttributeCache();
                    resetDrawMatrixCache();
                }
            }
            try {
                if (previousCullEnabled) {
                    RenderSystem.enableCull();
                } else {
                    RenderSystem.disableCull();
                }
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                if (previousDepthTestEnabled) {
                    RenderSystem.enableDepthTest();
                } else {
                    RenderSystem.disableDepthTest();
                }
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                RenderSystem.depthMask(previousDepthMask);
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                RenderSystem.depthFunc(previousDepthFunc);
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                RenderSystem.blendFuncSeparate(previousBlendSrcRgb, previousBlendDstRgb,
                        previousBlendSrcAlpha, previousBlendDstAlpha);
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                if (previousBlendEnabled) {
                    RenderSystem.enableBlend();
                } else {
                    RenderSystem.disableBlend();
                }
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                HbmGlVaoSafety.bindVertexArray(previousVao);
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            try {
                RenderSystem.activeTexture(previousActiveTexture);
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
            if (failure != null) {
                HbmNtm.LOGGER.error("HBM Iris/Oculus vanilla overlay state restore failed", failure);
            }
        }
    }

    public static void invalidateCaches() {
        endActiveBatch();
        cachedShaderProgram = -1;
        cachedShaderInstance = null;
        cachedPipelineGeneration = -1L;
        activeShaderProgramKnownCurrent = false;
        matrixLocations = HbmIrisDerivedMatrixUniforms.Locations.NONE;
        clearCommonUniformCache();
        clearCommonUniformValueCache();
        resetDrawAttributeCache();
        resetDrawMatrixCache();
    }

    public static void endFrame() {
        closePersistentIfActive();
        HbmIrisPhaseGuard.endFrame();
        long frameBeginCalls = currentFrameBeginCalls;
        long frameReuseHits = currentFrameReuseHits;
        long frameEndCalls = currentFrameEndCalls;
        long frameDrawCalls = currentFrameDrawCalls;
        long frameShadowDrawCalls = currentFrameShadowDrawCalls;
        long frameApplyFailures = currentFrameApplyFailures;
        long frameShaderRestoreAttempts = currentFrameShaderRestoreAttempts;
        long frameShaderRestoreSuccesses = currentFrameShaderRestoreSuccesses;
        long frameShaderRestoreFailures = currentFrameShaderRestoreFailures;
        currentFrameBeginCalls = 0L;
        currentFrameReuseHits = 0L;
        currentFrameEndCalls = 0L;
        currentFrameDrawCalls = 0L;
        currentFrameShadowDrawCalls = 0L;
        currentFrameApplyFailures = 0L;
        currentFrameShaderRestoreAttempts = 0L;
        currentFrameShaderRestoreSuccesses = 0L;
        currentFrameShaderRestoreFailures = 0L;
        lastFrameBeginCalls = frameBeginCalls;
        lastFrameReuseHits = frameReuseHits;
        lastFrameEndCalls = frameEndCalls;
        lastFrameDrawCalls = frameDrawCalls;
        lastFrameShadowDrawCalls = frameShadowDrawCalls;
        lastFrameApplyFailures = frameApplyFailures;
        lastFrameShaderRestoreAttempts = frameShaderRestoreAttempts;
        lastFrameShaderRestoreSuccesses = frameShaderRestoreSuccesses;
        lastFrameShaderRestoreFailures = frameShaderRestoreFailures;
        lastFrameApplyFailureReason = frameApplyFailures > 0L ? currentFrameApplyFailureReason : "none";
        currentFrameApplyFailureReason = "none";
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                activeBatch != null,
                beginCalls,
                reuseHits,
                endCalls,
                drawCalls,
                shadowDrawCalls,
                applyFailures,
                shaderRestoreAttempts,
                shaderRestoreSuccesses,
                shaderRestoreFailures,
                currentFrameBeginCalls,
                currentFrameReuseHits,
                currentFrameEndCalls,
                currentFrameDrawCalls,
                currentFrameShadowDrawCalls,
                currentFrameApplyFailures,
                currentFrameShaderRestoreAttempts,
                currentFrameShaderRestoreSuccesses,
                currentFrameShaderRestoreFailures,
                lastFrameBeginCalls,
                lastFrameReuseHits,
                lastFrameEndCalls,
                lastFrameDrawCalls,
                lastFrameShadowDrawCalls,
                lastFrameApplyFailures,
                lastFrameShaderRestoreAttempts,
                lastFrameShaderRestoreSuccesses,
                lastFrameShaderRestoreFailures,
                lastFrameApplyFailureReason);
    }

    private static void recordApplyFailure() {
        applyFailures++;
        currentFrameApplyFailures++;
        currentFrameApplyFailureReason = HbmIrisShaderApply.lastFailureReason();
    }

    private static void recordShaderRestoreAttempt() {
        shaderRestoreAttempts++;
        currentFrameShaderRestoreAttempts++;
    }

    private static void recordShaderRestoreSuccess() {
        shaderRestoreSuccesses++;
        currentFrameShaderRestoreSuccesses++;
    }

    private static void recordShaderRestoreFailure() {
        shaderRestoreFailures++;
        currentFrameShaderRestoreFailures++;
    }

    public static void uploadDrawMatrices(Matrix4f modelView) {
        ActiveBatch active = activeBatch;
        if (active == null || active.shader() == null || modelView == null) {
            return;
        }
        HbmIrisDerivedMatrixUniforms.Locations locations = resolveMatrixLocations(active.shader());
        if (locations == HbmIrisDerivedMatrixUniforms.Locations.NONE) {
            return;
        }
        modelView.get(MODEL_VIEW_FLOATS);
        boolean modelViewChanged = !lastModelViewValid || !matrixEquals(MODEL_VIEW_FLOATS, LAST_MODEL_VIEW_FLOATS);
        boolean linearChanged = !lastModelViewValid || !linearPartEquals(MODEL_VIEW_FLOATS, LAST_MODEL_VIEW_FLOATS);
        if (locations.modelView() >= 0 && modelViewChanged) {
            GL20.glUniformMatrix4fv(locations.modelView(), false, MODEL_VIEW_FLOATS);
        }
        boolean haveInverse = false;
        if (locations.modelViewInverse() >= 0 && modelViewChanged) {
            MATRIX_INVERSE.set(MODEL_VIEW_FLOATS).invertAffine();
            MATRIX_INVERSE.get(INVERSE_FLOATS);
            GL20.glUniformMatrix4fv(locations.modelViewInverse(), false, INVERSE_FLOATS);
            haveInverse = true;
        }
        if (locations.normalMat() >= 0 && linearChanged) {
            if (haveInverse) {
                NORMAL_MATRIX.set(MATRIX_INVERSE).transpose();
            } else {
                NORMAL_MATRIX.set(
                        MODEL_VIEW_FLOATS[0], MODEL_VIEW_FLOATS[1], MODEL_VIEW_FLOATS[2],
                        MODEL_VIEW_FLOATS[4], MODEL_VIEW_FLOATS[5], MODEL_VIEW_FLOATS[6],
                        MODEL_VIEW_FLOATS[8], MODEL_VIEW_FLOATS[9], MODEL_VIEW_FLOATS[10])
                        .invert().transpose();
            }
            NORMAL_MATRIX.get(NORMAL_FLOATS);
            GL20.glUniformMatrix3fv(locations.normalMat(), false, NORMAL_FLOATS);
        }
        if (modelViewChanged) {
            System.arraycopy(MODEL_VIEW_FLOATS, 0, LAST_MODEL_VIEW_FLOATS, 0, MODEL_VIEW_FLOATS.length);
            lastModelViewValid = true;
        }
    }

    public static void applyConstantLightmap(int uv2Location, int packedLight) {
        if (uv2Location < 0) {
            return;
        }
        int block = Math.max(0, Math.min(240, LightTexture.block(packedLight) * 16));
        int sky = Math.max(0, Math.min(240, LightTexture.sky(packedLight) * 16));
        boolean cacheable = activeBatch != null;
        if (!cacheable
                || uv2Location != lastConstantLightmapLocation
                || block != lastConstantLightmapBlock
                || sky != lastConstantLightmapSky) {
            GL30.glVertexAttribI2i(uv2Location, block, sky);
            if (cacheable) {
                lastConstantLightmapLocation = uv2Location;
                lastConstantLightmapBlock = block;
                lastConstantLightmapSky = sky;
            }
        }
    }

    public static void applyConstantColor(int colorLocation, int red, int green, int blue, int alpha) {
        if (colorLocation < 0) {
            return;
        }
        boolean cacheable = activeBatch != null;
        if (!cacheable
                || colorLocation != lastConstantColorLocation
                || red != lastConstantColorRed
                || green != lastConstantColorGreen
                || blue != lastConstantColorBlue
                || alpha != lastConstantColorAlpha) {
            GL20.glVertexAttrib4f(colorLocation, red / 255.0F, green / 255.0F, blue / 255.0F, alpha / 255.0F);
            if (cacheable) {
                lastConstantColorLocation = colorLocation;
                lastConstantColorRed = red;
                lastConstantColorGreen = green;
                lastConstantColorBlue = blue;
                lastConstantColorAlpha = alpha;
            }
        }
    }

    public static void applyConstantOverlay(int uv1Location, int packedOverlay) {
        if (uv1Location < 0) {
            return;
        }
        int overlayU = packedOverlay & 0xFFFF;
        int overlayV = packedOverlay >>> 16 & 0xFFFF;
        boolean cacheable = activeBatch != null;
        if (!cacheable
                || uv1Location != lastConstantOverlayLocation
                || overlayU != lastConstantOverlayU
                || overlayV != lastConstantOverlayV) {
            GL30.glVertexAttribI2i(uv1Location, overlayU, overlayV);
            if (cacheable) {
                lastConstantOverlayLocation = uv1Location;
                lastConstantOverlayU = overlayU;
                lastConstantOverlayV = overlayV;
            }
        }
    }

    public static void invalidateLightmapAttributeCache() {
        lastConstantLightmapLocation = -1;
        lastConstantLightmapBlock = Integer.MIN_VALUE;
        lastConstantLightmapSky = Integer.MIN_VALUE;
    }

    private static void invalidateColorAttributeCache() {
        lastConstantColorLocation = -1;
        lastConstantColorRed = Integer.MIN_VALUE;
        lastConstantColorGreen = Integer.MIN_VALUE;
        lastConstantColorBlue = Integer.MIN_VALUE;
        lastConstantColorAlpha = Integer.MIN_VALUE;
    }

    private static void invalidateOverlayAttributeCache() {
        lastConstantOverlayLocation = -1;
        lastConstantOverlayU = Integer.MIN_VALUE;
        lastConstantOverlayV = Integer.MIN_VALUE;
    }

    private static void resetDrawAttributeCache() {
        invalidateColorAttributeCache();
        invalidateOverlayAttributeCache();
        invalidateLightmapAttributeCache();
    }

    private static void resetDrawMatrixCache() {
        lastModelViewValid = false;
    }

    private static void tryRestoreState(String context, RenderType renderType, HbmIrisPhaseGuard phaseGuard,
            int previousBlockEntityId, int previousVao, int previousArrayBuffer, int previousActiveTexture,
            boolean previousCullEnabled, boolean previousDepthTestEnabled, boolean previousDepthMask, int previousDepthFunc,
            boolean previousBlendEnabled, int previousBlendSrcRgb, int previousBlendDstRgb,
            int previousBlendSrcAlpha, int previousBlendDstAlpha) {
        Throwable failure = null;
        if (renderType != null) {
            try {
                renderType.clearRenderState();
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
        }
        if (phaseGuard != null) {
            try {
                phaseGuard.close();
            } catch (Throwable throwable) {
                failure = rememberRestoreFailure(failure, throwable);
            }
        }
        try {
            HbmIrisExtendedShaderAccess.restoreCurrentRenderedBlockEntity(previousBlockEntityId);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            HbmGlVaoSafety.bindVertexArray(previousVao);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            if (previousCullEnabled) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            if (previousDepthTestEnabled) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            RenderSystem.depthMask(previousDepthMask);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            RenderSystem.depthFunc(previousDepthFunc);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            RenderSystem.blendFuncSeparate(previousBlendSrcRgb, previousBlendDstRgb,
                    previousBlendSrcAlpha, previousBlendDstAlpha);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            if (previousBlendEnabled) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        }
        try {
            RenderSystem.activeTexture(previousActiveTexture);
        } catch (Throwable throwable) {
            failure = rememberRestoreFailure(failure, throwable);
        } finally {
            resetDrawAttributeCache();
        }
        if (failure != null) {
            HbmNtm.LOGGER.error("HBM Iris/Oculus {} state restore failed", context, failure);
        }
    }

    private static Throwable rememberRestoreFailure(Throwable first, Throwable failure) {
        if (first == null) {
            return failure;
        }
        first.addSuppressed(failure);
        return first;
    }

    private static HbmIrisDerivedMatrixUniforms.Locations resolveMatrixLocations(ShaderInstance shader) {
        if (shader == null || shader.getId() <= 0) {
            matrixLocations = HbmIrisDerivedMatrixUniforms.Locations.NONE;
            return matrixLocations;
        }
        int program = shader.getId();
        long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
        if (cachedShaderProgram != program
                || cachedShaderInstance != shader
                || cachedPipelineGeneration != generation) {
            cachedShaderProgram = program;
            cachedShaderInstance = shader;
            cachedPipelineGeneration = generation;
            matrixLocations = HbmIrisDerivedMatrixUniforms.resolve(shader);
        }
        return matrixLocations;
    }

    private static void setupIrisSamplersAndUniforms(ShaderInstance shader) {
        setupIrisSamplersAndUniforms(shader, RenderSystem.getProjectionMatrix(), TextureAtlas.LOCATION_BLOCKS);
    }

    private static void setupIrisSamplersAndUniforms(ShaderInstance shader, Matrix4f projectionMatrix) {
        setupIrisSamplersAndUniforms(shader, projectionMatrix, TextureAtlas.LOCATION_BLOCKS);
    }

    private static void setupIrisSamplersAndUniforms(ShaderInstance shader, Matrix4f projectionMatrix,
            ResourceLocation baseTexture) {
        Matrix4f resolvedProjection = projectionMatrix != null ? projectionMatrix : RenderSystem.getProjectionMatrix();
        updateCommonUniformValueOwner(shader);
        if (shader.PROJECTION_MATRIX != null) {
            setProjectionUniformIfChanged(shader.PROJECTION_MATRIX, resolvedProjection);
        }
        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(IDENTITY);
        }
        updateCommonUniformCache(shader);
        if (fogStartUniform != null) {
            setFogStartUniformIfChanged(RenderSystem.getShaderFogStart());
        }
        if (fogEndUniform != null) {
            setFogEndUniformIfChanged(RenderSystem.getShaderFogEnd());
        }
        if (fogColorUniform != null) {
            float[] color = RenderSystem.getShaderFogColor();
            setFogColorUniformIfChanged(color);
        }
        HbmRenderFrameLight.prepareBlockLitSamplers(shader, baseTexture);
    }

    private static void updateCommonUniformValueOwner(ShaderInstance shader) {
        int program = shader == null ? -1 : shader.getId();
        long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
        if (cachedCommonUniformValueShader == shader
                && cachedCommonUniformValueProgram == program
                && cachedCommonUniformValuePipelineGeneration == generation
                && cachedCommonUniformValueShader != null) {
            return;
        }
        cachedCommonUniformValueShader = shader;
        cachedCommonUniformValueProgram = program;
        cachedCommonUniformValuePipelineGeneration = generation;
        clearCommonUniformValueCache();
    }

    private static void setProjectionUniformIfChanged(Uniform uniform, Matrix4f projectionMatrix) {
        if (!projectionUniformValid || !LAST_PROJECTION_MATRIX.equals(projectionMatrix)) {
            uniform.set(projectionMatrix);
            LAST_PROJECTION_MATRIX.set(projectionMatrix);
            projectionUniformValid = true;
        }
    }

    private static void setFogStartUniformIfChanged(float fogStart) {
        if (!fogStartUniformValid
                || Float.floatToIntBits(lastFogStart) != Float.floatToIntBits(fogStart)) {
            fogStartUniform.set(fogStart);
            lastFogStart = fogStart;
            fogStartUniformValid = true;
        }
    }

    private static void setFogEndUniformIfChanged(float fogEnd) {
        if (!fogEndUniformValid
                || Float.floatToIntBits(lastFogEnd) != Float.floatToIntBits(fogEnd)) {
            fogEndUniform.set(fogEnd);
            lastFogEnd = fogEnd;
            fogEndUniformValid = true;
        }
    }

    private static void setFogColorUniformIfChanged(float[] color) {
        int red = Float.floatToIntBits(color[0]);
        int green = Float.floatToIntBits(color[1]);
        int blue = Float.floatToIntBits(color[2]);
        int alpha = Float.floatToIntBits(color[3]);
        if (!fogColorUniformValid
                || lastFogColorRed != red
                || lastFogColorGreen != green
                || lastFogColorBlue != blue
                || lastFogColorAlpha != alpha) {
            fogColorUniform.set(color[0], color[1], color[2], color[3]);
            lastFogColorRed = red;
            lastFogColorGreen = green;
            lastFogColorBlue = blue;
            lastFogColorAlpha = alpha;
            fogColorUniformValid = true;
        }
    }

    private static void updateCommonUniformCache(ShaderInstance shader) {
        int program = shader.getId();
        long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
        if (cachedCommonUniformShader == shader
                && cachedCommonUniformProgram == program
                && cachedCommonUniformPipelineGeneration == generation) {
            return;
        }
        cachedCommonUniformShader = shader;
        cachedCommonUniformProgram = program;
        cachedCommonUniformPipelineGeneration = generation;
        fogStartUniform = shader.getUniform("FogStart");
        fogEndUniform = shader.getUniform("FogEnd");
        fogColorUniform = shader.getUniform("FogColor");
    }

    private static void clearCommonUniformCache() {
        cachedCommonUniformShader = null;
        cachedCommonUniformProgram = -1;
        cachedCommonUniformPipelineGeneration = -1L;
        fogStartUniform = null;
        fogEndUniform = null;
        fogColorUniform = null;
    }

    private static void clearCommonUniformValueCache() {
        projectionUniformValid = false;
        fogStartUniformValid = false;
        fogEndUniformValid = false;
        fogColorUniformValid = false;
        lastFogStart = 0.0F;
        lastFogEnd = 0.0F;
        lastFogColorRed = Integer.MIN_VALUE;
        lastFogColorGreen = Integer.MIN_VALUE;
        lastFogColorBlue = Integer.MIN_VALUE;
        lastFogColorAlpha = Integer.MIN_VALUE;
    }

    private static final Matrix4f IDENTITY = new Matrix4f();

    private static final ActiveBatch REUSABLE_ACTIVE_BATCH = new ActiveBatch();

    private static final class ActiveBatch {
        private Object stateKey;
        private RenderType renderType;
        private ShaderInstance shader;
        private HbmIrisPhaseGuard phaseGuard;
        private int previousBlockEntityId;
        private int previousVao;
        private int previousArrayBuffer;
        private int previousActiveTexture;
        private boolean previousCullEnabled;
        private boolean previousDepthTestEnabled;
        private boolean previousDepthMask;
        private int previousDepthFunc;
        private boolean previousBlendEnabled;
        private int previousBlendSrcRgb;
        private int previousBlendDstRgb;
        private int previousBlendSrcAlpha;
        private int previousBlendDstAlpha;
        private final Matrix4f projectionMatrix = new Matrix4f();
        private long pipelineGeneration;
        private boolean shadowPass;
        private ResourceLocation baseTexture;
        private long generation;

        private ActiveBatch activate(Object stateKey, RenderType renderType, ShaderInstance shader,
                HbmIrisPhaseGuard phaseGuard, int previousBlockEntityId, int previousVao,
                int previousArrayBuffer, int previousActiveTexture, boolean previousCullEnabled,
                boolean previousDepthTestEnabled, boolean previousDepthMask, int previousDepthFunc,
                boolean previousBlendEnabled, int previousBlendSrcRgb, int previousBlendDstRgb,
                int previousBlendSrcAlpha, int previousBlendDstAlpha, Matrix4f projectionMatrix,
                long pipelineGeneration, boolean shadowPass, ResourceLocation baseTexture) {
            this.generation++;
            this.stateKey = stateKey;
            this.renderType = renderType;
            this.shader = shader;
            this.phaseGuard = phaseGuard;
            this.previousBlockEntityId = previousBlockEntityId;
            this.previousVao = previousVao;
            this.previousArrayBuffer = previousArrayBuffer;
            this.previousActiveTexture = previousActiveTexture;
            this.previousCullEnabled = previousCullEnabled;
            this.previousDepthTestEnabled = previousDepthTestEnabled;
            this.previousDepthMask = previousDepthMask;
            this.previousDepthFunc = previousDepthFunc;
            this.previousBlendEnabled = previousBlendEnabled;
            this.previousBlendSrcRgb = previousBlendSrcRgb;
            this.previousBlendDstRgb = previousBlendDstRgb;
            this.previousBlendSrcAlpha = previousBlendSrcAlpha;
            this.previousBlendDstAlpha = previousBlendDstAlpha;
            this.projectionMatrix.set(projectionMatrix);
            this.pipelineGeneration = pipelineGeneration;
            this.shadowPass = shadowPass;
            this.baseTexture = baseTexture;
            return this;
        }

        private void clearAfterClose() {
            this.stateKey = null;
            this.renderType = null;
            this.shader = null;
            this.phaseGuard = null;
            this.baseTexture = null;
        }

        private boolean matches(Object stateKey, ShaderInstance shader, Matrix4f projectionMatrix,
                long pipelineGeneration, boolean shadowPass, ResourceLocation baseTexture) {
            return this.shader == shader
                    && Objects.equals(this.stateKey, stateKey)
                    && this.pipelineGeneration == pipelineGeneration
                    && this.shadowPass == shadowPass
                    && Objects.equals(this.baseTexture, baseTexture)
                    && matrixEquals(this.projectionMatrix, projectionMatrix);
        }

        private Object stateKey() {
            return stateKey;
        }

        private RenderType renderType() {
            return renderType;
        }

        private ShaderInstance shader() {
            return shader;
        }

        private HbmIrisPhaseGuard phaseGuard() {
            return phaseGuard;
        }

        private int previousBlockEntityId() {
            return previousBlockEntityId;
        }

        private int previousVao() {
            return previousVao;
        }

        private int previousArrayBuffer() {
            return previousArrayBuffer;
        }

        private int previousActiveTexture() {
            return previousActiveTexture;
        }

        private boolean previousCullEnabled() {
            return previousCullEnabled;
        }

        private boolean previousDepthTestEnabled() {
            return previousDepthTestEnabled;
        }

        private boolean previousDepthMask() {
            return previousDepthMask;
        }

        private int previousDepthFunc() {
            return previousDepthFunc;
        }

        private boolean previousBlendEnabled() {
            return previousBlendEnabled;
        }

        private int previousBlendSrcRgb() {
            return previousBlendSrcRgb;
        }

        private int previousBlendDstRgb() {
            return previousBlendDstRgb;
        }

        private int previousBlendSrcAlpha() {
            return previousBlendSrcAlpha;
        }

        private int previousBlendDstAlpha() {
            return previousBlendDstAlpha;
        }

        private Matrix4f projectionMatrix() {
            return projectionMatrix;
        }

        private long pipelineGeneration() {
            return pipelineGeneration;
        }

        private boolean shadowPass() {
            return shadowPass;
        }

        private ResourceLocation baseTexture() {
            return baseTexture;
        }

        private long generation() {
            return generation;
        }
    }

    private static boolean matrixEquals(Matrix4f left, Matrix4f right) {
        return left == right || right != null
                && Float.floatToIntBits(left.m00()) == Float.floatToIntBits(right.m00())
                && Float.floatToIntBits(left.m01()) == Float.floatToIntBits(right.m01())
                && Float.floatToIntBits(left.m02()) == Float.floatToIntBits(right.m02())
                && Float.floatToIntBits(left.m03()) == Float.floatToIntBits(right.m03())
                && Float.floatToIntBits(left.m10()) == Float.floatToIntBits(right.m10())
                && Float.floatToIntBits(left.m11()) == Float.floatToIntBits(right.m11())
                && Float.floatToIntBits(left.m12()) == Float.floatToIntBits(right.m12())
                && Float.floatToIntBits(left.m13()) == Float.floatToIntBits(right.m13())
                && Float.floatToIntBits(left.m20()) == Float.floatToIntBits(right.m20())
                && Float.floatToIntBits(left.m21()) == Float.floatToIntBits(right.m21())
                && Float.floatToIntBits(left.m22()) == Float.floatToIntBits(right.m22())
                && Float.floatToIntBits(left.m23()) == Float.floatToIntBits(right.m23())
                && Float.floatToIntBits(left.m30()) == Float.floatToIntBits(right.m30())
                && Float.floatToIntBits(left.m31()) == Float.floatToIntBits(right.m31())
                && Float.floatToIntBits(left.m32()) == Float.floatToIntBits(right.m32())
                && Float.floatToIntBits(left.m33()) == Float.floatToIntBits(right.m33());
    }

    private static boolean matrixEquals(float[] left, float[] right) {
        for (int i = 0; i < 16; i++) {
            if (Float.floatToIntBits(left[i]) != Float.floatToIntBits(right[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean linearPartEquals(float[] left, float[] right) {
        return Float.floatToIntBits(left[0]) == Float.floatToIntBits(right[0])
                && Float.floatToIntBits(left[1]) == Float.floatToIntBits(right[1])
                && Float.floatToIntBits(left[2]) == Float.floatToIntBits(right[2])
                && Float.floatToIntBits(left[4]) == Float.floatToIntBits(right[4])
                && Float.floatToIntBits(left[5]) == Float.floatToIntBits(right[5])
                && Float.floatToIntBits(left[6]) == Float.floatToIntBits(right[6])
                && Float.floatToIntBits(left[8]) == Float.floatToIntBits(right[8])
                && Float.floatToIntBits(left[9]) == Float.floatToIntBits(right[9])
                && Float.floatToIntBits(left[10]) == Float.floatToIntBits(right[10]);
    }

    public record Snapshot(
            boolean active,
            long beginCalls,
            long reuseHits,
            long endCalls,
            long drawCalls,
            long shadowDrawCalls,
            long applyFailures,
            long shaderRestoreAttempts,
            long shaderRestoreSuccesses,
            long shaderRestoreFailures,
            long currentFrameBeginCalls,
            long currentFrameReuseHits,
            long currentFrameEndCalls,
            long currentFrameDrawCalls,
            long currentFrameShadowDrawCalls,
            long currentFrameApplyFailures,
            long currentFrameShaderRestoreAttempts,
            long currentFrameShaderRestoreSuccesses,
            long currentFrameShaderRestoreFailures,
            long lastFrameBeginCalls,
            long lastFrameReuseHits,
            long lastFrameEndCalls,
            long lastFrameDrawCalls,
            long lastFrameShadowDrawCalls,
            long lastFrameApplyFailures,
            long lastFrameShaderRestoreAttempts,
            long lastFrameShaderRestoreSuccesses,
            long lastFrameShaderRestoreFailures,
            String lastApplyFailureReason) {

        public static final Snapshot EMPTY = new Snapshot(false,
                0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                "none");
    }
}

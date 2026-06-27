package com.hbm.ntm.client.render.shader;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.HbmGlVaoSafety;
import com.hbm.ntm.client.render.HbmRenderFrameLight;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
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
    private static final AtomicLong beginCalls = new AtomicLong();
    private static final AtomicLong reuseHits = new AtomicLong();
    private static final AtomicLong endCalls = new AtomicLong();
    private static final AtomicLong drawCalls = new AtomicLong();
    private static final AtomicLong shadowDrawCalls = new AtomicLong();
    private static final AtomicLong applyFailures = new AtomicLong();
    private static final AtomicLong currentFrameBeginCalls = new AtomicLong();
    private static final AtomicLong currentFrameReuseHits = new AtomicLong();
    private static final AtomicLong currentFrameEndCalls = new AtomicLong();
    private static final AtomicLong currentFrameDrawCalls = new AtomicLong();
    private static final AtomicLong currentFrameShadowDrawCalls = new AtomicLong();
    private static final AtomicLong currentFrameApplyFailures = new AtomicLong();
    private static final AtomicLong lastFrameBeginCalls = new AtomicLong();
    private static final AtomicLong lastFrameReuseHits = new AtomicLong();
    private static final AtomicLong lastFrameEndCalls = new AtomicLong();
    private static final AtomicLong lastFrameDrawCalls = new AtomicLong();
    private static final AtomicLong lastFrameShadowDrawCalls = new AtomicLong();
    private static final AtomicLong lastFrameApplyFailures = new AtomicLong();
    private static int cachedShaderProgram = -1;
    private static ShaderInstance cachedShaderInstance;
    private static long cachedPipelineGeneration = -1L;
    private static HbmIrisDerivedMatrixUniforms.Locations matrixLocations =
            HbmIrisDerivedMatrixUniforms.Locations.NONE;
    private static final Matrix4f MATRIX_INVERSE = new Matrix4f();
    private static final Matrix3f NORMAL_MATRIX = new Matrix3f();
    private static final float[] MODEL_VIEW_FLOATS = new float[16];
    private static final float[] INVERSE_FLOATS = new float[16];
    private static final float[] NORMAL_FLOATS = new float[9];
    private static int lastConstantLightmapLocation = -1;
    private static int lastConstantLightmapBlock = Integer.MIN_VALUE;
    private static int lastConstantLightmapSky = Integer.MIN_VALUE;

    private HbmIrisRenderBatch() {
    }

    public static boolean begin(Object stateKey, RenderType renderType, ShaderInstance shader) {
        if (renderType == null || shader == null) {
            return false;
        }
        ActiveBatch active = activeBatch;
        if (active != null && active.matches(stateKey, shader)) {
            HbmIrisExtendedShaderAccess.setCurrentRenderedBlockEntity(0);
            reuseHits.incrementAndGet();
            currentFrameReuseHits.incrementAndGet();
            return true;
        }
        endActiveBatch();
        int previousBlockEntityId = HbmIrisExtendedShaderAccess.setCurrentRenderedBlockEntity(0);
        HbmIrisPhaseGuard phaseGuard = HbmIrisPhaseGuard.pushBlockEntities();
        boolean renderStateSet = false;
        try {
            renderType.setupRenderState();
            renderStateSet = true;
            HbmRenderFrameLight.ensureLightTextureUpdated();
            setupIrisSamplersAndUniforms(shader);
            RenderSystem.setShader(() -> shader);
            if (!HbmIrisShaderApply.tryApply(shader)) {
                recordApplyFailure();
                return false;
            }
            HbmRenderFrameLight.bindBlockLitSamplerTextures(shader);
            resolveMatrixLocations(shader);
            resetDrawAttributeCache();
            activeBatch = new ActiveBatch(stateKey, renderType, shader, phaseGuard, previousBlockEntityId);
            beginCalls.incrementAndGet();
            currentFrameBeginCalls.incrementAndGet();
            return true;
        } finally {
            if (activeBatch == null) {
                if (renderStateSet) {
                    renderType.clearRenderState();
                }
                phaseGuard.close();
                HbmIrisExtendedShaderAccess.restoreCurrentRenderedBlockEntity(previousBlockEntityId);
                RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
            }
        }
    }

    public static void recordDraw(boolean shadowPass) {
        drawCalls.incrementAndGet();
        currentFrameDrawCalls.incrementAndGet();
        if (shadowPass) {
            shadowDrawCalls.incrementAndGet();
            currentFrameShadowDrawCalls.incrementAndGet();
        }
    }

    public static void endActiveBatch() {
        ActiveBatch active = activeBatch;
        if (active == null) {
            return;
        }
        activeBatch = null;
        try {
            try {
                active.shader().clear();
            } catch (Throwable throwable) {
                HbmNtm.LOGGER.debug("HBM Iris/Oculus shader clear failed: {}", throwable.toString());
            }
        } finally {
            try {
                try {
                    active.renderType().clearRenderState();
                } catch (Throwable throwable) {
                    HbmNtm.LOGGER.debug("HBM Iris/Oculus render state clear failed: {}", throwable.toString());
                }
            } finally {
                active.phaseGuard().close();
                HbmIrisExtendedShaderAccess.restoreCurrentRenderedBlockEntity(active.previousBlockEntityId());
                RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
                resetDrawAttributeCache();
            }
        }
        endCalls.incrementAndGet();
        currentFrameEndCalls.incrementAndGet();
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
        int previousVao = HbmGlVaoSafety.currentBinding();
        try {
            HbmGlVaoSafety.bindVertexArray(0);
            RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
            draw.run();
        } finally {
            if (activeBatch == active && active.shader() != null) {
                RenderSystem.setShader(active::shader);
                if (!HbmIrisShaderApply.tryApply(active.shader())) {
                    recordApplyFailure();
                    HbmNtm.LOGGER.warn("HBM Iris/Oculus vanilla overlay shader restore failed");
                }
                resolveMatrixLocations(active.shader());
                resetDrawAttributeCache();
            }
            HbmGlVaoSafety.bindVertexArray(previousVao);
        }
    }

    public static void invalidateCaches() {
        endActiveBatch();
        cachedShaderProgram = -1;
        cachedShaderInstance = null;
        cachedPipelineGeneration = -1L;
        matrixLocations = HbmIrisDerivedMatrixUniforms.Locations.NONE;
        resetDrawAttributeCache();
    }

    public static void endFrame() {
        endActiveBatch();
        lastFrameBeginCalls.set(currentFrameBeginCalls.getAndSet(0L));
        lastFrameReuseHits.set(currentFrameReuseHits.getAndSet(0L));
        lastFrameEndCalls.set(currentFrameEndCalls.getAndSet(0L));
        lastFrameDrawCalls.set(currentFrameDrawCalls.getAndSet(0L));
        lastFrameShadowDrawCalls.set(currentFrameShadowDrawCalls.getAndSet(0L));
        lastFrameApplyFailures.set(currentFrameApplyFailures.getAndSet(0L));
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                activeBatch != null,
                beginCalls.get(),
                reuseHits.get(),
                endCalls.get(),
                drawCalls.get(),
                shadowDrawCalls.get(),
                applyFailures.get(),
                currentFrameBeginCalls.get(),
                currentFrameReuseHits.get(),
                currentFrameEndCalls.get(),
                currentFrameDrawCalls.get(),
                currentFrameShadowDrawCalls.get(),
                currentFrameApplyFailures.get(),
                lastFrameBeginCalls.get(),
                lastFrameReuseHits.get(),
                lastFrameEndCalls.get(),
                lastFrameDrawCalls.get(),
                lastFrameShadowDrawCalls.get(),
                lastFrameApplyFailures.get());
    }

    private static void recordApplyFailure() {
        applyFailures.incrementAndGet();
        currentFrameApplyFailures.incrementAndGet();
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
        if (locations.modelView() >= 0) {
            GL20.glUniformMatrix4fv(locations.modelView(), false, MODEL_VIEW_FLOATS);
        }
        boolean haveInverse = false;
        if (locations.modelViewInverse() >= 0) {
            MATRIX_INVERSE.set(MODEL_VIEW_FLOATS).invert();
            MATRIX_INVERSE.get(INVERSE_FLOATS);
            GL20.glUniformMatrix4fv(locations.modelViewInverse(), false, INVERSE_FLOATS);
            haveInverse = true;
        }
        if (locations.normalMat() >= 0) {
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
    }

    public static void applyConstantLightmap(int uv2Location, int packedLight) {
        if (uv2Location < 0) {
            return;
        }
        int block = LightTexture.block(packedLight) * 16;
        int sky = LightTexture.sky(packedLight) * 16;
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

    public static void invalidateLightmapAttributeCache() {
        lastConstantLightmapLocation = -1;
        lastConstantLightmapBlock = Integer.MIN_VALUE;
        lastConstantLightmapSky = Integer.MIN_VALUE;
    }

    private static void resetDrawAttributeCache() {
        invalidateLightmapAttributeCache();
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
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer != null) {
            minecraft.gameRenderer.overlayTexture().setupOverlayColor();
            minecraft.gameRenderer.lightTexture().turnOnLightLayer();
        }
        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }
        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(IDENTITY);
        }
        Uniform fogStart = shader.getUniform("FogStart");
        if (fogStart != null) {
            fogStart.set(RenderSystem.getShaderFogStart());
        }
        Uniform fogEnd = shader.getUniform("FogEnd");
        if (fogEnd != null) {
            fogEnd.set(RenderSystem.getShaderFogEnd());
        }
        Uniform fogColor = shader.getUniform("FogColor");
        if (fogColor != null) {
            float[] color = RenderSystem.getShaderFogColor();
            fogColor.set(color[0], color[1], color[2], color[3]);
        }
        HbmRenderFrameLight.prepareBlockLitSamplers(shader);
    }

    private static final Matrix4f IDENTITY = new Matrix4f();

    private record ActiveBatch(
            Object stateKey,
            RenderType renderType,
            ShaderInstance shader,
            HbmIrisPhaseGuard phaseGuard,
            int previousBlockEntityId) {

        private boolean matches(Object stateKey, ShaderInstance shader) {
            return this.shader == shader && Objects.equals(this.stateKey, stateKey);
        }
    }

    public record Snapshot(
            boolean active,
            long beginCalls,
            long reuseHits,
            long endCalls,
            long drawCalls,
            long shadowDrawCalls,
            long applyFailures,
            long currentFrameBeginCalls,
            long currentFrameReuseHits,
            long currentFrameEndCalls,
            long currentFrameDrawCalls,
            long currentFrameShadowDrawCalls,
            long currentFrameApplyFailures,
            long lastFrameBeginCalls,
            long lastFrameReuseHits,
            long lastFrameEndCalls,
            long lastFrameDrawCalls,
            long lastFrameShadowDrawCalls,
            long lastFrameApplyFailures) {

        public static final Snapshot EMPTY = new Snapshot(false,
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L);
    }
}

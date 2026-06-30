package com.hbm.ntm.client.render;

import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.client.render.shader.HbmIrisExtendedShaderAccess;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public final class HbmRenderFrameFlags {
    private static final AtomicLong FRAME_GENERATION = new AtomicLong();
    private static volatile Snapshot current = Snapshot.initial();

    private HbmRenderFrameFlags() {
    }

    public static Snapshot beginFrame(Minecraft minecraft, RenderLevelStageEvent event) {
        ParticleStatus particleStatus = minecraft.options.particles().get();
        boolean shaderActive = HbmShaderCompatibilityDetector.isExternalShaderActive();
        long shaderPipelineGeneration = HbmShaderCompatibilityDetector.tickPipelineGeneration();
        HbmIrisExtendedShaderAccess.tickPass();
        boolean irisExtendedShaderAvailable = HbmShaderCompatibilityDetector.canUseIrisExtendedShader();
        boolean irisExtendedShaderPathEnabled = HbmClientConfig.experimentalIrisExtendedShaderPath()
                && irisExtendedShaderAvailable;
        boolean safeObjStaticBatching = HbmClientConfig.safeObjStaticBatching();
        boolean vboGeometryEnabled = HbmShaderCompatibilityDetector.useVboGeometry();
        boolean safeGpuBackend = safeObjStaticBatching && !shaderActive;
        boolean experimentalGpuBackend = HbmClientConfig.experimentalGpuBackend();
        boolean experimentalInstancing = HbmClientConfig.experimentalInstancing();
        boolean gpuBackendRequested = experimentalGpuBackend || safeGpuBackend || experimentalInstancing;
        boolean gpuBackendAllowed = gpuBackendRequested
                && (!shaderActive || !HbmClientConfig.disableGpuBackendWithShaders());
        boolean instancingShaderReady = HbmOptimizedRenderShaders.instancingShaderReady();
        boolean instancingGlReady = HbmInstancedGlCompat.supportsDrawArraysInstancing();
        boolean safeInstancing = safeGpuBackend;
        boolean instancingRequested = experimentalInstancing || safeInstancing;
        boolean instancingEnabled = instancingRequested && gpuBackendAllowed
                && instancingShaderReady && instancingGlReady;
        int maxInstancedInstancesPerDraw = HbmClientConfig.maxInstancedInstancesPerDraw();
        boolean instanceVboOrphanBeforeUpload = HbmClientConfig.instanceVboOrphanBeforeUpload();
        boolean mdiRequested = HbmClientConfig.experimentalMdi();
        boolean mdiEnabled = mdiRequested && instancingEnabled;
        boolean occlusionCullingEnabled = HbmClientConfig.experimentalOcclusionCulling();
        Snapshot snapshot = new Snapshot(
                FRAME_GENERATION.incrementAndGet(),
                event.getPartialTick(),
                minecraft.options.renderDistance().get(),
                particleStatus == ParticleStatus.ALL,
                particleStatus == ParticleStatus.MINIMAL,
                HbmClientConfig.nukeWarpShockwaveEnabled(),
                HbmClientConfig.debugNukeWarpShockwaveWireframe(),
                HbmClientConfig.coolingTowerParticles(),
                gpuBackendRequested,
                safeObjStaticBatching,
                vboGeometryEnabled,
                experimentalInstancing,
                safeInstancing,
                instancingRequested,
                instancingEnabled,
                maxInstancedInstancesPerDraw,
                instanceVboOrphanBeforeUpload,
                mdiRequested,
                mdiEnabled,
                occlusionCullingEnabled,
                instancingShaderReady,
                instancingGlReady,
                shaderActive,
                gpuBackendAllowed,
                HbmShaderCompatibilityDetector.isRenderingShadowPass(),
                shaderPipelineGeneration,
                irisExtendedShaderAvailable,
                irisExtendedShaderPathEnabled);
        current = snapshot;
        return snapshot;
    }

    public static void endFrame() {
        current = Snapshot.initial();
    }

    public static Snapshot current() {
        return current;
    }

    public record Snapshot(
            long frameGeneration,
            float partialTick,
            int renderDistanceChunks,
            boolean allParticles,
            boolean minimalParticles,
            boolean nukeWarpShockwaveEnabled,
            boolean debugNukeWarpShockwaveWireframe,
            boolean coolingTowerParticles,
            boolean experimentalGpuBackendEnabled,
            boolean safeObjStaticBatchingEnabled,
            boolean vboGeometryEnabled,
            boolean experimentalInstancingRequested,
            boolean safeInstancingRequested,
            boolean instancingRequested,
            boolean instancingEnabled,
            int maxInstancedInstancesPerDraw,
            boolean instanceVboOrphanBeforeUpload,
            boolean mdiRequested,
            boolean mdiEnabled,
            boolean occlusionCullingEnabled,
            boolean instancingShaderReady,
            boolean instancingGlReady,
            boolean shaderPackDetected,
            boolean gpuBackendAllowed,
            boolean shaderShadowPass,
            long shaderPipelineGeneration,
            boolean irisExtendedShaderAvailable,
            boolean irisExtendedShaderPathEnabled) {
        private static Snapshot initial() {
            return new Snapshot(0L, 0.0F, 0, false, false, true, false, true,
                    false, false, true, false, false, false, false,
                    HbmClientConfig.maxInstancedInstancesPerDraw(),
                    HbmClientConfig.instanceVboOrphanBeforeUpload(), false, false, false, false, false, false, false,
                    false, 0L, false, false);
        }
    }
}

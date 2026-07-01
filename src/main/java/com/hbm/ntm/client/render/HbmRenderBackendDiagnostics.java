package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.IrisCompanionQueueSnapshot;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.MdiAdditiveSnapshot;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.ModelCacheSnapshot;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.RenderBackendAdditiveSnapshot;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.RenderBackendInstancingSnapshot;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.RenderBackendIrisSnapshot;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.RenderBackendSnapshot;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.IrisCompanionShaderSnapshot;
import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import com.hbm.ntm.client.render.shader.HbmIrisExtendedShaderAccess;
import com.hbm.ntm.client.render.shader.HbmIrisPhaseGuard;
import com.hbm.ntm.client.render.shader.HbmIrisRenderBatch;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.hbm.ntm.client.renderer.LegacyVisibleMachineRenderer;
import com.hbm.ntm.client.renderer.LegacyVisibleMachineRenderer.VisibleMachineRouteCoverage;
import com.hbm.ntm.client.renderer.LegacyLightSampleCache;
import com.hbm.ntm.client.renderer.LegacyRenderLighting;
import com.hbm.ntm.config.HbmClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLCapabilities;

public final class HbmRenderBackendDiagnostics {
    private static final long LOG_INTERVAL_FRAMES = 120L;
    private static final long AUTO_LOG_INTERVAL_FRAMES = 600L;
    private static final long AUTO_LOG_MACHINE_SUBMISSIONS = 32L;
    private static final long AUTO_LOG_DRAW_CALLS_WITHOUT_OPTIMIZED_BACKEND = 128L;
    private static long lastLoggedFrame;
    private static long lastAutoLoggedFrame;
    private static boolean glBannerLogged;

    private HbmRenderBackendDiagnostics() {
    }

    public static void logEndFrameIfEnabled() {
        boolean configured = HbmClientConfig.renderBackendDiagnostics();
        HbmRenderFrameFlags.Snapshot flags = HbmRenderFrameFlags.current();
        long frame = flags.frameGeneration();
        if (frame <= 0L) {
            return;
        }

        RenderBackendSnapshot backend = LegacyWavefrontModel.renderBackendSnapshot();
        RenderBackendAdditiveSnapshot additive = LegacyWavefrontModel.renderBackendAdditiveSnapshot();
        RenderBackendInstancingSnapshot instancingSnapshot = LegacyWavefrontModel.renderBackendInstancingSnapshot();
        RenderBackendIrisSnapshot iris = LegacyWavefrontModel.renderBackendIrisSnapshot();
        if (!hasLastFrameActivity(backend, additive, instancingSnapshot, iris)) {
            return;
        }

        if (configured) {
            if (frame - lastLoggedFrame < LOG_INTERVAL_FRAMES) {
                return;
            }
            lastLoggedFrame = frame;
            logGlCapabilityBannerOnce();
            String message = summary(flags, backend, additive, instancingSnapshot, iris);
            HbmNtm.LOGGER.info(message);
            return;
        }

        String autoReason = autoLogReason(backend, additive, instancingSnapshot, iris);
        if (autoReason == null
                || (lastAutoLoggedFrame > 0L && frame - lastAutoLoggedFrame < AUTO_LOG_INTERVAL_FRAMES)) {
            return;
        }
        lastAutoLoggedFrame = frame;
        logGlCapabilityBannerOnce();
        HbmNtm.LOGGER.warn(
                "[HBM RenderBackend] diagnostics are disabled but OBJ render pressure was detected ({}). "
                        + "Enable client config rendering.renderBackendDiagnostics=true for continuous 120-frame "
                        + "summaries. {}",
                autoReason,
                summary(flags, backend, additive, instancingSnapshot, iris));
    }

    private static boolean hasLastFrameActivity(RenderBackendSnapshot backend, RenderBackendAdditiveSnapshot additive,
            RenderBackendInstancingSnapshot instancingSnapshot, RenderBackendIrisSnapshot iris) {
        InstancedFrameCounts instanced = instancedCounts(backend, additive, instancingSnapshot);
        MdiFrameCounts mdi = mdiCounts(backend, additive, instancingSnapshot);
        IrisCompanionQueueSnapshot irisQueue = iris.queuedFlush();
        IrisCompanionShaderSnapshot irisShaderAttributes = iris.shaderAttributes();
        HbmIrisRenderBatch.Snapshot persistent = iris.persistentBatch();
        HbmIrisPhaseGuard.Snapshot irisPhase = HbmIrisPhaseGuard.snapshot();
        HbmRenderFrameCulling.Snapshot culling = HbmRenderFrameCulling.currentSnapshot();
        return backend.lastFrameEstimatedDrawCalls() > 0L
                || instanced.optimizedFlushCalls > 0L
                || instanced.queuedBatches > 0L
                || instanced.drawCalls > 0L
                || instanced.overflowBatches > 0L
                || instanced.optimizedDuplicatePresentSkips > 0L
                || instancingSnapshot.lastFrameOptimizedDrawStateRestoreFailures() > 0L
                || instanced.duplicateInstances > 0L
                || instanced.staleInstancedBatches > 0L
                || instanced.staleIrisCompanionBatches > 0L
                || mdi.eligibleBatches > 0L
                || mdi.drawCalls > 0L
                || mdi.noSlotBatches > 0L
                || mdi.partialDrawFailures > 0L
                || mdi.stalePreparedGroups > 0L
                || mdi.dispatchDisableEvents > 0L
                || mdi.atlasRepackFailures > 0L
                || mdi.atlasInitFailures > 0L
                || iris.lastFrameDrawCalls() > 0L
                || iris.lastFrameFallbackBatches() > 0L
                || iris.lastFrameLightmapStorageFailures() > 0L
                || iris.lastFrameLightmapSlotReuses() > 0L
                || iris.lastFrameLightmapSlotUploads() > 0L
                || iris.lastFrameLightmapStagingFallbacks() > 0L
                || irisShaderAttributes.lastFrameCacheHits() > 0L
                || irisShaderAttributes.lastFrameCacheMisses() > 0L
                || irisShaderAttributes.lastFrameGenerationInvalidations() > 0L
                || irisShaderAttributes.lastFramePrimedAttributeSkips() > 0L
                || irisShaderAttributes.lastFrameVaoBindFailures() > 0L
                || persistent.lastFrameShaderRestoreAttempts() > 0L
                || persistent.lastFrameShaderRestoreFailures() > 0L
                || irisPhase.lastFramePushFailures() > 0L
                || irisPhase.lastFrameRestoreFailures() > 0L
                || irisQueue.lastFrameQueuedBatches() > 0L
                || irisQueue.lastFrameDrawCalls() > 0L
                || irisQueue.lastFrameFallbackBatches() > 0L
                || irisQueue.lastFrameDuplicateInstances() > 0L
                || HbmRenderFrameLight.currentFrameInvalidSamplerBindings() > 0L
                || HbmRenderFrameLight.lastFrameInvalidSamplerBindings() > 0L
                || culling.machineRendererSubmissions() > 0L
                || culling.frustumCulledQueries() > 0L
                || culling.occlusionCulledQueries() > 0L;
    }

    private static String autoLogReason(RenderBackendSnapshot backend, RenderBackendAdditiveSnapshot additive,
            RenderBackendInstancingSnapshot instancingSnapshot, RenderBackendIrisSnapshot iris) {
        InstancedFrameCounts instanced = instancedCounts(backend, additive, instancingSnapshot);
        MdiFrameCounts mdi = mdiCounts(backend, additive, instancingSnapshot);
        IrisCompanionQueueSnapshot irisQueue = iris.queuedFlush();
        HbmRenderFrameCulling.Snapshot culling = HbmRenderFrameCulling.currentSnapshot();
        long gpuFallbackBatches = backend.lastFrameGpuFallbackBatches()
                + iris.lastFrameFallbackBatches()
                + irisQueue.lastFrameFallbackBatches()
                + instanced.overflowBatches
                + mdi.noSlotBatches
                + mdi.partialDrawFailures;
        long cpuFallbackBatches = backend.lastFrameCpuFallbackBatches();
        long optimizedDraws = instanced.drawCalls + mdi.drawCalls
                + iris.lastFrameDrawCalls()
                + irisQueue.lastFrameDrawCalls();
        if (gpuFallbackBatches > 0L) {
            return "gpuFallback=" + gpuFallbackBatches + ",cpuFallback=" + cpuFallbackBatches
                    + ",lastFallback=" + backend.lastFallbackReason()
                    + ",detail=" + backend.lastFallbackDetail();
        }
        if (cpuFallbackBatches > 0L && optimizedDraws == 0L) {
            return "cpuFallbackWithoutOptimizedDraws=" + cpuFallbackBatches
                    + ",machines=" + culling.machineRendererSubmissions()
                    + ",lastFallback=" + backend.lastFallbackReason();
        }
        if (culling.machineRendererSubmissions() >= AUTO_LOG_MACHINE_SUBMISSIONS && optimizedDraws == 0L) {
            return "machineSubmissionsWithoutOptimizedDraws=" + culling.machineRendererSubmissions()
                    + ",drawCalls=" + backend.lastFrameEstimatedDrawCalls();
        }
        if (backend.lastFrameEstimatedDrawCalls() >= AUTO_LOG_DRAW_CALLS_WITHOUT_OPTIMIZED_BACKEND
                && optimizedDraws == 0L) {
            return "drawCallsWithoutOptimizedBackend=" + backend.lastFrameEstimatedDrawCalls()
                    + ",machines=" + culling.machineRendererSubmissions();
        }
        return null;
    }

    private static void logGlCapabilityBannerOnce() {
        if (glBannerLogged || !RenderSystem.isOnRenderThread()) {
            return;
        }
        try {
            GLCapabilities capabilities = HbmInstancedGlCompat.currentCapabilities();
            if (capabilities == null) {
                return;
            }
            glBannerLogged = true;
            boolean divisorCore = capabilities.glVertexAttribDivisor != 0L;
            boolean divisorArb = capabilities.glVertexAttribDivisorARB != 0L;
            boolean drawInstancedCore = capabilities.glDrawArraysInstanced != 0L;
            boolean drawInstancedArb = capabilities.glDrawArraysInstancedARB != 0L;
            boolean drawIndirectSingle = capabilities.glDrawArraysIndirect != 0L || capabilities.GL_ARB_draw_indirect;
            boolean drawIndirectMulti = capabilities.glMultiDrawArraysIndirect != 0L
                    || capabilities.GL_ARB_multi_draw_indirect;
            boolean baseInstance = capabilities.glDrawArraysInstancedBaseInstance != 0L
                    || capabilities.GL_ARB_base_instance;
            String message = String.format(
                    "[HBM RenderBackend] GL capabilities: vendor='%s', renderer='%s', version='%s', maxVertexAttribs=%d/%d, "
                            + "instancing(drawArrays=%s,divisor=%s,core=%s/%s,arb=%s/%s), "
                            + "mdi(drawIndirect=%s,multiDrawIndirect=%s,baseInstance=%s)",
                    glString(GL11.GL_VENDOR),
                    glString(GL11.GL_RENDERER),
                    glString(GL11.GL_VERSION),
                    GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS),
                    HbmInstancedGlCompat.requiredVertexAttribs(),
                    drawInstancedCore || drawInstancedArb,
                    divisorCore || divisorArb,
                    drawInstancedCore,
                    divisorCore,
                    drawInstancedArb,
                    divisorArb,
                    drawIndirectSingle || drawIndirectMulti,
                    drawIndirectMulti,
                    baseInstance);
            HbmNtm.LOGGER.info(message);
        } catch (RuntimeException exception) {
            HbmNtm.LOGGER.debug("[HBM RenderBackend] Failed to query GL capability banner", exception);
        }
    }

    private static String glString(int name) {
        String value = GL11.glGetString(name);
        return value == null ? "unknown" : value;
    }

    private static String summary(HbmRenderFrameFlags.Snapshot flags, RenderBackendSnapshot backend,
            RenderBackendAdditiveSnapshot additive, RenderBackendInstancingSnapshot instancingSnapshot,
            RenderBackendIrisSnapshot iris) {
        InstancedFrameCounts instanced = instancedCounts(backend, additive, instancingSnapshot);
        MdiFrameCounts mdi = mdiCounts(backend, additive, instancingSnapshot);
        IrisCompanionQueueSnapshot irisQueue = iris.queuedFlush();
        IrisCompanionShaderSnapshot irisShaderAttributes = iris.shaderAttributes();
        HbmIrisRenderBatch.Snapshot persistent = iris.persistentBatch();
        HbmRenderFrameCulling.Snapshot culling = HbmRenderFrameCulling.currentSnapshot();
        VisibleMachineRouteCoverage visibleRoutes = LegacyVisibleMachineRenderer.routeCoverageSnapshot();
        HbmShaderCompatibilityDetector.Snapshot shader = HbmShaderCompatibilityDetector.snapshot();
        HbmIrisExtendedShaderAccess.Snapshot irisAccess = HbmIrisExtendedShaderAccess.snapshot();
        HbmIrisPhaseGuard.Snapshot irisPhase = HbmIrisPhaseGuard.snapshot();
        LegacyLightSampleCache.Snapshot lightSamples = LegacyLightSampleCache.snapshot();
        LegacyRenderLighting.ProbeCacheSnapshot probeCache = LegacyRenderLighting.probeCacheSnapshot();
        ModelCacheSnapshot modelCache = backend.modelCache();
        return String.format(
                "[HBM RenderBackend] frame=%d backend=%s flags(gpuRequested=%s,gpuAllowed=%s,safeObj=%s,animDist=%d,staticDist=%d,lightDetailDist=%d,slicedLight=%s,instReq=%s/%s/%s,instancing=%s,instCap=%d,orphan=%s,mdiReq=%s,mdi=%s,occ=%s,shaderPack=%s,shadow=%s,instShader=%s,instGl=%s,irisAvail=%s,irisExt=%s) "
                        + "shader(api=%s,active=%s,pipeline=%s/%s/%d,ext=%s/%s,vboGeom=%s,keys=%s:%d/%d,cache=%s/%s,beid=%s/%s,phase=%s/%s/%s,push=%d/%d/%d,restore=%d/%d) "
                        + "cache(model=%d/%d/%d/%d,views=%d/%d,geom=%d/%d/%d,sel=%d/%d/%d,build=%d/%d/%d,hit/miss/clear=%d/%d/%d,handle=%d/%d/%d,missing=%d) "
                        + "draws(cpu=%d,gpu=%d,gpuUpload=%d/%d,gpuFallback=%d/%d,lastFallback=%s,detail=%s) "
                        + "culling(queries=%d,visible=%d,frustum=%d,distance=%d,shadowBypass=%d,noFrustum=%d,machines=%d,vertices=%d,route=%d/%d/%d/%d,partRuns=%d,objInst=%d/%d/%d,fade=%d/%d/%d,scoped=%d/%d/%d,unscoped=%d/%d/%d,occ=%d,occEnabled=%d,occDisabled=%d,occNoLevel=%d,near=%d,hit/miss/reuse=%d/%d/%d,ray=%d/%d,occVisible=%d,occCulled=%d,cache=%d,geom=%d,hotObj=%s) "
                        + "bakedObj(%s) "
                        + "visibleDefs(blocks/defs=%d/%d,default=%d/%d,profile=%d/%d,direct/fallback=%d/%d,itemParts=%d,partProps=%d) "
                        + "instanced(flush=%d/dup=%d/blocked=%d,tookMs=%d,stateRestoreFail=%d,queued=%d/%d,draws=%d,sliceOverflow=%d/%d,dup=%d,stale=%d/%d,irisStale=%d/%d) "
                        + "mdi(available=%s,dispatchDisabled=%s,drawIndirect=%s,multi=%s,baseInstance=%s,eligible=%d,draws=%d,dispatch=%d/%d,commands=%d,noSlot=%d/%d,partialFail=%d,stale=%d/%d,dispatchDisable=%d,repackFail=%d,initFail=%d,atlasParts=%d,atlasBytes=%d) "
                        + "iris(singleDraws=%d,shadowDraws=%d,upload=%d/%d,fallback=%d/%d,lightmapFail=%d,lightmapSlot=%d/%d,lightmapStaging=%d/%d/%d,shaderAttr=%d/%d/%d/%d/%d,queue=%d/%d,queueFlushes=%d,queueDraws=%d,queueFallback=%d/%d,queueDup=%d,persistentBegin=%d,reuse=%d,end=%d,draw=%d,persistentShadow=%d,restore=%d/%d/%d,applyFail=%d,lastApplyFailure=%s) "
                        + "light(sample=%d/%d/%d,probe=%d/%d/%d,sliced=%d/%d/%d,prune=%d/%d,cache=%d/%d/%d) "
                        + "sampler(invalid=%d/%d/%d)",
                flags.frameGeneration(),
                backend.name(),
                flags.experimentalGpuBackendEnabled(),
                flags.gpuBackendAllowed(),
                flags.safeObjStaticBatchingEnabled(),
                flags.modelUpdateDistanceBlocks(),
                flags.modelStaticRenderDistanceBlocks(),
                LegacyRenderLighting.lightCornerDetailDistanceBlocksForDiagnostics(),
                flags.useSlicedLight(),
                flags.safeInstancingRequested(),
                flags.experimentalInstancingRequested(),
                flags.instancingRequested(),
                flags.instancingEnabled(),
                flags.maxInstancedInstancesPerDraw(),
                flags.instanceVboOrphanBeforeUpload(),
                flags.mdiRequested(),
                flags.mdiEnabled(),
                flags.occlusionCullingEnabled(),
                flags.shaderPackDetected(),
                flags.shaderShadowPass(),
                flags.instancingShaderReady(),
                flags.instancingGlReady(),
                flags.irisExtendedShaderAvailable(),
                flags.irisExtendedShaderPathEnabled(),
                shader.apiPresent(),
                shader.shaderActive(),
                shader.pipelineReflectionAvailable(),
                shader.pipelineIdentitySampled(),
                shader.pipelineGeneration(),
                shader.irisExtendedShaderReflectionAvailable(),
                shader.irisExtendedShaderUsable(),
                shader.vboGeometryEnabled(),
                irisAccess.shaderKeyCandidatesBuilt(),
                irisAccess.mainShaderKeyCandidates(),
                irisAccess.shadowShaderKeyCandidates(),
                irisAccess.mainShaderCached(),
                irisAccess.shadowShaderCached(),
                irisAccess.blockEntityIdResetAvailable(),
                irisAccess.blockEntityIdMethodHandlesAvailable(),
                irisPhase.initialized(),
                irisPhase.available(),
                irisPhase.methodHandlesAvailable(),
                irisPhase.lastFrameActivePushes(),
                irisPhase.lastFramePushAttempts(),
                irisPhase.lastFramePushFailures(),
                irisPhase.lastFrameRestores(),
                irisPhase.lastFrameRestoreFailures(),
                modelCache.registeredModels(),
                modelCache.loadedModels(),
                modelCache.unloadedModels(),
                modelCache.failedModels(),
                modelCache.vboRequestedModels(),
                modelCache.rawModelViews(),
                modelCache.groups(),
                modelCache.faces(),
                modelCache.faceVertices(),
                modelCache.selectionCacheEntries(),
                modelCache.selectionCacheEmptyEntries(),
                modelCache.selectionCachePreparedEntries(),
                backend.groupPreparedBuilds(),
                backend.allPreparedBatchBuilds(),
                backend.selectionCachePreparedBatchBuilds(),
                backend.selectionCacheHits(),
                backend.selectionCacheMisses(),
                backend.selectionCacheClears(),
                backend.selectionHandleRefreshes(),
                backend.selectionHandleEmptyBuilds(),
                backend.selectionHandlePreparedBatchBuilds(),
                modelCache.missingPartWarningEntries(),
                backend.lastFrameCpuFallbackBatches(),
                backend.lastFrameGpuDrawCalls(),
                backend.gpuUploadAttempts(),
                backend.gpuUploadFailures(),
                backend.lastFrameGpuFallbackBatches(),
                backend.lastFrameGpuFallbackVertices(),
                backend.lastFallbackReason(),
                backend.lastFallbackDetail(),
                culling.visibilityQueries(),
                culling.visibleQueries(),
                culling.frustumCulledQueries(),
                culling.distanceCulledQueries(),
                culling.shadowPassBypassQueries(),
                culling.noFrustumQueries(),
                culling.machineRendererSubmissions(),
                culling.machineRendererVertices(),
                culling.machineRendererDefaultRenderAll(),
                culling.machineRendererDefaultParts(),
                culling.machineRendererProfileDirect(),
                culling.machineRendererProfileFallback(),
                culling.machineRendererPartRuns(),
                culling.objInstancedQueueRecords(),
                culling.objInstancedQueuedBatches(),
                culling.objInstancedQueuedInstances(),
                culling.objInstancedFadedRecords(),
                culling.objInstancedFadedBatches(),
                culling.objInstancedFadedInstances(),
                culling.objInstancedCullingScopedRecords(),
                culling.objInstancedCullingScopedBatches(),
                culling.objInstancedCullingScopedInstances(),
                culling.objInstancedUnscopedRecords(),
                culling.objInstancedUnscopedBatches(),
                culling.objInstancedUnscopedInstances(),
                culling.occlusionQueries(),
                culling.occlusionEnabledQueries(),
                culling.occlusionDisabledByConfigQueries(),
                culling.occlusionNoLevelQueries(),
                culling.occlusionNearBypassQueries(),
                culling.occlusionCacheHits(),
                culling.occlusionCacheMisses(),
                culling.occlusionCrossFrameReuses(),
                culling.occlusionRayTests(),
                culling.occlusionRaySteps(),
                culling.occlusionVisibleQueries(),
                culling.occlusionCulledQueries(),
                culling.occlusionCacheEntries(),
                culling.occlusionGeometryStamp(),
                HbmRenderFrameCulling.currentObjInstancedHotPartSummary(5),
                HbmBakedObjModelDiagnostics.summary(5),
                visibleRoutes.blockCount(),
                visibleRoutes.definitionCount(),
                visibleRoutes.defaultRenderAllDefinitions(),
                visibleRoutes.defaultPartDefinitions(),
                visibleRoutes.profileRenderAllDefinitions(),
                visibleRoutes.profilePartDefinitions(),
                visibleRoutes.profileDirectDefinitions(),
                visibleRoutes.profileFallbackDefinitions(),
                visibleRoutes.itemPartDefinitions(),
                visibleRoutes.partPropertyDefinitions(),
                instanced.optimizedFlushCalls,
                instanced.optimizedDuplicateFlushCalls,
                instanced.optimizedDuplicatePresentSkips,
                instanced.optimizedFlushNanos / 1_000_000L,
                instancingSnapshot.lastFrameOptimizedDrawStateRestoreFailures(),
                instanced.queuedBatches,
                instanced.queuedInstances,
                instanced.drawCalls,
                instanced.overflowBatches,
                instanced.overflowInstances,
                instanced.duplicateInstances,
                instanced.staleInstancedBatches,
                instanced.staleInstancedInstances,
                instanced.staleIrisCompanionBatches,
                instanced.staleIrisCompanionInstances,
                backend.mdiAvailable(),
                instancingSnapshot.mdiDispatchDisabled(),
                backend.mdiDrawIndirectSupported(),
                backend.mdiMultiDrawIndirectSupported(),
                backend.mdiBaseInstanceSupported(),
                mdi.eligibleBatches,
                mdi.drawCalls,
                mdi.multiDrawCalls,
                mdi.indirectLoopDrawCalls(),
                mdi.indirectCommands,
                mdi.noSlotBatches,
                mdi.noSlotInstances,
                mdi.partialDrawFailures,
                mdi.stalePreparedGroups,
                mdi.stalePreparedCommands,
                mdi.dispatchDisableEvents,
                mdi.atlasRepackFailures,
                mdi.atlasInitFailures,
                backend.mdiAtlasParts(),
                backend.mdiAtlasBytes(),
                iris.lastFrameDrawCalls(),
                iris.lastFrameShadowDrawCalls(),
                iris.uploadAttempts(),
                iris.uploadFailures(),
                iris.lastFrameFallbackBatches(),
                iris.lastFrameFallbackVertices(),
                iris.lastFrameLightmapStorageFailures(),
                iris.lastFrameLightmapSlotReuses(),
                iris.lastFrameLightmapSlotUploads(),
                iris.lightmapStagingFallbacks(),
                iris.currentFrameLightmapStagingFallbacks(),
                iris.lastFrameLightmapStagingFallbacks(),
                irisShaderAttributes.lastFrameCacheHits(),
                irisShaderAttributes.lastFrameCacheMisses(),
                irisShaderAttributes.lastFrameGenerationInvalidations(),
                irisShaderAttributes.lastFramePrimedAttributeSkips(),
                irisShaderAttributes.lastFrameVaoBindFailures(),
                irisQueue.lastFrameQueuedBatches(),
                irisQueue.lastFrameQueuedInstances(),
                irisQueue.lastFrameFlushes(),
                irisQueue.lastFrameDrawCalls(),
                irisQueue.lastFrameFallbackBatches(),
                irisQueue.lastFrameFallbackInstances(),
                irisQueue.lastFrameDuplicateInstances(),
                persistent.lastFrameBeginCalls(),
                persistent.lastFrameReuseHits(),
                persistent.lastFrameEndCalls(),
                persistent.lastFrameDrawCalls(),
                persistent.lastFrameShadowDrawCalls(),
                persistent.lastFrameShaderRestoreAttempts(),
                persistent.lastFrameShaderRestoreSuccesses(),
                persistent.lastFrameShaderRestoreFailures(),
                persistent.lastFrameApplyFailures(),
                persistent.lastApplyFailureReason(),
                lightSamples.currentFrameSamples(),
                lightSamples.currentFrameHits(),
                lightSamples.currentFrameMisses(),
                probeCache.currentFrameAnchoredProbeQueries(),
                probeCache.currentFrameAnchoredProbeHits(),
                probeCache.currentFrameAnchoredProbeMisses(),
                probeCache.currentFrameSlicedProbeQueries(),
                probeCache.currentFrameSlicedProbeHits(),
                probeCache.currentFrameSlicedProbeMisses(),
                probeCache.currentFrameProbeCachePruneRuns(),
                probeCache.currentFrameProbeCachePrunedEntries(),
                lightSamples.cachedPositions(),
                probeCache.anchoredCachedProbes(),
                probeCache.slicedCachedProbes(),
                HbmRenderFrameLight.invalidSamplerBindings(),
                HbmRenderFrameLight.currentFrameInvalidSamplerBindings(),
                HbmRenderFrameLight.lastFrameInvalidSamplerBindings());
    }

    private static InstancedFrameCounts instancedCounts(RenderBackendSnapshot backend,
            RenderBackendAdditiveSnapshot additive, RenderBackendInstancingSnapshot instancingSnapshot) {
        return new InstancedFrameCounts(
                instancingSnapshot.lastFrameOptimizedFlushCalls(),
                instancingSnapshot.lastFrameOptimizedDuplicateFlushCalls(),
                instancingSnapshot.lastFrameOptimizedDuplicatePresentSkips(),
                instancingSnapshot.lastFrameOptimizedFlushNanos(),
                backend.lastFrameInstancedQueuedBatches() + additive.instanced().lastFrameQueuedBatches(),
                backend.lastFrameInstancedQueuedInstances() + additive.instanced().lastFrameQueuedInstances(),
                backend.lastFrameInstancedDrawCalls() + additive.instanced().lastFrameDrawCalls(),
                backend.lastFrameInstancedOverflowBatches() + additive.instanced().lastFrameOverflowBatches(),
                backend.lastFrameInstancedOverflowInstances() + additive.instanced().lastFrameOverflowInstances(),
                instancingSnapshot.lastFrameDuplicateInstances(),
                instancingSnapshot.lastFrameStaleInstancedBatches(),
                instancingSnapshot.lastFrameStaleInstancedInstances(),
                instancingSnapshot.lastFrameStaleIrisCompanionBatches(),
                instancingSnapshot.lastFrameStaleIrisCompanionInstances());
    }

    private static MdiFrameCounts mdiCounts(RenderBackendSnapshot backend, RenderBackendAdditiveSnapshot additive,
            RenderBackendInstancingSnapshot instancingSnapshot) {
        MdiAdditiveSnapshot additiveMdi = additive.mdi();
        return new MdiFrameCounts(
                backend.lastFrameMdiEligibleBatches() + additiveMdi.lastFrameEligibleBatches(),
                backend.lastFrameMdiDrawCalls() + additiveMdi.lastFrameDrawCalls(),
                instancingSnapshot.lastFrameMdiMultiDrawCalls(),
                backend.lastFrameMdiIndirectCommands() + additiveMdi.lastFrameIndirectCommands(),
                backend.lastFrameMdiNoSlotBatches() + additiveMdi.lastFrameNoSlotBatches(),
                backend.lastFrameMdiNoSlotInstances() + additiveMdi.lastFrameNoSlotInstances(),
                instancingSnapshot.lastFrameMdiPartialDrawFailures(),
                instancingSnapshot.lastFrameMdiStalePreparedGroups(),
                instancingSnapshot.lastFrameMdiStalePreparedCommands(),
                instancingSnapshot.lastFrameMdiDispatchDisableEvents(),
                instancingSnapshot.lastFrameMdiAtlasRepackFailures(),
                instancingSnapshot.lastFrameMdiAtlasInitFailures());
    }

    private record InstancedFrameCounts(
            long optimizedFlushCalls,
            long optimizedDuplicateFlushCalls,
            long optimizedDuplicatePresentSkips,
            long optimizedFlushNanos,
            long queuedBatches,
            long queuedInstances,
            long drawCalls,
            long overflowBatches,
            long overflowInstances,
            long duplicateInstances,
            long staleInstancedBatches,
            long staleInstancedInstances,
            long staleIrisCompanionBatches,
            long staleIrisCompanionInstances) {
    }

    private record MdiFrameCounts(
            long eligibleBatches,
            long drawCalls,
            long multiDrawCalls,
            long indirectCommands,
            long noSlotBatches,
            long noSlotInstances,
            long partialDrawFailures,
            long stalePreparedGroups,
            long stalePreparedCommands,
            long dispatchDisableEvents,
            long atlasRepackFailures,
            long atlasInitFailures) {
        private long indirectLoopDrawCalls() {
            return Math.max(0L, drawCalls - multiDrawCalls);
        }
    }
}

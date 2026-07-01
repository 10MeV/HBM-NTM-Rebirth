package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.client.render.HbmRenderFrameFlags;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public final class LegacyRenderLighting {
    private static final double INSIDE_EPSILON = 1.0E-4D;
    private static final double MAX_INTERPOLATED_EXTENT = 1.05D;
    private static final double MAX_AUTO_ANCHOR_LOCAL_TRANSLATION_SQ = 96.0D * 96.0D;
    private static final double LIGHT_CORNER_DETAIL_MARGIN_BLOCKS = 48.0D;
    private static final float MODEL_SAMPLE_INSET = 1.0F / 64.0F;
    private static final float MODEL_SAMPLE_SHELL = 0.55F;
    private static final long PROBE_CACHE_PRUNE_EVERY_FRAMES = 600L;
    private static final long PROBE_CACHE_STALE_AFTER_FRAMES = 600L;
    private static final ThreadLocal<LightProbe> CURRENT_INSTANCE_LIGHT_PROBE = new ThreadLocal<>();
    private static final ThreadLocal<ModelViewSamplingAnchor> CURRENT_MODEL_VIEW_ANCHOR = new ThreadLocal<>();
    private static final ThreadLocal<ModelViewSamplingContext> MODEL_VIEW_SAMPLING_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<ModelLightScratch> MODEL_LIGHT_SCRATCH =
            ThreadLocal.withInitial(ModelLightScratch::new);
    private static final Map<Long, CachedLightProbe> ANCHORED_PROBE_CACHE = new HashMap<>();
    private static final Map<Long, CachedSlicedLightProbe> SLICED_ANCHORED_PROBE_CACHE = new HashMap<>();
    private static long frameGeneration;
    private static long anchoredProbeQueries;
    private static long anchoredProbeHits;
    private static long anchoredProbeMisses;
    private static long slicedProbeQueries;
    private static long slicedProbeHits;
    private static long slicedProbeMisses;
    private static long probeCachePruneRuns;
    private static long probeCachePrunedEntries;
    private static long currentFrameAnchoredProbeQueries;
    private static long currentFrameAnchoredProbeHits;
    private static long currentFrameAnchoredProbeMisses;
    private static long currentFrameSlicedProbeQueries;
    private static long currentFrameSlicedProbeHits;
    private static long currentFrameSlicedProbeMisses;
    private static long currentFrameProbeCachePruneRuns;
    private static long currentFrameProbeCachePrunedEntries;
    private static long lastFrameAnchoredProbeQueries;
    private static long lastFrameAnchoredProbeHits;
    private static long lastFrameAnchoredProbeMisses;
    private static long lastFrameSlicedProbeQueries;
    private static long lastFrameSlicedProbeHits;
    private static long lastFrameSlicedProbeMisses;
    private static long lastFrameProbeCachePruneRuns;
    private static long lastFrameProbeCachePrunedEntries;

    public static void beginFrame() {
        rollProbeCacheFrameStats();
        advanceFrameGeneration();
        clearCurrentProbe();
        MODEL_VIEW_SAMPLING_CONTEXT.remove();
    }

    public static void endBlockEntityPass() {
        advanceFrameGeneration();
        clearCurrentProbe();
        MODEL_VIEW_SAMPLING_CONTEXT.remove();
    }

    private static void advanceFrameGeneration() {
        frameGeneration++;
        if (frameGeneration % PROBE_CACHE_PRUNE_EVERY_FRAMES == 0L) {
            pruneProbeCaches();
        }
    }

    private static void rollProbeCacheFrameStats() {
        lastFrameAnchoredProbeQueries = currentFrameAnchoredProbeQueries;
        lastFrameAnchoredProbeHits = currentFrameAnchoredProbeHits;
        lastFrameAnchoredProbeMisses = currentFrameAnchoredProbeMisses;
        lastFrameSlicedProbeQueries = currentFrameSlicedProbeQueries;
        lastFrameSlicedProbeHits = currentFrameSlicedProbeHits;
        lastFrameSlicedProbeMisses = currentFrameSlicedProbeMisses;
        lastFrameProbeCachePruneRuns = currentFrameProbeCachePruneRuns;
        lastFrameProbeCachePrunedEntries = currentFrameProbeCachePrunedEntries;
        currentFrameAnchoredProbeQueries = 0L;
        currentFrameAnchoredProbeHits = 0L;
        currentFrameAnchoredProbeMisses = 0L;
        currentFrameSlicedProbeQueries = 0L;
        currentFrameSlicedProbeHits = 0L;
        currentFrameSlicedProbeMisses = 0L;
        currentFrameProbeCachePruneRuns = 0L;
        currentFrameProbeCachePrunedEntries = 0L;
    }

    public static void clearCurrentProbe() {
        CURRENT_INSTANCE_LIGHT_PROBE.remove();
        CURRENT_MODEL_VIEW_ANCHOR.remove();
    }

    public static int resolveBlockEntityLight(BlockEntity blockEntity, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            publishUniformProbe(packedLight);
            return packedLight;
        }
        BlockPos pos = blockEntity.getBlockPos();
        LightProbe probe = sampleBoundsProbe(level, new AABB(pos), packedLight, true);
        int resolved = brightest(packedLight,
                LegacyLightSampleCache.sample(level, pos),
                LegacyLightSampleCache.sample(level, pos.above()),
                probe.c000(),
                probe.c100(),
                probe.c010(),
                probe.c110(),
                probe.c001(),
                probe.c101(),
                probe.c011(),
                probe.c111());
        publishProbe(blockEntity, probe.withResolvedLight(resolved));
        return resolved;
    }

    public static int resolveMultiblockLight(BlockEntity blockEntity, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            publishUniformProbe(packedLight);
            return packedLight;
        }
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof MultiblockCoreBlock coreBlock) {
            BlockPos pos = blockEntity.getBlockPos();
            LegacyMultiblockLayout layout = coreBlock.getMultiblockLayout(state, level, pos);
            if (layout != null) {
                return resolveBoundsLight(blockEntity, layout.structureBoundingBox(pos, 0.0D), packedLight);
            }
        }
        return resolveBlockEntityLight(blockEntity, packedLight);
    }

    public static int resolveMachineLight(BlockEntity blockEntity, BlockState state,
            LegacyMachineDefinition definition, int packedLight) {
        return resolveBoundsLight(blockEntity, definition.lightingBoundingBox(state, blockEntity.getBlockPos()),
                packedLight);
    }

    public static int resolveBoundsLight(BlockEntity blockEntity, AABB bounds, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            publishUniformProbe(packedLight);
            return packedLight;
        }
        if (!isFinite(bounds)) {
            return resolveBlockEntityLight(blockEntity, packedLight);
        }

        BlockPos pos = blockEntity.getBlockPos();
        boolean interpolationSafe = isInterpolationSafe(bounds);
        LightProbe probe = sampleBoundsProbe(level, bounds, packedLight, interpolationSafe);
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerY = (bounds.minY + bounds.maxY) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;
        double sampleTopY = insideMax(bounds.maxY);

        int resolved = brightest(packedLight,
                LegacyLightSampleCache.sample(level, pos),
                LegacyLightSampleCache.sample(level, pos.above()),
                LegacyLightSampleCache.sample(level, BlockPos.containing(centerX, bounds.minY, centerZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(centerX, centerY, centerZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(centerX, sampleTopY, centerZ)),
                probe.c000(),
                probe.c100(),
                probe.c010(),
                probe.c110(),
                probe.c001(),
                probe.c101(),
                probe.c011(),
                probe.c111());
        publishProbe(blockEntity, probe.withResolvedLight(resolved));
        return resolved;
    }

    public static LightProbe currentInstanceLightProbe(int packedLight) {
        if (packedLight == LightTexture.FULL_BRIGHT) {
            return LightProbe.uniform(packedLight);
        }
        LightProbe probe = CURRENT_INSTANCE_LIGHT_PROBE.get();
        if (probe != null && probe.resolvedLight() == packedLight) {
            return probe.interpolationSafe() ? probe : LightProbe.uniform(packedLight);
        }
        return LightProbe.uniform(packedLight);
    }

    public static LightProbe sampleModelViewLight(Matrix4f modelView, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int packedLight) {
        return sampleModelViewLight(modelView, 0L, minX, minY, minZ, maxX, maxY, maxZ, packedLight);
    }

    public static LightProbe sampleModelViewLight(Matrix4f modelView, long partIdentityHash,
            float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int packedLight) {
        if (packedLight == LightTexture.FULL_BRIGHT || modelView == null || !areFinite(minX, minY, minZ, maxX, maxY, maxZ)) {
            return LightProbe.uniform(packedLight);
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || minecraft.gameRenderer == null) {
            return currentInstanceLightProbe(packedLight);
        }
        ModelLightScratch scratch = MODEL_LIGHT_SCRATCH.get();
        ModelViewSamplingContext context = MODEL_VIEW_SAMPLING_CONTEXT.get();
        if (context != null && context.level() == level) {
            return sampleAnchoredModelViewLight(context, scratch, modelView, partIdentityHash,
                    minX, minY, minZ, maxX, maxY, maxZ, packedLight);
        }
        ModelViewSamplingAnchor anchor = CURRENT_MODEL_VIEW_ANCHOR.get();
        if (anchor != null && anchor.level() == level && anchor.resolvedLight() == packedLight) {
            return sampleBlockAnchoredModelViewLight(anchor, scratch, modelView, partIdentityHash,
                    minX, minY, minZ, maxX, maxY, maxZ, packedLight);
        }
        if (shouldUseUniformModelProbe(modelView)) {
            return LightProbe.uniform(packedLight);
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        scratch.inverseViewRotation.identity().set(RenderSystem.getInverseViewRotationMatrix());
        float insetX = boundedInset(minX, maxX);
        float insetY = boundedInset(minY, maxY);
        float insetZ = boundedInset(minZ, maxZ);
        float sampleMinX = minX + insetX;
        float sampleMinY = minY + insetY;
        float sampleMinZ = minZ + insetZ;
        float sampleMaxX = maxX - insetX;
        float sampleMaxY = maxY - insetY;
        float sampleMaxZ = maxZ - insetZ;

        int c000 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMinX, sampleMinY, sampleMinZ,
                shellMin(sampleMinX), shellMin(sampleMinY), shellMin(sampleMinZ),
                packedLight);
        int c100 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMaxX, sampleMinY, sampleMinZ,
                shellMax(sampleMaxX), shellMin(sampleMinY), shellMin(sampleMinZ),
                packedLight);
        int c010 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMinX, sampleMaxY, sampleMinZ,
                shellMin(sampleMinX), shellMax(sampleMaxY), shellMin(sampleMinZ),
                packedLight);
        int c110 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMaxX, sampleMaxY, sampleMinZ,
                shellMax(sampleMaxX), shellMax(sampleMaxY), shellMin(sampleMinZ),
                packedLight);
        int c001 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMinX, sampleMinY, sampleMaxZ,
                shellMin(sampleMinX), shellMin(sampleMinY), shellMax(sampleMaxZ),
                packedLight);
        int c101 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMaxX, sampleMinY, sampleMaxZ,
                shellMax(sampleMaxX), shellMin(sampleMinY), shellMax(sampleMaxZ),
                packedLight);
        int c011 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMinX, sampleMaxY, sampleMaxZ,
                shellMin(sampleMinX), shellMax(sampleMaxY), shellMax(sampleMaxZ),
                packedLight);
        int c111 = sampleModelCorner(level, camera, scratch, modelView,
                sampleMaxX, sampleMaxY, sampleMaxZ,
                shellMax(sampleMaxX), shellMax(sampleMaxY), shellMax(sampleMaxZ),
                packedLight);
        int resolved = brightest(packedLight, c000, c100, c010, c110, c001, c101, c011, c111);
        return new LightProbe(resolved, c000, c100, c010, c110, c001, c101, c011, c111, true);
    }

    public static SlicedLightProbe sampleModelViewSlicedLight(Matrix4f modelView, long partIdentityHash,
            float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int packedLight) {
        if (packedLight == LightTexture.FULL_BRIGHT || modelView == null || !areFinite(minX, minY, minZ, maxX, maxY, maxZ)) {
            return SlicedLightProbe.uniform(packedLight);
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || minecraft.gameRenderer == null) {
            return SlicedLightProbe.from(currentInstanceLightProbe(packedLight));
        }
        ModelLightScratch scratch = MODEL_LIGHT_SCRATCH.get();
        ModelViewSamplingContext context = MODEL_VIEW_SAMPLING_CONTEXT.get();
        if (context != null && context.level() == level) {
            scratch.localPose.set(context.inverseBaseModelView()).mul(modelView);
            return sampleAnchoredSlicedLocalPoseLight(context.level(), context.blockPos(), scratch, partIdentityHash,
                    minX, minY, minZ, maxX, maxY, maxZ, packedLight);
        }
        ModelViewSamplingAnchor anchor = CURRENT_MODEL_VIEW_ANCHOR.get();
        if (anchor != null && anchor.level() == level && anchor.resolvedLight() == packedLight) {
            Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
            scratch.inverseViewRotation.identity().set(RenderSystem.getInverseViewRotationMatrix());
            scratch.localPose.set(scratch.inverseViewRotation).mul(modelView);
            BlockPos blockPos = anchor.blockPos();
            scratch.localPose.m30(scratch.localPose.m30() - (float) (blockPos.getX() - camera.x));
            scratch.localPose.m31(scratch.localPose.m31() - (float) (blockPos.getY() - camera.y));
            scratch.localPose.m32(scratch.localPose.m32() - (float) (blockPos.getZ() - camera.z));
            if (!isPlausibleAutoAnchorLocalPose(scratch.localPose)) {
                return SlicedLightProbe.from(currentInstanceLightProbe(packedLight));
            }
            return sampleAnchoredSlicedLocalPoseLight(anchor.level(), blockPos, scratch, partIdentityHash,
                    minX, minY, minZ, maxX, maxY, maxZ, packedLight);
        }
        if (shouldUseUniformModelProbe(modelView)) {
            return SlicedLightProbe.uniform(packedLight);
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        scratch.inverseViewRotation.identity().set(RenderSystem.getInverseViewRotationMatrix());
        return sampleCameraSlicedModelViewLight(level, camera, scratch, modelView,
                minX, minY, minZ, maxX, maxY, maxZ, packedLight);
    }

    public static ModelViewSamplingScope pushModelViewSampling(BlockEntity blockEntity, Matrix4f baseModelView) {
        ModelViewSamplingContext previous = MODEL_VIEW_SAMPLING_CONTEXT.get();
        Level level = blockEntity.getLevel();
        if (level != null && baseModelView != null) {
            MODEL_VIEW_SAMPLING_CONTEXT.set(new ModelViewSamplingContext(level, blockEntity.getBlockPos(),
                    new Matrix4f(baseModelView).invert()));
        }
        return new ModelViewSamplingScope(previous);
    }

    private static LightProbe sampleAnchoredModelViewLight(ModelViewSamplingContext context,
            ModelLightScratch scratch, Matrix4f modelView, long partIdentityHash, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int packedLight) {
        scratch.localPose.set(context.inverseBaseModelView()).mul(modelView);
        return sampleAnchoredLocalPoseLight(context.level(), context.blockPos(), scratch, partIdentityHash,
                minX, minY, minZ, maxX, maxY, maxZ, packedLight);
    }

    private static LightProbe sampleBlockAnchoredModelViewLight(ModelViewSamplingAnchor anchor,
            ModelLightScratch scratch, Matrix4f modelView, long partIdentityHash, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null) {
            return currentInstanceLightProbe(packedLight);
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        scratch.inverseViewRotation.identity().set(RenderSystem.getInverseViewRotationMatrix());
        scratch.localPose.set(scratch.inverseViewRotation).mul(modelView);
        BlockPos blockPos = anchor.blockPos();
        scratch.localPose.m30(scratch.localPose.m30() - (float) (blockPos.getX() - camera.x));
        scratch.localPose.m31(scratch.localPose.m31() - (float) (blockPos.getY() - camera.y));
        scratch.localPose.m32(scratch.localPose.m32() - (float) (blockPos.getZ() - camera.z));
        if (!isPlausibleAutoAnchorLocalPose(scratch.localPose)) {
            return currentInstanceLightProbe(packedLight);
        }
        return sampleAnchoredLocalPoseLight(anchor.level(), blockPos, scratch, partIdentityHash,
                minX, minY, minZ, maxX, maxY, maxZ, packedLight);
    }

    private static boolean isPlausibleAutoAnchorLocalPose(Matrix4f localPose) {
        double tx = localPose.m30();
        double ty = localPose.m31();
        double tz = localPose.m32();
        return Double.isFinite(tx) && Double.isFinite(ty) && Double.isFinite(tz)
                && tx * tx + ty * ty + tz * tz <= MAX_AUTO_ANCHOR_LOCAL_TRANSLATION_SQ;
    }

    private static LightProbe sampleAnchoredLocalPoseLight(Level level, BlockPos blockPos,
            ModelLightScratch scratch, long partIdentityHash, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int packedLight) {
        if (shouldUseUniformModelProbe(blockPos)) {
            return LightProbe.uniform(packedLight);
        }
        long cacheKey = 0L;
        if (partIdentityHash != 0L) {
            recordAnchoredProbeQuery();
            cacheKey = anchoredProbeCacheKey(blockPos, partIdentityHash, packedLight, scratch.localPose);
            CachedLightProbe cached = ANCHORED_PROBE_CACHE.get(cacheKey);
            if (cached != null && cached.frame() == frameGeneration) {
                recordAnchoredProbeHit();
                return cached.probe();
            }
        }

        float insetX = boundedInset(minX, maxX);
        float insetY = boundedInset(minY, maxY);
        float insetZ = boundedInset(minZ, maxZ);
        float sampleMinX = minX + insetX;
        float sampleMinY = minY + insetY;
        float sampleMinZ = minZ + insetZ;
        float sampleMaxX = maxX - insetX;
        float sampleMaxY = maxY - insetY;
        float sampleMaxZ = maxZ - insetZ;

        int c000 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMinX, sampleMinY, sampleMinZ,
                shellMin(sampleMinX), shellMin(sampleMinY), shellMin(sampleMinZ), packedLight);
        int c100 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMaxX, sampleMinY, sampleMinZ,
                shellMax(sampleMaxX), shellMin(sampleMinY), shellMin(sampleMinZ), packedLight);
        int c010 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMinX, sampleMaxY, sampleMinZ,
                shellMin(sampleMinX), shellMax(sampleMaxY), shellMin(sampleMinZ), packedLight);
        int c110 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMaxX, sampleMaxY, sampleMinZ,
                shellMax(sampleMaxX), shellMax(sampleMaxY), shellMin(sampleMinZ), packedLight);
        int c001 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMinX, sampleMinY, sampleMaxZ,
                shellMin(sampleMinX), shellMin(sampleMinY), shellMax(sampleMaxZ), packedLight);
        int c101 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMaxX, sampleMinY, sampleMaxZ,
                shellMax(sampleMaxX), shellMin(sampleMinY), shellMax(sampleMaxZ), packedLight);
        int c011 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMinX, sampleMaxY, sampleMaxZ,
                shellMin(sampleMinX), shellMax(sampleMaxY), shellMax(sampleMaxZ), packedLight);
        int c111 = sampleAnchoredModelCorner(level, blockPos, scratch,
                sampleMaxX, sampleMaxY, sampleMaxZ,
                shellMax(sampleMaxX), shellMax(sampleMaxY), shellMax(sampleMaxZ), packedLight);
        int resolved = brightest(packedLight, c000, c100, c010, c110, c001, c101, c011, c111);
        LightProbe probe = new LightProbe(resolved, c000, c100, c010, c110, c001, c101, c011, c111, true);
        if (cacheKey != 0L) {
            ANCHORED_PROBE_CACHE.put(cacheKey, new CachedLightProbe(probe, frameGeneration));
            recordAnchoredProbeMiss();
        }
        return probe;
    }

    private static SlicedLightProbe sampleAnchoredSlicedLocalPoseLight(Level level, BlockPos blockPos,
            ModelLightScratch scratch, long partIdentityHash, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int packedLight) {
        if (shouldUseUniformModelProbe(blockPos)) {
            return SlicedLightProbe.uniform(packedLight);
        }
        long cacheKey = 0L;
        if (partIdentityHash != 0L) {
            recordSlicedProbeQuery();
            cacheKey = anchoredProbeCacheKey(blockPos, partIdentityHash ^ 0x2F4C7A9E3779B97FL,
                    packedLight, scratch.localPose);
            CachedSlicedLightProbe cached = SLICED_ANCHORED_PROBE_CACHE.get(cacheKey);
            if (cached != null && cached.frame() == frameGeneration) {
                recordSlicedProbeHit();
                return cached.probe();
            }
        }

        float insetX = boundedInset(minX, maxX);
        float insetY = boundedInset(minY, maxY);
        float insetZ = boundedInset(minZ, maxZ);
        float sampleMinX = minX + insetX;
        float sampleMinY = minY + insetY;
        float sampleMinZ = minZ + insetZ;
        float sampleMaxX = maxX - insetX;
        float sampleMaxY = maxY - insetY;
        float sampleMaxZ = maxZ - insetZ;

        int[] probes = new int[16];
        int index = 0;
        for (int slice = 0; slice < 4; slice++) {
            float y = lerp(slice / 3.0F, sampleMinY, sampleMaxY);
            float shellY = shellYForSlice(slice, y, sampleMinY, sampleMaxY);
            probes[index++] = sampleAnchoredModelShellXZ(level, blockPos, scratch,
                    sampleMinX, y, sampleMinZ, shellMin(sampleMinX), shellY, shellMin(sampleMinZ), packedLight);
            probes[index++] = sampleAnchoredModelShellXZ(level, blockPos, scratch,
                    sampleMaxX, y, sampleMinZ, shellMax(sampleMaxX), shellY, shellMin(sampleMinZ), packedLight);
            probes[index++] = sampleAnchoredModelShellXZ(level, blockPos, scratch,
                    sampleMinX, y, sampleMaxZ, shellMin(sampleMinX), shellY, shellMax(sampleMaxZ), packedLight);
            probes[index++] = sampleAnchoredModelShellXZ(level, blockPos, scratch,
                    sampleMaxX, y, sampleMaxZ, shellMax(sampleMaxX), shellY, shellMax(sampleMaxZ), packedLight);
        }
        SlicedLightProbe probe = new SlicedLightProbe(brightest(packedLight, probes), probes);
        if (cacheKey != 0L) {
            SLICED_ANCHORED_PROBE_CACHE.put(cacheKey, new CachedSlicedLightProbe(probe, frameGeneration));
            recordSlicedProbeMiss();
        }
        return probe;
    }

    private static void pruneProbeCaches() {
        long frame = frameGeneration;
        int before = ANCHORED_PROBE_CACHE.size() + SLICED_ANCHORED_PROBE_CACHE.size();
        ANCHORED_PROBE_CACHE.entrySet().removeIf(entry ->
                frame - entry.getValue().frame() > PROBE_CACHE_STALE_AFTER_FRAMES);
        SLICED_ANCHORED_PROBE_CACHE.entrySet().removeIf(entry ->
                frame - entry.getValue().frame() > PROBE_CACHE_STALE_AFTER_FRAMES);
        int removed = before - ANCHORED_PROBE_CACHE.size() - SLICED_ANCHORED_PROBE_CACHE.size();
        recordProbeCachePrune(removed);
    }

    private static void recordAnchoredProbeQuery() {
        anchoredProbeQueries++;
        currentFrameAnchoredProbeQueries++;
    }

    private static void recordAnchoredProbeHit() {
        anchoredProbeHits++;
        currentFrameAnchoredProbeHits++;
    }

    private static void recordAnchoredProbeMiss() {
        anchoredProbeMisses++;
        currentFrameAnchoredProbeMisses++;
    }

    private static void recordSlicedProbeQuery() {
        slicedProbeQueries++;
        currentFrameSlicedProbeQueries++;
    }

    private static void recordSlicedProbeHit() {
        slicedProbeHits++;
        currentFrameSlicedProbeHits++;
    }

    private static void recordSlicedProbeMiss() {
        slicedProbeMisses++;
        currentFrameSlicedProbeMisses++;
    }

    private static void recordProbeCachePrune(int removed) {
        probeCachePruneRuns++;
        currentFrameProbeCachePruneRuns++;
        if (removed > 0) {
            probeCachePrunedEntries += removed;
            currentFrameProbeCachePrunedEntries += removed;
        }
    }

    public static ProbeCacheSnapshot probeCacheSnapshot() {
        return new ProbeCacheSnapshot(
                frameGeneration,
                ANCHORED_PROBE_CACHE.size(),
                SLICED_ANCHORED_PROBE_CACHE.size(),
                anchoredProbeQueries,
                anchoredProbeHits,
                anchoredProbeMisses,
                slicedProbeQueries,
                slicedProbeHits,
                slicedProbeMisses,
                probeCachePruneRuns,
                probeCachePrunedEntries,
                currentFrameAnchoredProbeQueries,
                currentFrameAnchoredProbeHits,
                currentFrameAnchoredProbeMisses,
                currentFrameSlicedProbeQueries,
                currentFrameSlicedProbeHits,
                currentFrameSlicedProbeMisses,
                currentFrameProbeCachePruneRuns,
                currentFrameProbeCachePrunedEntries,
                lastFrameAnchoredProbeQueries,
                lastFrameAnchoredProbeHits,
                lastFrameAnchoredProbeMisses,
                lastFrameSlicedProbeQueries,
                lastFrameSlicedProbeHits,
                lastFrameSlicedProbeMisses,
                lastFrameProbeCachePruneRuns,
                lastFrameProbeCachePrunedEntries);
    }

    private static SlicedLightProbe sampleCameraSlicedModelViewLight(Level level, Vec3 camera,
            ModelLightScratch scratch, Matrix4f modelView, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int packedLight) {
        float insetX = boundedInset(minX, maxX);
        float insetY = boundedInset(minY, maxY);
        float insetZ = boundedInset(minZ, maxZ);
        float sampleMinX = minX + insetX;
        float sampleMinY = minY + insetY;
        float sampleMinZ = minZ + insetZ;
        float sampleMaxX = maxX - insetX;
        float sampleMaxY = maxY - insetY;
        float sampleMaxZ = maxZ - insetZ;

        int[] probes = new int[16];
        int index = 0;
        for (int slice = 0; slice < 4; slice++) {
            float y = lerp(slice / 3.0F, sampleMinY, sampleMaxY);
            float shellY = shellYForSlice(slice, y, sampleMinY, sampleMaxY);
            probes[index++] = sampleModelShellXZ(level, camera, scratch, modelView,
                    sampleMinX, y, sampleMinZ, shellMin(sampleMinX), shellY, shellMin(sampleMinZ), packedLight);
            probes[index++] = sampleModelShellXZ(level, camera, scratch, modelView,
                    sampleMaxX, y, sampleMinZ, shellMax(sampleMaxX), shellY, shellMin(sampleMinZ), packedLight);
            probes[index++] = sampleModelShellXZ(level, camera, scratch, modelView,
                    sampleMinX, y, sampleMaxZ, shellMin(sampleMinX), shellY, shellMax(sampleMaxZ), packedLight);
            probes[index++] = sampleModelShellXZ(level, camera, scratch, modelView,
                    sampleMaxX, y, sampleMaxZ, shellMax(sampleMaxX), shellY, shellMax(sampleMaxZ), packedLight);
        }
        return new SlicedLightProbe(brightest(packedLight, probes), probes);
    }

    private static long anchoredProbeCacheKey(BlockPos blockPos, long partIdentityHash, int packedLight,
            Matrix4f localPose) {
        long key = mix64(blockPos.asLong());
        key ^= mix64(partIdentityHash);
        key ^= mix64(packedLight & 0xFFFFFFFFL);
        key ^= matrixPoseHash(localPose);
        return key == 0L ? 1L : key;
    }

    private static long matrixPoseHash(Matrix4f matrix) {
        long hash = 0x9E3779B97F4A7C15L;
        hash = mixMatrixFloat(hash, matrix.m00());
        hash = mixMatrixFloat(hash, matrix.m01());
        hash = mixMatrixFloat(hash, matrix.m02());
        hash = mixMatrixFloat(hash, matrix.m03());
        hash = mixMatrixFloat(hash, matrix.m10());
        hash = mixMatrixFloat(hash, matrix.m11());
        hash = mixMatrixFloat(hash, matrix.m12());
        hash = mixMatrixFloat(hash, matrix.m13());
        hash = mixMatrixFloat(hash, matrix.m20());
        hash = mixMatrixFloat(hash, matrix.m21());
        hash = mixMatrixFloat(hash, matrix.m22());
        hash = mixMatrixFloat(hash, matrix.m23());
        hash = mixMatrixFloat(hash, matrix.m30());
        hash = mixMatrixFloat(hash, matrix.m31());
        hash = mixMatrixFloat(hash, matrix.m32());
        hash = mixMatrixFloat(hash, matrix.m33());
        return hash;
    }

    private static long mixMatrixFloat(long hash, float value) {
        return mix64(hash ^ (Float.floatToIntBits(value) & 0xFFFFFFFFL));
    }

    private static long mix64(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static boolean shouldUseUniformModelProbe(BlockPos blockPos) {
        double detailDistanceSq = lightCornerDetailDistanceSq();
        if (!Double.isFinite(detailDistanceSq)) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null) {
            return false;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        double dx = blockPos.getX() + 0.5D - camera.x;
        double dy = blockPos.getY() + 0.5D - camera.y;
        double dz = blockPos.getZ() + 0.5D - camera.z;
        return dx * dx + dy * dy + dz * dz > detailDistanceSq;
    }

    private static boolean shouldUseUniformModelProbe(Matrix4f modelView) {
        double detailDistanceSq = lightCornerDetailDistanceSq();
        if (!Double.isFinite(detailDistanceSq)) {
            return false;
        }
        double x = modelView.m30();
        double y = modelView.m31();
        double z = modelView.m32();
        return x * x + y * y + z * z > detailDistanceSq;
    }

    private static double lightCornerDetailDistanceSq() {
        int staticDistanceBlocks = HbmRenderFrameFlags.current().modelStaticRenderDistanceBlocks();
        if (staticDistanceBlocks <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        double detailBlocks = staticDistanceBlocks - LIGHT_CORNER_DETAIL_MARGIN_BLOCKS;
        if (detailBlocks <= 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return detailBlocks * detailBlocks;
    }

    private static double insideMax(double max) {
        return Math.nextAfter(max, Double.NEGATIVE_INFINITY) - INSIDE_EPSILON;
    }

    private static float boundedInset(float min, float max) {
        return Math.min(MODEL_SAMPLE_INSET, Math.max(0.0F, (max - min) * 0.5F));
    }

    private static float shellMin(float value) {
        return value - MODEL_SAMPLE_SHELL;
    }

    private static float shellMax(float value) {
        return value + MODEL_SAMPLE_SHELL;
    }

    private static float shellYForSlice(int slice, float y, float minY, float maxY) {
        if (Math.abs(maxY - minY) <= MODEL_SAMPLE_INSET * 2.0F) {
            return shellMax(y);
        }
        if (slice == 0) {
            return shellMin(y);
        }
        if (slice == 3) {
            return shellMax(y);
        }
        return y;
    }

    private static int sampleModelCorner(Level level, Vec3 camera, ModelLightScratch scratch, Matrix4f modelView,
            float innerX, float innerY, float innerZ, float shellX, float shellY, float shellZ,
            int packedLightFallback) {
        int shellLight = sampleModelCornerAt(level, camera, scratch, modelView, shellX, shellY, shellZ,
                packedLightFallback);
        int innerLight = sampleModelCornerAt(level, camera, scratch, modelView, innerX, innerY, innerZ,
                packedLightFallback);
        return brightest(packedLightFallback, shellLight, innerLight);
    }

    private static int sampleModelCornerAt(Level level, Vec3 camera, ModelLightScratch scratch, Matrix4f modelView,
            float x, float y, float z, int packedLightFallback) {
        Vector3f corner = scratch.corner.set(x, y, z);
        modelView.transformPosition(corner);
        scratch.inverseViewRotation.transformPosition(corner);
        BlockPos pos = BlockPos.containing(camera.x + corner.x(), camera.y + corner.y(), camera.z + corner.z());
        return LegacyLightSampleCache.sampleNonSolid(level, pos, packedLightFallback);
    }

    private static int sampleAnchoredModelCorner(Level level, BlockPos anchor, ModelLightScratch scratch,
            float innerX, float innerY, float innerZ, float shellX, float shellY, float shellZ,
            int packedLightFallback) {
        int shellLight = sampleAnchoredModelCornerAt(level, anchor, scratch, shellX, shellY, shellZ,
                packedLightFallback);
        int innerLight = sampleAnchoredModelCornerAt(level, anchor, scratch, innerX, innerY, innerZ,
                packedLightFallback);
        return brightest(packedLightFallback, shellLight, innerLight);
    }

    private static int sampleAnchoredModelCornerAt(Level level, BlockPos anchor, ModelLightScratch scratch,
            float x, float y, float z, int packedLightFallback) {
        Vector3f corner = scratch.corner.set(x, y, z);
        scratch.localPose.transformPosition(corner);
        BlockPos pos = BlockPos.containing(anchor.getX() + corner.x(), anchor.getY() + corner.y(),
                anchor.getZ() + corner.z());
        return LegacyLightSampleCache.sampleNonSolid(level, pos, packedLightFallback);
    }

    private static int sampleModelShellXZ(Level level, Vec3 camera, ModelLightScratch scratch, Matrix4f modelView,
            float innerX, float y, float innerZ, float shellX, float shellY, float shellZ,
            int packedLightFallback) {
        int shellLight = sampleModelCornerAt(level, camera, scratch, modelView, shellX, y, shellZ,
                packedLightFallback);
        int verticalShellLight = sampleModelCornerAt(level, camera, scratch, modelView, shellX, shellY, shellZ,
                packedLightFallback);
        int innerLight = sampleModelCornerAt(level, camera, scratch, modelView, innerX, y, innerZ,
                packedLightFallback);
        return brightest(packedLightFallback, shellLight, verticalShellLight, innerLight);
    }

    private static int sampleAnchoredModelShellXZ(Level level, BlockPos anchor, ModelLightScratch scratch,
            float innerX, float y, float innerZ, float shellX, float shellY, float shellZ,
            int packedLightFallback) {
        int shellLight = sampleAnchoredModelCornerAt(level, anchor, scratch, shellX, y, shellZ,
                packedLightFallback);
        int verticalShellLight = sampleAnchoredModelCornerAt(level, anchor, scratch, shellX, shellY, shellZ,
                packedLightFallback);
        int innerLight = sampleAnchoredModelCornerAt(level, anchor, scratch, innerX, y, innerZ,
                packedLightFallback);
        return brightest(packedLightFallback, shellLight, verticalShellLight, innerLight);
    }

    private static LightProbe sampleBoundsProbe(Level level, AABB bounds, int packedLight, boolean interpolationSafe) {
        double maxX = insideMax(bounds.maxX);
        double maxY = insideMax(bounds.maxY);
        double maxZ = insideMax(bounds.maxZ);
        return new LightProbe(
                packedLight,
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, bounds.minY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, maxY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, maxY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, bounds.minY, maxZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, bounds.minY, maxZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, maxY, maxZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, maxY, maxZ)),
                interpolationSafe);
    }

    private static void publishUniformProbe(int packedLight) {
        publishProbe(LightProbe.uniform(packedLight));
    }

    private static void publishProbe(LightProbe probe) {
        CURRENT_INSTANCE_LIGHT_PROBE.set(probe);
        CURRENT_MODEL_VIEW_ANCHOR.remove();
    }

    private static void publishProbe(BlockEntity blockEntity, LightProbe probe) {
        CURRENT_INSTANCE_LIGHT_PROBE.set(probe);
        Level level = blockEntity.getLevel();
        if (level == null) {
            CURRENT_MODEL_VIEW_ANCHOR.remove();
        } else {
            CURRENT_MODEL_VIEW_ANCHOR.set(new ModelViewSamplingAnchor(level, blockEntity.getBlockPos(),
                    probe.resolvedLight()));
        }
    }

    private static boolean isFinite(AABB bounds) {
        return Double.isFinite(bounds.minX) && Double.isFinite(bounds.minY) && Double.isFinite(bounds.minZ)
                && Double.isFinite(bounds.maxX) && Double.isFinite(bounds.maxY) && Double.isFinite(bounds.maxZ);
    }

    private static boolean areFinite(float... values) {
        for (float value : values) {
            if (!Float.isFinite(value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInterpolationSafe(AABB bounds) {
        return bounds.getXsize() <= MAX_INTERPOLATED_EXTENT
                && bounds.getYsize() <= MAX_INTERPOLATED_EXTENT
                && bounds.getZsize() <= MAX_INTERPOLATED_EXTENT;
    }

    private static int brightest(int first, int... candidates) {
        int sky = LightTexture.sky(first);
        int block = LightTexture.block(first);
        for (int candidate : candidates) {
            sky = Math.max(sky, LightTexture.sky(candidate));
            block = Math.max(block, LightTexture.block(candidate));
        }
        return LightTexture.pack(block, sky);
    }

    private static float lerp(float delta, float min, float max) {
        return min + (max - min) * delta;
    }

    private LegacyRenderLighting() {
    }

    private static final class ModelLightScratch {
        private final Matrix4f inverseViewRotation = new Matrix4f();
        private final Matrix4f localPose = new Matrix4f();
        private final Vector3f corner = new Vector3f();
    }

    private record ModelViewSamplingContext(Level level, BlockPos blockPos, Matrix4f inverseBaseModelView) {
    }

    private record ModelViewSamplingAnchor(Level level, BlockPos blockPos, int resolvedLight) {
    }

    private record CachedLightProbe(LightProbe probe, long frame) {
    }

    private record CachedSlicedLightProbe(SlicedLightProbe probe, long frame) {
    }

    public record ProbeCacheSnapshot(
            long frameGeneration,
            int anchoredCachedProbes,
            int slicedCachedProbes,
            long anchoredProbeQueries,
            long anchoredProbeHits,
            long anchoredProbeMisses,
            long slicedProbeQueries,
            long slicedProbeHits,
            long slicedProbeMisses,
            long probeCachePruneRuns,
            long probeCachePrunedEntries,
            long currentFrameAnchoredProbeQueries,
            long currentFrameAnchoredProbeHits,
            long currentFrameAnchoredProbeMisses,
            long currentFrameSlicedProbeQueries,
            long currentFrameSlicedProbeHits,
            long currentFrameSlicedProbeMisses,
            long currentFrameProbeCachePruneRuns,
            long currentFrameProbeCachePrunedEntries,
            long lastFrameAnchoredProbeQueries,
            long lastFrameAnchoredProbeHits,
            long lastFrameAnchoredProbeMisses,
            long lastFrameSlicedProbeQueries,
            long lastFrameSlicedProbeHits,
            long lastFrameSlicedProbeMisses,
            long lastFrameProbeCachePruneRuns,
            long lastFrameProbeCachePrunedEntries) {
    }

    public static final class ModelViewSamplingScope implements AutoCloseable {
        private final ModelViewSamplingContext previous;
        private boolean closed;

        private ModelViewSamplingScope(ModelViewSamplingContext previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (previous == null) {
                MODEL_VIEW_SAMPLING_CONTEXT.remove();
            } else {
                MODEL_VIEW_SAMPLING_CONTEXT.set(previous);
            }
        }
    }

    public record LightProbe(int resolvedLight, int c000, int c100, int c010, int c110,
                             int c001, int c101, int c011, int c111, boolean interpolationSafe) {
        public static LightProbe uniform(int packedLight) {
            return new LightProbe(packedLight, packedLight, packedLight, packedLight, packedLight,
                    packedLight, packedLight, packedLight, packedLight, false);
        }

        private LightProbe withResolvedLight(int packedLight) {
            return new LightProbe(packedLight, c000, c100, c010, c110, c001, c101, c011, c111,
                    interpolationSafe);
        }
    }

    public record SlicedLightProbe(int resolvedLight, int[] probes) {
        public SlicedLightProbe {
            if (probes.length != 16) {
                throw new IllegalArgumentException("Sliced light probe requires exactly 16 packed light values");
            }
            probes = probes.clone();
        }

        public static SlicedLightProbe uniform(int packedLight) {
            int[] probes = new int[16];
            for (int i = 0; i < probes.length; i++) {
                probes[i] = packedLight;
            }
            return new SlicedLightProbe(packedLight, probes);
        }

        public static SlicedLightProbe from(LightProbe probe) {
            int[] probes = new int[16];
            probes[0] = probe.c000();
            probes[1] = probe.c100();
            probes[2] = probe.c001();
            probes[3] = probe.c101();
            probes[4] = blend(probe.c000(), probe.c010(), 1.0F / 3.0F);
            probes[5] = blend(probe.c100(), probe.c110(), 1.0F / 3.0F);
            probes[6] = blend(probe.c001(), probe.c011(), 1.0F / 3.0F);
            probes[7] = blend(probe.c101(), probe.c111(), 1.0F / 3.0F);
            probes[8] = blend(probe.c000(), probe.c010(), 2.0F / 3.0F);
            probes[9] = blend(probe.c100(), probe.c110(), 2.0F / 3.0F);
            probes[10] = blend(probe.c001(), probe.c011(), 2.0F / 3.0F);
            probes[11] = blend(probe.c101(), probe.c111(), 2.0F / 3.0F);
            probes[12] = probe.c010();
            probes[13] = probe.c110();
            probes[14] = probe.c011();
            probes[15] = probe.c111();
            return new SlicedLightProbe(probe.resolvedLight(), probes);
        }

        public int probe(int index) {
            return probes[index];
        }

        private static int blend(int min, int max, float delta) {
            int block = Math.round(LightTexture.block(min) + (LightTexture.block(max) - LightTexture.block(min)) * delta);
            int sky = Math.round(LightTexture.sky(min) + (LightTexture.sky(max) - LightTexture.sky(min)) * delta);
            return LightTexture.pack(block, sky);
        }
    }
}

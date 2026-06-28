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
    private static final double LIGHT_CORNER_DETAIL_MARGIN_BLOCKS = 48.0D;
    private static final float MODEL_SAMPLE_INSET = 1.0F / 64.0F;
    private static final ThreadLocal<LightProbe> CURRENT_INSTANCE_LIGHT_PROBE = new ThreadLocal<>();
    private static final ThreadLocal<ModelViewSamplingContext> MODEL_VIEW_SAMPLING_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<ModelLightScratch> MODEL_LIGHT_SCRATCH =
            ThreadLocal.withInitial(ModelLightScratch::new);
    private static final Map<Long, CachedLightProbe> ANCHORED_PROBE_CACHE = new HashMap<>();

    public static void beginFrame() {
        clearCurrentProbe();
        MODEL_VIEW_SAMPLING_CONTEXT.remove();
        ANCHORED_PROBE_CACHE.clear();
    }

    public static void clearCurrentProbe() {
        CURRENT_INSTANCE_LIGHT_PROBE.remove();
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
        publishProbe(probe.withResolvedLight(resolved));
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
        publishProbe(probe.withResolvedLight(resolved));
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

        int c000 = sampleModelCorner(level, camera, scratch, modelView, sampleMinX, sampleMinY, sampleMinZ,
                packedLight);
        int c100 = sampleModelCorner(level, camera, scratch, modelView, sampleMaxX, sampleMinY, sampleMinZ,
                packedLight);
        int c010 = sampleModelCorner(level, camera, scratch, modelView, sampleMinX, sampleMaxY, sampleMinZ,
                packedLight);
        int c110 = sampleModelCorner(level, camera, scratch, modelView, sampleMaxX, sampleMaxY, sampleMinZ,
                packedLight);
        int c001 = sampleModelCorner(level, camera, scratch, modelView, sampleMinX, sampleMinY, sampleMaxZ,
                packedLight);
        int c101 = sampleModelCorner(level, camera, scratch, modelView, sampleMaxX, sampleMinY, sampleMaxZ,
                packedLight);
        int c011 = sampleModelCorner(level, camera, scratch, modelView, sampleMinX, sampleMaxY, sampleMaxZ,
                packedLight);
        int c111 = sampleModelCorner(level, camera, scratch, modelView, sampleMaxX, sampleMaxY, sampleMaxZ,
                packedLight);
        int resolved = brightest(packedLight, c000, c100, c010, c110, c001, c101, c011, c111);
        return new LightProbe(resolved, c000, c100, c010, c110, c001, c101, c011, c111, true);
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
        if (shouldUseUniformModelProbe(context.blockPos())) {
            return LightProbe.uniform(packedLight);
        }
        long cacheKey = 0L;
        if (partIdentityHash != 0L) {
            cacheKey = anchoredProbeCacheKey(context.blockPos(), partIdentityHash, packedLight, scratch.localPose);
            CachedLightProbe cached = ANCHORED_PROBE_CACHE.get(cacheKey);
            if (cached != null) {
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

        int c000 = sampleAnchoredModelCorner(context, scratch, sampleMinX, sampleMinY, sampleMinZ, packedLight);
        int c100 = sampleAnchoredModelCorner(context, scratch, sampleMaxX, sampleMinY, sampleMinZ, packedLight);
        int c010 = sampleAnchoredModelCorner(context, scratch, sampleMinX, sampleMaxY, sampleMinZ, packedLight);
        int c110 = sampleAnchoredModelCorner(context, scratch, sampleMaxX, sampleMaxY, sampleMinZ, packedLight);
        int c001 = sampleAnchoredModelCorner(context, scratch, sampleMinX, sampleMinY, sampleMaxZ, packedLight);
        int c101 = sampleAnchoredModelCorner(context, scratch, sampleMaxX, sampleMinY, sampleMaxZ, packedLight);
        int c011 = sampleAnchoredModelCorner(context, scratch, sampleMinX, sampleMaxY, sampleMaxZ, packedLight);
        int c111 = sampleAnchoredModelCorner(context, scratch, sampleMaxX, sampleMaxY, sampleMaxZ, packedLight);
        int resolved = brightest(packedLight, c000, c100, c010, c110, c001, c101, c011, c111);
        LightProbe probe = new LightProbe(resolved, c000, c100, c010, c110, c001, c101, c011, c111, true);
        if (cacheKey != 0L) {
            ANCHORED_PROBE_CACHE.put(cacheKey, new CachedLightProbe(probe));
        }
        return probe;
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
        int renderDistanceChunks = HbmRenderFrameFlags.current().renderDistanceChunks();
        if (renderDistanceChunks <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        double maxBlocks = renderDistanceChunks * 16.0D;
        double detailBlocks = maxBlocks - LIGHT_CORNER_DETAIL_MARGIN_BLOCKS;
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

    private static int sampleModelCorner(Level level, Vec3 camera, ModelLightScratch scratch, Matrix4f modelView,
            float x, float y, float z, int packedLightFallback) {
        Vector3f corner = scratch.corner.set(x, y, z);
        modelView.transformPosition(corner);
        scratch.inverseViewRotation.transformPosition(corner);
        BlockPos pos = BlockPos.containing(camera.x + corner.x(), camera.y + corner.y(), camera.z + corner.z());
        return LegacyLightSampleCache.sampleNonSolid(level, pos, packedLightFallback);
    }

    private static int sampleAnchoredModelCorner(ModelViewSamplingContext context, ModelLightScratch scratch,
            float x, float y, float z, int packedLightFallback) {
        Vector3f corner = scratch.corner.set(x, y, z);
        scratch.localPose.transformPosition(corner);
        BlockPos anchor = context.blockPos();
        BlockPos pos = BlockPos.containing(anchor.getX() + corner.x(), anchor.getY() + corner.y(),
                anchor.getZ() + corner.z());
        return LegacyLightSampleCache.sampleNonSolid(context.level(), pos, packedLightFallback);
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

    private LegacyRenderLighting() {
    }

    private static final class ModelLightScratch {
        private final Matrix4f inverseViewRotation = new Matrix4f();
        private final Matrix4f localPose = new Matrix4f();
        private final Vector3f corner = new Vector3f();
    }

    private record ModelViewSamplingContext(Level level, BlockPos blockPos, Matrix4f inverseBaseModelView) {
    }

    private record CachedLightProbe(LightProbe probe) {
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
}

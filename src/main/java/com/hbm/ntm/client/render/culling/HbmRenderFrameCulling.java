package com.hbm.ntm.client.render.culling;

import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.hbm.ntm.client.render.HbmRenderFrameFlags;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * Per-render-frame culling context shared by current CPU renderers and future GPU batching.
 */
public final class HbmRenderFrameCulling {
    private static final double OCCLUSION_NEAR_DISTANCE_SQ = 16.0D;
    private static final double OCCLUSION_CAMERA_REUSE_MAX_DIST_SQ = 0.25D;
    private static final int OCCLUSION_TTL_TICKS = 20;
    private static final int OCCLUSION_MAX_RAY_STEPS = 100;
    private static final int OCCLUSION_MAX_CACHE_ENTRIES = 16384;
    private static final int OCCLUSION_MAX_KEEP_MANHATTAN_BLOCKS = 192;
    private static final AtomicLong FRAME_GENERATION = new AtomicLong();
    private static final Map<Long, OcclusionCacheEntry> OCCLUSION_CACHE = new LinkedHashMap<>(1024, 0.75F, true);
    private static final BlockPos.MutableBlockPos RAYCAST_POS = new BlockPos.MutableBlockPos();
    private static volatile Frustum blockEntityFrustum;
    private static volatile Vec3 cameraPosition = Vec3.ZERO;
    private static volatile boolean projectionCaptured;
    private static volatile long geometryStamp;
    private static volatile FrameStats currentStats = new FrameStats(0L);
    private static volatile Snapshot lastSnapshot = Snapshot.empty();

    private HbmRenderFrameCulling() {
    }

    public static void beginFrame(Minecraft minecraft, RenderLevelStageEvent event) {
        long frame = FRAME_GENERATION.incrementAndGet();
        currentStats = new FrameStats(frame);
        cameraPosition = event.getCamera() == null ? minecraft.gameRenderer.getMainCamera().getPosition() : event.getCamera().getPosition();
        blockEntityFrustum = event.getFrustum();
        Matrix4f projection = event.getProjectionMatrix();
        projectionCaptured = projection != null;
        pruneOcclusionCache(minecraft);
    }

    public static void endFrame() {
        FrameStats stats = currentStats;
        lastSnapshot = new Snapshot(
                stats.frameGeneration,
                projectionCaptured,
                blockEntityFrustum != null,
                cameraPosition,
                stats.visibilityQueries.get(),
                stats.visibleQueries.get(),
                stats.frustumCulledQueries.get(),
                stats.distanceCulledQueries.get(),
                stats.shadowPassBypassQueries.get(),
                stats.noFrustumQueries.get(),
                stats.machineRendererSubmissions.get(),
                stats.machineRendererVertices.get(),
                stats.occlusionQueries.get(),
                stats.occlusionEnabledQueries.get(),
                stats.occlusionDisabledByConfigQueries.get(),
                stats.occlusionNoLevelQueries.get(),
                stats.occlusionNearBypassQueries.get(),
                stats.occlusionCacheHits.get(),
                stats.occlusionCacheMisses.get(),
                stats.occlusionCrossFrameReuses.get(),
                stats.occlusionRayTests.get(),
                stats.occlusionRaySteps.get(),
                stats.occlusionVisibleQueries.get(),
                stats.occlusionCulledQueries.get(),
                OCCLUSION_CACHE.size(),
                geometryStamp);
    }

    public static void clear() {
        blockEntityFrustum = null;
        cameraPosition = Vec3.ZERO;
        projectionCaptured = false;
        currentStats = new FrameStats(FRAME_GENERATION.get());
        lastSnapshot = Snapshot.empty();
        clearOcclusionCache();
    }

    public static boolean shouldRender(AABB bounds, double maxDistanceSq) {
        if (bounds == null) {
            return true;
        }
        FrameStats stats = currentStats;
        stats.visibilityQueries.incrementAndGet();
        if (HbmShaderCompatibilityDetector.isRenderingShadowPass()) {
            stats.shadowPassBypassQueries.incrementAndGet();
            stats.visibleQueries.incrementAndGet();
            return true;
        }
        if (maxDistanceSq > 0.0D && distanceToCenterSq(bounds, cameraPosition) > maxDistanceSq) {
            stats.distanceCulledQueries.incrementAndGet();
            return false;
        }
        Frustum frustum = blockEntityFrustum;
        if (frustum == null) {
            stats.noFrustumQueries.incrementAndGet();
            stats.visibleQueries.incrementAndGet();
            return true;
        }
        if (!frustum.isVisible(bounds)) {
            stats.frustumCulledQueries.incrementAndGet();
            return false;
        }
        stats.visibleQueries.incrementAndGet();
        return true;
    }

    public static boolean shouldRender(BlockEntity blockEntity, AABB bounds, double maxDistanceSq) {
        if (!shouldRender(bounds, maxDistanceSq)) {
            return false;
        }
        return shouldRenderOcclusion(blockEntity, bounds);
    }

    public static void recordMachineRendererSubmission(BlockEntity blockEntity, int approximateVertices) {
        FrameStats stats = currentStats;
        stats.machineRendererSubmissions.incrementAndGet();
        if (approximateVertices > 0) {
            stats.machineRendererVertices.addAndGet(approximateVertices);
        }
    }

    public static Snapshot snapshot() {
        return lastSnapshot;
    }

    public static void noteClientGeometryChanged() {
        geometryStamp++;
        clearOcclusionCache();
    }

    private static boolean shouldRenderOcclusion(BlockEntity blockEntity, AABB bounds) {
        FrameStats stats = currentStats;
        stats.occlusionQueries.incrementAndGet();
        if (!HbmRenderFrameFlags.current().occlusionCullingEnabled()) {
            stats.occlusionDisabledByConfigQueries.incrementAndGet();
            return true;
        }
        stats.occlusionEnabledQueries.incrementAndGet();
        if (blockEntity == null || bounds == null || blockEntity.getLevel() == null) {
            stats.occlusionNoLevelQueries.incrementAndGet();
            return true;
        }
        if (HbmShaderCompatibilityDetector.isRenderingShadowPass()) {
            stats.shadowPassBypassQueries.incrementAndGet();
            return true;
        }
        double distSq = distanceToCenterSq(bounds, cameraPosition);
        if (distSq < OCCLUSION_NEAR_DISTANCE_SQ) {
            stats.occlusionNearBypassQueries.incrementAndGet();
            return true;
        }
        BlockPos pos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();
        long key = occlusionCacheKey(pos);
        long frame = FRAME_GENERATION.get();
        OcclusionCacheEntry cached = OCCLUSION_CACHE.get(key);
        if (cached != null && cached.frame == frame) {
            stats.occlusionCacheHits.incrementAndGet();
            if (cached.visible) {
                stats.occlusionVisibleQueries.incrementAndGet();
            } else {
                stats.occlusionCulledQueries.incrementAndGet();
            }
            return cached.visible;
        }
        if (cached != null && canReuseOcclusion(cached, level)) {
            stats.occlusionCacheHits.incrementAndGet();
            stats.occlusionCrossFrameReuses.incrementAndGet();
            cached.frame = frame;
            if (cached.visible) {
                stats.occlusionVisibleQueries.incrementAndGet();
            } else {
                stats.occlusionCulledQueries.incrementAndGet();
            }
            return cached.visible;
        }
        stats.occlusionCacheMisses.incrementAndGet();
        boolean visible = raycastVisible(level, bounds, stats);
        OCCLUSION_CACHE.put(key, new OcclusionCacheEntry(visible, frame, level.getGameTime(),
                cameraPosition.x, cameraPosition.y, cameraPosition.z, geometryStamp));
        trimOcclusionCache();
        if (visible) {
            stats.occlusionVisibleQueries.incrementAndGet();
        } else {
            stats.occlusionCulledQueries.incrementAndGet();
        }
        return visible;
    }

    private static double distanceToCenterSq(AABB bounds, Vec3 camera) {
        double dx = (bounds.minX + bounds.maxX) * 0.5D - camera.x;
        double dy = (bounds.minY + bounds.maxY) * 0.5D - camera.y;
        double dz = (bounds.minZ + bounds.maxZ) * 0.5D - camera.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static long occlusionCacheKey(BlockPos pos) {
        long key = pos.asLong();
        return HbmShaderCompatibilityDetector.isRenderingShadowPass() ? key ^ (1L << 62) : key;
    }

    private static boolean canReuseOcclusion(OcclusionCacheEntry cached, Level level) {
        if (!cached.visible || cached.geometryStamp != geometryStamp) {
            return false;
        }
        long age = level.getGameTime() - cached.gameTime;
        if (age < 0L || age > OCCLUSION_TTL_TICKS) {
            return false;
        }
        double dx = cameraPosition.x - cached.cameraX;
        double dy = cameraPosition.y - cached.cameraY;
        double dz = cameraPosition.z - cached.cameraZ;
        return dx * dx + dy * dy + dz * dz <= OCCLUSION_CAMERA_REUSE_MAX_DIST_SQ;
    }

    private static boolean raycastVisible(Level level, AABB bounds, FrameStats stats) {
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerY = (bounds.minY + bounds.maxY) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;
        if (!isRayOccluded(level, bounds, centerX, centerY, centerZ, stats)) {
            return true;
        }
        return !isRayOccluded(level, bounds, bounds.minX, bounds.minY, bounds.minZ, stats)
                || !isRayOccluded(level, bounds, bounds.maxX, bounds.minY, bounds.minZ, stats)
                || !isRayOccluded(level, bounds, bounds.minX, bounds.maxY, bounds.minZ, stats)
                || !isRayOccluded(level, bounds, bounds.maxX, bounds.maxY, bounds.minZ, stats)
                || !isRayOccluded(level, bounds, bounds.minX, bounds.minY, bounds.maxZ, stats)
                || !isRayOccluded(level, bounds, bounds.maxX, bounds.minY, bounds.maxZ, stats)
                || !isRayOccluded(level, bounds, bounds.minX, bounds.maxY, bounds.maxZ, stats)
                || !isRayOccluded(level, bounds, bounds.maxX, bounds.maxY, bounds.maxZ, stats)
                || !isRayOccluded(level, bounds, centerX, bounds.minY, centerZ, stats)
                || !isRayOccluded(level, bounds, centerX, bounds.maxY, centerZ, stats)
                || !isRayOccluded(level, bounds, bounds.minX, centerY, centerZ, stats)
                || !isRayOccluded(level, bounds, bounds.maxX, centerY, centerZ, stats)
                || !isRayOccluded(level, bounds, centerX, centerY, bounds.minZ, stats)
                || !isRayOccluded(level, bounds, centerX, centerY, bounds.maxZ, stats);
    }

    private static boolean isRayOccluded(Level level, AABB bounds, double endX, double endY, double endZ,
            FrameStats stats) {
        stats.occlusionRayTests.incrementAndGet();
        double startX = cameraPosition.x;
        double startY = cameraPosition.y;
        double startZ = cameraPosition.z;
        int currentX = Mth.floor(startX);
        int currentY = Mth.floor(startY);
        int currentZ = Mth.floor(startZ);
        int targetX = Mth.floor(endX);
        int targetY = Mth.floor(endY);
        int targetZ = Mth.floor(endZ);
        int stepX = Integer.signum(targetX - currentX);
        int stepY = Integer.signum(targetY - currentY);
        int stepZ = Integer.signum(targetZ - currentZ);
        if (stepX == 0 && stepY == 0 && stepZ == 0) {
            return false;
        }
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double deltaX = stepX == 0 ? Double.MAX_VALUE : Math.abs(1.0D / dx);
        double deltaY = stepY == 0 ? Double.MAX_VALUE : Math.abs(1.0D / dy);
        double deltaZ = stepZ == 0 ? Double.MAX_VALUE : Math.abs(1.0D / dz);
        double maxX = stepX == 0 ? Double.MAX_VALUE
                : (stepX > 0 ? (currentX + 1.0D - startX) * deltaX : (startX - currentX) * deltaX);
        double maxY = stepY == 0 ? Double.MAX_VALUE
                : (stepY > 0 ? (currentY + 1.0D - startY) * deltaY : (startY - currentY) * deltaY);
        double maxZ = stepZ == 0 ? Double.MAX_VALUE
                : (stepZ > 0 ? (currentZ + 1.0D - startZ) * deltaZ : (startZ - currentZ) * deltaZ);
        int startBlockX = currentX;
        int startBlockY = currentY;
        int startBlockZ = currentZ;
        for (int step = 0; step < OCCLUSION_MAX_RAY_STEPS; step++) {
            stats.occlusionRaySteps.incrementAndGet();
            if (currentX == targetX && currentY == targetY && currentZ == targetZ) {
                return false;
            }
            if (currentX != startBlockX || currentY != startBlockY || currentZ != startBlockZ) {
                RAYCAST_POS.set(currentX, currentY, currentZ);
                if (!intersectsBlock(bounds, currentX, currentY, currentZ) && isOccluder(level, RAYCAST_POS)) {
                    return true;
                }
            }
            if (maxX < maxY) {
                if (maxX < maxZ) {
                    currentX += stepX;
                    maxX += deltaX;
                } else {
                    currentZ += stepZ;
                    maxZ += deltaZ;
                }
            } else if (maxY < maxZ) {
                currentY += stepY;
                maxY += deltaY;
            } else {
                currentZ += stepZ;
                maxZ += deltaZ;
            }
        }
        return false;
    }

    private static boolean intersectsBlock(AABB bounds, int x, int y, int z) {
        return x + 1.0D > bounds.minX && x < bounds.maxX
                && y + 1.0D > bounds.minY && y < bounds.maxY
                && z + 1.0D > bounds.minZ && z < bounds.maxZ;
    }

    private static boolean isOccluder(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.isSolidRender(level, pos);
    }

    private static void pruneOcclusionCache(Minecraft minecraft) {
        if (OCCLUSION_CACHE.isEmpty() || minecraft.player == null) {
            return;
        }
        BlockPos playerPos = minecraft.player.blockPosition();
        int px = playerPos.getX();
        int py = playerPos.getY();
        int pz = playerPos.getZ();
        OCCLUSION_CACHE.entrySet().removeIf(entry -> {
            BlockPos pos = BlockPos.of(entry.getKey() & ~(1L << 62));
            int dist = Math.abs(pos.getX() - px) + Math.abs(pos.getY() - py) + Math.abs(pos.getZ() - pz);
            return dist > OCCLUSION_MAX_KEEP_MANHATTAN_BLOCKS;
        });
    }

    private static void trimOcclusionCache() {
        int overflow = OCCLUSION_CACHE.size() - OCCLUSION_MAX_CACHE_ENTRIES;
        if (overflow <= 0) {
            return;
        }
        Iterator<Map.Entry<Long, OcclusionCacheEntry>> iterator = OCCLUSION_CACHE.entrySet().iterator();
        while (overflow-- > 0 && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    private static void clearOcclusionCache() {
        OCCLUSION_CACHE.clear();
    }

    private static final class FrameStats {
        private final long frameGeneration;
        private final AtomicLong visibilityQueries = new AtomicLong();
        private final AtomicLong visibleQueries = new AtomicLong();
        private final AtomicLong frustumCulledQueries = new AtomicLong();
        private final AtomicLong distanceCulledQueries = new AtomicLong();
        private final AtomicLong shadowPassBypassQueries = new AtomicLong();
        private final AtomicLong noFrustumQueries = new AtomicLong();
        private final AtomicLong machineRendererSubmissions = new AtomicLong();
        private final AtomicLong machineRendererVertices = new AtomicLong();
        private final AtomicLong occlusionQueries = new AtomicLong();
        private final AtomicLong occlusionEnabledQueries = new AtomicLong();
        private final AtomicLong occlusionDisabledByConfigQueries = new AtomicLong();
        private final AtomicLong occlusionNoLevelQueries = new AtomicLong();
        private final AtomicLong occlusionNearBypassQueries = new AtomicLong();
        private final AtomicLong occlusionCacheHits = new AtomicLong();
        private final AtomicLong occlusionCacheMisses = new AtomicLong();
        private final AtomicLong occlusionCrossFrameReuses = new AtomicLong();
        private final AtomicLong occlusionRayTests = new AtomicLong();
        private final AtomicLong occlusionRaySteps = new AtomicLong();
        private final AtomicLong occlusionVisibleQueries = new AtomicLong();
        private final AtomicLong occlusionCulledQueries = new AtomicLong();

        private FrameStats(long frameGeneration) {
            this.frameGeneration = frameGeneration;
        }
    }

    public record Snapshot(
            long frameGeneration,
            boolean projectionCaptured,
            boolean frustumCaptured,
            Vec3 cameraPosition,
            long visibilityQueries,
            long visibleQueries,
            long frustumCulledQueries,
            long distanceCulledQueries,
            long shadowPassBypassQueries,
            long noFrustumQueries,
            long machineRendererSubmissions,
            long machineRendererVertices,
            long occlusionQueries,
            long occlusionEnabledQueries,
            long occlusionDisabledByConfigQueries,
            long occlusionNoLevelQueries,
            long occlusionNearBypassQueries,
            long occlusionCacheHits,
            long occlusionCacheMisses,
            long occlusionCrossFrameReuses,
            long occlusionRayTests,
            long occlusionRaySteps,
            long occlusionVisibleQueries,
            long occlusionCulledQueries,
            int occlusionCacheEntries,
            long occlusionGeometryStamp) {
        private static Snapshot empty() {
            return new Snapshot(0L, false, false, Vec3.ZERO, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0, 0L);
        }
    }

    private static final class OcclusionCacheEntry {
        private final boolean visible;
        private long frame;
        private final long gameTime;
        private final double cameraX;
        private final double cameraY;
        private final double cameraZ;
        private final long geometryStamp;

        private OcclusionCacheEntry(boolean visible, long frame, long gameTime,
                double cameraX, double cameraY, double cameraZ, long geometryStamp) {
            this.visible = visible;
            this.frame = frame;
            this.gameTime = gameTime;
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.cameraZ = cameraZ;
            this.geometryStamp = geometryStamp;
        }
    }
}

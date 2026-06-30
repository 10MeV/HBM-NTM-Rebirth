package com.hbm.ntm.client.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class LegacyLightSampleCache {
    private static final int SOLID_SAMPLE_SENTINEL = Integer.MIN_VALUE;
    private static final long PRUNE_EVERY_FRAMES = 600L;
    private static final long STALE_AFTER_FRAMES = 600L;
    private static final Map<Long, CachedLight> CACHE = new HashMap<>();
    private static final Map<Long, CachedNonSolidLight> NON_SOLID_CACHE = new HashMap<>();
    private static final Direction[] NON_SOLID_NEIGHBORS = {
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST,
            Direction.DOWN
    };
    private static final AtomicLong FRAME_GENERATION = new AtomicLong();
    private static final AtomicLong SAMPLES = new AtomicLong();
    private static final AtomicLong HITS = new AtomicLong();
    private static final AtomicLong MISSES = new AtomicLong();
    private static final AtomicLong CLEARS = new AtomicLong();
    private static final AtomicLong LAST_FRAME_SAMPLES = new AtomicLong();
    private static final AtomicLong LAST_FRAME_HITS = new AtomicLong();
    private static final AtomicLong LAST_FRAME_MISSES = new AtomicLong();
    private static long currentFrameSamples;
    private static long currentFrameHits;
    private static long currentFrameMisses;

    private LegacyLightSampleCache() {
    }

    public static synchronized void beginFrame() {
        LAST_FRAME_SAMPLES.set(currentFrameSamples);
        LAST_FRAME_HITS.set(currentFrameHits);
        LAST_FRAME_MISSES.set(currentFrameMisses);
        currentFrameSamples = 0L;
        currentFrameHits = 0L;
        currentFrameMisses = 0L;
        long frame = FRAME_GENERATION.incrementAndGet();
        if (frame % PRUNE_EVERY_FRAMES == 0L) {
            pruneStale(frame);
        }
    }

    public static synchronized void endBlockEntityPass() {
        long frame = FRAME_GENERATION.incrementAndGet();
        if (frame % PRUNE_EVERY_FRAMES == 0L) {
            pruneStale(frame);
        }
    }

    public static synchronized void clear() {
        CACHE.clear();
        NON_SOLID_CACHE.clear();
        CLEARS.incrementAndGet();
    }

    public static synchronized int sample(Level level, BlockPos pos) {
        SAMPLES.incrementAndGet();
        currentFrameSamples++;
        long key = pos.asLong();
        long frame = FRAME_GENERATION.get();
        CachedLight cached = CACHE.get(key);
        if (cached != null && cached.frame() == frame) {
            HITS.incrementAndGet();
            currentFrameHits++;
            return cached.packedLight();
        }
        int light = LevelRenderer.getLightColor(level, pos);
        CACHE.put(key, new CachedLight(light, frame));
        MISSES.incrementAndGet();
        currentFrameMisses++;
        return light;
    }

    public static synchronized int sampleNonSolid(Level level, BlockPos pos, int packedLightFallback) {
        SAMPLES.incrementAndGet();
        currentFrameSamples++;
        long key = pos.asLong();
        long frame = FRAME_GENERATION.get();
        CachedNonSolidLight cached = NON_SOLID_CACHE.get(key);
        if (cached != null && cached.frame() == frame) {
            HITS.incrementAndGet();
            currentFrameHits++;
            return cached.resolve(packedLightFallback);
        }
        try {
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                int neighborLight = brightestNonSolidNeighbor(level, pos);
                if (neighborLight != SOLID_SAMPLE_SENTINEL) {
                    NON_SOLID_CACHE.put(key, new CachedNonSolidLight(neighborLight, true, frame));
                    MISSES.incrementAndGet();
                    currentFrameMisses++;
                    return brightest(packedLightFallback, neighborLight);
                }
                NON_SOLID_CACHE.put(key, CachedNonSolidLight.solid(frame));
                MISSES.incrementAndGet();
                currentFrameMisses++;
                return packedLightFallback;
            }
        } catch (RuntimeException ignored) {
            NON_SOLID_CACHE.put(key, CachedNonSolidLight.solid(frame));
            MISSES.incrementAndGet();
            currentFrameMisses++;
            return packedLightFallback;
        }
        int light = LevelRenderer.getLightColor(level, pos);
        CACHE.put(key, new CachedLight(light, frame));
        NON_SOLID_CACHE.put(key, new CachedNonSolidLight(light, false, frame));
        MISSES.incrementAndGet();
        currentFrameMisses++;
        return light;
    }

    private static int brightestNonSolidNeighbor(Level level, BlockPos pos) {
        int resolved = SOLID_SAMPLE_SENTINEL;
        for (Direction direction : NON_SOLID_NEIGHBORS) {
            BlockPos samplePos = pos.relative(direction);
            try {
                if (level.getBlockState(samplePos).isSolidRender(level, samplePos)) {
                    continue;
                }
                int light = LevelRenderer.getLightColor(level, samplePos);
                CACHE.put(samplePos.asLong(), new CachedLight(light, FRAME_GENERATION.get()));
                resolved = resolved == SOLID_SAMPLE_SENTINEL ? light : brightest(resolved, light);
            } catch (RuntimeException ignored) {
                // Try the remaining sides before falling back to the caller-provided packed light.
            }
        }
        return resolved;
    }

    private static int brightest(int first, int second) {
        return LightTexture.pack(
                Math.max(LightTexture.block(first), LightTexture.block(second)),
                Math.max(LightTexture.sky(first), LightTexture.sky(second)));
    }

    private static void pruneStale(long frame) {
        CACHE.entrySet().removeIf(entry -> frame - entry.getValue().frame() > STALE_AFTER_FRAMES);
        NON_SOLID_CACHE.entrySet().removeIf(entry -> frame - entry.getValue().frame() > STALE_AFTER_FRAMES);
        CLEARS.incrementAndGet();
    }

    public static synchronized Snapshot snapshot() {
        return new Snapshot(
                FRAME_GENERATION.get(),
                CACHE.size() + NON_SOLID_CACHE.size(),
                SAMPLES.get(),
                HITS.get(),
                MISSES.get(),
                CLEARS.get(),
                currentFrameSamples,
                currentFrameHits,
                currentFrameMisses,
                LAST_FRAME_SAMPLES.get(),
                LAST_FRAME_HITS.get(),
                LAST_FRAME_MISSES.get());
    }

    public record Snapshot(
            long frameGeneration,
            int cachedPositions,
            long samples,
            long hits,
            long misses,
            long clears,
            long currentFrameSamples,
            long currentFrameHits,
            long currentFrameMisses,
            long lastFrameSamples,
            long lastFrameHits,
            long lastFrameMisses) {
    }

    private record CachedLight(int packedLight, long frame) {
    }

    private record CachedNonSolidLight(int packedLight, boolean fallbackFloor, long frame) {
        private static CachedNonSolidLight solid(long frame) {
            return new CachedNonSolidLight(SOLID_SAMPLE_SENTINEL, false, frame);
        }

        private int resolve(int packedLightFallback) {
            if (packedLight == SOLID_SAMPLE_SENTINEL) {
                return packedLightFallback;
            }
            return fallbackFloor ? brightest(packedLightFallback, packedLight) : packedLight;
        }
    }
}

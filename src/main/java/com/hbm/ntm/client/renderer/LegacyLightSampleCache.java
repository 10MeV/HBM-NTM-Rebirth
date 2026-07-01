package com.hbm.ntm.client.renderer;

import java.util.HashMap;
import java.util.Map;
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
    private static long frameGeneration;
    private static long samples;
    private static long hits;
    private static long misses;
    private static long clears;
    private static long currentFrameSamples;
    private static long currentFrameHits;
    private static long currentFrameMisses;
    private static long lastFrameSamples;
    private static long lastFrameHits;
    private static long lastFrameMisses;

    private LegacyLightSampleCache() {
    }

    public static void beginFrame() {
        lastFrameSamples = currentFrameSamples;
        lastFrameHits = currentFrameHits;
        lastFrameMisses = currentFrameMisses;
        currentFrameSamples = 0L;
        currentFrameHits = 0L;
        currentFrameMisses = 0L;
        frameGeneration++;
        if (frameGeneration % PRUNE_EVERY_FRAMES == 0L) {
            pruneStale(frameGeneration);
        }
    }

    public static void endBlockEntityPass() {
        frameGeneration++;
        if (frameGeneration % PRUNE_EVERY_FRAMES == 0L) {
            pruneStale(frameGeneration);
        }
    }

    public static void clear() {
        CACHE.clear();
        NON_SOLID_CACHE.clear();
        clears++;
    }

    public static int sample(Level level, BlockPos pos) {
        samples++;
        currentFrameSamples++;
        long key = pos.asLong();
        long frame = frameGeneration;
        CachedLight cached = CACHE.get(key);
        if (cached != null && cached.frame() == frame) {
            hits++;
            currentFrameHits++;
            return cached.packedLight();
        }
        int light = LevelRenderer.getLightColor(level, pos);
        CACHE.put(key, new CachedLight(light, frame));
        misses++;
        currentFrameMisses++;
        return light;
    }

    public static int sampleNonSolid(Level level, BlockPos pos, int packedLightFallback) {
        samples++;
        currentFrameSamples++;
        long key = pos.asLong();
        long frame = frameGeneration;
        CachedNonSolidLight cached = NON_SOLID_CACHE.get(key);
        if (cached != null && cached.frame() == frame) {
            hits++;
            currentFrameHits++;
            return cached.resolve(packedLightFallback);
        }
        try {
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                int neighborLight = brightestNonSolidNeighbor(level, pos);
                if (neighborLight != SOLID_SAMPLE_SENTINEL) {
                    NON_SOLID_CACHE.put(key, new CachedNonSolidLight(neighborLight, true, frame));
                    misses++;
                    currentFrameMisses++;
                    return brightest(packedLightFallback, neighborLight);
                }
                NON_SOLID_CACHE.put(key, CachedNonSolidLight.solid(frame));
                misses++;
                currentFrameMisses++;
                return packedLightFallback;
            }
        } catch (RuntimeException ignored) {
            NON_SOLID_CACHE.put(key, CachedNonSolidLight.solid(frame));
            misses++;
            currentFrameMisses++;
            return packedLightFallback;
        }
        int light = LevelRenderer.getLightColor(level, pos);
        CACHE.put(key, new CachedLight(light, frame));
        NON_SOLID_CACHE.put(key, new CachedNonSolidLight(light, false, frame));
        misses++;
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
                CACHE.put(samplePos.asLong(), new CachedLight(light, frameGeneration));
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
        clears++;
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                frameGeneration,
                CACHE.size() + NON_SOLID_CACHE.size(),
                samples,
                hits,
                misses,
                clears,
                currentFrameSamples,
                currentFrameHits,
                currentFrameMisses,
                lastFrameSamples,
                lastFrameHits,
                lastFrameMisses);
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

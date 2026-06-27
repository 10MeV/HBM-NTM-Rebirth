package com.hbm.ntm.client.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class LegacyLightSampleCache {
    private static final Map<Long, Integer> CACHE = new HashMap<>();
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
        CACHE.clear();
        CLEARS.incrementAndGet();
        FRAME_GENERATION.incrementAndGet();
    }

    public static synchronized void clear() {
        CACHE.clear();
        CLEARS.incrementAndGet();
    }

    public static synchronized int sample(Level level, BlockPos pos) {
        SAMPLES.incrementAndGet();
        currentFrameSamples++;
        long key = pos.asLong();
        Integer cached = CACHE.get(key);
        if (cached != null) {
            HITS.incrementAndGet();
            currentFrameHits++;
            return cached;
        }
        int light = LevelRenderer.getLightColor(level, pos);
        CACHE.put(key, light);
        MISSES.incrementAndGet();
        currentFrameMisses++;
        return light;
    }

    public static synchronized Snapshot snapshot() {
        return new Snapshot(
                FRAME_GENERATION.get(),
                CACHE.size(),
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
}

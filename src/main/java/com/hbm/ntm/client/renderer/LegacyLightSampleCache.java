package com.hbm.ntm.client.renderer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class LegacyLightSampleCache {
    private static final int SOLID_SAMPLE_SENTINEL = Integer.MIN_VALUE;
    private static final long PRUNE_EVERY_FRAMES = 600L;
    private static final long STALE_AFTER_FRAMES = 600L;
    private static final Long2ObjectOpenHashMap<CachedLight> CACHE = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectOpenHashMap<CachedNonSolidLight> NON_SOLID_CACHE =
            new Long2ObjectOpenHashMap<>();
    private static final ThreadLocal<BlockPos.MutableBlockPos> SAMPLE_POS =
            ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private static final Direction[] NON_SOLID_NEIGHBORS = {
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST,
            Direction.DOWN
    };
    private static boolean lastSampleValid;
    private static int lastSampleX;
    private static int lastSampleY;
    private static int lastSampleZ;
    private static int lastSampleLight;
    private static long lastSampleFrame = -1L;
    private static boolean lastNonSolidValid;
    private static int lastNonSolidX;
    private static int lastNonSolidY;
    private static int lastNonSolidZ;
    private static CachedNonSolidLight lastNonSolidEntry;
    private static long lastNonSolidFrame = -1L;
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
        invalidateFastSlots();
        if (frameGeneration % PRUNE_EVERY_FRAMES == 0L) {
            pruneStale(frameGeneration);
        }
    }

    public static void endBlockEntityPass() {
        frameGeneration++;
        invalidateFastSlots();
        if (frameGeneration % PRUNE_EVERY_FRAMES == 0L) {
            pruneStale(frameGeneration);
        }
    }

    public static void clear() {
        CACHE.clear();
        NON_SOLID_CACHE.clear();
        invalidateFastSlots();
        clears++;
    }

    public static int sample(Level level, BlockPos pos) {
        return sample(level, pos.getX(), pos.getY(), pos.getZ());
    }

    public static int sample(Level level, double x, double y, double z) {
        return sample(level, Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public static int sample(Level level, int x, int y, int z) {
        samples++;
        currentFrameSamples++;
        long frame = frameGeneration;
        if (lastSampleValid && lastSampleFrame == frame
                && lastSampleX == x && lastSampleY == y && lastSampleZ == z) {
            hits++;
            currentFrameHits++;
            return lastSampleLight;
        }
        long key = BlockPos.asLong(x, y, z);
        CachedLight cached = CACHE.get(key);
        if (cached != null && cached.frame() == frame) {
            promoteSampleSlot(x, y, z, cached.packedLight(), frame);
            hits++;
            currentFrameHits++;
            return cached.packedLight();
        }
        BlockPos.MutableBlockPos pos = SAMPLE_POS.get();
        pos.set(x, y, z);
        int light = LevelRenderer.getLightColor(level, pos);
        cacheLight(key, cached, x, y, z, light, frame);
        misses++;
        currentFrameMisses++;
        return light;
    }

    public static int sampleNonSolid(Level level, BlockPos pos, int packedLightFallback) {
        return sampleNonSolid(level, pos.getX(), pos.getY(), pos.getZ(), packedLightFallback);
    }

    public static int sampleNonSolid(Level level, double x, double y, double z, int packedLightFallback) {
        return sampleNonSolid(level, Mth.floor(x), Mth.floor(y), Mth.floor(z), packedLightFallback);
    }

    public static int sampleNonSolid(Level level, int x, int y, int z, int packedLightFallback) {
        samples++;
        currentFrameSamples++;
        long frame = frameGeneration;
        if (lastNonSolidValid && lastNonSolidFrame == frame
                && lastNonSolidX == x && lastNonSolidY == y && lastNonSolidZ == z
                && lastNonSolidEntry != null && lastNonSolidEntry.frame() == frame) {
            hits++;
            currentFrameHits++;
            return lastNonSolidEntry.resolve(packedLightFallback);
        }
        long key = BlockPos.asLong(x, y, z);
        CachedNonSolidLight cached = NON_SOLID_CACHE.get(key);
        if (cached != null && cached.frame() == frame) {
            promoteNonSolidSlot(x, y, z, cached, frame);
            hits++;
            currentFrameHits++;
            return cached.resolve(packedLightFallback);
        }
        BlockPos.MutableBlockPos pos = SAMPLE_POS.get();
        pos.set(x, y, z);
        try {
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                int neighborLight = brightestNonSolidNeighbor(level, x, y, z);
                if (neighborLight != SOLID_SAMPLE_SENTINEL) {
                    cacheNonSolidLight(key, cached, x, y, z, neighborLight, true, frame);
                    misses++;
                    currentFrameMisses++;
                    return brightest(packedLightFallback, neighborLight);
                }
                cacheSolidSample(key, cached, x, y, z, frame);
                misses++;
                currentFrameMisses++;
                return packedLightFallback;
            }
        } catch (RuntimeException ignored) {
            cacheSolidSample(key, cached, x, y, z, frame);
            misses++;
            currentFrameMisses++;
            return packedLightFallback;
        }
        int light = LevelRenderer.getLightColor(level, pos);
        cacheLight(key, x, y, z, light, frame);
        cacheNonSolidLight(key, cached, x, y, z, light, false, frame);
        misses++;
        currentFrameMisses++;
        return light;
    }

    private static int brightestNonSolidNeighbor(Level level, int x, int y, int z) {
        int resolved = SOLID_SAMPLE_SENTINEL;
        BlockPos.MutableBlockPos samplePos = SAMPLE_POS.get();
        for (Direction direction : NON_SOLID_NEIGHBORS) {
            int sampleX = x + direction.getStepX();
            int sampleY = y + direction.getStepY();
            int sampleZ = z + direction.getStepZ();
            samplePos.set(sampleX, sampleY, sampleZ);
            try {
                if (level.getBlockState(samplePos).isSolidRender(level, samplePos)) {
                    continue;
                }
                int light = LevelRenderer.getLightColor(level, samplePos);
                cacheLight(BlockPos.asLong(sampleX, sampleY, sampleZ), sampleX, sampleY, sampleZ,
                        light, frameGeneration);
                resolved = resolved == SOLID_SAMPLE_SENTINEL ? light : brightest(resolved, light);
            } catch (RuntimeException ignored) {
                // Try the remaining sides before falling back to the caller-provided packed light.
            }
        }
        return resolved;
    }

    private static void cacheLight(long key, int x, int y, int z, int packedLight, long frame) {
        cacheLight(key, CACHE.get(key), x, y, z, packedLight, frame);
    }

    private static void cacheLight(long key, CachedLight cached, int x, int y, int z,
            int packedLight, long frame) {
        if (cached == null) {
            cached = new CachedLight();
            CACHE.put(key, cached);
        }
        cached.update(packedLight, frame);
        promoteSampleSlot(x, y, z, packedLight, frame);
    }

    private static void cacheNonSolidLight(long key, CachedNonSolidLight cached, int x, int y, int z,
            int packedLight, boolean fallbackFloor, long frame) {
        if (cached == null) {
            cached = new CachedNonSolidLight();
            NON_SOLID_CACHE.put(key, cached);
        }
        cached.update(packedLight, fallbackFloor, frame);
        promoteNonSolidSlot(x, y, z, cached, frame);
    }

    private static void cacheSolidSample(long key, CachedNonSolidLight cached, int x, int y, int z,
            long frame) {
        if (cached == null) {
            cached = new CachedNonSolidLight();
            NON_SOLID_CACHE.put(key, cached);
        }
        cached.updateSolid(frame);
        promoteNonSolidSlot(x, y, z, cached, frame);
    }

    private static void promoteSampleSlot(int x, int y, int z, int packedLight, long frame) {
        lastSampleValid = true;
        lastSampleX = x;
        lastSampleY = y;
        lastSampleZ = z;
        lastSampleLight = packedLight;
        lastSampleFrame = frame;
    }

    private static void promoteNonSolidSlot(int x, int y, int z, CachedNonSolidLight cached, long frame) {
        lastNonSolidValid = true;
        lastNonSolidX = x;
        lastNonSolidY = y;
        lastNonSolidZ = z;
        lastNonSolidEntry = cached;
        lastNonSolidFrame = frame;
    }

    private static void invalidateFastSlots() {
        lastSampleValid = false;
        lastSampleFrame = -1L;
        lastNonSolidValid = false;
        lastNonSolidEntry = null;
        lastNonSolidFrame = -1L;
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

    private static final class CachedLight {
        private int packedLight;
        private long frame;

        private void update(int packedLight, long frame) {
            this.packedLight = packedLight;
            this.frame = frame;
        }

        private int packedLight() {
            return packedLight;
        }

        private long frame() {
            return frame;
        }
    }

    private static final class CachedNonSolidLight {
        private int packedLight;
        private boolean fallbackFloor;
        private long frame;

        private void update(int packedLight, boolean fallbackFloor, long frame) {
            this.packedLight = packedLight;
            this.fallbackFloor = fallbackFloor;
            this.frame = frame;
        }

        private void updateSolid(long frame) {
            update(SOLID_SAMPLE_SENTINEL, false, frame);
        }

        private long frame() {
            return frame;
        }

        private int resolve(int packedLightFallback) {
            if (packedLight == SOLID_SAMPLE_SENTINEL) {
                return packedLightFallback;
            }
            return fallbackFloor ? brightest(packedLightFallback, packedLight) : packedLight;
        }
    }
}

package com.hbm.ntm.explosion;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.util.HbmBlockStateUtil;
import com.hbm.ntm.util.HbmConcurrentBitSet;
import com.hbm.ntm.world.SubChunkKey;
import com.hbm.ntm.world.SubChunkSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.BiConsumer;

/**
 * Threaded DDA ray tracer used by MK5 explosion algorithms 1 and 2.
 *
 * <p>World and chunk access stays on the server thread. The server thread
 * creates immutable section snapshots and caches block resistance values;
 * workers only read those snapshots and build a destruction bit map. Actual
 * block mutation is performed later on the server thread under the MK5 time
 * budget.</p>
 */
public class ExplosionNukeRayParallelized extends ExplosionNukeRayBatched {
    private static final int PARALLEL_NBT_VERSION = 1;
    private static final int PHASE_RESTART_TRACE = 0;
    private static final int PHASE_DESTROY = 1;
    private static final int PHASE_COMPLETE = 2;
    private static final int RAY_CLAIM_SIZE = 64;
    private static final float NUKE_RESISTANCE_CUTOFF = 2_000_000.0F;
    private static final float INITIAL_ENERGY_FACTOR = 0.3F;
    private static final double RESOLUTION_FACTOR = 1.0D;
    private static final double RAY_DIRECTION_EPSILON = 1.0E-6D;
    private static final double PROCESSING_EPSILON = 1.0E-9D;
    private static final float MIN_EFFECTIVE_DISTANCE = 0.01F;
    private static final AtomicInteger POOL_SEQUENCE = new AtomicInteger();

    private final Level ddaLevel;
    private final double explosionX;
    private final double explosionY;
    private final double explosionZ;
    private final int originX;
    private final int originY;
    private final int originZ;
    private final int strength;
    private final int radius;
    private final int algorithm;
    private final int minBuildHeight;
    private final int maxBuildHeight;
    private final int bitSetSize;
    private final int rayCount;
    private final BiConsumer<Integer, Integer> chunkLoader;
    private final List<SubChunkKey> cacheKeys;
    private final Map<SubChunkKey, SubChunkSnapshot> snapshots;
    private final Map<BlockState, Float> resistanceCache = new HashMap<>();
    private final ConcurrentMap<ChunkPos, HbmConcurrentBitSet> destructionMap;
    private final ConcurrentMap<ChunkPos, ConcurrentMap<Integer, DoubleAdder>> damageMap;
    private final ArrayDeque<ChunkPos> orderedChunks = new ArrayDeque<>();
    private final AtomicInteger nextRay = new AtomicInteger();
    private final AtomicInteger activeWorkers = new AtomicInteger();
    private final AtomicReference<Throwable> workerFailure = new AtomicReference<>();

    private int cacheCursor;
    private volatile boolean cacheComplete;
    private volatile boolean workersStarted;
    private volatile boolean traceComplete;
    private volatile boolean consolidationComplete;
    private volatile boolean destructionOrderBuilt;
    private volatile boolean destroyComplete;
    private volatile boolean cancelled;
    private boolean failureReported;
    private boolean batchedSaveFallback;
    private ExecutorService executor;

    public ExplosionNukeRayParallelized(Level level, double x, double y, double z, int strength, int speed, int radius) {
        this(level, x, y, z, strength, speed, radius, (chunkX, chunkZ) -> {
        });
    }

    public ExplosionNukeRayParallelized(Level level, double x, double y, double z, int strength, int speed, int radius,
            BiConsumer<Integer, Integer> chunkLoader) {
        super(level, (int) x, (int) y, (int) z, strength, speed, radius, chunkLoader);
        this.ddaLevel = level;
        this.explosionX = x;
        this.explosionY = y;
        this.explosionZ = z;
        this.originX = (int) Math.floor(x);
        this.originY = (int) Math.floor(y);
        this.originZ = (int) Math.floor(z);
        this.strength = strength;
        this.radius = Math.max(0, radius);
        this.algorithm = BombConfig.explosionAlgorithm() == 1 ? 1 : 2;
        this.minBuildHeight = level.getMinBuildHeight();
        this.maxBuildHeight = level.getMaxBuildHeight();
        this.bitSetSize = Math.max(0, maxBuildHeight - minBuildHeight) * 16 * 16;
        this.rayCount = Math.max(0, (int) (2.5D * Math.PI * (double) strength * (double) strength
                * RESOLUTION_FACTOR));
        this.chunkLoader = chunkLoader;
        this.cacheKeys = SubChunkKey.aroundSphere(level, new BlockPos(originX, originY, originZ), this.radius, 14);
        this.snapshots = new HashMap<>(Math.max(16, cacheKeys.size()));
        int chunkCapacity = Math.max(16, (int) cacheKeys.stream().map(SubChunkKey::chunkLong).distinct().count());
        this.destructionMap = new ConcurrentHashMap<>(chunkCapacity);
        this.damageMap = new ConcurrentHashMap<>(chunkCapacity);
    }

    @Override
    public void cacheChunksTick(int processTimeMs) {
        if (batchedSaveFallback) {
            super.cacheChunksTick(processTimeMs);
            return;
        }
        if (cancelled || destroyComplete || workersStarted || ddaLevel.isClientSide()) {
            return;
        }

        long deadline = deadline(processTimeMs);
        while (cacheCursor < cacheKeys.size() && System.nanoTime() < deadline) {
            cacheSection(cacheKeys.get(cacheCursor++));
        }
        if (cacheCursor >= cacheKeys.size()) {
            cacheComplete = true;
            startWorkers();
        }
    }

    private void cacheSection(SubChunkKey key) {
        chunkLoader.accept(key.getChunkXPos(), key.getChunkZPos());
        SubChunkSnapshot snapshot = SubChunkSnapshot.getSnapshot(ddaLevel, key, BombConfig.chunkLoadingEnabled());
        snapshots.put(key, snapshot);
        if (!snapshot.isEmpty()) {
            for (BlockState state : snapshot.paletteSnapshot()) {
                if (!state.isAir()) {
                    resistanceCache.computeIfAbsent(state, ExplosionNukeRayParallelized::readNukeResistance);
                }
            }
        }
    }

    private synchronized void startWorkers() {
        if (workersStarted || cancelled || !cacheComplete) {
            return;
        }
        workersStarted = true;
        if (rayCount == 0 || radius == 0 || bitSetSize == 0) {
            traceComplete = true;
            consolidationComplete = true;
            snapshots.clear();
            resistanceCache.clear();
            return;
        }

        int workerCount = Math.max(1, Math.min(rayCount,
                Runtime.getRuntime().availableProcessors() - 1));
        activeWorkers.set(workerCount);
        int poolId = POOL_SEQUENCE.incrementAndGet();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable,
                    "HBM-MK5-DDA-" + poolId + "-" + activeWorkers.get());
            thread.setDaemon(true);
            return thread;
        };
        executor = Executors.newFixedThreadPool(workerCount, threadFactory);
        for (int i = 0; i < workerCount; i++) {
            executor.execute(this::traceWorker);
        }
    }

    private void traceWorker() {
        try {
            while (!cancelled && !Thread.currentThread().isInterrupted()) {
                int firstRay = nextRay.getAndAdd(RAY_CLAIM_SIZE);
                if (firstRay >= rayCount) {
                    break;
                }
                int endRay = Math.min(rayCount, firstRay + RAY_CLAIM_SIZE);
                for (int ray = firstRay; ray < endRay && !cancelled; ray++) {
                    traceRay(ray);
                }
            }
        } catch (Throwable throwable) {
            workerFailure.compareAndSet(null, throwable);
            cancelled = true;
        } finally {
            if (activeWorkers.decrementAndGet() == 0) {
                finishTracing();
            }
        }
    }

    private void finishTracing() {
        traceComplete = true;
        if (!cancelled && workerFailure.get() == null && algorithm == 2) {
            runConsolidation();
        } else {
            consolidationComplete = true;
        }
        damageMap.clear();
        snapshots.clear();
        resistanceCache.clear();
        ExecutorService currentExecutor = executor;
        if (currentExecutor != null) {
            currentExecutor.shutdown();
        }
    }

    private void traceRay(int rayIndex) {
        Direction direction = directionFor(rayIndex, rayCount);
        double directionX = direction.x;
        double directionY = direction.y;
        double directionZ = direction.z;
        int x = originX;
        int y = originY;
        int z = originZ;
        int stepX = step(directionX);
        int stepY = step(directionY);
        int stepZ = step(directionZ);
        double tDeltaX = delta(directionX, stepX);
        double tDeltaY = delta(directionY, stepY);
        double tDeltaZ = delta(directionZ, stepZ);
        double tMaxX = firstBoundary(explosionX, x, stepX, tDeltaX);
        double tMaxY = firstBoundary(explosionY, y, stepY, tDeltaY);
        double tMaxZ = firstBoundary(explosionZ, z, stepZ, tDeltaZ);
        double currentRayPosition = 0.0D;
        float energy = strength * INITIAL_ENERGY_FACTOR;

        while (energy > 0.0F && !cancelled && !Thread.currentThread().isInterrupted()) {
            if (y < minBuildHeight || y >= maxBuildHeight || currentRayPosition >= radius - PROCESSING_EPSILON) {
                break;
            }

            double voxelExit = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
            double segmentLength = voxelExit - currentRayPosition;
            boolean stopAfterSegment = false;
            if (currentRayPosition + segmentLength > radius - PROCESSING_EPSILON) {
                segmentLength = Math.max(0.0D, radius - currentRayPosition);
                stopAfterSegment = true;
            }

            if (segmentLength > PROCESSING_EPSILON) {
                SubChunkKey snapshotKey = SubChunkKey.ofBlock(x, y, z);
                SubChunkSnapshot snapshot = snapshots.get(snapshotKey);
                if (snapshot != null && !snapshot.isEmpty()) {
                    BlockState state = snapshot.getBlockStateUnchecked(x & 15, y & 15, z & 15);
                    if (!state.isAir()) {
                        float resistance = cachedResistance(state);
                        if (resistance >= NUKE_RESISTANCE_CUTOFF) {
                            energy = 0.0F;
                        } else {
                            float damage = (float) (energyLoss(resistance, currentRayPosition) * segmentLength);
                            energy -= damage;
                            if (damage > 0.0F) {
                                int bitIndex = bitIndex(x, y, z);
                                ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
                                if (algorithm == 2) {
                                    damageMap.computeIfAbsent(chunkPos, ignored -> new ConcurrentHashMap<>(256))
                                            .computeIfAbsent(bitIndex, ignored -> new DoubleAdder())
                                            .add(damage);
                                } else if (energy > 0.0F) {
                                    destructionMap.computeIfAbsent(chunkPos,
                                            ignored -> new HbmConcurrentBitSet(bitSetSize)).set(bitIndex);
                                }
                            }
                        }
                    }
                }
            }

            currentRayPosition = voxelExit;
            if (energy <= 0.0F || stopAfterSegment) {
                break;
            }
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    tMaxX += tDeltaX;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            } else if (tMaxY < tMaxZ) {
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
        }
    }

    private void runConsolidation() {
        for (Map.Entry<ChunkPos, ConcurrentMap<Integer, DoubleAdder>> chunkEntry : damageMap.entrySet()) {
            ChunkPos chunkPos = chunkEntry.getKey();
            HbmConcurrentBitSet destructionBits = null;
            for (Map.Entry<Integer, DoubleAdder> damageEntry : chunkEntry.getValue().entrySet()) {
                int bitIndex = damageEntry.getKey();
                float accumulatedDamage = (float) damageEntry.getValue().sum();
                if (accumulatedDamage <= 0.0F) {
                    continue;
                }
                int y = minBuildHeight + (bitIndex >>> 8);
                int x = (chunkPos.x << 4) | ((bitIndex >>> 4) & 15);
                int z = (chunkPos.z << 4) | (bitIndex & 15);
                SubChunkSnapshot snapshot = snapshots.get(SubChunkKey.ofBlock(x, y, z));
                if (snapshot == null || snapshot.isEmpty()) {
                    continue;
                }
                BlockState state = snapshot.getBlockStateUnchecked(x & 15, y & 15, z & 15);
                if (state.isAir() || accumulatedDamage < cachedResistance(state) * RESOLUTION_FACTOR) {
                    continue;
                }
                if (destructionBits == null) {
                    destructionBits = destructionMap.computeIfAbsent(chunkPos,
                            ignored -> new HbmConcurrentBitSet(bitSetSize));
                }
                destructionBits.set(bitIndex);
            }
        }
        consolidationComplete = true;
    }

    @Override
    public void destructionTick(int processTimeMs) {
        if (batchedSaveFallback) {
            super.destructionTick(processTimeMs);
            return;
        }
        if (destroyComplete || !traceComplete || !consolidationComplete) {
            return;
        }
        Throwable failure = workerFailure.get();
        if (failure != null) {
            if (!failureReported) {
                failureReported = true;
                HbmNtm.LOGGER.error("MK5 parallel DDA worker failed; falling back to the batched worker", failure);
            }
            cancelDda(false);
            batchedSaveFallback = true;
            super.cacheChunksTick(processTimeMs);
            super.destructionTick(processTimeMs);
            return;
        }
        if (!destructionOrderBuilt) {
            List<ChunkPos> chunks = new ArrayList<>(destructionMap.keySet());
            chunks.sort(Comparator.comparingInt(this::chunkDistance));
            orderedChunks.addAll(chunks);
            destructionOrderBuilt = true;
        }

        long deadline = deadline(processTimeMs);
        while (!orderedChunks.isEmpty() && System.nanoTime() < deadline) {
            ChunkPos chunkPos = orderedChunks.peekFirst();
            HbmConcurrentBitSet bits = destructionMap.get(chunkPos);
            if (bits == null || bits.isEmpty()) {
                destructionMap.remove(chunkPos);
                orderedChunks.removeFirst();
                continue;
            }
            chunkLoader.accept(chunkPos.x, chunkPos.z);
            ddaLevel.getChunk(chunkPos.x, chunkPos.z);
            int bit = bits.nextSetBit(0);
            while (bit >= 0 && System.nanoTime() < deadline) {
                clearBit(chunkPos, bit);
                bits.clear(bit);
                bit = bits.nextSetBit(bit + 1);
            }
            if (bits.isEmpty()) {
                destructionMap.remove(chunkPos);
                orderedChunks.removeFirst();
            }
        }
        if (orderedChunks.isEmpty() && destructionMap.isEmpty()) {
            destroyComplete = true;
        }
    }

    private void clearBit(ChunkPos chunkPos, int bitIndex) {
        int y = minBuildHeight + (bitIndex >>> 8);
        if (y < minBuildHeight || y >= maxBuildHeight) {
            return;
        }
        int x = (chunkPos.x << 4) | ((bitIndex >>> 4) & 15);
        int z = (chunkPos.z << 4) | (bitIndex & 15);
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = ddaLevel.getBlockState(pos);
        if (!state.isAir() || !state.getFluidState().isEmpty()) {
            LegacyExplosionFluidCleanup.clearBlockOrLegacyLiquidNeighborhood(ddaLevel, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isComplete() {
        return batchedSaveFallback ? super.isComplete() : destroyComplete;
    }

    @Override
    public void cancel() {
        if (batchedSaveFallback) {
            super.cancel();
            return;
        }
        cancelDda(true);
    }

    private void cancelDda(boolean complete) {
        cancelled = true;
        ExecutorService currentExecutor = executor;
        if (currentExecutor != null) {
            currentExecutor.shutdownNow();
            try {
                currentExecutor.awaitTermination(100L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        // HashMap snapshots are read by workers. If a worker did not react to
        // interruption within the short join window, the last worker owns the
        // cleanup in finishTracing() instead of racing a HashMap.clear().
        if (activeWorkers.get() == 0) {
            snapshots.clear();
            resistanceCache.clear();
        }
        damageMap.clear();
        destructionMap.clear();
        orderedChunks.clear();
        if (complete) {
            destroyComplete = true;
        }
    }

    @Override
    public void saveToNbt(CompoundTag tag, String name) {
        if (batchedSaveFallback) {
            super.saveToNbt(tag, name);
            return;
        }
        tag.putInt(name + "parallelVersion", PARALLEL_NBT_VERSION);
        if (destroyComplete) {
            tag.putInt(name + "parallelPhase", PHASE_COMPLETE);
            return;
        }
        if (!consolidationComplete) {
            // No world mutation happens before consolidation, so restarting the
            // trace after a load is lossless and avoids serializing live workers.
            tag.putInt(name + "parallelPhase", PHASE_RESTART_TRACE);
            return;
        }

        tag.putInt(name + "parallelPhase", PHASE_DESTROY);
        ListTag chunks = new ListTag();
        for (Map.Entry<ChunkPos, HbmConcurrentBitSet> entry : destructionMap.entrySet()) {
            HbmConcurrentBitSet bits = entry.getValue();
            int[] packedBits = new int[(int) Math.min(Integer.MAX_VALUE, bits.cardinality())];
            int index = 0;
            for (int bit = bits.nextSetBit(0); bit >= 0 && index < packedBits.length;
                    bit = bits.nextSetBit(bit + 1)) {
                packedBits[index++] = bit;
            }
            if (index != packedBits.length) {
                packedBits = java.util.Arrays.copyOf(packedBits, index);
            }
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("x", entry.getKey().x);
            chunkTag.putInt("z", entry.getKey().z);
            chunkTag.putIntArray("bits", packedBits);
            chunks.add(chunkTag);
        }
        tag.put(name + "parallelChunks", chunks);
    }

    @Override
    public void readFromNbt(CompoundTag tag, String name) {
        if (!tag.contains(name + "parallelVersion")) {
            batchedSaveFallback = true;
            cancelDda(false);
            super.readFromNbt(tag, name);
            return;
        }

        int phase = tag.getInt(name + "parallelPhase");
        if (phase == PHASE_COMPLETE) {
            cacheComplete = true;
            workersStarted = true;
            traceComplete = true;
            consolidationComplete = true;
            destructionOrderBuilt = true;
            destroyComplete = true;
            return;
        }
        if (phase != PHASE_DESTROY) {
            return;
        }

        ListTag chunks = tag.getList(name + "parallelChunks", 10);
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag chunkTag = chunks.getCompound(i);
            ChunkPos chunkPos = new ChunkPos(chunkTag.getInt("x"), chunkTag.getInt("z"));
            HbmConcurrentBitSet bits = new HbmConcurrentBitSet(bitSetSize);
            for (int bit : chunkTag.getIntArray("bits")) {
                bits.set(bit);
            }
            if (!bits.isEmpty()) {
                destructionMap.put(chunkPos, bits);
            }
        }
        cacheCursor = cacheKeys.size();
        cacheComplete = true;
        workersStarted = true;
        traceComplete = true;
        consolidationComplete = true;
    }

    private int bitIndex(int x, int y, int z) {
        return ((y - minBuildHeight) << 8) | ((x & 15) << 4) | (z & 15);
    }

    private int chunkDistance(ChunkPos chunkPos) {
        return Math.abs((originX >> 4) - chunkPos.x) + Math.abs((originZ >> 4) - chunkPos.z);
    }

    private float cachedResistance(BlockState state) {
        Float resistance = resistanceCache.get(state);
        return resistance == null ? NUKE_RESISTANCE_CUTOFF : resistance;
    }

    private static float readNukeResistance(BlockState state) {
        if (state.getBlock() instanceof LiquidBlock) {
            return 0.1F;
        }
        if (state.is(Blocks.SANDSTONE)) {
            return HbmBlockStateUtil.explosionResistance(Blocks.STONE.defaultBlockState());
        }
        if (state.is(Blocks.OBSIDIAN)) {
            return HbmBlockStateUtil.explosionResistance(Blocks.STONE.defaultBlockState()) * 3.0F;
        }
        return HbmBlockStateUtil.explosionResistance(state);
    }

    private double energyLoss(float resistance, double currentRayPosition) {
        double effectiveDistance = Math.max(currentRayPosition, MIN_EFFECTIVE_DISTANCE);
        return Math.pow(resistance + 1.0D, 3.0D * (effectiveDistance / radius)) - 1.0D;
    }

    private static int step(double direction) {
        if (Math.abs(direction) < RAY_DIRECTION_EPSILON) {
            return 0;
        }
        return direction > 0.0D ? 1 : -1;
    }

    private static double delta(double direction, int step) {
        return step == 0 ? Double.POSITIVE_INFINITY : 1.0D / Math.abs(direction);
    }

    private static double firstBoundary(double position, int block, int step, double delta) {
        if (step == 0) {
            return Double.POSITIVE_INFINITY;
        }
        return (step > 0 ? block + 1.0D - position : position - block) * delta;
    }

    private static Direction directionFor(int index, int count) {
        if (count <= 1) {
            return new Direction(1.0D, 0.0D, 0.0D);
        }
        double phi = Math.PI * (3.0D - Math.sqrt(5.0D));
        double y = 1.0D - (index / (double) (count - 1)) * 2.0D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double angle = phi * index;
        return new Direction(Math.cos(angle) * horizontal, y, Math.sin(angle) * horizontal);
    }

    private static long deadline(int processTimeMs) {
        return System.nanoTime() + Math.max(1L, processTimeMs) * 1_000_000L;
    }

    private record Direction(double x, double y, double z) {
    }
}

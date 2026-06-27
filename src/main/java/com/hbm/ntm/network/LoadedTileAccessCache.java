package com.hbm.ntm.network;

import com.hbm.ntm.api.tile.LoadedTile;
import com.hbm.ntm.util.HbmMachinePerformanceCounters;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class LoadedTileAccessCache {
    public static final int NULL_CACHE_TICKS = 20;
    public static final int NONNULL_CACHE_TICKS = 60;

    private static final Map<Key, Entry> CACHE = new HashMap<>();

    private LoadedTileAccessCache() {
    }

    public static BlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level == null || pos == null || !isLoadedBlock(level, pos)) {
            return null;
        }
        long gameTime = level.getGameTime();
        Key key = new Key(level.dimension(), pos);
        Entry entry = CACHE.get(key);
        if (entry != null && !entry.hasExpired(level, gameTime)) {
            HbmMachinePerformanceCounters.tileCacheHit();
            return entry.blockEntity();
        }
        HbmMachinePerformanceCounters.tileCacheMiss();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CACHE.put(key, new Entry(blockEntity, gameTime + (blockEntity == null ? NULL_CACHE_TICKS : NONNULL_CACHE_TICKS)));
        return blockEntity;
    }

    public static void invalidate(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        if (CACHE.remove(new Key(level.dimension(), pos)) != null) {
            HbmMachinePerformanceCounters.tileCacheInvalidation();
        }
    }

    public static void clearLevel(Level level) {
        if (level == null) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        for (Iterator<Key> iterator = CACHE.keySet().iterator(); iterator.hasNext();) {
            if (iterator.next().dimension().equals(dimension)) {
                iterator.remove();
                HbmMachinePerformanceCounters.tileCacheInvalidation();
            }
        }
    }

    public static void clearAll() {
        if (HbmMachinePerformanceCounters.isEnabled()) {
            for (int i = 0; i < CACHE.size(); i++) {
                HbmMachinePerformanceCounters.tileCacheInvalidation();
            }
        }
        CACHE.clear();
    }

    public static int size() {
        return CACHE.size();
    }

    private static boolean isLoadedBlock(Level level, BlockPos pos) {
        return level != null && pos != null && level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private record Key(ResourceKey<Level> dimension, BlockPos pos) {
        private Key {
            pos = pos.immutable();
        }
    }

    private static final class Entry {
        private final WeakReference<BlockEntity> blockEntity;
        private final boolean nullEntry;
        private final long expiresOn;

        private Entry(BlockEntity blockEntity, long expiresOn) {
            this.blockEntity = blockEntity == null ? null : new WeakReference<>(blockEntity);
            this.nullEntry = blockEntity == null;
            this.expiresOn = expiresOn;
        }

        private boolean hasExpired(Level level, long gameTime) {
            if (gameTime >= expiresOn) {
                return true;
            }
            BlockEntity current = blockEntity();
            if (nullEntry) {
                return false;
            }
            if (current == null || current.isRemoved() || current.getLevel() == null) {
                return true;
            }
            if (!isLoadedBlock(level, current.getBlockPos())) {
                return true;
            }
            return current instanceof LoadedTile loadedTile && !loadedTile.isLoaded();
        }

        private BlockEntity blockEntity() {
            return blockEntity == null ? null : blockEntity.get();
        }
    }
}

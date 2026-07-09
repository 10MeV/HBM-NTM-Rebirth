package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class WorldUtil {
    private static final int ENTITY_SPAWN_LOAD_RADIUS = 2;
    private static final int LEGACY_WORLD_XZ_LIMIT = 30_000_000;
    private static final int LEGACY_OUT_OF_BOUNDS_HEIGHT = 64;

    public static Optional<LevelChunk> provideChunk(ServerLevel level, int chunkX, int chunkZ) {
        try {
            ChunkAccess chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk instanceof LevelChunk levelChunk) {
                return Optional.of(levelChunk);
            }
            ChunkPos pos = new ChunkPos(chunkX, chunkZ);
            if (diskChunkStatus(level, pos) != DiskChunkStatus.FULL) {
                return Optional.empty();
            }
            chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
            return chunk instanceof LevelChunk levelChunk ? Optional.of(levelChunk) : Optional.empty();
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    public static ChunkAccessReport inspectChunk(Level level, int chunkX, int chunkZ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return ChunkAccessReport.clientOrUnsupported(chunkX, chunkZ);
        }
        boolean loaded = serverLevel.hasChunk(chunkX, chunkZ);
        try {
            ChunkAccess chunk = serverLevel.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk instanceof LevelChunk) {
                return new ChunkAccessReport(chunkX, chunkZ, loaded, true, false, "full");
            }
            if (loaded) {
                return new ChunkAccessReport(chunkX, chunkZ, true, false, false, "loaded_not_full");
            }
            DiskChunkStatus disk = diskChunkStatus(serverLevel, new ChunkPos(chunkX, chunkZ));
            return new ChunkAccessReport(chunkX, chunkZ, false, disk == DiskChunkStatus.FULL, disk.failed(),
                    disk.detail());
        } catch (RuntimeException ex) {
            return new ChunkAccessReport(chunkX, chunkZ, loaded, false, true, ex.getClass().getSimpleName());
        }
    }

    public static ChunkAccessReport inspectChunk(Level level, ChunkPos pos) {
        return inspectChunk(level, pos.x, pos.z);
    }

    public static ChunkAccessReport inspectChunkAtBlock(Level level, BlockPos pos) {
        return inspectChunk(level, blockToChunkCoord(pos.getX()), blockToChunkCoord(pos.getZ()));
    }

    public static Optional<LevelChunk> provideChunk(ServerLevel level, ChunkPos pos) {
        return provideChunk(level, pos.x, pos.z);
    }

    public static Optional<LevelChunk> provideChunk(Level level, int chunkX, int chunkZ) {
        return level instanceof ServerLevel serverLevel ? provideChunk(serverLevel, chunkX, chunkZ) : Optional.empty();
    }

    public static Optional<LevelChunk> provideChunk(Level level, ChunkPos pos) {
        return provideChunk(level, pos.x, pos.z);
    }

    public static Optional<LevelChunk> provideChunkAtBlock(Level level, BlockPos pos) {
        return provideChunk(level, blockToChunkCoord(pos.getX()), blockToChunkCoord(pos.getZ()));
    }

    public static boolean isChunkLoaded(Level level, int chunkX, int chunkZ) {
        return level != null && level.hasChunk(chunkX, chunkZ);
    }

    public static boolean isChunkLoaded(Level level, ChunkPos pos) {
        return isChunkLoaded(level, pos.x, pos.z);
    }

    public static boolean isBlockLoaded(Level level, BlockPos pos) {
        return isChunkLoaded(level, blockToChunkCoord(pos.getX()), blockToChunkCoord(pos.getZ()));
    }

    public static int legacyGetHeightValue(Level level, int x, int z) {
        Objects.requireNonNull(level, "level");
        if (x < -LEGACY_WORLD_XZ_LIMIT || z < -LEGACY_WORLD_XZ_LIMIT
                || x >= LEGACY_WORLD_XZ_LIMIT || z >= LEGACY_WORLD_XZ_LIMIT) {
            return LEGACY_OUT_OF_BOUNDS_HEIGHT;
        }
        if (!isChunkLoaded(level, blockToChunkCoord(x), blockToChunkCoord(z))) {
            return 0;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, topBlockY(level), z);
        for (int y = topBlockY(level); y >= bottomBlockY(level); y--) {
            cursor.setY(y);
            if (legacyHeightMapBlocksLight(level, cursor, level.getBlockState(cursor))) {
                return y + 1;
            }
        }
        return bottomBlockY(level);
    }

    public static boolean legacyHeightMapBlocksLight(Level level, BlockPos pos, BlockState state) {
        return state.getLightBlock(level, pos) != 0;
    }

    public static int legacyGetTopSolidOrLiquidBlock(Level level, int x, int z) {
        Objects.requireNonNull(level, "level");
        LevelChunk chunk = level.getChunk(blockToChunkCoord(x), blockToChunkCoord(z));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, topBlockY(level), z);
        for (int y = topBlockY(level); y > bottomBlockY(level); y--) {
            cursor.setY(y);
            BlockState state = chunk.getBlockState(cursor);
            if (legacyTopSolidOrLiquidBlocksMovement(level, cursor, state)) {
                return y + 1;
            }
        }
        return -1;
    }

    public static boolean legacyTopSolidOrLiquidBlocksMovement(Level level, BlockPos pos, BlockState state) {
        return state.blocksMotion()
                && !(state.getBlock() instanceof LeavesBlock)
                && !state.is(BlockTags.LEAVES);
    }

    public static Optional<BlockState> getLoadedBlockState(Level level, BlockPos pos) {
        if (level == null || pos == null || !level.isInWorldBounds(pos) || !isBlockLoaded(level, pos)) {
            return Optional.empty();
        }
        try {
            return Optional.of(level.getBlockState(pos));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public static Optional<BlockEntity> getLoadedBlockEntity(Level level, BlockPos pos) {
        if (level == null || pos == null || !level.isInWorldBounds(pos) || !isBlockLoaded(level, pos)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(level.getBlockEntity(pos));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public static int blockToChunkCoord(int blockCoord) {
        return SectionPos.blockToSectionCoord(blockCoord);
    }

    public static int localBlockCoord(int blockCoord) {
        return blockCoord & 15;
    }

    public static ChunkPos blockToChunkPos(BlockPos pos) {
        return new ChunkPos(blockToChunkCoord(pos.getX()), blockToChunkCoord(pos.getZ()));
    }

    public static long blockToChunkLong(BlockPos pos) {
        return ChunkPos.asLong(blockToChunkCoord(pos.getX()), blockToChunkCoord(pos.getZ()));
    }

    public static ChunkPos chunkPosAt(double x, double z) {
        return new ChunkPos(blockToChunkCoord(Mth.floor(x)), blockToChunkCoord(Mth.floor(z)));
    }

    public static ChunkPos chunkPosAt(Entity entity) {
        return chunkPosAt(entity.getX(), entity.getZ());
    }

    public static List<ChunkPos> chunksInSquare(int centerChunkX, int centerChunkZ, int radius) {
        int safeRadius = Math.max(0, radius);
        List<ChunkPos> chunks = new ArrayList<>((safeRadius * 2 + 1) * (safeRadius * 2 + 1));
        for (int x = centerChunkX - safeRadius; x <= centerChunkX + safeRadius; x++) {
            for (int z = centerChunkZ - safeRadius; z <= centerChunkZ + safeRadius; z++) {
                chunks.add(new ChunkPos(x, z));
            }
        }
        return chunks;
    }

    public static List<ChunkPos> chunksInSquare(ChunkPos center, int radius) {
        return chunksInSquare(center.x, center.z, radius);
    }

    public static ChunkBatchReport inspectChunks(Level level, Collection<ChunkPos> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return new ChunkBatchReport(0, 0, 0, 0, List.of());
        }
        int loaded = 0;
        int full = 0;
        int failed = 0;
        List<ChunkAccessReport> reports = new ArrayList<>();
        for (ChunkPos chunk : chunks) {
            ChunkAccessReport report = inspectChunk(level, chunk);
            reports.add(report);
            if (report.loaded()) {
                loaded++;
            }
            if (report.full()) {
                full++;
            }
            if (report.failed()) {
                failed++;
            }
        }
        return new ChunkBatchReport(reports.size(), loaded, full, failed, reports);
    }

    public static ChunkBatchReport inspectChunksInSquare(Level level, int centerChunkX, int centerChunkZ, int radius) {
        return inspectChunks(level, chunksInSquare(centerChunkX, centerChunkZ, radius));
    }

    public static ChunkBatchReport inspectChunksInSquare(Level level, ChunkPos center, int radius) {
        return inspectChunksInSquare(level, center.x, center.z, radius);
    }

    public static ChunkLoadReport loadChunksInSquare(ServerLevel level, int centerChunkX, int centerChunkZ,
                                                     int radius) {
        int safeRadius = Math.max(0, radius);
        List<ChunkPos> loaded = new ArrayList<>();
        int requested = 0;
        int failed = 0;
        for (ChunkPos chunk : chunksInSquare(centerChunkX, centerChunkZ, safeRadius)) {
            requested++;
            try {
                level.getChunk(chunk.x, chunk.z);
                loaded.add(chunk);
            } catch (RuntimeException ex) {
                failed++;
            }
        }
        return new ChunkLoadReport(centerChunkX, centerChunkZ, safeRadius, requested, loaded.size(), failed, loaded);
    }

    public static ChunkLoadReport loadChunksInSquare(ServerLevel level, ChunkPos center, int radius) {
        return loadChunksInSquare(level, center.x, center.z, radius);
    }

    public static ChunkLoadReport loadChunksForEntitySpawn(Entity entity) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            ChunkPos center = chunkPosAt(entity);
            return ChunkLoadReport.empty(center.x, center.z, ENTITY_SPAWN_LOAD_RADIUS);
        }
        return loadChunksInSquare(serverLevel, chunkPosAt(entity), ENTITY_SPAWN_LOAD_RADIUS);
    }

    public static boolean loadAndSpawnEntityInWorld(Entity entity) {
        Level level = entity.level();
        ChunkPos center = chunkPosAt(entity);
        loadChunksInSquareStrict(level, center.x, center.z, ENTITY_SPAWN_LOAD_RADIUS);
        return level.addFreshEntity(entity);
    }

    public static int minBuildHeight(Level level) {
        return level.getMinBuildHeight();
    }

    public static int maxBuildHeight(Level level) {
        return level.getMaxBuildHeight();
    }

    public static int bottomBlockY(Level level) {
        return level.getMinBuildHeight();
    }

    public static int topBlockY(Level level) {
        return level.getMaxBuildHeight() - 1;
    }

    public static boolean isInBuildHeight(Level level, int y) {
        return level.isInWorldBounds(new BlockPos(0, y, 0));
    }

    public static int clampToBuildHeight(Level level, int y) {
        return Mth.clamp(y, bottomBlockY(level), topBlockY(level));
    }

    public static BlockPos clampToBuildHeight(Level level, BlockPos pos) {
        int y = clampToBuildHeight(level, pos.getY());
        return y == pos.getY() ? pos : new BlockPos(pos.getX(), y, pos.getZ());
    }

    public static int blockToSectionY(int blockY) {
        return SectionPos.blockToSectionCoord(blockY);
    }

    public static int minSectionY(Level level) {
        return blockToSectionY(bottomBlockY(level));
    }

    public static int maxSectionY(Level level) {
        return blockToSectionY(topBlockY(level));
    }

    public static int sectionCountY(Level level) {
        return maxSectionY(level) - minSectionY(level) + 1;
    }

    public static int sectionIndex(Level level, int sectionY) {
        return sectionY - minSectionY(level);
    }

    public static int sectionYFromIndex(Level level, int sectionIndex) {
        return minSectionY(level) + sectionIndex;
    }

    public static boolean isBelowBuildHeight(Level level, int y) {
        return y < bottomBlockY(level);
    }

    public static boolean isAboveBuildHeight(Level level, int y) {
        return y > topBlockY(level);
    }

    private WorldUtil() {
    }

    private static void loadChunksInSquareStrict(Level level, int centerChunkX, int centerChunkZ, int radius) {
        for (ChunkPos chunk : chunksInSquare(centerChunkX, centerChunkZ, radius)) {
            level.getChunk(chunk.x, chunk.z);
        }
    }

    private static DiskChunkStatus diskChunkStatus(ServerLevel level, ChunkPos pos) {
        try {
            Optional<CompoundTag> diskTag = level.getChunkSource().chunkMap.read(pos).join();
            if (diskTag.isEmpty()) {
                return DiskChunkStatus.ABSENT;
            }
            CompoundTag tag = diskTag.get();
            if (!tag.contains("Status", 8)) {
                return DiskChunkStatus.INVALID;
            }
            ChunkStatus.ChunkType type = ChunkSerializer.getChunkTypeFromTag(tag);
            return type == ChunkStatus.ChunkType.LEVELCHUNK ? DiskChunkStatus.FULL : DiskChunkStatus.PROTOCHUNK;
        } catch (Throwable ex) {
            return DiskChunkStatus.FAILED;
        }
    }

    private enum DiskChunkStatus {
        ABSENT("disk_absent", false),
        FULL("disk_full", false),
        PROTOCHUNK("disk_proto", false),
        INVALID("disk_invalid", true),
        FAILED("disk_read_failed", true);

        private final String detail;
        private final boolean failed;

        DiskChunkStatus(String detail, boolean failed) {
            this.detail = detail;
            this.failed = failed;
        }

        private String detail() {
            return detail;
        }

        private boolean failed() {
            return failed;
        }
    }

    public record ChunkLoadReport(int centerChunkX, int centerChunkZ, int radius, int requestedChunks,
                                  int loadedChunks, int failedChunks, List<ChunkPos> chunks) {
        public ChunkLoadReport {
            chunks = chunks == null ? List.of() : List.copyOf(chunks);
        }

        public static ChunkLoadReport empty(int centerChunkX, int centerChunkZ, int radius) {
            return new ChunkLoadReport(centerChunkX, centerChunkZ, Math.max(0, radius), 0, 0, 0, List.of());
        }

        public boolean complete() {
            return failedChunks == 0 && loadedChunks == requestedChunks;
        }

        public boolean failed() {
            return failedChunks > 0;
        }
    }

    public record ChunkAccessReport(int chunkX, int chunkZ, boolean loaded, boolean full, boolean failed,
                                    String detail) {
        public ChunkAccessReport {
            detail = detail == null || detail.isBlank() ? "unknown" : detail;
        }

        public static ChunkAccessReport clientOrUnsupported(int chunkX, int chunkZ) {
            return new ChunkAccessReport(chunkX, chunkZ, false, false, false, "client_or_unsupported_level");
        }

        public ChunkPos pos() {
            return new ChunkPos(chunkX, chunkZ);
        }

        public boolean available() {
            return full && !failed;
        }
    }

    public record ChunkBatchReport(int requestedChunks, int loadedChunks, int fullChunks, int failedChunks,
                                   List<ChunkAccessReport> chunks) {
        public ChunkBatchReport {
            chunks = chunks == null ? List.of() : List.copyOf(chunks);
        }

        public boolean complete() {
            return requestedChunks == fullChunks && failedChunks == 0;
        }
    }
}

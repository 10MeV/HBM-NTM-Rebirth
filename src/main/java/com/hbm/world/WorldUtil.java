package com.hbm.world;

import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Legacy package bridge for 1.7.10 world helpers.
 */
@Deprecated(forRemoval = false)
public final class WorldUtil {
    private WorldUtil() {
    }

    public static LevelChunk provideChunk(ServerLevel level, int chunkX, int chunkZ) {
        return com.hbm.ntm.world.WorldUtil.provideChunk(level, chunkX, chunkZ).orElse(null);
    }

    public static LevelChunk provideChunk(ServerLevel level, ChunkPos pos) {
        return provideChunk(level, pos.x, pos.z);
    }

    public static LevelChunk provideChunk(ServerLevel level, ChunkCoordIntPair pos) {
        return provideChunk(level, pos.chunkXPos, pos.chunkZPos);
    }

    public static LevelChunk provideChunk(Level level, int chunkX, int chunkZ) {
        return com.hbm.ntm.world.WorldUtil.provideChunk(level, chunkX, chunkZ).orElse(null);
    }

    public static LevelChunk provideChunk(Level level, ChunkPos pos) {
        return provideChunk(level, pos.x, pos.z);
    }

    public static LevelChunk provideChunk(Level level, ChunkCoordIntPair pos) {
        return provideChunk(level, pos.chunkXPos, pos.chunkZPos);
    }

    public static LevelChunk provideChunkAtBlock(Level level, BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.provideChunkAtBlock(level, pos).orElse(null);
    }

    public static Optional<LevelChunk> provideChunkOptional(ServerLevel level, int chunkX, int chunkZ) {
        return com.hbm.ntm.world.WorldUtil.provideChunk(level, chunkX, chunkZ);
    }

    public static Optional<LevelChunk> provideChunkOptional(ServerLevel level, ChunkCoordIntPair pos) {
        return provideChunkOptional(level, pos.chunkXPos, pos.chunkZPos);
    }

    public static Optional<LevelChunk> provideChunkOptional(Level level, int chunkX, int chunkZ) {
        return com.hbm.ntm.world.WorldUtil.provideChunk(level, chunkX, chunkZ);
    }

    public static Optional<LevelChunk> provideChunkOptional(Level level, ChunkCoordIntPair pos) {
        return provideChunkOptional(level, pos.chunkXPos, pos.chunkZPos);
    }

    public static void loadAndSpawnEntityInWorld(Entity entity) {
        com.hbm.ntm.world.WorldUtil.loadAndSpawnEntityInWorld(entity);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkLoadReport loadChunksForEntitySpawn(Entity entity) {
        return com.hbm.ntm.world.WorldUtil.loadChunksForEntitySpawn(entity);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkLoadReport loadChunksInSquare(ServerLevel level,
            int centerChunkX, int centerChunkZ, int radius) {
        return com.hbm.ntm.world.WorldUtil.loadChunksInSquare(level, centerChunkX, centerChunkZ, radius);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkLoadReport loadChunksInSquare(ServerLevel level,
            ChunkPos center, int radius) {
        return com.hbm.ntm.world.WorldUtil.loadChunksInSquare(level, center, radius);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkLoadReport loadChunksInSquare(ServerLevel level,
            ChunkCoordIntPair center, int radius) {
        return loadChunksInSquare(level, center.chunkXPos, center.chunkZPos, radius);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkAccessReport inspectChunk(Level level, int chunkX, int chunkZ) {
        return com.hbm.ntm.world.WorldUtil.inspectChunk(level, chunkX, chunkZ);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkAccessReport inspectChunk(Level level, ChunkPos pos) {
        return com.hbm.ntm.world.WorldUtil.inspectChunk(level, pos);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkAccessReport inspectChunk(Level level, ChunkCoordIntPair pos) {
        return inspectChunk(level, pos.chunkXPos, pos.chunkZPos);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkAccessReport inspectChunkAtBlock(Level level, BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.inspectChunkAtBlock(level, pos);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkBatchReport inspectChunks(Level level, Collection<ChunkPos> chunks) {
        return com.hbm.ntm.world.WorldUtil.inspectChunks(level, chunks);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkBatchReport inspectChunksInSquare(Level level,
            int centerChunkX, int centerChunkZ, int radius) {
        return com.hbm.ntm.world.WorldUtil.inspectChunksInSquare(level, centerChunkX, centerChunkZ, radius);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkBatchReport inspectChunksInSquare(Level level,
            ChunkPos center, int radius) {
        return com.hbm.ntm.world.WorldUtil.inspectChunksInSquare(level, center, radius);
    }

    public static com.hbm.ntm.world.WorldUtil.ChunkBatchReport inspectChunksInSquare(Level level,
            ChunkCoordIntPair center, int radius) {
        return inspectChunksInSquare(level, center.chunkXPos, center.chunkZPos, radius);
    }

    public static boolean isChunkLoaded(Level level, int chunkX, int chunkZ) {
        return com.hbm.ntm.world.WorldUtil.isChunkLoaded(level, chunkX, chunkZ);
    }

    public static boolean isChunkLoaded(Level level, ChunkPos pos) {
        return com.hbm.ntm.world.WorldUtil.isChunkLoaded(level, pos);
    }

    public static boolean isChunkLoaded(Level level, ChunkCoordIntPair pos) {
        return isChunkLoaded(level, pos.chunkXPos, pos.chunkZPos);
    }

    public static boolean isBlockLoaded(Level level, BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.isBlockLoaded(level, pos);
    }

    public static int legacyGetHeightValue(Level level, int x, int z) {
        return com.hbm.ntm.world.WorldUtil.legacyGetHeightValue(level, x, z);
    }

    public static int getHeightValue(Level level, int x, int z) {
        return legacyGetHeightValue(level, x, z);
    }

    public static boolean legacyHeightMapBlocksLight(Level level, BlockPos pos, BlockState state) {
        return com.hbm.ntm.world.WorldUtil.legacyHeightMapBlocksLight(level, pos, state);
    }

    public static int legacyGetTopSolidOrLiquidBlock(Level level, int x, int z) {
        return com.hbm.ntm.world.WorldUtil.legacyGetTopSolidOrLiquidBlock(level, x, z);
    }

    public static int getTopSolidOrLiquidBlock(Level level, int x, int z) {
        return legacyGetTopSolidOrLiquidBlock(level, x, z);
    }

    public static boolean legacyTopSolidOrLiquidBlocksMovement(Level level, BlockPos pos, BlockState state) {
        return com.hbm.ntm.world.WorldUtil.legacyTopSolidOrLiquidBlocksMovement(level, pos, state);
    }

    public static Optional<BlockState> getLoadedBlockState(Level level, BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.getLoadedBlockState(level, pos);
    }

    public static Optional<BlockEntity> getLoadedBlockEntity(Level level, BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.getLoadedBlockEntity(level, pos);
    }

    public static int blockToChunkCoord(int blockCoord) {
        return com.hbm.ntm.world.WorldUtil.blockToChunkCoord(blockCoord);
    }

    public static int localBlockCoord(int blockCoord) {
        return com.hbm.ntm.world.WorldUtil.localBlockCoord(blockCoord);
    }

    public static ChunkPos blockToChunkPos(BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.blockToChunkPos(pos);
    }

    public static long blockToChunkLong(BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.blockToChunkLong(pos);
    }

    public static ChunkPos chunkPosAt(double x, double z) {
        return com.hbm.ntm.world.WorldUtil.chunkPosAt(x, z);
    }

    public static ChunkPos chunkPosAt(Entity entity) {
        return com.hbm.ntm.world.WorldUtil.chunkPosAt(entity);
    }

    public static List<ChunkPos> chunksInSquare(int centerChunkX, int centerChunkZ, int radius) {
        return com.hbm.ntm.world.WorldUtil.chunksInSquare(centerChunkX, centerChunkZ, radius);
    }

    public static List<ChunkPos> chunksInSquare(ChunkPos center, int radius) {
        return com.hbm.ntm.world.WorldUtil.chunksInSquare(center, radius);
    }

    public static int minBuildHeight(Level level) {
        return com.hbm.ntm.world.WorldUtil.minBuildHeight(level);
    }

    public static int maxBuildHeight(Level level) {
        return com.hbm.ntm.world.WorldUtil.maxBuildHeight(level);
    }

    public static int bottomBlockY(Level level) {
        return com.hbm.ntm.world.WorldUtil.bottomBlockY(level);
    }

    public static int topBlockY(Level level) {
        return com.hbm.ntm.world.WorldUtil.topBlockY(level);
    }

    public static boolean isInBuildHeight(Level level, int y) {
        return com.hbm.ntm.world.WorldUtil.isInBuildHeight(level, y);
    }

    public static int clampToBuildHeight(Level level, int y) {
        return com.hbm.ntm.world.WorldUtil.clampToBuildHeight(level, y);
    }

    public static BlockPos clampToBuildHeight(Level level, BlockPos pos) {
        return com.hbm.ntm.world.WorldUtil.clampToBuildHeight(level, pos);
    }

    public static int blockToSectionY(int blockY) {
        return com.hbm.ntm.world.WorldUtil.blockToSectionY(blockY);
    }

    public static int minSectionY(Level level) {
        return com.hbm.ntm.world.WorldUtil.minSectionY(level);
    }

    public static int maxSectionY(Level level) {
        return com.hbm.ntm.world.WorldUtil.maxSectionY(level);
    }

    public static int sectionCountY(Level level) {
        return com.hbm.ntm.world.WorldUtil.sectionCountY(level);
    }

    public static int sectionIndex(Level level, int sectionY) {
        return com.hbm.ntm.world.WorldUtil.sectionIndex(level, sectionY);
    }

    public static int sectionYFromIndex(Level level, int sectionIndex) {
        return com.hbm.ntm.world.WorldUtil.sectionYFromIndex(level, sectionIndex);
    }

    public static boolean isBelowBuildHeight(Level level, int y) {
        return com.hbm.ntm.world.WorldUtil.isBelowBuildHeight(level, y);
    }

    public static boolean isAboveBuildHeight(Level level, int y) {
        return com.hbm.ntm.world.WorldUtil.isAboveBuildHeight(level, y);
    }
}

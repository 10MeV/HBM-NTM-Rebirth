package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Optional;

public final class WorldUtil {
    private static final int ENTITY_SPAWN_LOAD_RADIUS = 2;

    public static Optional<LevelChunk> provideChunk(ServerLevel level, int chunkX, int chunkZ) {
        try {
            ChunkAccess chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            return chunk instanceof LevelChunk levelChunk ? Optional.of(levelChunk) : Optional.empty();
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public static boolean loadAndSpawnEntityInWorld(Entity entity) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return level.addFreshEntity(entity);
        }

        int chunkX = Mth.floor(entity.getX() / 16.0D);
        int chunkZ = Mth.floor(entity.getZ() / 16.0D);
        for (int x = chunkX - ENTITY_SPAWN_LOAD_RADIUS; x <= chunkX + ENTITY_SPAWN_LOAD_RADIUS; x++) {
            for (int z = chunkZ - ENTITY_SPAWN_LOAD_RADIUS; z <= chunkZ + ENTITY_SPAWN_LOAD_RADIUS; z++) {
                serverLevel.getChunk(x, z);
            }
        }
        return serverLevel.addFreshEntity(entity);
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

    public static boolean isBelowBuildHeight(Level level, int y) {
        return y < bottomBlockY(level);
    }

    public static boolean isAboveBuildHeight(Level level, int y) {
        return y > topBlockY(level);
    }

    private WorldUtil() {
    }
}

package com.hbm.ntm.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ChunkRadiationManager {
    private static int diffusionTimer;
    private static int worldEffectTimer;

    public static float getRadiation(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0.0F;
        }
        return getData(serverLevel).get(new ChunkPos(pos));
    }

    public static void setRadiation(Level level, BlockPos pos, float radiation) {
        if (level instanceof ServerLevel serverLevel) {
            getData(serverLevel).set(new ChunkPos(pos), radiation);
        }
    }

    public static void incrementRadiation(Level level, BlockPos pos, float radiation) {
        setRadiation(level, pos, getRadiation(level, pos) + radiation);
    }

    public static void decrementRadiation(Level level, BlockPos pos, float radiation) {
        setRadiation(level, pos, Math.max(getRadiation(level, pos) - radiation, 0.0F));
    }

    public static void clear(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            getData(serverLevel).clear();
        }
    }

    public static void tick(ServerLevel level) {
        diffusionTimer++;
        if (diffusionTimer >= 20) {
            getData(level).updateDiffusion();
            diffusionTimer = 0;
        }

        worldEffectTimer++;
        if (worldEffectTimer >= 5) {
            handleWorldEffects(level);
            spawnRadiationFog(level);
            worldEffectTimer = 0;
        }
    }

    private static void handleWorldEffects(ServerLevel level) {
        List<Map.Entry<Long, Float>> entries = new ArrayList<>(getData(level).entries());
        if (entries.isEmpty()) {
            return;
        }

        int chunks = Math.min(5, entries.size());
        for (int c = 0; c < chunks; c++) {
            Map.Entry<Long, Float> entry = entries.get(level.random.nextInt(entries.size()));
            if (entry.getValue() < 10.0F) {
                continue;
            }
            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            for (int i = 0; i < 10; i++) {
                int x = chunkPos.getMinBlockX() + level.random.nextInt(16);
                int z = chunkPos.getMinBlockZ() + level.random.nextInt(16);
                BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).below();
                BlockState state = level.getBlockState(surface);
                if (state.is(Blocks.GRASS_BLOCK)) {
                    level.setBlock(surface, Blocks.DIRT.defaultBlockState(), 2);
                } else if (state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)) {
                    level.setBlock(surface, Blocks.AIR.defaultBlockState(), 2);
                } else if (state.is(BlockTags.LEAVES) && level.random.nextInt(7) <= 5) {
                    level.setBlock(surface, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void spawnRadiationFog(ServerLevel level) {
        List<Map.Entry<Long, Float>> entries = new ArrayList<>(getData(level).entries());
        if (entries.isEmpty()) {
            return;
        }

        Map.Entry<Long, Float> entry = entries.get(level.random.nextInt(entries.size()));
        if (entry.getValue() < 10.0F || level.random.nextInt(12) != 0) {
            return;
        }
        ChunkPos chunkPos = new ChunkPos(entry.getKey());
        int x = chunkPos.getMinBlockX() + level.random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + level.random.nextInt(16);
        BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
        level.sendParticles(ParticleTypes.MYCELIUM, x + 0.5D, surface.getY() + 0.2D, z + 0.5D, 1, 0.35D, 0.2D, 0.35D, 0.01D);
    }

    private static RadiationSavedData getData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                RadiationSavedData::load,
                RadiationSavedData::new,
                RadiationSavedData.DATA_NAME);
    }

    private ChunkRadiationManager() {
    }
}

package com.hbm.ntm.radiation;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChunkRadiationManager {
    private static final int LEGACY_WORLD_EFFECT_CHUNKS = 5;
    private static final int LEGACY_WORLD_EFFECT_OPERATIONS = 10;
    private static final int LEGACY_WORLD_EFFECT_THRESHOLD = 10;
    private static final Map<ResourceKey<Level>, Integer> DIFFUSION_TIMERS = new HashMap<>();
    public static final String LEGACY_CHUNK_NBT_KEY = "hfr_simple_radiation";

    public static float getRadiation(Level level, BlockPos pos) {
        if (!RadiationConfig.ENABLE_CHUNK_RADS.get() || !(level instanceof ServerLevel serverLevel)) {
            return 0.0F;
        }
        return getData(serverLevel).get(new ChunkPos(pos));
    }

    public static void setRadiation(Level level, BlockPos pos, float radiation) {
        if (RadiationConfig.ENABLE_CHUNK_RADS.get() && level instanceof ServerLevel serverLevel) {
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

    public static void loadLegacyChunkRadiation(ServerLevel level, ChunkPos chunkPos, float radiation) {
        if (RadiationConfig.ENABLE_CHUNK_RADS.get()) {
            getData(level).loadChunk(chunkPos, radiation);
        }
    }

    public static float getChunkRadiation(ServerLevel level, ChunkPos chunkPos) {
        return RadiationConfig.ENABLE_CHUNK_RADS.get() ? getData(level).get(chunkPos) : 0.0F;
    }

    public static void spawnDebugRadiationFog(ServerLevel level, BlockPos pos) {
        level.sendParticles(ModParticleTypes.RADIATION_FOG.get(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static void tick(ServerLevel level) {
        if (!RadiationConfig.ENABLE_CHUNK_RADS.get()) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        int timer = DIFFUSION_TIMERS.getOrDefault(dimension, 0) + 1;
        if (timer >= 20) {
            List<ChunkPos> fogCandidates = getData(level).updateDiffusion(level, RadiationConfig.FOG_RAD.get());
            spawnRadiationFog(level, fogCandidates);
            timer = 0;
        }
        DIFFUSION_TIMERS.put(dimension, timer);

        if (RadiationConfig.WORLD_RAD_EFFECTS.get()) {
            handleWorldEffects(level);
        }
    }

    private static void handleWorldEffects(ServerLevel level) {
        List<Map.Entry<Long, Float>> entries = getData(level).loadedEntries(level);
        if (entries.isEmpty()) {
            return;
        }

        for (int c = 0; c < LEGACY_WORLD_EFFECT_CHUNKS; c++) {
            Map.Entry<Long, Float> entry = entries.get(level.random.nextInt(entries.size()));
            if (entry.getValue() < LEGACY_WORLD_EFFECT_THRESHOLD) {
                continue;
            }
            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                continue;
            }

            for (int i = 0; i < LEGACY_WORLD_EFFECT_OPERATIONS; i++) {
                for (int a = 0; a < 16; a++) {
                    for (int b = 0; b < 16; b++) {
                        if (level.random.nextInt(3) != 0) {
                            continue;
                        }

                        int x = chunkPos.getMinBlockX() + a;
                        int z = chunkPos.getMinBlockZ() + b;
                        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).below(level.random.nextInt(2));
                        BlockState state = level.getBlockState(surface);
                        if (state.is(Blocks.GRASS_BLOCK)) {
                            level.setBlock(surface, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 2);
                        } else if (state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)) {
                            level.setBlock(surface, Blocks.AIR.defaultBlockState(), 2);
                        } else if (state.is(BlockTags.LEAVES) && !state.is(ModBlocks.WASTE_LEAVES.get())) {
                            if (level.random.nextInt(7) <= 5) {
                                level.setBlock(surface, ModBlocks.WASTE_LEAVES.get().defaultBlockState(), 2);
                            } else {
                                level.setBlock(surface, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void spawnRadiationFog(ServerLevel level, List<ChunkPos> candidates) {
        if (candidates.isEmpty()) {
            return;
        }

        int chance = RadiationConfig.FOG_CHANCE.get();
        for (ChunkPos chunkPos : candidates) {
            if (level.random.nextInt(chance) != 0 || !level.hasChunk(chunkPos.x, chunkPos.z)) {
                continue;
            }

            spawnRadiationFog(level, chunkPos);
        }
    }

    private static void spawnRadiationFog(ServerLevel level, ChunkPos chunkPos) {
        int x = chunkPos.getMinBlockX() + level.random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + level.random.nextInt(16);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
        level.sendParticles(ModParticleTypes.RADIATION_FOG.get(), x, surface.getY() + level.random.nextInt(5), z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {
            getData(serverLevel).remove(chunkPos);
        }
    }

    public static void unloadLevel(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            DIFFUSION_TIMERS.remove(serverLevel.dimension());
        }
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

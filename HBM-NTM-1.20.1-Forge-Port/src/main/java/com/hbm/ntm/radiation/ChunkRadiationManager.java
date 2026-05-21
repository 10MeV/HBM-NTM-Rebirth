package com.hbm.ntm.radiation;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public final class ChunkRadiationManager {
    private static final DustParticleOptions RADIATION_FOG_PARTICLE = new DustParticleOptions(new Vector3f(0.85F, 0.9F, 0.5F), 3.0F);
    private static final int LEGACY_WORLD_EFFECT_CHUNKS = 5;
    private static final int LEGACY_WORLD_EFFECT_THRESHOLD = 10;
    private static int diffusionTimer;
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
        if (RadiationConfig.ENABLE_CHUNK_RADS.get() && radiation > 0.0F) {
            getData(level).set(chunkPos, radiation);
        }
    }

    public static float getChunkRadiation(ServerLevel level, ChunkPos chunkPos) {
        return RadiationConfig.ENABLE_CHUNK_RADS.get() ? getData(level).get(chunkPos) : 0.0F;
    }

    public static void tick(ServerLevel level) {
        if (!RadiationConfig.ENABLE_CHUNK_RADS.get()) {
            return;
        }

        diffusionTimer++;
        if (diffusionTimer >= 20) {
            getData(level).updateDiffusion(level);
            spawnRadiationFog(level);
            diffusionTimer = 0;
        }

        if (RadiationConfig.WORLD_RAD_EFFECTS.get()) {
            handleWorldEffects(level);
        }
    }

    private static void handleWorldEffects(ServerLevel level) {
        List<Map.Entry<Long, Float>> entries = getData(level).loadedEntries(level);
        if (entries.isEmpty()) {
            return;
        }

        int chunks = Math.min(LEGACY_WORLD_EFFECT_CHUNKS, entries.size());
        int operations = Math.max(0, RadiationConfig.WORLD_RAD.get());
        int threshold = Math.min(LEGACY_WORLD_EFFECT_THRESHOLD, RadiationConfig.WORLD_RAD_THRESHOLD.get());
        for (int c = 0; c < chunks; c++) {
            Map.Entry<Long, Float> entry = entries.get(level.random.nextInt(entries.size()));
            if (entry.getValue() < threshold) {
                continue;
            }
            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                continue;
            }

            for (int i = 0; i < operations; i++) {
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

    private static void spawnRadiationFog(ServerLevel level) {
        List<Map.Entry<Long, Float>> entries = getData(level).loadedEntries(level);
        if (entries.isEmpty()) {
            return;
        }

        Map.Entry<Long, Float> entry = entries.get(level.random.nextInt(entries.size()));
        if (entry.getValue() <= RadiationConfig.FOG_RAD.get() || level.random.nextInt(RadiationConfig.FOG_CHANCE.get()) != 0) {
            return;
        }
        ChunkPos chunkPos = new ChunkPos(entry.getKey());
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            return;
        }

        int x = chunkPos.getMinBlockX() + level.random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + level.random.nextInt(16);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
        level.sendParticles(RADIATION_FOG_PARTICLE, x + 0.5D, surface.getY() + level.random.nextInt(5), z + 0.5D, 12, 2.5D, 0.2D, 2.5D, 0.0D);
        level.sendParticles(ParticleTypes.SMOKE, x + 0.5D, surface.getY() + level.random.nextInt(5), z + 0.5D, 3, 2.0D, 0.1D, 2.0D, 0.01D);
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {
            getData(serverLevel).remove(chunkPos);
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

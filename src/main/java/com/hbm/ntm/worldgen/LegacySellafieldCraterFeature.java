package com.hbm.ntm.worldgen;

import com.hbm.ntm.config.WorldgenConfig;
import com.hbm.ntm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Chunk-slice rewrite of 1.7.10 {@code MapGenCrater}, registered by
 * {@code HbmWorld#registerNTMTerrain()} as the Sellafield desert hotspot.
 */
public final class LegacySellafieldCraterFeature extends Feature<NoneFeatureConfiguration> {
    private static final int LEGACY_RANGE = 8;
    private static final int MIN_RADIUS = 8;
    private static final int MAX_RADIUS_EXCLUSIVE = 64;
    private static final int SET_BLOCK_FLAGS = Block.UPDATE_CLIENTS;

    public LegacySellafieldCraterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int frequency = WorldgenConfig.radiationHotspotSpawnChunks();
        if (!WorldgenConfig.radiationHotspotsEnabled() || frequency <= 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        int targetChunkX = Math.floorDiv(context.origin().getX(), 16);
        int targetChunkZ = Math.floorDiv(context.origin().getZ(), 16);
        long worldSeed = level.getSeed();
        LegacyRandomSource worldSeedRandom = new LegacyRandomSource(worldSeed);
        long xMultiplier = worldSeedRandom.nextLong();
        long zMultiplier = worldSeedRandom.nextLong();
        boolean placedAny = false;

        // This is MapGenBase's source-chunk scan order.  The feature is installed in every
        // overworld biome: a desert source can legitimately carve its rim into a neighbouring biome.
        for (int sourceChunkX = targetChunkX - LEGACY_RANGE; sourceChunkX <= targetChunkX + LEGACY_RANGE; sourceChunkX++) {
            for (int sourceChunkZ = targetChunkZ - LEGACY_RANGE; sourceChunkZ <= targetChunkZ + LEGACY_RANGE; sourceChunkZ++) {
                long sourceSeed = (long) sourceChunkX * xMultiplier ^ (long) sourceChunkZ * zMultiplier ^ worldSeed;
                RandomSource random = new WorldgenRandom(new LegacyRandomSource(sourceSeed));
                if (random.nextInt(frequency) != 0 || !isLegacyDesert(level, sourceChunkX, sourceChunkZ)) {
                    continue;
                }

                int radius = random.nextInt(MAX_RADIUS_EXCLUSIVE - MIN_RADIUS) + MIN_RADIUS;
                placedAny |= carveSourceSlice(level, random, sourceChunkX, sourceChunkZ,
                        targetChunkX, targetChunkZ, radius, radius * 0.35D);
            }
        }
        return placedAny;
    }

    private static boolean isLegacyDesert(WorldGenLevel level, int sourceChunkX, int sourceChunkZ) {
        // Legacy getBiomeGenForCoords(sourceChunkX * 16, sourceChunkZ * 16) is a two-dimensional
        // lookup.  Sea level is only a stable modern Y carrier for that same X/Z source position.
        return level.getBiome(new BlockPos(sourceChunkX * 16, level.getSeaLevel(), sourceChunkZ * 16)).is(Biomes.DESERT);
    }

    private static boolean carveSourceSlice(WorldGenLevel level, RandomSource random, int sourceChunkX, int sourceChunkZ,
                                             int targetChunkX, int targetChunkZ, int radius, double depth) {
        int sourceToTargetX = targetChunkX - sourceChunkX;
        int sourceToTargetZ = targetChunkZ - sourceChunkZ;
        int targetBaseX = targetChunkX * 16;
        int targetBaseZ = targetChunkZ * 16;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        BlockState slakedSellafield = ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        // Reverse X/Z/Y loop ordering, random jitter and the two independent nextInt(3) calls
        // deliberately match MapGenCrater's block-array implementation.
        for (int localX = 15; localX >= 0; localX--) {
            for (int localZ = 15; localZ >= 0; localZ--) {
                int x = targetBaseX + localX;
                int z = targetBaseZ + localZ;
                int surfaceY = findLegacySurface(level, cursor, x, z, minY, maxY);
                if (surfaceY <= minY) {
                    continue;
                }

                int relativeX = sourceToTargetX * 16 + localX;
                int relativeZ = sourceToTargetZ * 16 + localZ;
                double distance = Math.sqrt((double) relativeX * relativeX + (double) relativeZ * relativeZ);
                if (distance - random.nextInt(3) > radius) {
                    continue;
                }

                int carveDepth = (int) Mth.clamp(depthAt(distance, radius, depth), 0.0D, surfaceY - minY - 1.0D);
                for (int offset = 0; offset < carveDepth; offset++) {
                    cursor.set(x, surfaceY - offset, z);
                    placedAny |= level.setBlock(cursor, Blocks.AIR.defaultBlockState(), SET_BLOCK_FLAGS);
                }

                int floorY = surfaceY - carveDepth;
                int fillDepth = Math.min(3, floorY - minY - 1);
                // The legacy branch chose regolith for the centre and rock outside it.  HbmWorld assigns
                // both fields to sellafield_slaked, but retain the roll so source random consumption stays exact.
                random.nextInt(3);
                for (int offset = 0; offset < fillDepth; offset++) {
                    cursor.set(x, floorY - offset, z);
                    placedAny |= level.setBlock(cursor, slakedSellafield, SET_BLOCK_FLAGS);
                }
            }
        }
        return placedAny;
    }

    private static int findLegacySurface(WorldGenLevel level, BlockPos.MutableBlockPos cursor,
                                         int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (state.canOcclude() || !state.getFluidState().isEmpty()) {
                return y;
            }
        }
        return minY;
    }

    private static double depthAt(double distance, double radius, double depth) {
        return -distance * distance / (radius * radius) * depth + depth;
    }
}

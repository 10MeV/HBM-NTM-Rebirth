package com.hbm.ntm.worldgen;

import com.hbm.ntm.block.LanternBehemothBlock;
import com.hbm.ntm.block.PinkCloudBroadcasterBlock;
import com.hbm.ntm.blockentity.LandmineBlockEntity;
import com.hbm.ntm.blockentity.LanternBehemothBlockEntity;
import com.hbm.ntm.config.WorldgenConfig;
import com.hbm.ntm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;

/**
 * Source-backed, non-structure portion of 1.7.10 {@code HbmWorldGen#generateSurface}.
 * Excluded structures/rewards are deliberately absent; the retained calls share one random stream
 * in their original broadcaster, AP mine, Behemoth lantern, chlorine geyser order.
 */
public final class LegacySurfaceFixturesFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SET_BLOCK_FLAGS = Block.UPDATE_ALL;
    private static final String[][] CHLORINE_GEYSER_TEMPLATE = {
            {".SSS.", "SSSSS", "SSSSS", "SSSSS", ".SSS."},
            {".SSS.", "SSSSS", "SSSSS", "SSSSS", ".SSS."},
            {".SSS.", "SWYWS", "SYWWS", "SWYYS", ".SSS."},
            {".SSS.", "SAAAS", "SAAAS", "SAAAS", ".SSS."},
            {".SSS.", "SS.SS", "SAAAS", "SS.SS", ".SSS."},
            {".GGG.", "GRSGS", "SGCGS", "GSGRG", ".GGG."}
    };

    public LegacySurfaceFixturesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (level.getLevel().dimension() != Level.OVERWORLD || !surfaceFixturesEnabled(level)) {
            return false;
        }

        RandomSource random = context.random();
        BlockPos chunkOrigin = context.origin();
        boolean placed = placeBroadcaster(level, random, chunkOrigin);
        placed |= placeApMine(level, random, chunkOrigin);
        placed |= placeBehemothLantern(level, random, chunkOrigin);
        placed |= placeChlorineGeyser(level, random, chunkOrigin);
        return placed;
    }

    private static boolean surfaceFixturesEnabled(WorldGenLevel level) {
        return switch (WorldgenConfig.legacySurfaceFixtureMapFeaturesMode()) {
            case 0 -> false;
            case 1 -> true;
            default -> worldMapFeaturesEnabled(level);
        };
    }

    private static boolean worldMapFeaturesEnabled(WorldGenLevel level) {
        ServerLevel serverLevel = level.getLevel();
        WorldData worldData = serverLevel.getServer().getWorldData();
        return worldData instanceof PrimaryLevelData primary && primary.worldGenOptions().generateStructures();
    }

    private static boolean placeBroadcaster(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int frequency = WorldgenConfig.broadcasterSpawnChunks();
        if (frequency <= 0 || random.nextInt(frequency) != 0) {
            return false;
        }
        int x = origin.getX() + random.nextInt(16);
        int z = origin.getZ() + random.nextInt(16);
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos support = pos.below();
        if (!inBuildHeight(level, pos) || !inBuildHeight(level, support) || !hasTorchSupport(level, support)) {
            return false;
        }
        Direction facing = switch (random.nextInt(4)) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            default -> Direction.EAST;
        };
        return level.setBlock(pos, ModBlocks.BROADCASTER_PC.get().defaultBlockState()
                .setValue(PinkCloudBroadcasterBlock.FACING, facing), SET_BLOCK_FLAGS);
    }

    private static boolean placeApMine(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int frequency = WorldgenConfig.landmineSpawnChunks();
        if (!WorldgenConfig.landmineSpawningEnabled() || frequency <= 0 || random.nextInt(frequency) != 0) {
            return false;
        }
        int x = origin.getX() + random.nextInt(16) + 8;
        int z = origin.getZ() + random.nextInt(16) + 8;
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        for (int y = surfaceY + 2; y >= surfaceY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos support = pos.below();
            if (!inBuildHeight(level, pos) || !inBuildHeight(level, support) || !hasTorchSupport(level, support)) {
                continue;
            }
            if (!level.setBlock(pos, ModBlocks.MINE_AP.get().defaultBlockState(), SET_BLOCK_FLAGS)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LandmineBlockEntity landmine) {
                landmine.waitingForPlayer = true;
                landmine.setChanged();
            }
            return true;
        }
        return false;
    }

    private static boolean placeBehemothLantern(WorldGenLevel level, RandomSource random, BlockPos origin) {
        if (random.nextInt(2000) != 0) {
            return false;
        }
        int x = origin.getX() + random.nextInt(16);
        int z = origin.getZ() + random.nextInt(16);
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos support = pos.below();
        if (!inBuildHeight(level, support) || !hasTorchSupport(level, support) || !isReplaceable(level, pos)) {
            return false;
        }
        for (int segment = 1; segment <= 4; segment++) {
            if (!inBuildHeight(level, pos.above(segment))) {
                return false;
            }
        }
        if (!level.setBlock(pos, ModBlocks.LANTERN_BEHEMOTH.get().defaultBlockState()
                .setValue(LanternBehemothBlock.SEGMENT, 0), SET_BLOCK_FLAGS)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LanternBehemothBlockEntity lantern) {
            lantern.setBroken(true);
        }
        // The old branch rolled its optional booklet reward here.  Booklets are intentionally
        // excluded, but retaining the roll keeps the following chlorine-geyser stream aligned.
        random.nextInt(2);
        return true;
    }

    private static boolean placeChlorineGeyser(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int frequency = WorldgenConfig.chlorineGeyserSpawnChunks();
        if (frequency <= 0 || !level.getBiome(new BlockPos(origin.getX(), level.getSeaLevel(), origin.getZ())).is(Biomes.PLAINS)
                || random.nextInt(frequency) != 0) {
            return false;
        }
        int x = origin.getX() + random.nextInt(16);
        int z = origin.getZ() + random.nextInt(16);
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos support = new BlockPos(x, surfaceY - 1, z);
        if (!inBuildHeight(level, support) || !level.getBlockState(support).is(Blocks.GRASS_BLOCK)) {
            return false;
        }
        // Geyser#generate performs this guaranteed one-bound roll before generate_r0().
        random.nextInt(1);
        return placeChlorineGeyserTemplate(level, x, surfaceY, z);
    }

    private static boolean placeChlorineGeyserTemplate(WorldGenLevel level, int centerX, int surfaceY, int centerZ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int layer = 0; layer < CHLORINE_GEYSER_TEMPLATE.length; layer++) {
            int y = surfaceY - 6 + layer;
            for (int z = 0; z < 5; z++) {
                String row = CHLORINE_GEYSER_TEMPLATE[layer][z];
                for (int x = 0; x < 5; x++) {
                    if (row.charAt(x) == '.') {
                        continue;
                    }
                    cursor.set(centerX - 2 + x, y, centerZ - 2 + z);
                    if (!inBuildHeight(level, cursor)) {
                        return false;
                    }
                }
            }
        }
        boolean placed = false;
        for (int layer = 0; layer < CHLORINE_GEYSER_TEMPLATE.length; layer++) {
            int y = surfaceY - 6 + layer;
            for (int z = 0; z < 5; z++) {
                String row = CHLORINE_GEYSER_TEMPLATE[layer][z];
                for (int x = 0; x < 5; x++) {
                    BlockState state = geyserState(row.charAt(x));
                    if (state == null) {
                        continue;
                    }
                    cursor.set(centerX - 2 + x, y, centerZ - 2 + z);
                    placed |= level.setBlock(cursor, state, SET_BLOCK_FLAGS);
                }
            }
        }
        return placed;
    }

    private static BlockState geyserState(char marker) {
        return switch (marker) {
            case 'S' -> Blocks.STONE.defaultBlockState();
            case 'W' -> Blocks.WATER.defaultBlockState();
            case 'Y' -> ModBlocks.legacyBlock("block_yellowcake").get().defaultBlockState();
            case 'A' -> Blocks.AIR.defaultBlockState();
            case 'G' -> Blocks.GRASS_BLOCK.defaultBlockState();
            case 'R' -> Blocks.GRAVEL.defaultBlockState();
            case 'C' -> ModBlocks.GEYSIR_CHLORINE.get().defaultBlockState();
            default -> null;
        };
    }

    private static boolean inBuildHeight(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos);
    }

    private static boolean hasTorchSupport(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP) || state.getBlock() instanceof FenceBlock;
    }

    private static boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        return inBuildHeight(level, pos) && level.getBlockState(pos).canBeReplaced();
    }
}

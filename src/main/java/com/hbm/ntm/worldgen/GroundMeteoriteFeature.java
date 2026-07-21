package com.hbm.ntm.worldgen;

import com.hbm.ntm.config.WorldgenConfig;
import com.hbm.ntm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;

/**
 * The ordinary ground-refresh branch of 1.7.10 {@code Meteorite}.
 *
 * <p>This deliberately has no falling meteor, impact, special-meteor, explosion, damage, or dungeon branch.
 * Its placement shapes and weighted block selections follow {@code Meteorite#generate(..., false, false, false)}.
 */
public final class GroundMeteoriteFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SET_BLOCK_FLAGS = Block.UPDATE_CLIENTS;

    public GroundMeteoriteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int spawnChunks = WorldgenConfig.meteoriteSpawnChunks();
        RandomSource random = context.random();
        if (spawnChunks <= 0 || random.nextInt(spawnChunks) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos chunkOrigin = context.origin();
        int x = chunkOrigin.getX() + random.nextInt(16) + 8;
        int z = chunkOrigin.getZ() + random.nextInt(16) + 8;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - random.nextInt(10);
        BlockPos support = new BlockPos(x, y - 2, z);

        // Legacy: !b.isAir && !b.getMaterial().isLiquid() && y > 1. The lower build limit replaces 0.
        if (y <= level.getMinBuildHeight() + 1 || level.isOutsideBuildHeight(support)) {
            return false;
        }
        BlockState supportState = level.getBlockState(support);
        if (supportState.isAir() || !supportState.getFluidState().isEmpty()) {
            return false;
        }

        switch (random.nextInt(3)) {
            case 0 -> generateLarge(level, random, x, y, z);
            case 1 -> generateMedium(level, random, x, y, z);
            case 2 -> generateSmall(level, random, x, y, z);
            default -> throw new IllegalStateException("Unexpected three-way meteor roll");
        }
        return true;
    }

    private void generateLarge(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        int hull = random.nextInt(4);
        int outerPadding = hull == 2 ? 1 + random.nextInt(2) : hull == 3 ? 2 : 0;
        int innerPadding = random.nextInt(hull == 0 ? 3 : 2);
        int core = random.nextInt(2);
        if (innerPadding > 0) core = 2;

        List<BlockState> hullBlocks = hullBlocks(hull);
        List<BlockState> outerBlocks = outerPaddingBlocks(outerPadding);
        List<BlockState> innerBlocks = innerPaddingBlocks(innerPadding);
        List<BlockState> coreBlocks = coreBlocks(core);

        switch (random.nextInt(5)) {
            case 0 -> largeOne(level, random, x, y, z, hullBlocks, outerBlocks, innerBlocks, coreBlocks);
            case 1 -> largeTwo(level, random, x, y, z, hullBlocks, outerBlocks, innerBlocks, coreBlocks);
            case 2 -> largeThree(level, random, x, y, z, hullBlocks, outerBlocks, innerBlocks, coreBlocks);
            case 3 -> largeFour(level, random, x, y, z, hullBlocks, outerBlocks, innerBlocks, coreBlocks);
            case 4 -> largeFive(level, random, x, y, z, hullBlocks, outerBlocks, innerBlocks, coreBlocks);
            default -> throw new IllegalStateException("Unexpected five-way meteor roll");
        }
    }

    private void generateMedium(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        int hull = random.nextInt(4);
        int outerPadding = hull == 2 ? 1 + random.nextInt(2) : hull == 3 ? 2 : 0;
        int innerPadding = random.nextInt(hull == 0 ? 3 : 2);
        int core = random.nextInt(2);
        if (innerPadding > 0) core = 2;

        List<BlockState> hullBlocks = hullBlocks(hull);
        List<BlockState> outerBlocks = outerPaddingBlocks(outerPadding);
        List<BlockState> innerBlocks = innerPaddingBlocks(innerPadding);
        List<BlockState> coreBlocks = coreBlocks(core);
        List<BlockState> smallCoreBlocks = smallCoreBlocks(core);

        // The source uses smallCoreBlocks only for genM1; retain that historic selection contract.
        switch (random.nextInt(6)) {
            case 0 -> mediumOne(level, random, x, y, z, hullBlocks, smallCoreBlocks);
            case 1 -> mediumTwo(level, random, x, y, z, hullBlocks, outerBlocks, coreBlocks);
            case 2 -> mediumThree(level, random, x, y, z, hullBlocks, outerBlocks, coreBlocks);
            case 3 -> mediumFour(level, random, x, y, z, hullBlocks, outerBlocks, innerBlocks, coreBlocks);
            case 4 -> mediumFive(level, random, x, y, z, hullBlocks, innerBlocks, coreBlocks);
            case 5 -> mediumSix(level, random, x, y, z, hullBlocks, innerBlocks, coreBlocks);
            default -> throw new IllegalStateException("Unexpected six-way meteor roll");
        }
    }

    private void generateSmall(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        int hull = random.nextInt(4);
        int core = random.nextInt(3);
        List<BlockState> hullBlocks = hullBlocks(hull);
        box(level, random, x, y, z, hullBlocks);
        set(level, x, y, z, pick(random, smallCoreBlocks(core)));
    }

    private void largeOne(WorldGenLevel level, RandomSource random, int x, int y, int z,
            List<BlockState> hull, List<BlockState> outer, List<BlockState> inner, List<BlockState> core) {
        sphere7(level, random, x, y, z, hull); star5(level, random, x, y, z, outer); star3(level, random, x, y, z, inner); set(level, x, y, z, pick(random, core));
    }
    private void largeTwo(WorldGenLevel level, RandomSource random, int x, int y, int z,
            List<BlockState> hull, List<BlockState> outer, List<BlockState> inner, List<BlockState> core) {
        sphere7(level, random, x, y, z, hull); sphere5(level, random, x, y, z, outer); star3(level, random, x, y, z, inner); set(level, x, y, z, pick(random, core));
    }
    private void largeThree(WorldGenLevel level, RandomSource random, int x, int y, int z,
            List<BlockState> hull, List<BlockState> outer, List<BlockState> inner, List<BlockState> core) {
        sphere7(level, random, x, y, z, hull); sphere5(level, random, x, y, z, outer); box(level, random, x, y, z, inner); set(level, x, y, z, pick(random, core));
    }
    private void largeFour(WorldGenLevel level, RandomSource random, int x, int y, int z,
            List<BlockState> hull, List<BlockState> outer, List<BlockState> inner, List<BlockState> core) {
        sphere7(level, random, x, y, z, hull); sphere5(level, random, x, y, z, outer); box(level, random, x, y, z, inner); star3(level, random, x, y, z, meteorOres()); set(level, x, y, z, pick(random, core));
    }
    private void largeFive(WorldGenLevel level, RandomSource random, int x, int y, int z,
            List<BlockState> hull, List<BlockState> outer, List<BlockState> inner, List<BlockState> core) {
        sphere7(level, random, x, y, z, hull); sphere5(level, random, x, y, z, outer); star5(level, random, x, y, z, inner); star3(level, random, x, y, z, meteorOres()); set(level, x, y, z, pick(random, core));
    }
    private void mediumOne(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> hull, List<BlockState> core) {
        sphere5(level, random, x, y, z, hull); set(level, x, y, z, pick(random, core));
    }
    private void mediumTwo(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> hull, List<BlockState> outer, List<BlockState> core) {
        sphere5(level, random, x, y, z, hull); star3(level, random, x, y, z, outer); set(level, x, y, z, pick(random, core));
    }
    private void mediumThree(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> hull, List<BlockState> outer, List<BlockState> core) {
        sphere5(level, random, x, y, z, hull); box(level, random, x, y, z, outer); set(level, x, y, z, pick(random, core));
    }
    private void mediumFour(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> hull, List<BlockState> outer, List<BlockState> inner, List<BlockState> core) {
        sphere5(level, random, x, y, z, hull); box(level, random, x, y, z, outer); star3(level, random, x, y, z, inner); set(level, x, y, z, pick(random, core));
    }
    private void mediumFive(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> hull, List<BlockState> inner, List<BlockState> core) {
        sphere5(level, random, x, y, z, hull); box(level, random, x, y, z, inner); set(level, x, y, z, pick(random, core));
    }
    private void mediumSix(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> hull, List<BlockState> inner, List<BlockState> core) {
        sphere5(level, random, x, y, z, hull); box(level, random, x, y, z, inner); star3(level, random, x, y, z, meteorOres()); set(level, x, y, z, pick(random, core));
    }

    private void sphere7(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> blocks) {
        prism(level, random, x, y, z, blocks, -3, 3, -1, 1, -1, 1); prism(level, random, x, y, z, blocks, -1, 1, -3, 3, -1, 1); prism(level, random, x, y, z, blocks, -1, 1, -1, 1, -3, 3);
        prism(level, random, x, y, z, blocks, -2, 2, -2, 2, -1, 1); prism(level, random, x, y, z, blocks, -1, 1, -2, 2, -2, 2); prism(level, random, x, y, z, blocks, -2, 2, -1, 1, -2, 2);
    }

    private void sphere5(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> blocks) {
        prism(level, random, x, y, z, blocks, -2, 2, -1, 1, -1, 1); prism(level, random, x, y, z, blocks, -1, 1, -2, 2, -1, 1); prism(level, random, x, y, z, blocks, -1, 1, -1, 1, -2, 2);
    }

    private void box(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> blocks) {
        prism(level, random, x, y, z, blocks, -1, 1, -1, 1, -1, 1);
    }

    private void star5(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> blocks) {
        box(level, random, x, y, z, blocks);
        set(level, x + 2, y, z, pick(random, blocks)); set(level, x - 2, y, z, pick(random, blocks));
        set(level, x, y + 2, z, pick(random, blocks)); set(level, x, y - 2, z, pick(random, blocks));
        set(level, x, y, z + 2, pick(random, blocks)); set(level, x, y, z - 2, pick(random, blocks));
    }

    private void star3(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> blocks) {
        set(level, x, y, z, pick(random, blocks)); set(level, x + 1, y, z, pick(random, blocks)); set(level, x - 1, y, z, pick(random, blocks));
        set(level, x, y + 1, z, pick(random, blocks)); set(level, x, y - 1, z, pick(random, blocks));
        set(level, x, y, z + 1, pick(random, blocks)); set(level, x, y, z - 1, pick(random, blocks));
    }

    private void prism(WorldGenLevel level, RandomSource random, int x, int y, int z, List<BlockState> blocks,
            int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int dx = minX; dx <= maxX; dx++) {
            for (int dy = minY; dy <= maxY; dy++) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    set(level, x + dx, y + dy, z + dz, pick(random, blocks));
                }
            }
        }
    }

    private void set(WorldGenLevel level, int x, int y, int z, BlockState replacement) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.isOutsideBuildHeight(pos)) return;
        BlockState target = level.getBlockState(pos);
        float hardness = target.getDestroySpeed(level, pos);
        if (hardness != -1.0F && hardness < 10_000.0F) level.setBlock(pos, replacement, SET_BLOCK_FLAGS);
    }

    private static List<BlockState> hullBlocks(int hull) {
        return switch (hull) {
            case 0 -> List.of(state("block_meteor_molten"));
            case 1 -> List.of(state("block_meteor_cobble"));
            case 2 -> weightedBrokenTreasure();
            case 3 -> List.of(state("block_meteor_molten"), state("block_meteor_broken"));
            default -> throw new IllegalArgumentException("Unknown meteor hull " + hull);
        };
    }
    private static List<BlockState> outerPaddingBlocks(int padding) {
        return switch (padding) {
            case 0 -> List.of(state("block_meteor_cobble"));
            case 1 -> weightedBrokenTreasure();
            case 2 -> List.of(state("block_meteor_cobble"), state("block_meteor_broken"));
            default -> throw new IllegalArgumentException("Unknown meteor outer padding " + padding);
        };
    }
    private static List<BlockState> innerPaddingBlocks(int padding) {
        return switch (padding) {
            case 0 -> weightedBrokenTreasure();
            case 1 -> List.of(state("block_meteor_broken"));
            case 2 -> List.of(state("block_meteor_cobble"));
            default -> throw new IllegalArgumentException("Unknown meteor inner padding " + padding);
        };
    }
    private static List<BlockState> coreBlocks(int core) {
        return switch (core) {
            case 0 -> List.of(state("block_meteor"));
            case 1 -> List.of(state("block_meteor_treasure"));
            case 2 -> meteorOres();
            default -> throw new IllegalArgumentException("Unknown meteor core " + core);
        };
    }
    private static List<BlockState> smallCoreBlocks(int core) {
        return switch (core) {
            case 0 -> List.of(state("block_meteor"));
            case 1 -> List.of(state("block_meteor_treasure"));
            case 2 -> List.of(state("block_meteor_treasure"), state("block_meteor"));
            default -> throw new IllegalArgumentException("Unknown small meteor core " + core);
        };
    }
    private static List<BlockState> weightedBrokenTreasure() {
        List<BlockState> weighted = new ArrayList<>(100);
        for (int index = 0; index < 99; index++) weighted.add(state("block_meteor_broken"));
        weighted.add(state("block_meteor_treasure"));
        return weighted;
    }
    private static List<BlockState> meteorOres() {
        return List.of(ModBlocks.ORE_METEOR_IRON.get().defaultBlockState(), ModBlocks.ORE_METEOR_COPPER.get().defaultBlockState(),
                ModBlocks.ORE_METEOR_ALUMINIUM.get().defaultBlockState(), ModBlocks.ORE_METEOR_RAREEARTH.get().defaultBlockState(),
                ModBlocks.ORE_METEOR_COBALT.get().defaultBlockState());
    }
    private static BlockState state(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) throw new IllegalStateException("Missing ground meteorite block " + legacyName);
        return block.get().defaultBlockState();
    }
    private static BlockState pick(RandomSource random, List<BlockState> states) { return states.get(random.nextInt(states.size())); }
}

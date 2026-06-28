package com.hbm.ntm.worldgen;

import com.hbm.config.GeneralConfig;
import com.hbm.ntm.blockentity.BedrockOreDepositBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.BedrockOreBaseItem;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LegacyOreSetFeature extends Feature<LegacyOreSetFeature.Configuration> {
    private static final RuleTest STONE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    private static final RuleTest DEEPSLATE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    private static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);
    private static final RuleTest END_STONE = new BlockMatchTest(Blocks.END_STONE);
    private static final double SCHIST_SCALE = 0.01D;
    private static final double ORE_CAVE_SCALE = 0.01D;
    private static final int SCHIST_THRESHOLD = 5;
    private static final int OIL_BUBBLE_FREQUENCY = 100;
    private static final int BEDROCK_OIL_FREQUENCY = 200;
    private static final int OIL_SAND_FREQUENCY = 200;
    private static final int NEW_BEDROCK_ORE_FREQUENCY = 10;
    private static final int BEDROCK_ORE_COLOR = 0xD78A16;
    private static final int LIMESTONE_MODERN_MIN_Y = -40;
    private static final long OIL_BUBBLE_RANDOM_SALT = 0x48424d4f494c31L;
    private static final long OIL_SAND_RANDOM_SALT = 0x48424d4f494c32L;
    private static final long BEDROCK_OIL_RANDOM_SALT = 0x48424d4f494c33L;
    private static final long DEPTH_DEPOSIT_RANDOM_SALT = 0x48424d44455031L;

    private static final List<OreEntry> OVERWORLD = List.of(
            stoneOre("ore_uranium", "deepslate_ore_uranium", 7, 5, 5, 20),
            stoneOre("ore_thorium", "deepslate_ore_thorium", 7, 5, 5, 25),
            stoneOre("ore_titanium", "deepslate_ore_titanium", 8, 6, 5, 30),
            stoneOre("ore_sulfur", "deepslate_ore_sulfur", 5, 8, 5, 30),
            stoneOre("ore_aluminium", "deepslate_ore_aluminium", 7, 6, 5, 40),
            stoneOre("ore_fluorite", "deepslate_ore_fluorite", 6, 4, 5, 45),
            stoneOre("ore_niter", "deepslate_ore_niter", 6, 6, 5, 30),
            stoneOre("ore_tungsten", "deepslate_ore_tungsten", 10, 8, 5, 30),
            stoneOre("ore_lead", "deepslate_ore_lead", 6, 9, 5, 30),
            stoneOre("ore_beryllium", "deepslate_ore_beryllium", 6, 4, 5, 30),
            stoneOre("ore_rare", "deepslate_ore_rare", 6, 5, 5, 20),
            stoneOre("ore_lignite", "deepslate_ore_lignite", 2, 24, 35, 25),
            stoneOre("ore_asbestos", "deepslate_ore_asbestos", 2, 4, 16, 16),
            stoneOre("ore_cinnebar", "deepslate_ore_cinnebar", 1, 4, 8, 16),
            stoneOre("ore_cobalt", "deepslate_ore_cobalt", 2, 4, 4, 8),
            rareStoneOre("ore_alexandrite", "deepslate_ore_alexandrite", 100, 3, 10, 5),
            stoneOre("cluster_iron", null, 4, 6, 15, 45),
            stoneOre("cluster_titanium", null, 2, 6, 15, 30),
            stoneOre("cluster_aluminium", null, 3, 6, 15, 35),
            stoneOre("cluster_copper", null, 4, 6, 15, 20),
            baseStoneOre("stone_resource_limestone", 1, 16, 25, 30)
    );

    private static final List<OreEntry> NETHER = List.of(
            ore("ore_nether_uranium", NETHERRACK, 8, 6, 0, 127),
            ore("ore_nether_tungsten", NETHERRACK, 10, 10, 0, 127),
            ore("ore_nether_sulfur", NETHERRACK, 26, 12, 0, 127),
            ore("ore_nether_fire", NETHERRACK, 24, 6, 0, 127),
            ore("ore_nether_coal", NETHERRACK, 8, 32, 16, 96),
            ore("ore_nether_cobalt", NETHERRACK, 2, 6, 100, 26),
            ore("ore_nether_plutonium", NETHERRACK, 8, 4, 0, 127)
    );

    private static final List<OreEntry> END = List.of(
            ore("ore_tikite", END_STONE, 8, 6, 0, 127)
    );

    private static final List<GneissOreEntry> GNEISS_ORES = List.of(
            gneissOre("ore_gneiss_iron", 25, 6, 30, 10),
            gneissOre("ore_gneiss_gold", 10, 6, 30, 10),
            gneissOre("ore_gneiss_uranium", 21, 6, 30, 10),
            gneissOre("ore_gneiss_asbestos", 6, 6, 30, 10),
            gneissOre("ore_gneiss_lithium", 6, 6, 30, 10),
            gneissOre("ore_gneiss_rare", 6, 6, 30, 10),
            gneissOre("ore_gneiss_gas", 15, 10, 30, 10)
    );

    private static final List<ResourceLayerEntry> RESOURCE_LAYERS = List.of(
            resourceLayer("stone_resource_hematite", 0, 0.04D, 0.25D, 230.0D),
            resourceLayer("stone_resource_bauxite", 1, 0.03D, 0.15D, 300.0D),
            resourceLayer("stone_resource_malachite", 2, 0.1D, 0.15D, 275.0D)
    );

    private static final List<OreCaveEntry> ORE_CAVES = List.of(
            oreCave("stone_resource_sulfur", "stalactite_sulfur", "stalagmite_sulfur", 1.5D, 20, 20, 30, true),
            oreCave("stone_resource_asbestos", "stalactite_asbestos", "stalagmite_asbestos", 1.75D, 20, 20, 25, false)
    );

    // oldMinY is a 1.7.10 bottom-relative coordinate. DepthDeposit skips old y=0, so the
    // modern bottom deposit layer starts one block above the current dimension min build height.
    private static final List<DepthDepositEntry> OVERWORLD_DEPTH_DEPOSITS = List.of(
            depthDeposit("cluster_depth_iron", "stone_depth", 24, 5, 0.6D, 0, 3, false),
            depthDeposit("cluster_depth_titanium", "stone_depth", 32, 5, 0.6D, 0, 3, false),
            depthDeposit("cluster_depth_tungsten", "stone_depth", 32, 5, 0.6D, 0, 3, false),
            depthDeposit("ore_depth_cinnebar", "stone_depth", 16, 5, 0.8D, 0, 3, false),
            depthDeposit("ore_depth_zirconium", "stone_depth", 16, 5, 0.8D, 0, 3, false),
            depthDeposit("ore_depth_borax", "stone_depth", 16, 5, 0.8D, 0, 3, false)
    );

    private static final List<DepthDepositEntry> NETHER_DEPTH_DEPOSITS = List.of(
            depthDeposit("ore_depth_nether_neodymium", "stone_depth_nether", 16, 7, 0.6D, 0, 3, false),
            depthDeposit("ore_depth_nether_neodymium", "stone_depth_nether", 16, 7, 0.6D, 125, 3, true)
    );

    private final Map<Long, LegacyPerlinNoise2D> schistNoises = new ConcurrentHashMap<>();
    private final Map<ResourceNoiseKey, ResourceLayerNoise> resourceLayerNoises = new ConcurrentHashMap<>();
    private final Map<OreCaveNoiseKey, LegacyPerlinNoise2D> oreCaveNoises = new ConcurrentHashMap<>();

    public LegacyOreSetFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        List<OreEntry> entries = switch (context.config().dimension()) {
            case "overworld" -> OVERWORLD;
            case "nether" -> NETHER;
            case "end" -> END;
            default -> List.of();
        };
        boolean placedAny = false;
        if ("overworld".equals(context.config().dimension())) {
            placedAny |= placeOilTerrain(context);
            placedAny |= placeSchistStratum(context);
            placedAny |= placeResourceLayers(context);
            placedAny |= placeOreCaves(context);
            placedAny |= placeDepthDeposits(context, OVERWORLD_DEPTH_DEPOSITS, true);
            placedAny |= placeGneissOres(context);
            placedAny |= placeNewBedrockOres(context);
            placedAny |= placeAustraliumDeposit(context);
        } else if ("nether".equals(context.config().dimension())) {
            placedAny |= placeDepthDeposits(context, NETHER_DEPTH_DEPOSITS, false);
        }
        for (OreEntry entry : entries) {
            placedAny |= placeEntry(context, entry);
        }
        return placedAny;
    }

    private boolean placeOilTerrain(FeaturePlaceContext<Configuration> context) {
        boolean placedAny = false;
        placedAny |= placeOilBubble(context);
        placedAny |= placeOilSandBubble(context);
        placedAny |= placeBedrockOil(context);
        return placedAny;
    }

    private boolean placeOilBubble(FeaturePlaceContext<Configuration> context) {
        RegistryObject<? extends Block> oil = ModBlocks.legacyBlock("ore_oil");
        if (oil == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        ChunkBounds bounds = chunkBounds(context.origin());
        boolean placedAny = false;
        for (SourceChunk source : sourceChunks(bounds, 32)) {
            ChunkBounds sourceBounds = source.bounds();
            Holder<Biome> biome = level.getBiome(new BlockPos(sourceBounds.minX(), level.getSeaLevel(), sourceBounds.minZ()));
            int frequency = OIL_BUBBLE_FREQUENCY;
            Biome.ClimateSettings climate = biome.value().getModifiedClimateSettings();
            if (climate.temperature() >= 2.0F && climate.downfall() < 0.1F) {
                frequency /= 3;
            }
            frequency = Math.max(1, frequency);
            RandomSource random = sourceRandom(level.getSeed(), source.chunkX(), source.chunkZ(), OIL_BUBBLE_RANDOM_SALT);
            if (random.nextInt(frequency) != frequency - 1) {
                continue;
            }

            int centerX = sourceBounds.randomX(random);
            int centerZ = sourceBounds.randomZ(random);
            int centerY = mapLegacyOreY(level, 15 + random.nextInt(25));
            double radius = random.nextInt(16 - 8) + 8;
            placedAny |= placeOilBubbleShapeSlice(level, bounds, centerX, centerY, centerZ, radius,
                    oil.get().defaultBlockState(), false, OilBubbleTarget.STONE, level.getSeed());
            placedAny |= addSurfaceOilDamage(level, sourceRandom(level.getSeed(), source.chunkX(), source.chunkZ(),
                            OIL_BUBBLE_RANDOM_SALT + 1), bounds, centerX, centerZ, 7, 150, true);
            placedAny |= addOilSpotHole(level, bounds, centerX, centerZ);
        }
        return placedAny;
    }

    private boolean placeOilSandBubble(FeaturePlaceContext<Configuration> context) {
        RegistryObject<? extends Block> oilSand = ModBlocks.legacyBlock("ore_oil_sand");
        if (oilSand == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        ChunkBounds bounds = chunkBounds(context.origin());
        boolean placedAny = false;
        for (SourceChunk source : sourceChunks(bounds, 64)) {
            ChunkBounds sourceBounds = source.bounds();
            Holder<Biome> biome = level.getBiome(new BlockPos(sourceBounds.minX(), level.getSeaLevel(), sourceBounds.minZ()));
            Biome.ClimateSettings climate = biome.value().getModifiedClimateSettings();
            if (biome.value().hasPrecipitation() || climate.temperature() < 1.5F) {
                continue;
            }
            RandomSource random = sourceRandom(level.getSeed(), source.chunkX(), source.chunkZ(), OIL_SAND_RANDOM_SALT);
            if (random.nextInt(OIL_SAND_FREQUENCY) != OIL_SAND_FREQUENCY - 1) {
                continue;
            }

            int centerX = sourceBounds.randomX(random);
            int centerZ = sourceBounds.randomZ(random);
            int centerY = findSurfaceSandY(level, centerX, centerZ);
            double radius = random.nextInt(48 - 16) + 16;
            placedAny |= placeOilBubbleShapeSlice(level, bounds, centerX, centerY, centerZ, radius,
                    oilSand.get().defaultBlockState(), true, OilBubbleTarget.SAND, level.getSeed());
        }
        return placedAny;
    }

    private boolean placeBedrockOil(FeaturePlaceContext<Configuration> context) {
        RegistryObject<? extends Block> bedrockOil = ModBlocks.legacyBlock("ore_bedrock_oil");
        if (bedrockOil == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        ChunkBounds bounds = chunkBounds(context.origin());
        int minY = level.getMinBuildHeight();
        BlockState bedrockOilState = bedrockOil.get().defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        for (SourceChunk source : sourceChunks(bounds, 32)) {
            RandomSource random = sourceRandom(level.getSeed(), source.chunkX(), source.chunkZ(), BEDROCK_OIL_RANDOM_SALT);
            if (random.nextInt(BEDROCK_OIL_FREQUENCY) != BEDROCK_OIL_FREQUENCY - 2) {
                continue;
            }
            ChunkBounds sourceBounds = source.bounds();
            int centerX = sourceBounds.randomX(random);
            int centerZ = sourceBounds.randomZ(random);
            for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    for (int yOffset = 0; yOffset < 5; yOffset++) {
                        int y = minY + yOffset;
                        cursor.set(x, y, z);
                        if (level.isOutsideBuildHeight(cursor)) {
                            continue;
                        }
                        BlockState target = level.getBlockState(cursor);
                        int dx = x - centerX;
                        int dz = z - centerZ;
                        if ((target.is(Blocks.BEDROCK) || isOilStoneReplaceable(level, cursor, target))
                                && Math.abs(dx) < 5 && Math.abs(dz) < 5
                                && Math.abs(dx) + Math.abs(yOffset) + Math.abs(dz) <= 6) {
                            placedAny |= level.setBlock(cursor, bedrockOilState, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
            placedAny |= addSurfaceOilDamage(level, sourceRandom(level.getSeed(), source.chunkX(), source.chunkZ(),
                    BEDROCK_OIL_RANDOM_SALT + 1), bounds, centerX, centerZ, 5, 50, false);
        }
        return placedAny;
    }

    private boolean placeOilBubbleShapeSlice(WorldGenLevel level, ChunkBounds bounds, int centerX,
            int centerY, int centerZ, double radius, BlockState result, boolean fuzzy,
            OilBubbleTarget targetType, long seed) {
        double radiusSqr = (radius * radius) / 2.0D;
        int yMin = Math.max(level.getMinBuildHeight(), Mth.floor(centerY - radius));
        int yMax = Math.min(level.getMaxBuildHeight() - 1, Mth.ceil(centerY + radius));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                for (int y = yMin; y <= yMax; y++) {
                    cursor.set(x, y, z);
                    BlockState target = level.getBlockState(cursor);
                    if (!isOilBubbleReplaceable(level, cursor, target, targetType)) {
                        continue;
                    }
                    int dx = x - centerX;
                    int dy = centerY - y;
                    int dz = z - centerZ;
                    double dist = dx * dx + dz * dz + dy * dy * 3.0D;
                    if (fuzzy) {
                        dist -= blockRandomDouble(seed, x, y, z, OIL_SAND_RANDOM_SALT) * radiusSqr / 3.0D;
                    }
                    if (dist < radiusSqr) {
                        placedAny |= level.setBlock(cursor, result, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        return placedAny;
    }

    private static boolean isOilBubbleReplaceable(WorldGenLevel level, BlockPos pos, BlockState target,
            OilBubbleTarget targetType) {
        return switch (targetType) {
            case STONE -> isOilStoneReplaceable(level, pos, target);
            case SAND -> target.is(Blocks.SAND) || target.is(Blocks.RED_SAND);
        };
    }

    private static boolean isOilStoneReplaceable(WorldGenLevel level, BlockPos pos, BlockState target) {
        return target.is(BlockTags.BASE_STONE_OVERWORLD) && target.isCollisionShapeFullBlock(level, pos);
    }

    private static int findSurfaceSandY(WorldGenLevel level, int x, int z) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = surfaceY; y >= Math.max(level.getMinBuildHeight(), surfaceY - 16); y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
                return y;
            }
        }
        return Mth.clamp(surfaceY - 1, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
    }

    private boolean addSurfaceOilDamage(WorldGenLevel level, RandomSource random, ChunkBounds bounds, int centerX,
            int centerZ, int width, int count, boolean normalBubbleGradient) {
        RegistryObject<? extends Block> dirtDead = ModBlocks.legacyBlock("dirt_dead");
        RegistryObject<? extends Block> dirtOily = ModBlocks.legacyBlock("dirt_oily");
        RegistryObject<? extends Block> sandDirty = ModBlocks.legacyBlock("sand_dirty");
        RegistryObject<? extends Block> sandDirtyRed = ModBlocks.legacyBlock("sand_dirty_red");
        RegistryObject<? extends Block> stoneCracked = ModBlocks.legacyBlock("stone_cracked");
        RegistryObject<? extends Block> oilSand = ModBlocks.legacyBlock("ore_oil_sand");
        if (dirtDead == null || dirtOily == null || sandDirty == null || sandDirtyRed == null
                || stoneCracked == null) {
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        for (int i = 0; i < count; i++) {
            int offX = (int) (random.nextGaussian() * width);
            int offZ = (int) (random.nextGaussian() * width);
            int x = centerX + offX;
            int z = centerZ + offZ;
            if (!bounds.contains(x, z)) {
                continue;
            }
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            for (int y = surfaceY; y > surfaceY - 4 && y >= level.getMinBuildHeight(); y--) {
                cursor.set(x, y, z);
                BlockState target = level.getBlockState(cursor);
                if (target.is(Blocks.GRASS_BLOCK) || target.is(Blocks.DIRT)) {
                    BlockState replacement;
                    if (normalBubbleGradient) {
                        int distSq = offX * offX + offZ * offZ;
                        boolean inner = distSq < (width / 2) * (width / 2);
                        replacement = (inner ? dirtOily : dirtDead).get().defaultBlockState();
                    } else {
                        replacement = (random.nextInt(10) == 0 ? dirtOily : dirtDead).get().defaultBlockState();
                    }
                    placedAny |= level.setBlock(cursor, replacement, Block.UPDATE_CLIENTS);
                    break;
                }
                if (target.is(Blocks.SAND) || target.is(Blocks.RED_SAND)
                        || (oilSand != null && target.is(oilSand.get()))) {
                    placedAny |= level.setBlock(cursor, (target.is(Blocks.RED_SAND) ? sandDirtyRed : sandDirty)
                            .get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    break;
                }
                if (target.is(Blocks.STONE)) {
                    placedAny |= level.setBlock(cursor, stoneCracked.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    break;
                }
            }
        }
        return placedAny;
    }

    private boolean addOilSpotHole(WorldGenLevel level, ChunkBounds bounds, int centerX, int centerZ) {
        RegistryObject<? extends Block> stoneCracked = ModBlocks.legacyBlock("stone_cracked");
        RegistryObject<? extends Block> oilSpill = ModBlocks.legacyBlock("oil_spill");
        if (stoneCracked == null || oilSpill == null) {
            return false;
        }
        boolean placedAny = false;
        if (bounds.contains(centerX, centerZ)) {
            placedAny |= addOilSpotHoleColumn(level, centerX, centerZ, true,
                    stoneCracked.get().defaultBlockState(), oilSpill.get().defaultBlockState());
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int x = centerX + direction.getStepX();
            int z = centerZ + direction.getStepZ();
            if (bounds.contains(x, z)) {
                placedAny |= addOilSpotHoleColumn(level, x, z, false, stoneCracked.get().defaultBlockState(),
                        oilSpill.get().defaultBlockState());
            }
        }
        return placedAny;
    }

    private boolean addOilSpotHoleColumn(WorldGenLevel level, int x, int z, boolean center, BlockState stoneCracked,
            BlockState oilSpill) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        int solids = 0;
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            cursor.set(x, y, z);
            BlockState target = level.getBlockState(cursor);
            if (target.isAir()) {
                continue;
            }
            if (!target.getFluidState().isEmpty()) {
                break;
            }
            if (!target.isCollisionShapeFullBlock(level, cursor)) {
                continue;
            }
            solids++;
            if (center) {
                if (solids < 3) {
                    placedAny |= level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                } else if (solids == 3) {
                    placedAny |= level.setBlock(cursor, oilSpill, Block.UPDATE_CLIENTS);
                } else if (solids < 7) {
                    placedAny |= level.setBlock(cursor, stoneCracked, Block.UPDATE_CLIENTS);
                } else {
                    break;
                }
            } else {
                placedAny |= level.setBlock(cursor, stoneCracked, Block.UPDATE_CLIENTS);
                if (solids >= 4) {
                    break;
                }
            }
        }
        return placedAny;
    }

    private boolean placeEntry(FeaturePlaceContext<Configuration> context, OreEntry entry) {
        RegistryObject<? extends Block> ore = ModBlocks.legacyBlock(entry.blockName());
        if (ore == null || entry.veinCount() <= 0) {
            return false;
        }
        if ("ore_nether_plutonium".equals(entry.blockName()) && !GeneralConfig.enablePlutoniumOre) {
            return false;
        }
        if (entry.rarity() > 1 && context.random().nextInt(entry.rarity()) != 0) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placedAny = false;
        for (int attempt = 0; attempt < entry.veinCount(); attempt++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            int oldY = entry.oldMinY() + (entry.oldVariance() > 0 ? random.nextInt(entry.oldVariance()) : 0);
            int y = entry.limestoneDeepExtension() ? randomLimestoneY(level, random) : mapLegacyOreY(level, oldY);
            List<LegacyOreTarget> targets = new ArrayList<>();
            if (entry.baseStoneTarget()) {
                targets.add(LegacyOreTarget.baseStone(ore.get().defaultBlockState()));
            } else {
                targets.add(LegacyOreTarget.rule(entry.target(), ore.get().defaultBlockState()));
            }
            if (!entry.baseStoneTarget() && entry.deepslateBlockName() != null) {
                RegistryObject<? extends Block> deepslate = ModBlocks.legacyBlock(entry.deepslateBlockName());
                if (deepslate != null) {
                    targets.add(LegacyOreTarget.rule(DEEPSLATE_REPLACEABLES, deepslate.get().defaultBlockState()));
                }
            }
            placedAny |= placeLegacyMinable(level, random, new BlockPos(x, y, z), targets, entry.size());
        }
        return placedAny;
    }

    private static boolean placeLegacyMinable(WorldGenLevel level, RandomSource random, BlockPos origin,
                                              List<LegacyOreTarget> targets, int size) {
        float angle = random.nextFloat() * (float) Math.PI;
        double xRadius = (double) size / 8.0D;
        double minX = (double) (origin.getX() + 8) + Math.sin(angle) * xRadius;
        double maxX = (double) (origin.getX() + 8) - Math.sin(angle) * xRadius;
        double minZ = (double) (origin.getZ() + 8) + Math.cos(angle) * xRadius;
        double maxZ = (double) (origin.getZ() + 8) - Math.cos(angle) * xRadius;
        double minY = origin.getY() + random.nextInt(3) - 2;
        double maxY = origin.getY() + random.nextInt(3) - 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int step = 0; step < size; step++) {
            float progress = (float) step / (float) size;
            double centerX = minX + (maxX - minX) * progress;
            double centerY = minY + (maxY - minY) * progress;
            double centerZ = minZ + (maxZ - minZ) * progress;
            double randomRadius = random.nextDouble() * (double) size / 16.0D;
            double radius = ((double) (Mth.sin((float) Math.PI * progress) + 1.0F) * randomRadius + 1.0D) / 2.0D;

            int startX = Mth.floor(centerX - radius);
            int startY = Mth.floor(centerY - radius);
            int startZ = Mth.floor(centerZ - radius);
            int endX = Mth.floor(centerX + radius);
            int endY = Mth.floor(centerY + radius);
            int endZ = Mth.floor(centerZ + radius);

            for (int x = startX; x <= endX; x++) {
                double normalizedX = ((double) x + 0.5D - centerX) / radius;
                if (normalizedX * normalizedX >= 1.0D) {
                    continue;
                }
                for (int y = startY; y <= endY; y++) {
                    if (level.isOutsideBuildHeight(y)) {
                        continue;
                    }
                    double normalizedY = ((double) y + 0.5D - centerY) / radius;
                    if (normalizedX * normalizedX + normalizedY * normalizedY >= 1.0D) {
                        continue;
                    }
                    for (int z = startZ; z <= endZ; z++) {
                        double normalizedZ = ((double) z + 0.5D - centerZ) / radius;
                        if (normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ >= 1.0D) {
                            continue;
                        }
                        cursor.set(x, y, z);
                        BlockState targetState = level.getBlockState(cursor);
                        for (LegacyOreTarget target : targets) {
                            if (target.test(level, cursor, targetState, random)) {
                                placedAny |= level.setBlock(cursor, target.state, Block.UPDATE_CLIENTS);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return placedAny;
    }

    private boolean placeSchistStratum(FeaturePlaceContext<Configuration> context) {
        RegistryObject<? extends Block> gneiss = ModBlocks.legacyBlock("stone_gneiss");
        if (gneiss == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        BlockState gneissState = gneiss.get().defaultBlockState();
        LegacyPerlinNoise2D noise = schistNoise(level.getSeed());
        BlockPos origin = context.origin();
        ChunkBounds bounds = legacyDecorateBounds(origin);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                double n = noise.getValue(x * SCHIST_SCALE, z * SCHIST_SCALE);
                if (n <= SCHIST_THRESHOLD) {
                    continue;
                }
                int range = (int) ((n - SCHIST_THRESHOLD) * 3);
                if (range > 4) {
                    range = 8 - range;
                }
                if (range < 0) {
                    continue;
                }
                for (int oldY = 30 - range; oldY <= 30 + range; oldY++) {
                    LegacyYSpan span = mapLegacyOreYSpan(level, oldY);
                    for (int y = span.minY(); y <= span.maxY(); y++) {
                        cursor.set(x, y, z);
                        if (level.isOutsideBuildHeight(cursor)) {
                            continue;
                        }
                        BlockState target = level.getBlockState(cursor);
                        if (isSchistReplaceable(level, cursor, target)) {
                            placedAny |= level.setBlock(cursor, gneissState, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
        return placedAny;
    }

    private LegacyPerlinNoise2D schistNoise(long seed) {
        return schistNoises.computeIfAbsent(seed, key ->
                new LegacyPerlinNoise2D(key, 4));
    }

    private static boolean isSchistReplaceable(WorldGenLevel level, BlockPos pos, BlockState target) {
        return target.is(BlockTags.BASE_STONE_OVERWORLD) && target.isCollisionShapeFullBlock(level, pos);
    }

    private boolean placeResourceLayers(FeaturePlaceContext<Configuration> context) {
        boolean placedAny = false;
        for (ResourceLayerEntry entry : RESOURCE_LAYERS) {
            placedAny |= placeResourceLayer(context, entry);
        }
        return placedAny;
    }

    private boolean placeResourceLayer(FeaturePlaceContext<Configuration> context, ResourceLayerEntry entry) {
        RegistryObject<? extends Block> resource = ModBlocks.legacyBlock(entry.blockName());
        if (resource == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        ChunkBounds bounds = legacyDecorateBounds(origin);
        BlockState resourceState = resource.get().defaultBlockState();
        ResourceLayerNoise noise = resourceLayerNoise(level.getSeed(), entry.id());
        double[][] cacheX = new double[16][65];
        double[][] cacheZ = new double[16][65];
        boolean[][] passes = new boolean[16][65];
        for (int o = 0; o < 16; o++) {
            for (int oldY = 64; oldY > 5; oldY--) {
                cacheX[o][oldY] = noise.x().getValue(oldY * entry.scaleV(),
                        (bounds.minZ() + o) * entry.scaleH());
                cacheZ[o][oldY] = noise.z().getValue((bounds.minX() + o) * entry.scaleH(),
                        oldY * entry.scaleV());
            }
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        for (int ox = 0; ox < 16; ox++) {
            int x = bounds.minX() + ox;
            for (int oz = 0; oz < 16; oz++) {
                int z = bounds.minZ() + oz;
                double ny = noise.y().getValue(x * entry.scaleH(), z * entry.scaleH());
                for (int oldY = 64; oldY > 5; oldY--) {
                    double nx = cacheX[oz][oldY];
                    double nz = cacheX[ox][oldY];
                    passes[oz][oldY] = nx * ny * nz > entry.threshold();
                }
                for (int oldY = 64; oldY > 5; oldY--) {
                    if (!passes[oz][oldY]) {
                        continue;
                    }
                    int minY = mapLegacyOreY(level, oldY);
                    int maxY = minY;
                    if (oldY < 64 && passes[oz][oldY + 1]) {
                        maxY = Math.max(maxY, mapLegacyOreY(level, oldY + 1) - 1);
                    }
                    for (int y = minY; y <= maxY; y++) {
                        cursor.set(x, y, z);
                        if (level.isOutsideBuildHeight(cursor)) {
                            continue;
                        }
                        BlockState target = level.getBlockState(cursor);
                        if (isResourceLayerReplaceable(level, cursor, target)) {
                            placedAny |= level.setBlock(cursor, resourceState, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
        return placedAny;
    }

    private ResourceLayerNoise resourceLayerNoise(long seed, int id) {
        return resourceLayerNoises.computeIfAbsent(new ResourceNoiseKey(seed, id), key -> new ResourceLayerNoise(
                new LegacyPerlinNoise2D(seed + 101 + id, 4),
                new LegacyPerlinNoise2D(seed + 102 + id, 4),
                new LegacyPerlinNoise2D(seed + 103 + id, 4)));
    }

    private static boolean isResourceLayerReplaceable(WorldGenLevel level, BlockPos pos, BlockState target) {
        return target.is(BlockTags.BASE_STONE_OVERWORLD) && target.isCollisionShapeFullBlock(level, pos);
    }

    private boolean placeOreCaves(FeaturePlaceContext<Configuration> context) {
        boolean placedAny = false;
        for (OreCaveEntry entry : ORE_CAVES) {
            placedAny |= placeOreCave(context, entry);
        }
        return placedAny;
    }

    private boolean placeOreCave(FeaturePlaceContext<Configuration> context, OreCaveEntry entry) {
        RegistryObject<? extends Block> resource = ModBlocks.legacyBlock(entry.blockName());
        RegistryObject<? extends Block> stalactite = ModBlocks.legacyBlock(entry.stalactiteBlockName());
        RegistryObject<? extends Block> stalagmite = ModBlocks.legacyBlock(entry.stalagmiteBlockName());
        if (resource == null || stalactite == null || stalagmite == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        ChunkBounds bounds = legacyDecorateBounds(origin);
        BlockState resourceState = resource.get().defaultBlockState();
        BlockState stalactiteState = stalactite.get().defaultBlockState();
        BlockState stalagmiteState = stalagmite.get().defaultBlockState();
        BlockState fluidState = entry.hasFluid() ? ModBlocks.SULFURIC_ACID_BLOCK.get().defaultBlockState() : null;
        LegacyPerlinNoise2D noise = oreCaveNoise(level.getSeed(), entry.yLevel());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                double n = noise.getValue(x * ORE_CAVE_SCALE, z * ORE_CAVE_SCALE);
                if (n <= entry.threshold()) {
                    continue;
                }
                int range = (int) ((n - entry.threshold()) * entry.rangeMult());
                if (range > entry.maxRange()) {
                    range = (entry.maxRange() * 2) - range;
                }
                if (range < 0) {
                    continue;
                }

                int centerY = mapLegacyOreY(level, entry.yLevel());
                for (int y = centerY - range; y <= centerY + range; y++) {
                    cursor.set(x, y, z);
                    if (level.isOutsideBuildHeight(cursor)) {
                        continue;
                    }
                    BlockState target = level.getBlockState(cursor);
                    if (isOreCaveReplaceable(level, cursor, target)) {
                        if (fluidState != null && random.nextBoolean()
                                && canPlaceOreCaveFluid(level, cursor, fluidState, resourceState)) {
                            placedAny |= level.setBlock(cursor, fluidState, Block.UPDATE_CLIENTS);
                            placedAny |= placeOreCaveFluidRim(level, cursor, resourceState);
                        } else if (isNextToCaveAirOrSpike(level, cursor)) {
                            placedAny |= level.setBlock(cursor, resourceState, Block.UPDATE_CLIENTS);
                        }
                    } else if (canTryOreCaveSpike(level, cursor, target) && random.nextInt(5) == 0) {
                        if (canPlaceCaveSpike(level, cursor, stalactiteState)) {
                            placedAny |= level.setBlock(cursor, stalactiteState, Block.UPDATE_CLIENTS);
                        } else if (canPlaceCaveSpike(level, cursor, stalagmiteState)) {
                            placedAny |= level.setBlock(cursor, stalagmiteState, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
        return placedAny;
    }

    private LegacyPerlinNoise2D oreCaveNoise(long seed, int yLevel) {
        return oreCaveNoises.computeIfAbsent(new OreCaveNoiseKey(seed, yLevel),
                key -> new LegacyPerlinNoise2D(seed + yLevel, 2));
    }

    private static boolean isOreCaveReplaceable(WorldGenLevel level, BlockPos pos, BlockState target) {
        return target.is(BlockTags.BASE_STONE_OVERWORLD) && target.isCollisionShapeFullBlock(level, pos);
    }

    private static boolean canPlaceOreCaveFluid(WorldGenLevel level, BlockPos pos, BlockState fluidState,
                                                BlockState resourceState) {
        BlockPos above = pos.above();
        BlockPos below = pos.below();
        if (level.isOutsideBuildHeight(above) || level.isOutsideBuildHeight(below)) {
            return false;
        }

        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.isAir() && !isOreCaveSpike(aboveState)) {
            return false;
        }

        if (!level.getBlockState(below).isCollisionShapeFullBlock(level, below)) {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            BlockState sideState = level.getBlockState(side);
            if (!sideState.isCollisionShapeFullBlock(level, side) && !sideState.is(fluidState.getBlock())) {
                return false;
            }
        }

        return isOreCaveReplaceable(level, pos, level.getBlockState(pos))
                && isOreCaveReplaceable(level, below, level.getBlockState(below))
                && resourceState.canSurvive(level, below);
    }

    private static boolean placeOreCaveFluidRim(WorldGenLevel level, BlockPos pos, BlockState resourceState) {
        boolean placedAny = false;
        BlockPos below = pos.below();
        if (!level.isOutsideBuildHeight(below) && isOreCaveReplaceable(level, below, level.getBlockState(below))) {
            placedAny |= level.setBlock(below, resourceState, Block.UPDATE_CLIENTS);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            if (!level.isOutsideBuildHeight(side) && isOreCaveReplaceable(level, side, level.getBlockState(side))) {
                placedAny |= level.setBlock(side, resourceState, Block.UPDATE_CLIENTS);
            }
        }
        return placedAny;
    }

    private static boolean isNextToCaveAirOrSpike(WorldGenLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            neighborPos.setWithOffset(pos, direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.isAir() || isOreCaveSpike(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canTryOreCaveSpike(WorldGenLevel level, BlockPos pos, BlockState target) {
        return (target.isAir() || !target.isCollisionShapeFullBlock(level, pos)) && target.getFluidState().isEmpty();
    }

    private static boolean canPlaceCaveSpike(WorldGenLevel level, BlockPos pos, BlockState spikeState) {
        return !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).canBeReplaced()
                && spikeState.canSurvive(level, pos);
    }

    private static boolean isOreCaveSpike(BlockState state) {
        return state.is(ModBlocks.STALACTITE_SULFUR.get()) || state.is(ModBlocks.STALACTITE_ASBESTOS.get())
                || state.is(ModBlocks.STALAGMITE_SULFUR.get()) || state.is(ModBlocks.STALAGMITE_ASBESTOS.get());
    }

    private boolean placeGneissOres(FeaturePlaceContext<Configuration> context) {
        RegistryObject<? extends Block> gneiss = ModBlocks.legacyBlock("stone_gneiss");
        if (gneiss == null) {
            return false;
        }
        boolean placedAny = false;
        RuleTest gneissTarget = new BlockMatchTest(gneiss.get());
        for (GneissOreEntry entry : GNEISS_ORES) {
            placedAny |= placeGneissOre(context, entry, gneissTarget);
        }
        return placedAny;
    }

    private boolean placeGneissOre(FeaturePlaceContext<Configuration> context, GneissOreEntry entry,
                                   RuleTest gneissTarget) {
        RegistryObject<? extends Block> ore = ModBlocks.legacyBlock(entry.blockName());
        if (ore == null || entry.veinCount() <= 0) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placedAny = false;
        for (int attempt = 0; attempt < entry.veinCount(); attempt++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            int oldY = entry.oldMinY() + (entry.oldVariance() > 0 ? random.nextInt(entry.oldVariance()) : 0);
            int y = mapLegacyOreY(level, oldY);
            placedAny |= placeLegacyMinable(level, random, new BlockPos(x, y, z),
                    List.of(LegacyOreTarget.rule(gneissTarget, ore.get().defaultBlockState())), entry.size());
        }
        return placedAny;
    }

    private boolean placeDepthDeposits(FeaturePlaceContext<Configuration> context, List<DepthDepositEntry> entries,
                                       boolean overworld) {
        boolean placedAny = false;
        for (DepthDepositEntry entry : entries) {
            placedAny |= placeDepthDeposit(context, entry, overworld);
        }
        return placedAny;
    }

    private boolean placeDepthDeposit(FeaturePlaceContext<Configuration> context, DepthDepositEntry entry,
                                      boolean overworld) {
        if (entry.chance() <= 0) {
            return false;
        }
        RegistryObject<? extends Block> ore = ModBlocks.legacyBlock(entry.blockName());
        RegistryObject<? extends Block> filler = ModBlocks.legacyBlock(entry.fillerBlockName());
        if (ore == null || filler == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        ChunkBounds bounds = chunkBounds(context.origin());
        boolean placedAny = false;
        for (SourceChunk source : sourceChunks(bounds, entry.size())) {
            RandomSource random = sourceRandom(level.getSeed(), source.chunkX(), source.chunkZ(),
                    DEPTH_DEPOSIT_RANDOM_SALT + entry.blockName().hashCode());
            if (random.nextInt(entry.chance()) != 0) {
                continue;
            }
            ChunkBounds sourceBounds = source.bounds();
            int centerX = sourceBounds.randomDecorateX(random);
            int centerY = mapLegacyDepthDepositY(level, entry.oldMinY(), entry.oldYDev(), entry.fromTop(), random);
            int centerZ = sourceBounds.randomDecorateZ(random);
            placedAny |= generateDepthDeposit(level, bounds, centerX, centerY, centerZ, entry.size(), entry.fill(),
                    ore.get().defaultBlockState(), filler.get().defaultBlockState(), overworld,
                    level.getSeed() ^ entry.blockName().hashCode());
        }
        return placedAny;
    }

    private static boolean generateDepthDeposit(WorldGenLevel level, ChunkBounds bounds,
                                                int centerX, int centerY, int centerZ, int size, double fill, BlockState ore,
                                                BlockState filler, boolean overworld, long seed) {
        boolean placedAny = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = Math.max(bounds.minX(), centerX - size); x <= Math.min(bounds.maxX(), centerX + size); x++) {
            for (int y = centerY - size; y <= centerY + size; y++) {
                cursor.set(x, y, centerZ);
                if (level.isOutsideBuildHeight(cursor) || y <= level.getMinBuildHeight()) {
                    continue;
                }
                for (int z = Math.max(bounds.minZ(), centerZ - size); z <= Math.min(bounds.maxZ(), centerZ + size); z++) {
                    cursor.set(x, y, z);
                    BlockState target = level.getBlockState(cursor);
                    if (!isDepthReplaceable(target, overworld)) {
                        continue;
                    }
                    int dx = centerX - x;
                    int dy = centerY - y;
                    int dz = centerZ - z;
                    double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (len + blockRandomInt(seed, x, y, z, 2, 0x31L) < size * fill) {
                        placedAny |= level.setBlock(cursor, ore, Block.UPDATE_CLIENTS);
                    } else if (len + blockRandomInt(seed, x, y, z, 2, 0x32L) <= size) {
                        placedAny |= level.setBlock(cursor, filler, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        return placedAny;
    }

    private static boolean isDepthReplaceable(BlockState target, boolean overworld) {
        if (target.is(Blocks.BEDROCK)) {
            return true;
        }
        return overworld ? target.is(BlockTags.BASE_STONE_OVERWORLD) : target.is(Blocks.NETHERRACK);
    }

    private boolean placeNewBedrockOres(FeaturePlaceContext<Configuration> context) {
        RegistryObject<? extends Block> bedrockOre = ModBlocks.legacyBlock("ore_bedrock");
        RegistryObject<? extends Block> depthRock = ModBlocks.legacyBlock("stone_depth");
        if (bedrockOre == null || depthRock == null
                || context.random().nextInt(NEW_BEDROCK_ORE_FREQUENCY) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        ChunkBounds bounds = chunkBounds(origin);
        int centerX = origin.getX() + 8 + random.nextInt(2);
        int centerZ = origin.getZ() + 8 + random.nextInt(2);
        int minY = level.getMinBuildHeight();
        ItemStack resource = new ItemStack(ModItems.BEDROCK_ORE_BASE.get());
        BedrockOreBaseItem.setOreAmount(resource, centerX, centerZ, 1.0D);
        BedrockBoreRequirement requirement = bedrockBoreRequirement(averageBedrockOreDensity(centerX, centerZ));

        BlockState oreState = bedrockOre.get().defaultBlockState();
        BlockState depthState = depthRock.get().defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;
        for (int x = Math.max(bounds.minX(), centerX - 1); x <= Math.min(bounds.maxX(), centerX + 1); x++) {
            for (int z = Math.max(bounds.minZ(), centerZ - 1); z <= Math.min(bounds.maxZ(), centerZ + 1); z++) {
                cursor.set(x, minY, z);
                if (level.isOutsideBuildHeight(cursor) || !level.getBlockState(cursor).is(Blocks.BEDROCK)) {
                    continue;
                }
                if ((x == centerX && z == centerZ) || random.nextBoolean()) {
                    if (level.setBlock(cursor, oreState, Block.UPDATE_CLIENTS)) {
                        if (level.getBlockEntity(cursor) instanceof BedrockOreDepositBlockEntity deposit) {
                            deposit.configure(resource, requirement.fluid(), requirement.amount(),
                                    requirement.tier(), BEDROCK_ORE_COLOR, random.nextInt(10));
                        }
                        placedAny = true;
                    }
                }
            }
        }

        for (int x = Math.max(bounds.minX(), centerX - 3); x <= Math.min(bounds.maxX(), centerX + 3); x++) {
            for (int z = Math.max(bounds.minZ(), centerZ - 3); z <= Math.min(bounds.maxZ(), centerZ + 3); z++) {
                for (int yOffset = 1; yOffset < 7; yOffset++) {
                    int y = minY + yOffset;
                    cursor.set(x, y, z);
                    if (level.isOutsideBuildHeight(cursor)) {
                        continue;
                    }
                    BlockState target = level.getBlockState(cursor);
                    if ((yOffset < 3 || target.is(Blocks.BEDROCK))
                            && (target.is(Blocks.BEDROCK) || isDepthReplaceable(target, true))) {
                        placedAny |= level.setBlock(cursor, depthState, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        return placedAny;
    }

    private boolean placeAustraliumDeposit(FeaturePlaceContext<Configuration> context) {
        RegistryObject<? extends Block> australium = ModBlocks.legacyBlock("ore_australium");
        if (australium == null) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placedAny = false;
        int attempts = random.nextInt(4);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            if (x < -450 || x > -350 || z < -450 || z > -350) {
                continue;
            }
            int y = mapLegacyOreY(level, 15 + random.nextInt(15));
            List<LegacyOreTarget> targets = List.of(
                    LegacyOreTarget.rule(STONE_REPLACEABLES, australium.get().defaultBlockState()),
                    LegacyOreTarget.rule(DEEPSLATE_REPLACEABLES, australium.get().defaultBlockState())
            );
            placedAny |= placeLegacyMinable(level, random, new BlockPos(x, y, z), targets, 50);
        }
        return placedAny;
    }

    private static double averageBedrockOreDensity(int x, int z) {
        double total = 0.0D;
        for (BedrockOreType type : BedrockOreType.values()) {
            total += BedrockOreBaseItem.getOreLevel(x, z, type);
        }
        return total / BedrockOreType.values().length;
    }

    private static BedrockBoreRequirement bedrockBoreRequirement(double density) {
        if (density > 1.5D) {
            return new BedrockBoreRequirement(4, HbmFluids.SOLVENT, 2_000);
        }
        if (density > 1.0D) {
            return new BedrockBoreRequirement(3, HbmFluids.SULFURIC_ACID, 1_000);
        }
        if (density > 0.75D) {
            return new BedrockBoreRequirement(2, HbmFluids.WATER, 1_000);
        }
        return new BedrockBoreRequirement(1, HbmFluids.NONE, 0);
    }

    private static int mapLegacyOreY(WorldGenLevel level, int oldY) {
        int minY = level.getMinBuildHeight();
        int height = Math.max(1, level.getMaxBuildHeight() - minY);
        int offset = Mth.floor(Mth.clamp(oldY, 0, 127) / 127.0D * (height - 1));
        return Mth.clamp(minY + offset, minY, level.getMaxBuildHeight() - 1);
    }

    private static LegacyYSpan mapLegacyOreYSpan(WorldGenLevel level, int oldY) {
        int minY = mapLegacyOreY(level, oldY);
        int nextY = mapLegacyOreY(level, oldY + 1);
        int maxY = Math.max(minY, nextY - 1);
        return new LegacyYSpan(minY, maxY);
    }

    private static int randomLimestoneY(WorldGenLevel level, RandomSource random) {
        int minY = Math.max(level.getMinBuildHeight(), LIMESTONE_MODERN_MIN_Y);
        int maxY = Math.max(minY, mapLegacyOreY(level, 54));
        return minY + random.nextInt(maxY - minY + 1);
    }

    private static int mapLegacyDepthDepositY(WorldGenLevel level, int oldMinY, int oldYDev, boolean fromTop,
                                              RandomSource random) {
        int offset = oldMinY + (oldYDev > 0 ? random.nextInt(oldYDev) : 0);
        if (fromTop) {
            return Mth.clamp(level.getMaxBuildHeight() - 128 + offset, level.getMinBuildHeight() + 1,
                    level.getMaxBuildHeight() - 1);
        }
        return Mth.clamp(level.getMinBuildHeight() + 1 + offset, level.getMinBuildHeight() + 1,
                level.getMaxBuildHeight() - 1);
    }

    private static OreEntry stoneOre(String blockName, String deepslateBlockName, int veinCount, int size,
                                     int oldMinY, int oldVariance) {
        return new OreEntry(blockName, deepslateBlockName, STONE_REPLACEABLES, false, false, 1, veinCount, size,
                oldMinY, oldVariance);
    }

    private static OreEntry rareStoneOre(String blockName, String deepslateBlockName, int rarity, int size,
                                         int oldMinY, int oldVariance) {
        return new OreEntry(blockName, deepslateBlockName, STONE_REPLACEABLES, false, false, rarity, 1, size, oldMinY,
                oldVariance);
    }

    private static OreEntry ore(String blockName, RuleTest target, int veinCount, int size, int oldMinY,
                                int oldVariance) {
        return new OreEntry(blockName, null, target, false, false, 1, veinCount, size, oldMinY, oldVariance);
    }

    private static OreEntry baseStoneOre(String blockName, int veinCount, int size, int oldMinY, int oldVariance) {
        return new OreEntry(blockName, null, null, true, "stone_resource_limestone".equals(blockName), 1, veinCount,
                size, oldMinY, oldVariance);
    }

    private static DepthDepositEntry depthDeposit(String blockName, String fillerBlockName, int chance, int size,
                                                  double fill, int oldMinY, int oldYDev, boolean fromTop) {
        return new DepthDepositEntry(blockName, fillerBlockName, chance, size, fill, oldMinY, oldYDev, fromTop);
    }

    private static GneissOreEntry gneissOre(String blockName, int veinCount, int size, int oldMinY, int oldVariance) {
        return new GneissOreEntry(blockName, veinCount, size, oldMinY, oldVariance);
    }

    private static ResourceLayerEntry resourceLayer(String blockName, int id, double scaleH, double scaleV,
                                                    double threshold) {
        return new ResourceLayerEntry(blockName, id, scaleH, scaleV, threshold);
    }

    private static OreCaveEntry oreCave(String blockName, String stalactiteBlockName, String stalagmiteBlockName,
            double threshold, int rangeMult, int maxRange, int yLevel, boolean hasFluid) {
        return new OreCaveEntry(blockName, stalactiteBlockName, stalagmiteBlockName, threshold, rangeMult, maxRange,
                yLevel, hasFluid);
    }

    private record OreEntry(String blockName, String deepslateBlockName, RuleTest target, boolean baseStoneTarget,
                            boolean limestoneDeepExtension, int rarity, int veinCount, int size, int oldMinY,
                            int oldVariance) {
    }

    private record GneissOreEntry(String blockName, int veinCount, int size, int oldMinY, int oldVariance) {
    }

    private record ResourceLayerEntry(String blockName, int id, double scaleH, double scaleV, double threshold) {
    }

    private record OreCaveEntry(String blockName, String stalactiteBlockName, String stalagmiteBlockName,
                                double threshold, int rangeMult, int maxRange, int yLevel, boolean hasFluid) {
    }

    private record ResourceNoiseKey(long seed, int id) {
    }

    private record OreCaveNoiseKey(long seed, int yLevel) {
    }

    private enum OilBubbleTarget {
        STONE,
        SAND
    }

    private record ResourceLayerNoise(LegacyPerlinNoise2D x, LegacyPerlinNoise2D y, LegacyPerlinNoise2D z) {
    }

    private record LegacyOreTarget(RuleTest rule, BlockState state, boolean baseStoneOverworld) {
        static LegacyOreTarget rule(RuleTest rule, BlockState state) {
            return new LegacyOreTarget(rule, state, false);
        }

        static LegacyOreTarget baseStone(BlockState state) {
            return new LegacyOreTarget(null, state, true);
        }

        boolean test(WorldGenLevel level, BlockPos pos, BlockState target, RandomSource random) {
            if (baseStoneOverworld) {
                return target.is(BlockTags.BASE_STONE_OVERWORLD) && target.isCollisionShapeFullBlock(level, pos);
            }
            return rule.test(target, random);
        }
    }

    private record DepthDepositEntry(String blockName, String fillerBlockName, int chance, int size, double fill,
                                     int oldMinY, int oldYDev, boolean fromTop) {
    }

    private record BedrockBoreRequirement(int tier, FluidType fluid, int amount) {
    }

    private static ChunkBounds chunkBounds(BlockPos origin) {
        int chunkX = Math.floorDiv(origin.getX(), 16);
        int chunkZ = Math.floorDiv(origin.getZ(), 16);
        return chunkBounds(chunkX, chunkZ);
    }

    private static ChunkBounds legacyDecorateBounds(BlockPos origin) {
        ChunkBounds base = chunkBounds(origin);
        return new ChunkBounds(base.minX() + 8, base.maxX() + 8, base.minZ() + 8, base.maxZ() + 8);
    }

    private static ChunkBounds chunkBounds(int chunkX, int chunkZ) {
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;
        return new ChunkBounds(minX, minX + 15, minZ, minZ + 15);
    }

    private static List<SourceChunk> sourceChunks(ChunkBounds target, int reach) {
        int minChunkX = Math.floorDiv(target.minX() - reach, 16);
        int maxChunkX = Math.floorDiv(target.maxX() + reach, 16);
        int minChunkZ = Math.floorDiv(target.minZ() - reach, 16);
        int maxChunkZ = Math.floorDiv(target.maxZ() + reach, 16);
        List<SourceChunk> chunks = new ArrayList<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                chunks.add(new SourceChunk(chunkX, chunkZ, chunkBounds(chunkX, chunkZ)));
            }
        }
        return chunks;
    }

    private static RandomSource sourceRandom(long worldSeed, int chunkX, int chunkZ, long salt) {
        WorldgenRandom seeder = new WorldgenRandom(new LegacyRandomSource(worldSeed ^ salt));
        long xSeed = seeder.nextLong() | 1L;
        long zSeed = seeder.nextLong() | 1L;
        return RandomSource.create((long) chunkX * xSeed ^ (long) chunkZ * zSeed ^ worldSeed ^ salt);
    }

    private static double blockRandomDouble(long seed, int x, int y, int z, long salt) {
        long mixed = mixBlockSeed(seed, x, y, z, salt);
        return (double) ((mixed >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
    }

    private static int blockRandomInt(long seed, int x, int y, int z, int bound, long salt) {
        if (bound <= 1) {
            return 0;
        }
        long mixed = mixBlockSeed(seed, x, y, z, salt);
        return Math.floorMod((int) (mixed >>> 32), bound);
    }

    private static long mixBlockSeed(long seed, int x, int y, int z, long salt) {
        long mixed = seed ^ salt;
        mixed ^= (long) x * 0x9E3779B97F4A7C15L;
        mixed ^= (long) y * 0xC2B2AE3D27D4EB4FL;
        mixed ^= (long) z * 0x165667B19E3779F9L;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return mixed;
    }

    private record ChunkBounds(int minX, int maxX, int minZ, int maxZ) {
        int randomX(RandomSource random) {
            return minX + random.nextInt(16);
        }

        int randomZ(RandomSource random) {
            return minZ + random.nextInt(16);
        }

        int randomDecorateX(RandomSource random) {
            return minX + 8 + random.nextInt(16);
        }

        int randomDecorateZ(RandomSource random) {
            return minZ + 8 + random.nextInt(16);
        }

        boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }

    private record SourceChunk(int chunkX, int chunkZ, ChunkBounds bounds) {
    }

    private record LegacyYSpan(int minY, int maxY) {
    }

    private static final class LegacyPerlinNoise2D {
        private final LegacySimplexNoise2D[] levels;

        private LegacyPerlinNoise2D(long seed, int octaves) {
            Random random = new Random(seed);
            this.levels = new LegacySimplexNoise2D[octaves];
            for (int i = 0; i < octaves; i++) {
                this.levels[i] = new LegacySimplexNoise2D(random);
            }
        }

        private double getValue(double x, double y) {
            double value = 0.0D;
            double scale = 1.0D;
            for (LegacySimplexNoise2D level : levels) {
                value += level.getValue(x * scale, y * scale) / scale;
                scale /= 2.0D;
            }
            return value;
        }
    }

    private static final class LegacySimplexNoise2D {
        private static final int[][] GRADIENTS = {
                {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
                {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
                {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}
        };
        private static final double SQRT_3 = Math.sqrt(3.0D);
        private static final double F2 = 0.5D * (SQRT_3 - 1.0D);
        private static final double G2 = (3.0D - SQRT_3) / 6.0D;
        private final int[] permutation = new int[512];
        private final double xOffset;
        private final double yOffset;
        private final double zOffset;

        private LegacySimplexNoise2D(Random random) {
            this.xOffset = random.nextDouble() * 256.0D;
            this.yOffset = random.nextDouble() * 256.0D;
            this.zOffset = random.nextDouble() * 256.0D;
            int[] source = new int[256];
            for (int i = 0; i < 256; i++) {
                source[i] = i;
            }
            for (int i = 0; i < 256; i++) {
                int swapIndex = random.nextInt(256 - i) + i;
                int value = source[i];
                source[i] = source[swapIndex];
                source[swapIndex] = value;
                permutation[i] = source[i];
                permutation[i + 256] = permutation[i];
            }
        }

        private double getValue(double x, double y) {
            x += xOffset;
            y += yOffset;
            double skew = (x + y) * F2;
            int cellX = fastFloor(x + skew);
            int cellY = fastFloor(y + skew);
            double unskew = (cellX + cellY) * G2;
            double originX = cellX - unskew;
            double originY = cellY - unskew;
            double x0 = x - originX;
            double y0 = y - originY;

            int stepX;
            int stepY;
            if (x0 > y0) {
                stepX = 1;
                stepY = 0;
            } else {
                stepX = 0;
                stepY = 1;
            }

            double x1 = x0 - stepX + G2;
            double y1 = y0 - stepY + G2;
            double x2 = x0 - 1.0D + 2.0D * G2;
            double y2 = y0 - 1.0D + 2.0D * G2;
            int ii = cellX & 255;
            int jj = cellY & 255;
            int gi0 = permutation[ii + permutation[jj]] % 12;
            int gi1 = permutation[ii + stepX + permutation[jj + stepY]] % 12;
            int gi2 = permutation[ii + 1 + permutation[jj + 1]] % 12;

            return 70.0D * (corner(gi0, x0, y0) + corner(gi1, x1, y1) + corner(gi2, x2, y2));
        }

        private static int fastFloor(double value) {
            int floor = (int) value;
            return value < floor ? floor - 1 : floor;
        }

        private static double corner(int gradient, double x, double y) {
            double t = 0.5D - x * x - y * y;
            if (t < 0.0D) {
                return 0.0D;
            }
            t *= t;
            int[] grad = GRADIENTS[gradient];
            return t * t * (grad[0] * x + grad[1] * y);
        }
    }

    public record Configuration(String dimension) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("dimension").forGetter(Configuration::dimension)
        ).apply(instance, Configuration::new));
    }
}

package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyDeadPlantBlock;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.world.OilSpot;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Source-contract regression coverage for {@code world.feature.OilSpot}. */
@PrefixGameTestTemplate(false)
public final class OilSpotGameTests {
    // Minecraft's empty GameTest template has a top barrier at relative Y=122.
    // OilSpot correctly starts from that highest opaque block, so the fixture
    // must put its source-world surface above it.
    private static final int TEST_SURFACE_Y = 124;

    private OilSpotGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(OilSpotGameTests.class);
    }

    /** Vanilla flowers become {@code plant_dead:FLOWER}; mustard willow is explicitly protected. */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "oilSpot")
    public static void oilSpotConvertsFlowersButPreservesMustardWillow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Empty-template cells are cleared independently while the full suite is
        // running.  Keep the source-shaped four-block scan in a dedicated forced
        // chunk so another cell cannot add a taller height-map entry mid-test.
        BlockPos fixtureBase = helper.absolutePos(new BlockPos(0, TEST_SURFACE_Y, 640_000));
        BlockPos flowerPos = fixtureBase.offset(3, 0, 3);
        BlockPos willowPos = fixtureBase.offset(8, 0, 3);
        forceChunk(level, flowerPos, true);
        forceChunk(level, willowPos, true);
        try {
            level.setBlock(flowerPos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(flowerPos, Blocks.DANDELION.defaultBlockState(), Block.UPDATE_ALL);
            placeHeightAnchor(level, flowerPos);
            requireOilSpotScanIncludes(level, flowerPos);
            OilSpot.generateOilSpot(level, flowerPos, 0, 1, false);
            requireDeadPlant(level.getBlockState(flowerPos), LegacyDeadPlantBlock.Type.FLOWER,
                    "OilSpot must replace a vanilla flower with plant_dead:FLOWER");

            level.setBlock(willowPos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(willowPos, ModBlocks.PLANT_FLOWER_CD0.get().defaultBlockState(), Block.UPDATE_ALL);
            placeHeightAnchor(level, willowPos);
            requireOilSpotScanIncludes(level, willowPos);
            OilSpot.generateOilSpot(level, willowPos, 0, 1, false);
            if (!level.getBlockState(willowPos).is(ModBlocks.PLANT_FLOWER_CD0.get())) {
                throw new AssertionError("OilSpot must preserve source-protected mustard willow CD0");
            }
        } finally {
            clearColumn(level, flowerPos);
            clearColumn(level, willowPos);
            forceChunk(level, flowerPos, false);
            forceChunk(level, willowPos, false);
        }
        helper.succeed();
    }

    /** Old metadata only removes a leaf when its decay-check bit is set. */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "oilSpot")
    public static void oilSpotRemovesOnlyNaturallyDecayingLeaves(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos fixtureBase = helper.absolutePos(new BlockPos(0, TEST_SURFACE_Y, 640_000));
        BlockPos decayingPos = fixtureBase.offset(3, 0, 8);
        BlockPos connectedPos = fixtureBase.offset(8, 0, 8);
        forceChunk(level, decayingPos, true);
        forceChunk(level, connectedPos, true);
        try {
            level.setBlock(decayingPos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(decayingPos, oakLeaves(false, 7), Block.UPDATE_ALL);
            placeHeightAnchor(level, decayingPos);
            requireOilSpotScanIncludes(level, decayingPos);
            OilSpot.generateOilSpot(level, decayingPos, 0, 1, false);
            if (!level.getBlockState(decayingPos).isAir()) {
                throw new AssertionError("OilSpot must remove a natural leaf block at legacy decay distance");
            }

            level.setBlock(connectedPos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(connectedPos, oakLeaves(false, 1), Block.UPDATE_ALL);
            placeHeightAnchor(level, connectedPos);
            requireOilSpotScanIncludes(level, connectedPos);
            OilSpot.generateOilSpot(level, connectedPos, 0, 1, false);
            if (!level.getBlockState(connectedPos).is(Blocks.OAK_LEAVES)) {
                throw new AssertionError("OilSpot must not remove a non-persistent leaf that is still connected");
            }
        } finally {
            clearColumn(level, decayingPos);
            clearColumn(level, connectedPos);
            forceChunk(level, decayingPos, false);
            forceChunk(level, connectedPos, false);
        }
        helper.succeed();
    }

    /**
     * User-approved replacement for old WorldUtil#setBiome: write the target
     * X/Z quart through every modern biome section and use native chunk sync.
     */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "worldBiomeBridge")
    public static void worldUtilBiomeBridgeWritesEverySectionQuartAndUsesNativeSync(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos target = helper.absolutePos(new BlockPos(4, 8, 2_700_000));
        forceChunk(level, target, true);
        try {
            LevelChunk chunk = level.getChunk(target.getX() >> 4, target.getZ() >> 4);
            Holder<Biome> desert = level.registryAccess().registryOrThrow(Registries.BIOME)
                    .getHolderOrThrow(Biomes.DESERT);
            Holder<Biome> requestedBiome = desert;
            int localQuartX = QuartPos.fromBlock(target.getX()) & 3;
            int localQuartZ = QuartPos.fromBlock(target.getZ()) & 3;
            boolean everyTargetQuartIsDesert = true;
            for (LevelChunkSection section : chunk.getSections()) {
                for (int quartY = 0; quartY < 4; quartY++) {
                    if (!section.getNoiseBiome(localQuartX, quartY, localQuartZ).equals(desert)) {
                        everyTargetQuartIsDesert = false;
                        break;
                    }
                }
                if (!everyTargetQuartIsDesert) {
                    break;
                }
            }
            if (everyTargetQuartIsDesert) {
                requestedBiome = level.registryAccess().registryOrThrow(Registries.BIOME)
                        .getHolderOrThrow(Biomes.PLAINS);
            }
            boolean changed = WorldUtil.setBiome(level, target.getX(), target.getZ(), requestedBiome);
            if (!changed) {
                throw new AssertionError("quart biome bridge must report a changed target for its selected target biome");
            }
            if (!chunk.isUnsaved()) {
                throw new AssertionError("quart biome bridge must mark its changed LevelChunk unsaved");
            }
            for (int sectionIndex = 0; sectionIndex < chunk.getSections().length; sectionIndex++) {
                LevelChunkSection section = chunk.getSection(sectionIndex);
                for (int quartY = 0; quartY < 4; quartY++) {
                    if (!section.getNoiseBiome(localQuartX, quartY, localQuartZ).equals(requestedBiome)) {
                        throw new AssertionError("quart biome bridge did not update section=" + sectionIndex
                                + ", quartY=" + quartY);
                    }
                }
            }
            // Native ChunkMap resend replaces the old custom single-block/chunk packet.
            WorldUtil.syncBiomeChange(level, target.getX(), target.getZ());
            WorldUtil.syncBiomeChangeBlock(level, target.getX(), target.getZ());
        } finally {
            forceChunk(level, target, false);
        }
        helper.succeed();
    }

    private static BlockState oakLeaves(boolean persistent, int distance) {
        return Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, persistent)
                .setValue(LeavesBlock.DISTANCE, distance);
    }

    private static void requireDeadPlant(BlockState state, LegacyDeadPlantBlock.Type expected, String message) {
        if (!(state.getBlock() instanceof LegacyDeadPlantBlock)
                || state.getValue(LegacyDeadPlantBlock.TYPE) != expected) {
            throw new AssertionError(message + "; got " + state);
        }
    }

    /**
     * OilSpot starts at the legacy height-map result and scans only four blocks
     * downward. Anchor the fixture's surface without using a block that OilSpot
     * itself transforms, so each target remains inside that source-shaped window.
     */
    private static void placeHeightAnchor(ServerLevel level, BlockPos target) {
        // This fixture lives in a forced chunk outside the packed template grid.  Remove
        // any generated terrain above the controlled surface, otherwise its height-map
        // entry would legitimately sit outside OilSpot's source four-cell scan.
        for (int y = target.getY() + 2; y < level.getMaxBuildHeight(); y++) {
            level.removeBlock(new BlockPos(target.getX(), y, target.getZ()), false);
        }
        level.setBlock(target.above(), Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void requireOilSpotScanIncludes(ServerLevel level, BlockPos target) {
        int surfaceY = WorldUtil.legacyGetHeightValue(level, target.getX(), target.getZ());
        if (target.getY() > surfaceY || target.getY() <= surfaceY - 4) {
            throw new AssertionError("OilSpot fixture target " + target + " is outside its source four-cell scan; "
                    + "height=" + surfaceY + ", state=" + level.getBlockState(target));
        }
    }

    private static void clearColumn(ServerLevel level, BlockPos pos) {
        level.removeBlock(pos.above(), false);
        level.removeBlock(pos, false);
        level.removeBlock(pos.below(), false);
    }

    private static void forceChunk(ServerLevel level, BlockPos pos, boolean forced) {
        ChunkPos chunk = new ChunkPos(pos);
        level.setChunkForced(chunk.x, chunk.z, forced);
    }
}

package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Regression coverage for the source-backed 1.7.10 surface flower and reed contracts. */
@PrefixGameTestTemplate(false)
public final class LegacySurfacePlantGameTests {
    private static final String[] CONFIGURED_FEATURES = {
            "legacy_flower_foxglove", "legacy_flower_nightshade", "legacy_flower_tobacco",
            "legacy_flower_weed", "legacy_reeds_river", "legacy_reeds_beach"
    };

    private LegacySurfacePlantGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(LegacySurfacePlantGameTests.class);
    }

    /**
     * 1.7.10 {@code HbmWorldGen} delegates these plants to surface-height placement.  This asserts the
     * datapack registration and the blocks' source-specific survival rules in a modern negative-Y world.
     */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "legacySurfacePlants")
    public static void configuredFeaturesAndWaterSurfaceSurvivalKeepLegacyContracts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertTrue(level.getMinBuildHeight() < 0,
                "the GameTest dimension must retain modern negative build height; do not regress surface features to y=0");

        Registry<ConfiguredFeature<?, ?>> configuredFeatures = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE);
        for (String id : CONFIGURED_FEATURES) {
            assertTrue(configuredFeatures.get(new ResourceLocation(HbmNtm.MOD_ID, id)) != null,
                    "legacy surface-plant configured feature must load from the shipped datapack: " + id);
        }

        BlockPos flowerPos = helper.absolutePos(new BlockPos(3, 5, 3));
        level.setBlock(flowerPos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        assertTrue(ModBlocks.PLANT_FLOWER_FOXGLOVE.get().defaultBlockState().canSurvive(level, flowerPos),
                "foxglove must retain the legacy dirt/grass/farmland soil contract");
        level.setBlock(flowerPos, ModBlocks.PLANT_FLOWER_FOXGLOVE.get().defaultBlockState(), Block.UPDATE_ALL);
        assertTrue(level.getBlockState(flowerPos).is(ModBlocks.PLANT_FLOWER_FOXGLOVE.get()),
                "a valid legacy flower surface accepts its generated flower");

        BlockPos reedsPos = helper.absolutePos(new BlockPos(7, 5, 3));
        level.setBlock(reedsPos.below(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        assertTrue(ModBlocks.PLANT_REEDS.get().defaultBlockState().canSurvive(level, reedsPos),
                "reeds must survive directly above both modern still and flowing water states");
        level.setBlock(reedsPos, ModBlocks.PLANT_REEDS.get().defaultBlockState(), Block.UPDATE_ALL);
        assertTrue(level.getBlockState(reedsPos).is(ModBlocks.PLANT_REEDS.get()),
                "a valid water surface accepts legacy reeds");
        level.setBlock(reedsPos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        assertTrue(level.getBlockState(reedsPos).isAir(),
                "legacy reeds must clear without a support block instead of surviving on stone, deepslate, or y=0 assumptions");

        helper.succeed();
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

package com.hbm.ntm.explosion;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;

final class LegacyExplosionBlockChecks {
    static boolean isLegacyVanillaLog(BlockState state) {
        return state.is(Blocks.OAK_LOG)
                || state.is(Blocks.SPRUCE_LOG)
                || state.is(Blocks.BIRCH_LOG)
                || state.is(Blocks.JUNGLE_LOG)
                || state.is(Blocks.ACACIA_LOG)
                || state.is(Blocks.DARK_OAK_LOG);
    }

    static boolean isLegacyVanillaPlanks(BlockState state) {
        return state.is(Blocks.OAK_PLANKS)
                || state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.BIRCH_PLANKS)
                || state.is(Blocks.JUNGLE_PLANKS)
                || state.is(Blocks.ACACIA_PLANKS)
                || state.is(Blocks.DARK_OAK_PLANKS);
    }

    static boolean isLegacyVanillaLeaves(BlockState state) {
        return state.is(Blocks.OAK_LEAVES)
                || state.is(Blocks.SPRUCE_LEAVES)
                || state.is(Blocks.BIRCH_LEAVES)
                || state.is(Blocks.JUNGLE_LEAVES)
                || state.is(Blocks.ACACIA_LEAVES)
                || state.is(Blocks.DARK_OAK_LEAVES);
    }

    static boolean isLegacyDirtBlock(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL);
    }

    static boolean isLegacyGlassBlock(BlockState state) {
        return state.is(Blocks.GLASS) || state.getBlock() instanceof StainedGlassBlock;
    }

    static boolean isLegacyVanillaDoor(BlockState state) {
        return state.is(Blocks.OAK_DOOR) || state.is(Blocks.IRON_DOOR);
    }

    private LegacyExplosionBlockChecks() {
    }
}

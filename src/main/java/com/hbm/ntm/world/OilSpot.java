package com.hbm.ntm.world;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class OilSpot {
    private OilSpot() {
    }

    public static void generateOilSpot(Level level, BlockPos origin, int width, int count) {
        for (int i = 0; i < count; i++) {
            int x = origin.getX() + (int) (level.random.nextGaussian() * width);
            int z = origin.getZ() + (int) (level.random.nextGaussian() * width);
            int surfaceY = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                    new BlockPos(x, 0, z)).getY();

            for (int y = surfaceY; y > surfaceY - 4 && y > level.getMinBuildHeight(); y--) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof TallGrassBlock) {
                    if (level.random.nextInt(10) != 0) {
                        level.removeBlock(pos, false);
                    }
                    continue;
                }
                Block replacement = replacementFor(level, state);
                if (replacement != null && replacement != Blocks.AIR) {
                    level.setBlock(pos, replacement.defaultBlockState(), Block.UPDATE_ALL);
                    break;
                } else if (replacement == Blocks.AIR) {
                    level.removeBlock(pos, false);
                    break;
                }
            }
        }
    }

    private static Block replacementFor(Level level, BlockState state) {
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
            return legacyBlock(level.random.nextInt(10) == 0 ? "dirt_oily" : "dirt_dead");
        }
        Block oilSand = legacyBlock("ore_oil_sand");
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || (oilSand != null && state.is(oilSand))) {
            return isRedSandLike(state) ? legacyBlock("sand_dirty_red") : legacyBlock("sand_dirty");
        }
        if (state.is(Blocks.STONE)) {
            return legacyBlock("stone_cracked");
        }
        if (state.getBlock() instanceof LeavesBlock && !state.getValue(LeavesBlock.PERSISTENT)) {
            return Blocks.AIR;
        }
        if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.TALL_FLOWERS)
                || state.getBlock() instanceof DoublePlantBlock || state.getBlock() instanceof BushBlock) {
            // 1.7.10 replaces these with plant_dead variants; defer until that plant metadata split is migrated.
            return null;
        }
        return null;
    }

    private static boolean isRedSandLike(BlockState state) {
        return state.is(Blocks.RED_SAND);
    }

    private static Block legacyBlock(String legacyName) {
        var block = ModBlocks.legacyBlock(legacyName);
        return block == null || !block.isPresent() ? null : block.get();
    }
}

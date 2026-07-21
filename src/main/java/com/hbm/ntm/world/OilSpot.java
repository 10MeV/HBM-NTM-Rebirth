package com.hbm.ntm.world;

import com.hbm.ntm.block.LegacyDeadPlantBlock;
import com.hbm.ntm.block.LegacyNtmFlowerBlock;
import com.hbm.ntm.block.LegacyTallPlantBlock;
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
import net.minecraftforge.common.IPlantable;

public final class OilSpot {
    private OilSpot() {
    }

    public static void generateOilSpot(Level level, BlockPos origin, int width, int count) {
        generateOilSpot(level, origin, width, count, false);
    }

    /** Source-shaped port of {@code OilSpot.generateOilSpot(..., addWillows)}. */
    public static void generateOilSpot(Level level, BlockPos origin, int width, int count, boolean addWillows) {
        for (int i = 0; i < count; i++) {
            int x = origin.getX() + (int) (level.random.nextGaussian() * width);
            int z = origin.getZ() + (int) (level.random.nextGaussian() * width);
            int surfaceY = WorldUtil.legacyGetHeightValue(level, x, z);

            for (int y = surfaceY; y > surfaceY - 4 && y > level.getMinBuildHeight(); y--) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(pos);
                if (isMustardWillow(state)) {
                    continue;
                }

                if (level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())
                        && !(state.getBlock() instanceof LegacyDeadPlantBlock)) {
                    replacePlant(level, pos, state);
                    state = level.getBlockState(pos);
                }

                if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
                    Block replacement = legacyBlock(level.random.nextInt(10) == 0 ? "dirt_oily" : "dirt_dead");
                    if (replacement != null) {
                        level.setBlock(pos, replacement.defaultBlockState(), Block.UPDATE_ALL);
                        placeWillow(level, pos, addWillows);
                    }
                    break;
                }

                Block replacement = replacementFor(state);
                if (replacement != null) {
                    level.setBlock(pos, replacement.defaultBlockState(), Block.UPDATE_ALL);
                    break;
                }
            }
        }
    }

    private static Block replacementFor(BlockState state) {
        Block oilSand = legacyBlock("ore_oil_sand");
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || (oilSand != null && state.is(oilSand))) {
            return isRedSandLike(state) ? legacyBlock("sand_dirty_red") : legacyBlock("sand_dirty");
        }
        if (state.is(Blocks.STONE)) {
            return legacyBlock("stone_cracked");
        }
        if (state.getBlock() instanceof LeavesBlock
                && !state.getValue(LeavesBlock.PERSISTENT)
                && state.getValue(LeavesBlock.DISTANCE) == 7) {
            return Blocks.AIR;
        }
        return null;
    }

    private static void replacePlant(Level level, BlockPos pos, BlockState state) {
        LegacyDeadPlantBlock.Type type = null;
        if (state.getBlock() instanceof TallGrassBlock) {
            if (level.random.nextInt(10) == 0) {
                type = state.is(Blocks.FERN) ? LegacyDeadPlantBlock.Type.FERN : LegacyDeadPlantBlock.Type.GRASS;
            } else {
                level.removeBlock(pos, false);
            }
        } else if (state.is(BlockTags.FLOWERS)) {
            type = LegacyDeadPlantBlock.Type.FLOWER;
        } else if (state.getBlock() instanceof DoublePlantBlock || state.getBlock() instanceof LegacyTallPlantBlock) {
            type = LegacyDeadPlantBlock.Type.BIGFLOWER;
        } else if (state.getBlock() instanceof BushBlock || state.getBlock() instanceof IPlantable) {
            type = LegacyDeadPlantBlock.Type.GENERIC;
        }
        if (type != null) {
            LegacyDeadPlantBlock deadPlant = (LegacyDeadPlantBlock) ModBlocks.PLANT_DEAD_BIGFLOWER.get();
            level.setBlock(pos, deadPlant.stateFor(type), Block.UPDATE_ALL);
        }
    }

    private static boolean isMustardWillow(BlockState state) {
        if (state.getBlock() instanceof LegacyNtmFlowerBlock flower) {
            return flower.kind() == LegacyNtmFlowerBlock.Kind.CD0 || flower.kind() == LegacyNtmFlowerBlock.Kind.CD1;
        }
        if (state.getBlock() instanceof LegacyTallPlantBlock plant) {
            return plant.kind() == LegacyTallPlantBlock.Kind.CD2
                    || plant.kind() == LegacyTallPlantBlock.Kind.CD3
                    || plant.kind() == LegacyTallPlantBlock.Kind.CD4;
        }
        return false;
    }

    private static void placeWillow(Level level, BlockPos groundPos, boolean addWillows) {
        if (!addWillows || level.random.nextInt(50) != 0) {
            return;
        }
        BlockPos willowPos = groundPos.above();
        BlockState willow = ModBlocks.PLANT_FLOWER_CD0.get().defaultBlockState();
        if (level.isEmptyBlock(willowPos) && willow.canSurvive(level, willowPos)) {
            level.setBlock(willowPos, willow, Block.UPDATE_ALL);
        }
    }

    private static boolean isRedSandLike(BlockState state) {
        return state.is(Blocks.RED_SAND);
    }

    private static Block legacyBlock(String legacyName) {
        var block = ModBlocks.legacyBlock(legacyName);
        return block == null || !block.isPresent() ? null : block.get();
    }
}

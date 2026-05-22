package com.hbm.ntm.explosion;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public final class NukeEnvironmentalEffect {
    public static void applyStandardAOE(Level level, int x, int y, int z, int radius, int jaggedness) {
        if (level == null || level.isClientSide() || radius <= 0) {
            return;
        }

        int radiusSquared = radius * radius;
        int halfRadiusSquared = radiusSquared / 2;
        int randomBound = Math.max(1, jaggedness);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            int worldX = xx + x;
            int xx2 = xx * xx;
            for (int yy = -radius; yy < radius; yy++) {
                int worldY = yy + y;
                int yy2 = xx2 + yy * yy;
                for (int zz = -radius; zz < radius; zz++) {
                    int distanceSquared = yy2 + zz * zz;
                    if (distanceSquared < halfRadiusSquared + level.random.nextInt(randomBound)) {
                        applyStandardEffect(level, cursor.set(worldX, worldY, zz + z));
                    }
                }
            }
        }
    }

    public static void applyStandardEffect(Level level, int x, int y, int z) {
        applyStandardEffect(level, new BlockPos(x, y, z));
    }

    public static void applyStandardEffect(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState input = level.getBlockState(pos);
        if (input.isAir()) {
            return;
        }

        BlockState replacement = null;
        int chance = 100;

        if (input.is(Blocks.SAND) || input.is(Blocks.RED_SAND)) {
            replacement = legacyState("block_trinitite");
            chance = 20;
        } else if (input.is(Blocks.MYCELIUM)) {
            replacement = ModBlocks.WASTE_MYCELIUM.get().defaultBlockState();
        } else if (input.is(BlockTags.LOGS)) {
            replacement = legacyState("waste_log");
        } else if (input.is(BlockTags.PLANKS)) {
            replacement = legacyState("waste_planks");
        } else if (input.is(Blocks.MOSSY_COBBLESTONE)) {
            replacement = legacyState("ore_oil");
            chance = 50;
        } else if (input.is(Blocks.COAL_ORE) || input.is(Blocks.DEEPSLATE_COAL_ORE)) {
            replacement = Blocks.DIAMOND_ORE.defaultBlockState();
            chance = 10;
        } else if (isLegacy(input, "ore_uranium") || isLegacy(input, "ore_gneiss_uranium")) {
            replacement = legacyState("ore_schrabidium");
            chance = 10;
        } else if (isLegacy(input, "ore_nether_uranium")) {
            replacement = legacyState("ore_nether_schrabidium");
            chance = 10;
        } else if (isLegacy(input, "ore_nether_plutonium")) {
            replacement = legacyState("ore_nether_schrabidium");
            chance = 25;
        } else if (input.is(Blocks.BROWN_MUSHROOM_BLOCK) || input.is(Blocks.RED_MUSHROOM_BLOCK)) {
            replacement = legacyState("waste_planks");
        } else if (input.is(Blocks.END_STONE)) {
            replacement = legacyState("ore_tikite");
            chance = 1;
        } else if (input.is(Blocks.CLAY)) {
            replacement = Blocks.TERRACOTTA.defaultBlockState();
        } else if (input.isFlammable(level, pos, Direction.UP)) {
            replacement = BaseFireBlock.getState(level, pos);
        }

        if (replacement != null && level.random.nextInt(1000) < chance) {
            level.setBlock(pos, replacement, 2);
        }
    }

    private static boolean isLegacy(BlockState state, String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block != null && state.is(block.get());
    }

    private static BlockState legacyState(String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block == null ? null : block.get().defaultBlockState();
    }

    private NukeEnvironmentalEffect() {
    }
}

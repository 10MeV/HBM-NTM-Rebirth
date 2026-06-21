package com.hbm.ntm.explosion;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public final class NukeEnvironmentalEffect {
    private static final RegistryObject<? extends Block> WASTE_TRINITITE = requireLegacyBlock("waste_trinitite");
    private static final RegistryObject<? extends Block> WASTE_TRINITITE_RED = requireLegacyBlock("waste_trinitite_red");
    private static final RegistryObject<? extends Block> ORE_OIL = requireLegacyBlock("ore_oil");
    private static final RegistryObject<? extends Block> ORE_URANIUM = requireLegacyBlock("ore_uranium");
    private static final RegistryObject<? extends Block> ORE_SCHRABIDIUM = requireLegacyBlock("ore_schrabidium");
    private static final RegistryObject<? extends Block> ORE_NETHER_URANIUM = requireLegacyBlock("ore_nether_uranium");
    private static final RegistryObject<? extends Block> ORE_NETHER_PLUTONIUM = requireLegacyBlock("ore_nether_plutonium");
    private static final RegistryObject<? extends Block> ORE_NETHER_SCHRABIDIUM = requireLegacyBlock("ore_nether_schrabidium");
    private static final RegistryObject<? extends Block> ORE_TIKITE = requireLegacyBlock("ore_tikite");

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
                if (level.isOutsideBuildHeight(worldY)) {
                    continue;
                }
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
        if (level == null || level.isClientSide() || level.isOutsideBuildHeight(pos)) {
            return;
        }

        BlockState input = level.getBlockState(pos);
        if (input.isAir()) {
            return;
        }

        BlockState replacement = null;
        int chance = 100;

        if (input.is(Blocks.SAND) || input.is(Blocks.RED_SAND)) {
            replacement = legacyState(input.is(Blocks.RED_SAND) ? WASTE_TRINITITE_RED : WASTE_TRINITITE);
            chance = 20;
        } else if (input.is(Blocks.MYCELIUM)) {
            replacement = ModBlocks.WASTE_MYCELIUM.get().defaultBlockState();
        } else if (LegacyExplosionBlockChecks.isLegacyVanillaLog(input)) {
            replacement = ModBlocks.WASTE_LOG.get().defaultBlockState();
        } else if (LegacyExplosionBlockChecks.isLegacyVanillaPlanks(input)) {
            replacement = ModBlocks.WASTE_PLANKS.get().defaultBlockState();
        } else if (input.is(Blocks.MOSSY_COBBLESTONE)) {
            replacement = legacyState(ORE_OIL);
            chance = 50;
        } else if (input.is(Blocks.COAL_ORE)) {
            replacement = Blocks.DIAMOND_ORE.defaultBlockState();
            chance = 10;
        } else if (input.is(ORE_URANIUM.get())) {
            replacement = legacyState(ORE_SCHRABIDIUM);
            chance = 10;
        } else if (input.is(ORE_NETHER_URANIUM.get())) {
            replacement = legacyState(ORE_NETHER_SCHRABIDIUM);
            chance = 10;
        } else if (input.is(ORE_NETHER_PLUTONIUM.get())) {
            replacement = legacyState(ORE_NETHER_SCHRABIDIUM);
            chance = 25;
        } else if (input.is(Blocks.MUSHROOM_STEM)) {
            replacement = ModBlocks.WASTE_PLANKS.get().defaultBlockState();
        } else if (input.is(Blocks.END_STONE)) {
            replacement = legacyState(ORE_TIKITE);
            chance = 1;
        } else if (input.is(Blocks.CLAY)) {
            replacement = Blocks.TERRACOTTA.defaultBlockState();
        } else if (input.isFlammable(level, pos, Direction.UP)) {
            replacement = Blocks.FIRE.defaultBlockState();
        }

        if (replacement != null && level.random.nextInt(1000) < chance) {
            level.setBlock(pos, replacement, 2);
        }
    }

    private static RegistryObject<? extends Block> requireLegacyBlock(String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return Objects.requireNonNull(block, "Missing legacy block hbm_ntm_rebirth:" + name);
    }

    private static BlockState legacyState(RegistryObject<? extends Block> block) {
        return block.get().defaultBlockState();
    }

    private NukeEnvironmentalEffect() {
    }
}

package com.hbm.ntm.world.feature;

import com.hbm.ntm.block.LegacyGlyphidSpawnerBlock;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

/** Exact small Glyphid hive schematic used by the Scout construction task. */
public final class GlyphidHive {
    private static final int[][][] SCHEMATIC_SMALL = {
            {{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0}},
            {{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,1,1,1,1,1,0,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,0,1,1,1,1,1,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0}},
            {{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,1,1,1,3,3,3,1,1,1,0},{0,1,1,1,3,3,3,1,1,1,0},{0,1,1,1,3,3,3,1,1,1,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0}},
            {{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,1,1,2,2,2,1,1,0,0},{0,1,1,2,2,2,2,2,1,1,0},{0,1,1,2,2,2,2,2,1,1,0},{0,1,1,2,2,2,2,2,1,1,0},{0,0,1,1,2,2,2,1,1,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0}},
            {{0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,1,1,1,1,1,1,1,1,1,0},{0,1,1,1,1,1,1,1,1,1,0},{0,1,1,1,1,1,1,1,1,1,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,1,1,1,1,1,1,1,0,0},{0,0,0,0,1,1,1,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0}}
    };

    private GlyphidHive() {
    }

    /**
     * Direct carrier for legacy {@code GlyphidHive.generateSmall}.  The `loot` decoration cells deliberately remain
     * unmaterialized because `deco_loot`/reward-crate content is a project-wide hard exclusion; Scout calls this with
     * {@code loot=false}, so only the excluded bone-cache branch is omitted from its runtime construction path.
     */
    public static void generateSmall(LevelAccessor level, BlockPos origin, RandomSource random, boolean infected, boolean loot) {
        RegistryObject<? extends Block> glyphidBase = requireBlock("glyphid_base");
        RegistryObject<? extends Block> glyphidSpawner = requireBlock("glyphid_spawner");
        int variant = infected ? 1 : 0;
        BlockState baseState = LegacyGlyphidSpawnerBlock.withLegacyVariant(glyphidBase.get().defaultBlockState(), variant);
        BlockState spawnerState = LegacyGlyphidSpawnerBlock.withLegacyVariant(glyphidSpawner.get().defaultBlockState(), variant);
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 11; z++) {
                    int cell = SCHEMATIC_SMALL[4 - y][x][z];
                    BlockPos pos = origin.offset(x - 5, y - 2, z - 5);
                    if (cell == 1) {
                        level.setBlock(pos, baseState, Block.UPDATE_CLIENTS);
                    } else if (cell == 2) {
                        level.setBlock(pos, random.nextInt(3) == 0 ? spawnerState : baseState, Block.UPDATE_CLIENTS);
                    } else if (cell == 3) {
                        int decoration = random.nextInt(3);
                        if (decoration == 0) {
                            level.setBlock(pos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState()
                                    .setValue(SkullBlock.ROTATION, random.nextInt(16)), Block.UPDATE_ALL);
                        } else if (decoration == 2 && !loot) {
                            level.setBlock(pos, baseState, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
    }

    private static RegistryObject<? extends Block> requireBlock(String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        if (block == null) {
            throw new IllegalStateException("Missing source-backed Glyphid hive block: " + name);
        }
        return block;
    }
}

package com.hbm.ntm.radiation;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LegacyRadiationWorldUtil {
    public static int legacyHeightValue(Level level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, level.getMaxBuildHeight() - 1, z);
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            if (isLegacyHeightBlocking(level, cursor, state)) {
                return y + 1;
            }
        }
        return level.getMinBuildHeight();
    }

    public static boolean isLegacyHeightBlocking(Level level, BlockPos pos, BlockState state) {
        if (state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(ModBlocks.FALLOUT.get())
                || state.is(ModBlocks.LEAVES_LAYER.get())
                || state.is(ModBlocks.WASTE_LEAVES.get())) {
            return false;
        }

        if (state.is(BlockTags.LEAVES)) {
            return true;
        }

        if (state.is(Blocks.FARMLAND)) {
            return true;
        }

        if (isModernRadiationTransparentWoodOrFence(state)) {
            return false;
        }

        return state.isSolidRender(level, pos);
    }

    private static boolean isModernRadiationTransparentWoodOrFence(BlockState state) {
        if (state.is(BlockTags.LOGS)
                || state.is(BlockTags.LOGS_THAT_BURN)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.FENCES)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockTags.WOODEN_BUTTONS)
                || state.is(BlockTags.WOODEN_PRESSURE_PLATES)
                || state.is(BlockTags.ALL_SIGNS)
                || state.is(BlockTags.ALL_HANGING_SIGNS)
                || state.is(ModBlocks.WASTE_LOG.get())
                || state.is(ModBlocks.WASTE_PLANKS.get())) {
            return true;
        }

        ResourceLocation id = HbmRegistryUtil.blockKey(state.getBlock());
        String path = id.getPath();
        return hasPathToken(path, "wood")
                || hasPathToken(path, "log")
                || hasPathToken(path, "logs")
                || hasPathToken(path, "plank")
                || hasPathToken(path, "planks")
                || hasPathToken(path, "fence")
                || hasPathToken(path, "fences");
    }

    private static boolean hasPathToken(String path, String token) {
        String[] parts = path.split("_");
        for (String part : parts) {
            if (part.equals(token)) {
                return true;
            }
        }
        return false;
    }

    private LegacyRadiationWorldUtil() {
    }
}

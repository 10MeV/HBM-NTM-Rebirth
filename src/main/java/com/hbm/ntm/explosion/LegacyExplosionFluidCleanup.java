package com.hbm.ntm.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public final class LegacyExplosionFluidCleanup {
    public static boolean isLegacyLiquidBlock(BlockState state) {
        return state != null && state.getBlock() instanceof LiquidBlock;
    }

    public static void clearLegacyLiquidNeighborhood(Level level, BlockPos center, int flags) {
        if (level == null || center == null) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (!level.isOutsideBuildHeight(cursor) && isLegacyLiquidBlock(level.getBlockState(cursor))) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), flags);
                    }
                }
            }
        }
    }

    public static void addLegacyLiquidNeighborhood(Level level, BlockPos center, Set<BlockPos> positions) {
        if (level == null || center == null || positions == null) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (level.isInWorldBounds(cursor) && isLegacyLiquidBlock(level.getBlockState(cursor))) {
                        positions.add(cursor.immutable());
                    }
                }
            }
        }
    }

    public static void clearBlockOrLegacyLiquidNeighborhood(Level level, BlockPos pos, int flags) {
        if (level == null || pos == null || level.isOutsideBuildHeight(pos)) {
            return;
        }
        if (isLegacyLiquidBlock(level.getBlockState(pos))) {
            clearLegacyLiquidNeighborhood(level, pos, flags);
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), flags);
    }

    private LegacyExplosionFluidCleanup() {
    }
}

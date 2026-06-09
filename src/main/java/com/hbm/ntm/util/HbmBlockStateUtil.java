package com.hbm.ntm.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class HbmBlockStateUtil {
    private HbmBlockStateUtil() {
    }

    public static float explosionResistance(BlockState state) {
        return state.getExplosionResistance(null, null, null);
    }

    public static float explosionResistance(BlockState state, BlockGetter level, BlockPos pos) {
        return explosionResistance(state, level, pos, null);
    }

    public static float explosionResistance(BlockState state, BlockGetter level, BlockPos pos,
            @Nullable Explosion explosion) {
        return state.getExplosionResistance(level, pos, explosion);
    }
}

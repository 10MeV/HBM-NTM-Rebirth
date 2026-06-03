package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface LegacyLookOverlayBlockProvider {
    @Nullable
    LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState);
}

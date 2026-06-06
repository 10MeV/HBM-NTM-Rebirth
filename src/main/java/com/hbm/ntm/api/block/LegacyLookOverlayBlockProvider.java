package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface LegacyLookOverlayBlockProvider {
    @Nullable
    LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState);

    @Nullable
    default LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos, BlockState viewedState) {
        return getLookOverlay(level, viewedPos, viewedState);
    }
}

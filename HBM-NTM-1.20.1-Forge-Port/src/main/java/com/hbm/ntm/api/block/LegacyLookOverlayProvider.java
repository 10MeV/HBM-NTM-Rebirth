package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface LegacyLookOverlayProvider {
    @Nullable
    LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos);

    @Nullable
    default LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos) {
        return getLookOverlay(level, viewedPos);
    }
}

package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface LegacyLookOverlayProvider {
    @Nullable
    LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos);
}

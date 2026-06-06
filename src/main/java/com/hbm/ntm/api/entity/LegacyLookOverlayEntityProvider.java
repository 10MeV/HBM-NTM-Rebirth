package com.hbm.ntm.api.entity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public interface LegacyLookOverlayEntityProvider {
    @Nullable
    LegacyLookOverlay getLookOverlay(Level level, Player player, EntityHitResult hit);
}

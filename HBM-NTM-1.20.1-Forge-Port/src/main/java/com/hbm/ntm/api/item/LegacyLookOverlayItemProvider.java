package com.hbm.ntm.api.item;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public interface LegacyLookOverlayItemProvider {
    @Nullable
    LegacyLookOverlay getLookOverlay(Level level, Player player, ItemStack stack, BlockHitResult hit);
}

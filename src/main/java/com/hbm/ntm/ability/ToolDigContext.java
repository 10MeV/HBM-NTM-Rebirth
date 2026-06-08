package com.hbm.ntm.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public record ToolDigContext(Level level, BlockPos pos, Player player, ItemStack toolStack,
                             ExtraBlockBreaker extraBlockBreaker, @Nullable BlockHitResult hitResult) {
    public void breakExtraBlock(BlockPos extraPos) {
        extraBlockBreaker.breakExtraBlock(level, extraPos, player, pos, toolStack);
    }

    @FunctionalInterface
    public interface ExtraBlockBreaker {
        void breakExtraBlock(Level level, BlockPos pos, Player player, BlockPos origin, ItemStack toolStack);
    }
}

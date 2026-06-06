package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface LegacyMultiblockPlaceable {
    @Nullable
    BlockState getDirectPlacementState(BlockPlaceContext context);

    BlockPos getDirectPlacementCore(BlockPlaceContext context, BlockState state);

    boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos);

    default boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        return canPlaceDirectMultiblock(level, corePos, temporaryPos);
    }

    default void afterDirectCorePlaced(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
    }

    void completeDirectMultiblockPlacement(Level level, BlockPos corePos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack);
}

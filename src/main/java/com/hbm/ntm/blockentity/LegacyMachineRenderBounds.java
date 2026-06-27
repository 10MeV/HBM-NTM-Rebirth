package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class LegacyMachineRenderBounds {
    public static final AABB INFINITE_EXTENT_AABB = new AABB(
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    private LegacyMachineRenderBounds() {
    }

    public static AABB visibleMultiblockOr(BlockEntity blockEntity, AABB fallback) {
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            return block.definition().renderBoundingBox(state, blockEntity.getBlockPos());
        }
        return fallback;
    }
}

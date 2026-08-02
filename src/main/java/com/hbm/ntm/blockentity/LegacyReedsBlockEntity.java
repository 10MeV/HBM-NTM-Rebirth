package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Renderer-only carrier for the legacy multi-layer water reeds visual. */
public final class LegacyReedsBlockEntity extends BlockEntity {
    public LegacyReedsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_REEDS.get(), pos, state);
    }
}

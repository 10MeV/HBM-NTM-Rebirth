package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Renderer-only carrier for the legacy two-pass phosphor-vine visual. */
public final class PhosphorVineBlockEntity extends BlockEntity {
    public PhosphorVineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PHOSPHOR_VINE.get(), pos, state);
    }
}

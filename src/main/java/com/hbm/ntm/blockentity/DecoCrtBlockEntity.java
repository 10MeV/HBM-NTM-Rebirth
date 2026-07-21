package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Renderer-only state carrier; all legacy CRT state is encoded in the block state. */
public final class DecoCrtBlockEntity extends BlockEntity {
    public DecoCrtBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECO_CRT.get(), pos, state);
    }
}

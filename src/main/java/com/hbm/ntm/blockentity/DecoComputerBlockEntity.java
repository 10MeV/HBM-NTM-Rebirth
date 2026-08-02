package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Renderer-only carrier; the legacy computer has no persistent tile state. */
public final class DecoComputerBlockEntity extends BlockEntity {
    public DecoComputerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.DECO_COMPUTER.get(), pos, state); }
}

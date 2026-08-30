package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Deliberately state-free: the legacy generator's runtime implementation was commented out. */
public class IndustrialGeneratorBlockEntity extends BlockEntity {
    public IndustrialGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIAL_GENERATOR.get(), pos, state);
    }
}

package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FluidDuctBoxBlockEntity extends FluidPipeBlockEntity {
    public FluidDuctBoxBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FLUID_DUCT_BOX.get(), pos, state);
    }

    protected FluidDuctBoxBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }
}

package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RedCableBlockEntity extends HbmEnergyNodeBlockEntity {
    public RedCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CABLE.get(), pos, state);
    }
}

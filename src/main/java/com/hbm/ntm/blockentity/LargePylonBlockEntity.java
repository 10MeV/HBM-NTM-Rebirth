package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LargePylonBlockEntity extends HbmLegacyLargePylonBlockEntity {
    public LargePylonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_PYLON_LARGE.get(), pos, state);
    }
}

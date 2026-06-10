package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SubstationBlockEntity extends HbmLegacySubstationBlockEntity {
    public SubstationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUBSTATION.get(), pos, state);
    }
}

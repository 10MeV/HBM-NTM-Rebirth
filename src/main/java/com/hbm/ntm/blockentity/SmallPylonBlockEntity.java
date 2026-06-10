package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SmallPylonBlockEntity extends HbmLegacyPylonBlockEntity {
    public SmallPylonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_PYLON.get(), pos, state);
    }
}

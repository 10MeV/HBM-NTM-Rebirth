package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyMediumPylonBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MediumPylonBlockEntity extends HbmLegacyMediumPylonBlockEntity {
    public MediumPylonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_PYLON_MEDIUM.get(), pos, state);
    }

    @Override
    protected boolean hasTransformerPort() {
        return getBlockState().getBlock() instanceof LegacyMediumPylonBlock pylon && pylon.kind().transformer();
    }
}

package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Empty modern identity carrier for legacy TileEntityDecoBlock boxcar rendering. */
public class BoxcarBlockEntity extends BlockEntity {
    public BoxcarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOXCAR.get(), pos, state);
    }
}

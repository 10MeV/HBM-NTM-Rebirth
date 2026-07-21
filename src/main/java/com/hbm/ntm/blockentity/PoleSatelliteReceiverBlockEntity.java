package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renderer carrier for the legacy {@code TileEntityDecoPoleSatelliteReceiver}.
 * The original tile owns no gameplay or persisted state.
 */
public final class PoleSatelliteReceiverBlockEntity extends BlockEntity {
    public PoleSatelliteReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLE_SATELLITE_RECEIVER.get(), pos, state);
    }
}

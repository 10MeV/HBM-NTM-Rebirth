package com.hbm.blocks.generic;

import com.hbm.ntm.block.RadiatingFallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for falling hazard blocks.
 */
@Deprecated(forRemoval = false)
public class BlockHazardFalling extends RadiatingFallingBlock {
    private boolean beaconable;

    public BlockHazardFalling(BlockBehaviour.Properties properties, float chunkRadiationPerTick) {
        super(properties, chunkRadiationPerTick);
    }

    public BlockHazardFalling makeBeaconable() {
        this.beaconable = true;
        return this;
    }
}

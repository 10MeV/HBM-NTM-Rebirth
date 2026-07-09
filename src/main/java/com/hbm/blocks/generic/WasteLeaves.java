package com.hbm.blocks.generic;

import com.hbm.ntm.block.LegacyWasteLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for dead leaves.
 */
@Deprecated(forRemoval = false)
public class WasteLeaves extends LegacyWasteLeavesBlock {
    public WasteLeaves(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

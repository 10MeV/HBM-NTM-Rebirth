package com.hbm.blocks.generic;

import com.hbm.ntm.block.LegacyWasteLogBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for charred and frozen logs.
 */
@Deprecated(forRemoval = false)
public class WasteLog extends LegacyWasteLogBlock {
    public WasteLog(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

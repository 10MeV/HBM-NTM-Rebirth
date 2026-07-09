package com.hbm.blocks.generic;

import com.hbm.ntm.block.RadioactiveWasteEarthBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for registered waste earth/mycelium blocks.
 */
@Deprecated(forRemoval = false)
public class WasteEarth extends RadioactiveWasteEarthBlock {
    public WasteEarth(BlockBehaviour.Properties properties, boolean mycelium) {
        super(properties, mycelium);
    }
}

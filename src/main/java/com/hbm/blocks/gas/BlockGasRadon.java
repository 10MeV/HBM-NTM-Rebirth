package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyGasRadonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for radon gas.
 */
@Deprecated(forRemoval = false)
public class BlockGasRadon extends LegacyGasRadonBlock {
    public BlockGasRadon() {
        this(defaultGasProperties());
    }

    public BlockGasRadon(BlockBehaviour.Properties properties) {
        super(properties, Kind.NORMAL);
    }
}

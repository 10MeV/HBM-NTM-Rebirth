package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyGasRadonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for tomb radon gas.
 */
@Deprecated(forRemoval = false)
public class BlockGasRadonTomb extends LegacyGasRadonBlock {
    public BlockGasRadonTomb() {
        this(defaultGasProperties());
    }

    public BlockGasRadonTomb(BlockBehaviour.Properties properties) {
        super(properties, Kind.TOMB);
    }
}

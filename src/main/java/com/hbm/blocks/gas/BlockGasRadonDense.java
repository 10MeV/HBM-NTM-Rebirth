package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyGasRadonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for dense radon gas.
 */
@Deprecated(forRemoval = false)
public class BlockGasRadonDense extends LegacyGasRadonBlock {
    public BlockGasRadonDense() {
        this(defaultGasProperties());
    }

    public BlockGasRadonDense(BlockBehaviour.Properties properties) {
        super(properties, Kind.DENSE);
    }
}

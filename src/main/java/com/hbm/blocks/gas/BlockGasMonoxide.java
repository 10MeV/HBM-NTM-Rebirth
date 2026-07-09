package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyToxicGasBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for carbon monoxide gas.
 */
@Deprecated(forRemoval = false)
public class BlockGasMonoxide extends LegacyToxicGasBlock {
    public BlockGasMonoxide() {
        this(defaultGasProperties());
    }

    public BlockGasMonoxide(BlockBehaviour.Properties properties) {
        super(properties, Kind.MONOXIDE);
    }
}

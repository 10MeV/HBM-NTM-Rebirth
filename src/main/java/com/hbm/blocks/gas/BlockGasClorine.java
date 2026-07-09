package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyToxicGasBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for chlorine gas. The legacy class name uses
 * the original "Clorine" spelling.
 */
@Deprecated(forRemoval = false)
public class BlockGasClorine extends LegacyToxicGasBlock {
    public BlockGasClorine() {
        this(defaultGasProperties());
    }

    public BlockGasClorine(BlockBehaviour.Properties properties) {
        super(properties, Kind.CHLORINE);
    }
}

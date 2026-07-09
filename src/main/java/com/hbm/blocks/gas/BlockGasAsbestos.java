package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyToxicGasBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for asbestos gas.
 */
@Deprecated(forRemoval = false)
public class BlockGasAsbestos extends LegacyToxicGasBlock {
    public BlockGasAsbestos() {
        this(defaultGasProperties());
    }

    public BlockGasAsbestos(BlockBehaviour.Properties properties) {
        super(properties, Kind.ASBESTOS);
    }
}

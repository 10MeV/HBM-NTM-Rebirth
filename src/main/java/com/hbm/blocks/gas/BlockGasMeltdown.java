package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyGasMeltdownBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for meltdown gas.
 */
@Deprecated(forRemoval = false)
public class BlockGasMeltdown extends LegacyGasMeltdownBlock {
    public BlockGasMeltdown() {
        this(defaultGasProperties());
    }

    public BlockGasMeltdown(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

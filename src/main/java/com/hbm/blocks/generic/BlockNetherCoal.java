package com.hbm.blocks.generic;

import com.hbm.ntm.block.LegacyNetherCoalOreBlock;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for nether coal ore.
 */
@Deprecated(forRemoval = false)
public class BlockNetherCoal extends LegacyNetherCoalOreBlock {
    public BlockNetherCoal(String legacyName, BlockBehaviour.Properties properties, Supplier<? extends Block> gas,
            boolean randomTick, int rate, boolean onBreak) {
        this(legacyName, properties, gas, randomTick, rate, onBreak, false);
    }

    public BlockNetherCoal(String legacyName, BlockBehaviour.Properties properties, Supplier<? extends Block> gas,
            boolean randomTick, int rate, boolean onBreak, boolean onNeighbour) {
        super(legacyName, properties, gas, randomTick, rate, onBreak, onNeighbour);
    }
}

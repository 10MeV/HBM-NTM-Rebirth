package com.hbm.blocks.generic;

import com.hbm.ntm.block.LegacyOutgasBlock;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for outgassing ore/resource blocks.
 */
@Deprecated(forRemoval = false)
public class BlockOutgas extends LegacyOutgasBlock {
    final boolean randomTick;
    final int rate;
    final boolean onBreak;
    final boolean onNeighbour;

    public BlockOutgas(String legacyName, BlockBehaviour.Properties properties, Supplier<? extends Block> gas,
            boolean randomTick, int rate, boolean onBreak) {
        this(legacyName, properties, gas, randomTick, rate, onBreak, false);
    }

    public BlockOutgas(String legacyName, BlockBehaviour.Properties properties, Supplier<? extends Block> gas,
            boolean randomTick, int rate, boolean onBreak, boolean onNeighbour) {
        this(legacyName, properties, gas, randomTick, rate, onBreak, onNeighbour, false);
    }

    public BlockOutgas(String legacyName, BlockBehaviour.Properties properties, Supplier<? extends Block> gas,
            boolean randomTick, int rate, boolean onBreak, boolean onNeighbour, boolean walkingRelease) {
        this(legacyName, properties, gas, randomTick, rate, onBreak, onNeighbour, walkingRelease, false);
    }

    public BlockOutgas(String legacyName, BlockBehaviour.Properties properties, Supplier<? extends Block> gas,
            boolean randomTick, int rate, boolean onBreak, boolean onNeighbour, boolean walkingRelease,
            boolean volumeReleaseOnBreak) {
        super(legacyName, properties, gas, onBreak, onNeighbour, walkingRelease, volumeReleaseOnBreak);
        this.randomTick = randomTick;
        this.rate = rate;
        this.onBreak = onBreak;
        this.onNeighbour = onNeighbour;
    }
}

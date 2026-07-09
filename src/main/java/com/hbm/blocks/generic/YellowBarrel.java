package com.hbm.blocks.generic;

import api.hbm.block.IFuckingExplode;
import com.hbm.ntm.block.LegacyRadiationBarrelBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for radioactive and vitrified barrels.
 */
@Deprecated(forRemoval = false)
public class YellowBarrel extends LegacyRadiationBarrelBlock implements IFuckingExplode {
    public YellowBarrel(BlockBehaviour.Properties properties, float chunkRadiationPerTick) {
        super(properties, chunkRadiationPerTick);
    }
}

package com.hbm.blocks.gas;

import com.hbm.ntm.block.LegacyGasBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Legacy 1.7.10 package bridge for gas block class hierarchy checks.
 */
@Deprecated(forRemoval = false)
public abstract class BlockGasBase extends LegacyGasBlock {
    final float red;
    final float green;
    final float blue;

    public BlockGasBase(float red, float green, float blue) {
        this(defaultGasProperties(), red, green, blue);
    }

    public BlockGasBase(BlockBehaviour.Properties properties, float red, float green, float blue) {
        super(properties, red, green, blue);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    protected static BlockBehaviour.Properties defaultGasProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F, 0.0F)
                .noCollission()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }
}

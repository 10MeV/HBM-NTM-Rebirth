package com.hbm.blocks.generic;

import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Legacy 1.7.10 package bridge for the radiation absorber block.
 */
@Deprecated(forRemoval = false)
public class BlockAbsorber extends LegacyRadAbsorberBlock {
    public enum EnumAbsorberTier {
        BASE(2.5F, "absorber"),
        RED(10.0F, "absorber_red"),
        GREEN(100.0F, "absorber_green"),
        PINK(10000.0F, "absorber_pink");

        public final float absorbAmount;
        public final String textureName;

        EnumAbsorberTier(float absorbAmount, String textureName) {
            this.absorbAmount = absorbAmount;
            this.textureName = textureName;
        }
    }

    public BlockAbsorber(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public EnumAbsorberTier getTier(int meta) {
        EnumAbsorberTier[] values = EnumAbsorberTier.values();
        return values[Math.abs(meta % values.length)];
    }

    public int getSubCount() {
        return EnumAbsorberTier.values().length;
    }
}

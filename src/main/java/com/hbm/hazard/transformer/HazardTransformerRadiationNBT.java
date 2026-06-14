package com.hbm.hazard.transformer;

import com.hbm.hazard.HazardEntry;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Legacy package facade for the NBT radiation hazard transformer.
 */
@Deprecated(forRemoval = false)
public class HazardTransformerRadiationNBT extends HazardTransformerBase {
    public static final String RAD_KEY = com.hbm.ntm.radiation.NbtRadiationHazardTransformer.RAD_KEY;

    private final com.hbm.ntm.radiation.NbtRadiationHazardTransformer delegate =
            new com.hbm.ntm.radiation.NbtRadiationHazardTransformer();

    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        List<com.hbm.ntm.radiation.HazardEntry> modernEntries = toModernEntries(entries);
        delegate.transformPost(stack, modernEntries);
        replaceLegacy(entries, modernEntries);
    }

    @Override
    public com.hbm.ntm.radiation.HazardTransformer toModern() {
        return delegate;
    }
}

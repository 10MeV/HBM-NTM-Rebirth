package com.hbm.hazard.transformer;

import com.hbm.hazard.HazardEntry;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Legacy package facade for the Applied Energistics storage radiation hazard transformer.
 */
@Deprecated(forRemoval = false)
public class HazardTransformerRadiationME extends HazardTransformerBase {
    private final com.hbm.ntm.radiation.MeRadiationHazardTransformer delegate =
            new com.hbm.ntm.radiation.MeRadiationHazardTransformer();

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

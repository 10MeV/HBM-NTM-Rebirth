package com.hbm.hazard.transformer;

import com.hbm.hazard.HazardEntry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy package facade for 1.7.10 hazard transformers.
 */
@Deprecated(forRemoval = false)
public abstract class HazardTransformerBase {
    public void transformPre(ItemStack stack, List<HazardEntry> entries) {
    }

    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
    }

    public com.hbm.ntm.radiation.HazardTransformer toModern() {
        return new com.hbm.ntm.radiation.HazardTransformer() {
            @Override
            public void transformPre(ItemStack stack, List<com.hbm.ntm.radiation.HazardEntry> entries) {
                List<HazardEntry> legacyEntries = toLegacy(entries);
                HazardTransformerBase.this.transformPre(stack, legacyEntries);
                replaceModern(entries, legacyEntries);
            }

            @Override
            public void transformPost(ItemStack stack, List<com.hbm.ntm.radiation.HazardEntry> entries) {
                List<HazardEntry> legacyEntries = toLegacy(entries);
                HazardTransformerBase.this.transformPost(stack, legacyEntries);
                replaceModern(entries, legacyEntries);
            }
        };
    }

    protected static List<HazardEntry> toLegacy(List<com.hbm.ntm.radiation.HazardEntry> entries) {
        List<HazardEntry> legacyEntries = new ArrayList<>();
        for (com.hbm.ntm.radiation.HazardEntry entry : entries) {
            legacyEntries.add(HazardEntry.fromModern(entry));
        }
        return legacyEntries;
    }

    protected static List<com.hbm.ntm.radiation.HazardEntry> toModernEntries(List<HazardEntry> entries) {
        List<com.hbm.ntm.radiation.HazardEntry> modernEntries = new ArrayList<>();
        for (HazardEntry entry : entries) {
            modernEntries.add(entry.toModern());
        }
        return modernEntries;
    }

    protected static void replaceModern(List<com.hbm.ntm.radiation.HazardEntry> entries, List<HazardEntry> legacyEntries) {
        entries.clear();
        for (HazardEntry entry : legacyEntries) {
            entries.add(entry.toModern());
        }
    }

    protected static void replaceLegacy(List<HazardEntry> entries, List<com.hbm.ntm.radiation.HazardEntry> modernEntries) {
        entries.clear();
        entries.addAll(toLegacy(modernEntries));
    }
}

package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.SatelliteItem;
import net.minecraft.world.item.ItemStack;

/** Stack-aware equivalent of the legacy XSatelliteRegistry ComparableStack map. */
public final class XSatelliteRegistry {
    private XSatelliteRegistry() {
    }

    public static LegacySatelliteType typeFromItemStack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof SatelliteItem
                ? SatelliteItem.variantOf(stack).satelliteType()
                : null;
    }
}

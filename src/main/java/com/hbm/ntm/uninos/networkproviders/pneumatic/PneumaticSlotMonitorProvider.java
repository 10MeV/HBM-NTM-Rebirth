package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.world.item.ItemStack;

public interface PneumaticSlotMonitorProvider {
    PneumaticSlotMonitor[] getMonitors();

    ItemStack getSlotAt(int index);

    long getAmountAt(int index);

    boolean isAvailableToCache(PneumaticStackCache cache);

    PneumaticNetwork getRelevantNetwork();

    default boolean hasExpired() {
        return false;
    }

    default void onNewCacheHasJoined(PneumaticStackCache stackCache, PneumaticNetwork network) {
        for (PneumaticSlotMonitor monitor : getMonitors()) {
            if (!stackCache.hasExpired() && isAvailableToCache(stackCache)) {
                stackCache.addToCache(monitor);
            }
        }
    }

    default void updateMonitors() {
        for (PneumaticSlotMonitor monitor : getMonitors()) {
            monitor.checkUpdate();
        }
    }
}

package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface PneumaticSlotMonitorProvider {
    PneumaticSlotMonitor[] getMonitors();

    @Nullable ItemStack getSlotAt(int index);

    long getAmountAt(int index);

    boolean isAvailableToCache(PneumaticStackCache cache);

    PneumaticNetwork getRelevantNetwork();

    default boolean hasExpired() {
        return false;
    }

    /**
     * Legacy StackCache delegates mutations back to the owning inventory. The
     * return value is the quantity left over after the requested operation.
     */
    default long useUpItem(int index, long amount) {
        return amount;
    }

    default long addItem(int index, long amount) {
        return amount;
    }

    default boolean allowTypeSetting() {
        return false;
    }

    default long setupType(int index, ItemStack stack, long amount) {
        return amount;
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

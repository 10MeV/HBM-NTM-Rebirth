package com.hbm.ntm.api.entity;

public record RadarHostSyncState(long power, boolean jammed, int redstonePower, int entryCount) {
    public static final int BLOCK_UPDATE_INTERVAL_TICKS = 10;

    public RadarHostSyncState {
        power = Math.max(0L, power);
        redstonePower = Math.max(0, Math.min(15, redstonePower));
        entryCount = Math.max(0, entryCount);
    }

    public SyncPlan planAfter(RadarHostSyncState after, boolean mapChanged, boolean mapClearDirty, long gameTime) {
        RadarHostSyncState current = after != null ? after : this;
        boolean redstoneChanged = redstonePower != current.redstonePower;
        boolean jammedChanged = jammed != current.jammed;
        boolean changed = power != current.power
                || jammedChanged
                || redstoneChanged
                || entryCount != current.entryCount
                || mapChanged
                || mapClearDirty;
        boolean blockUpdate = changed
                && (gameTime % BLOCK_UPDATE_INTERVAL_TICKS == 0L || redstoneChanged || jammedChanged);
        return new SyncPlan(changed, redstoneChanged, mapChanged || mapClearDirty, blockUpdate);
    }

    public record SyncPlan(boolean changed, boolean redstoneChanged, boolean mapSync, boolean blockUpdate) {
    }
}

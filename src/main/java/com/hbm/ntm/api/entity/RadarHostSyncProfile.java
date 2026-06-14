package com.hbm.ntm.api.entity;

import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RadarHostSyncProfile {
    public static final int LEGACY_NETWORK_PACK_RANGE = 50;

    private RadarHostSyncProfile() {
    }

    public static TickSyncPlan plan(long gameTime, RadarHostSyncState before, RadarHostSyncState after,
            boolean mapChanged, boolean mapClearDirty) {
        RadarHostSyncState.SyncPlan syncPlan = before.planAfter(after, mapChanged, mapClearDirty, gameTime);
        return new TickSyncPlan(syncPlan, LEGACY_NETWORK_PACK_RANGE);
    }

    public static boolean apply(Level level, BlockPos pos, BlockState state, RadarBlockEntity radar,
            TickSyncPlan plan, boolean mapClearDirty) {
        if (level == null || pos == null || state == null || radar == null || plan == null) {
            return mapClearDirty;
        }
        RadarHostSyncState.SyncPlan syncPlan = plan.syncPlan();
        if (syncPlan.redstoneChanged()) {
            level.updateNeighborsAt(pos, state.getBlock());
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        if (syncPlan.changed()) {
            radar.setChanged();
            if (syncPlan.mapSync()) {
                ModMessages.syncTileToTracking(radar, radar);
                mapClearDirty = false;
            }
            if (syncPlan.blockUpdate()) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }
        radar.networkPackNT(plan.legacyNetworkPackRange());
        return mapClearDirty;
    }

    public record TickSyncPlan(RadarHostSyncState.SyncPlan syncPlan, int legacyNetworkPackRange) {
    }
}

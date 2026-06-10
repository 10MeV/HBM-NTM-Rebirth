package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class RadarScreenDisplayProfile {
    public static final int SERVER_SYNC_INTERVAL_TICKS = 100;
    public static final int LINKED_SYNC_DELAY_TICKS = 25;
    public static final double MAX_RENDER_DISTANCE_SQUARED = 65_536.0D;
    public static final int VIEW_DISTANCE = 256;

    public static boolean shouldSyncBeforeReset(RadarScreenSnapshot snapshot) {
        return snapshot != null && (snapshot.linked() || !snapshot.entries().isEmpty());
    }

    public static RadarScreenSnapshot resetAfterServerTick(RadarScreenSnapshot snapshot) {
        return RadarScreenSnapshot.UNLINKED;
    }

    public static AABB renderBoundingBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 1.0D,
                pos.getY(),
                pos.getZ() - 1.0D,
                pos.getX() + 2.0D,
                pos.getY() + 2.0D,
                pos.getZ() + 2.0D);
    }

    private RadarScreenDisplayProfile() {
    }
}

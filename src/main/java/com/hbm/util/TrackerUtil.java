package com.hbm.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for entity tracker helpers.
 */
@Deprecated(forRemoval = false)
public final class TrackerUtil {
    private TrackerUtil() {
    }

    public static void sendTeleport(Level level, Entity entity) {
        com.hbm.ntm.util.TrackerUtil.sendTeleport(level, entity);
    }

    public static void sendTeleport(Entity entity) {
        com.hbm.ntm.util.TrackerUtil.sendTeleport(entity);
    }

    public static void setTrackingRange(Level level, Entity entity, int range) {
        com.hbm.ntm.util.TrackerUtil.setTrackingRange(level, entity, range);
    }

    public static boolean canChangeTrackingRangeAtRuntime() {
        return com.hbm.ntm.util.TrackerUtil.canChangeTrackingRangeAtRuntime();
    }
}

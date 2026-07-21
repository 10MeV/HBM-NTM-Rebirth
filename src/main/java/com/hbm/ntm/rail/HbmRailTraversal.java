package com.hbm.ntm.rail;

import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Exact traversal loop from 1.7.10 {@code EntityRailCarBase#getRelPosAlongRail}.
 */
public final class HbmRailTraversal {
    public static final int MAX_RAIL_SEGMENTS_PER_MOVE = 30;

    private HbmRailTraversal() {
    }

    /**
     * Moves a rail-car point over connected HBM rails. A {@code null} result
     * is the legacy derail signal: missing rail, mismatched gauge, malformed
     * rail context, or more than thirty traversed rail segments.
     */
    public static Vec3 travel(Level level, BlockPos anchor, double distanceToCover,
            HbmRail.TrackGauge gauge, Vec3 position, float yaw, HbmRail.MoveContext moveContext) {
        if (level == null || anchor == null || gauge == null || position == null || moveContext == null) {
            return null;
        }

        if (distanceToCover < 0.0D) {
            distanceToCover *= -1.0D;
            yaw += 180.0F;
        }

        Vec3 next = position;
        int segments = 0;
        do {
            if (++segments > MAX_RAIL_SEGMENTS_PER_MOVE) {
                return null;
            }

            // Legacy rail dummies used the same Block subclass as their core.
            // Modern multiblocks use a shared dummy carrier, so resolve the
            // owner before testing the IRailNTM-equivalent interface.
            BlockState state = MultiblockHelper.resolveCoreState(level, anchor);
            if (!(state.getBlock() instanceof HbmRail rail)) {
                return null;
            }

            double radians = -yaw * (Math.PI / 180.0D);
            Vec3 motion = new Vec3(Math.sin(radians), 0.0D, Math.cos(radians));
            if (segments == 1) {
                next = rail.getTravelLocation(level, anchor, next, motion, 0.0D,
                        new HbmRail.RailContext(), moveContext);
                if (next == null) {
                    return null;
                }
            }

            // This is intentionally evaluated after legacy negative-distance
            // normalization, exactly as in EntityRailCarBase.
            boolean flip = distanceToCover < 0.0D;
            if (rail.getGauge(level, anchor) != gauge) {
                return null;
            }

            HbmRail.RailContext railContext = new HbmRail.RailContext();
            Vec3 previous = next;
            next = rail.getTravelLocation(level, anchor, previous, motion, distanceToCover, railContext, moveContext);
            if (next == null) {
                return null;
            }
            distanceToCover = railContext.overshoot();
            // The legacy loop only consumed RailContext.pos when an overshoot
            // actually caused another rail segment to be traversed. Waypoint
            // rails intentionally leave it unset while travel remains inside a
            // node link, so requiring it for the terminal zero-distance case
            // was a modern-port-only derail condition.
            if (distanceToCover != 0.0D) {
                if (railContext.nextRailPos() == null) {
                    return null;
                }
                anchor = railContext.nextRailPos();
            }
            yaw = yawBetween(next, previous) * (flip ? -1.0F : 1.0F);
        } while (distanceToCover != 0.0D);

        return next;
    }

    /** 1.7.10 {@code EntityRailCarBase#generateYaw(Vec3, Vec3)}. */
    public static float yawBetween(Vec3 front, Vec3 back) {
        double deltaX = front.x - back.x;
        double deltaZ = front.z - back.z;
        double radians = -Math.atan2(deltaX, deltaZ);
        return Mth.wrapDegrees((float) (radians * 180.0D / Math.PI));
    }
}

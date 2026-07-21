package com.hbm.ntm.rail;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Modern carrier for the 1.7.10 {@code com.hbm.blocks.rail.IRailNTM} contract.
 *
 * <p>It deliberately describes HBM's custom rail geometry rather than vanilla
 * {@code BaseRailBlock}: rail-car entities use the returned world position,
 * yaw, remaining distance and next core position to traverse multi-block
 * standard/narrow rails.</p>
 */
public interface HbmRail {
    Vec3 getSnappingPosition(Level level, BlockPos railPos, Vec3 trainPosition);

    Vec3 getTravelLocation(Level level, BlockPos railPos, Vec3 trainPosition, Vec3 motion,
            double speed, RailContext context, MoveContext moveContext);

    TrackGauge getGauge(Level level, BlockPos railPos);

    enum TrackGauge {
        STANDARD,
        NARROW
    }

    /** Mutable per-segment result, matching legacy {@code IRailNTM.RailContext}. */
    final class RailContext {
        private float yaw;
        private double overshoot;
        private BlockPos nextRailPos;

        public float yaw() {
            return yaw;
        }

        public double overshoot() {
            return overshoot;
        }

        public BlockPos nextRailPos() {
            return nextRailPos;
        }

        public RailContext yaw(float value) {
            yaw = value;
            return this;
        }

        public RailContext overshoot(double value) {
            overshoot = value;
            return this;
        }

        public RailContext nextRailPos(BlockPos value) {
            nextRailPos = value;
            return this;
        }
    }

    /** Mutable collision result for front/back bogie checks on buffers. */
    final class MoveContext {
        private final RailCheckType type;
        private final double collisionBogieDistance;
        private boolean collision;
        private double overshoot;

        public MoveContext(RailCheckType type, double collisionBogieDistance) {
            this.type = type;
            this.collisionBogieDistance = collisionBogieDistance;
        }

        public RailCheckType type() {
            return type;
        }

        public double collisionBogieDistance() {
            return collisionBogieDistance;
        }

        public boolean collision() {
            return collision;
        }

        public double overshoot() {
            return overshoot;
        }

        public void markCollision(double value) {
            collision = true;
            overshoot = value;
        }
    }

    enum RailCheckType {
        CORE,
        FRONT,
        BACK,
        OTHER
    }
}
